package resource

import (
	"archive/tar"
	"archive/zip"
	"compress/gzip"
	"context"
	"fmt"
	"io"
	"net"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"
)

// Deployer handles deployment execution
type Deployer struct{}

// NewDeployer creates a new Deployer instance
func NewDeployer() *Deployer {
	return &Deployer{}
}

// Deploy executes a deployment
func (d *Deployer) Deploy(ctx context.Context, req DeployRequest) (*DeployResult, error) {
	started := time.Now()
	result := &DeployResult{
		RequestID: req.RequestID,
		StartedAt: started,
		Status:    "IN_PROGRESS",
	}

	// Validate configuration
	if err := d.validateConfig(&req.Config); err != nil {
		result.Status = "FAILED"
		result.Error = err.Error()
		result.FinishedAt = time.Now()
		return result, nil
	}

	// Create deployment result
	deployResult := &DeploymentResult{
		Status:    DeploymentStatusInProgress,
		StartedAt: started,
		Version:   extractVersionFromPath(req.Config.PackagePath),
	}

	// Process each target host (or localhost if no targets specified)
	targets := req.Config.TargetHosts
	if len(targets) == 0 {
		targets = []string{"localhost"}
	}

	hostResults := make([]HostDeployResult, 0, len(targets))

	for _, host := range targets {
		hostResult := d.deployToHost(ctx, host, &req.Config)
		hostResults = append(hostResults, hostResult)

		// Update overall status if any host failed
		if hostResult.Status == DeploymentStatusFailed {
			deployResult.Status = DeploymentStatusFailed
		}
	}

	// Determine final status
	allSuccess := true
	for _, hr := range hostResults {
		if hr.Status != DeploymentStatusSuccess {
			allSuccess = false
			break
		}
	}

	if allSuccess {
		deployResult.Status = DeploymentStatusSuccess
		result.Status = "SUCCESS"
	} else {
		result.Status = "FAILED"
	}

	deployResult.HostResults = hostResults
	deployResult.FinishedAt = time.Now()
	result.Result = deployResult
	result.FinishedAt = time.Now()
	result.DurationMs = result.FinishedAt.Sub(started).Milliseconds()

	return result, nil
}

// validateConfig validates the deployment configuration
func (d *Deployer) validateConfig(config *DeploymentConfig) error {
	if config.PackagePath == "" {
		return fmt.Errorf("package path is required")
	}
	if config.DeployDir == "" {
		config.DeployDir = "/opt/deploy" // Default deploy directory
	}
	return nil
}

// deployToHost deploys to a single host
func (d *Deployer) deployToHost(ctx context.Context, host string, config *DeploymentConfig) HostDeployResult {
	started := time.Now()
	result := HostDeployResult{
		Host:      host,
		Status:    DeploymentStatusInProgress,
		StartedAt: started,
	}

	// For localhost, deploy directly
	if host == "localhost" {
		return d.deployLocal(ctx, config)
	}

	// For remote hosts, would need SSH or other remote execution
	// For now, return error for remote hosts
	result.Status = DeploymentStatusFailed
	result.FinishedAt = time.Now()
	result.Error = fmt.Sprintf("remote deployment to %s not implemented", host)
	return result
}

// deployLocal deploys to the local machine
func (d *Deployer) deployLocal(ctx context.Context, config *DeploymentConfig) HostDeployResult {
	started := time.Now()
	result := HostDeployResult{
		Host:      "localhost",
		Status:    DeploymentStatusInProgress,
		StartedAt: started,
	}

	// Create deploy directory
	if err := os.MkdirAll(config.DeployDir, 0755); err != nil {
		result.Status = DeploymentStatusFailed
		result.FinishedAt = time.Now()
		result.Error = fmt.Sprintf("failed to create deploy directory: %v", err)
		return result
	}

	// Stop existing service if stop command provided
	if config.StopCmd != "" {
		if err := d.executeCommand(ctx, config.StopCmd, config.DeployDir, config.Timeout); err != nil {
			// Log but continue, service might not be running
			fmt.Printf("Stop command failed (may be expected): %v\n", err)
		}
	}

	// Execute pre-deploy command
	if config.PreDeployCmd != "" {
		if err := d.executeCommand(ctx, config.PreDeployCmd, config.DeployDir, config.Timeout); err != nil {
			result.Status = DeploymentStatusFailed
			result.FinishedAt = time.Now()
			result.Error = fmt.Sprintf("pre-deploy command failed: %v", err)
			return result
		}
	}

	// Extract package
	if err := d.extractPackage(config.PackagePath, config.DeployDir); err != nil {
		result.Status = DeploymentStatusFailed
		result.FinishedAt = time.Now()
		result.Error = fmt.Sprintf("package extraction failed: %v", err)
		return result
	}

	// Execute post-deploy command
	if config.PostDeployCmd != "" {
		if err := d.executeCommand(ctx, config.PostDeployCmd, config.DeployDir, config.Timeout); err != nil {
			result.Status = DeploymentStatusFailed
			result.FinishedAt = time.Now()
			result.Error = fmt.Sprintf("post-deploy command failed: %v", err)
			return result
		}
	}

	// Start service
	if config.StartCmd != "" {
		if err := d.executeCommand(ctx, config.StartCmd, config.DeployDir, config.Timeout); err != nil {
			result.Status = DeploymentStatusFailed
			result.FinishedAt = time.Now()
			result.Error = fmt.Sprintf("start command failed: %v", err)
			return result
		}
	}

	// Perform health check
	if config.HealthCheck != nil {
		healthResult := d.performHealthCheck(ctx, config.HealthCheck)
		result.HealthCheck = &healthResult

		if !healthResult.Healthy {
			result.Status = DeploymentStatusFailed
			result.FinishedAt = time.Now()
			result.Error = healthResult.Message
			return result
		}
	}

	result.Status = DeploymentStatusSuccess
	result.FinishedAt = time.Now()
	return result
}

// executeCommand executes a shell command
func (d *Deployer) executeCommand(ctx context.Context, command, workDir string, timeoutMs int64) error {
	// Create context with timeout if specified
	execCtx := ctx
	if timeoutMs > 0 {
		timeoutCtx, cancel := context.WithTimeout(ctx, time.Duration(timeoutMs)*time.Millisecond)
		defer cancel()
		execCtx = timeoutCtx
	}

	cmd := exec.CommandContext(execCtx, "sh", "-c", command)
	cmd.Dir = workDir

	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("command failed: %w, output: %s", err, output)
	}

	return nil
}

// extractPackage extracts a package to the deploy directory
func (d *Deployer) extractPackage(packagePath, deployDir string) error {
	// Determine package type from extension
	ext := strings.ToLower(filepath.Ext(packagePath))

	switch ext {
	case ".gz":
		if strings.HasSuffix(strings.ToLower(packagePath), ".tar.gz") {
			return d.extractTarGz(packagePath, deployDir)
		}
		return d.extractGz(packagePath, deployDir)
	case ".zip":
		return d.extractZip(packagePath, deployDir)
	case ".tar":
		return d.extractTar(packagePath, deployDir)
	default:
		// Try to copy as-is for other types (e.g., Docker tar files)
		return d.copyFile(packagePath, deployDir)
	}
}

// extractTarGz extracts a tar.gz archive
func (d *Deployer) extractTarGz(packagePath, deployDir string) error {
	file, err := os.Open(packagePath)
	if err != nil {
		return err
	}
	defer file.Close()

	gzipReader, err := gzip.NewReader(file)
	if err != nil {
		return err
	}
	defer gzipReader.Close()

	tarReader := tar.NewReader(gzipReader)

	for {
		header, err := tarReader.Next()
		if err == io.EOF {
			break
		}
		if err != nil {
			return err
		}

		targetPath := filepath.Join(deployDir, header.Name)

		switch header.Typeflag {
		case tar.TypeDir:
			if err := os.MkdirAll(targetPath, os.FileMode(header.Mode)); err != nil {
				return err
			}
		case tar.TypeReg:
			if err := os.MkdirAll(filepath.Dir(targetPath), 0755); err != nil {
				return err
			}
			outFile, err := os.OpenFile(targetPath, os.O_CREATE|os.O_WRONLY, os.FileMode(header.Mode))
			if err != nil {
				return err
			}
			if _, err := io.Copy(outFile, tarReader); err != nil {
				outFile.Close()
				return err
			}
			outFile.Close()
		case tar.TypeSymlink:
			if err := os.Symlink(header.Linkname, targetPath); err != nil {
				return err
			}
		}
	}

	return nil
}

// extractTar extracts a tar archive
func (d *Deployer) extractTar(packagePath, deployDir string) error {
	file, err := os.Open(packagePath)
	if err != nil {
		return err
	}
	defer file.Close()

	tarReader := tar.NewReader(file)

	for {
		header, err := tarReader.Next()
		if err == io.EOF {
			break
		}
		if err != nil {
			return err
		}

		targetPath := filepath.Join(deployDir, header.Name)

		switch header.Typeflag {
		case tar.TypeDir:
			if err := os.MkdirAll(targetPath, os.FileMode(header.Mode)); err != nil {
				return err
			}
		case tar.TypeReg:
			if err := os.MkdirAll(filepath.Dir(targetPath), 0755); err != nil {
				return err
			}
			outFile, err := os.OpenFile(targetPath, os.O_CREATE|os.O_WRONLY, os.FileMode(header.Mode))
			if err != nil {
				return err
			}
			if _, err := io.Copy(outFile, tarReader); err != nil {
				outFile.Close()
				return err
			}
			outFile.Close()
		case tar.TypeSymlink:
			if err := os.Symlink(header.Linkname, targetPath); err != nil {
				return err
			}
		}
	}

	return nil
}

// extractZip extracts a zip archive
func (d *Deployer) extractZip(packagePath, deployDir string) error {
	reader, err := zip.OpenReader(packagePath)
	if err != nil {
		return err
	}
	defer reader.Close()

	for _, file := range reader.File {
		targetPath := filepath.Join(deployDir, file.Name)

		if file.FileInfo().IsDir() {
			if err := os.MkdirAll(targetPath, file.Mode()); err != nil {
				return err
			}
			continue
		}

		if err := os.MkdirAll(filepath.Dir(targetPath), 0755); err != nil {
			return err
		}

		outFile, err := os.OpenFile(targetPath, os.O_CREATE|os.O_WRONLY, file.Mode())
		if err != nil {
			return err
		}

		srcFile, err := file.Open()
		if err != nil {
			outFile.Close()
			return err
		}

		if _, err := io.Copy(outFile, srcFile); err != nil {
			srcFile.Close()
			outFile.Close()
			return err
		}

		srcFile.Close()
		outFile.Close()
	}

	return nil
}

// extractGz extracts a gz file
func (d *Deployer) extractGz(packagePath, deployDir string) error {
	file, err := os.Open(packagePath)
	if err != nil {
		return err
	}
	defer file.Close()

	gzipReader, err := gzip.NewReader(file)
	if err != nil {
		return err
	}
	defer gzipReader.Close()

	// Remove .gz extension for target filename
	targetName := strings.TrimSuffix(filepath.Base(packagePath), ".gz")
	targetPath := filepath.Join(deployDir, targetName)

	outFile, err := os.Create(targetPath)
	if err != nil {
		return err
	}
	defer outFile.Close()

	if _, err := io.Copy(outFile, gzipReader); err != nil {
		return err
	}

	return nil
}

// copyFile copies a file to the deploy directory
func (d *Deployer) copyFile(packagePath, deployDir string) error {
	srcFile, err := os.Open(packagePath)
	if err != nil {
		return err
	}
	defer srcFile.Close()

	targetPath := filepath.Join(deployDir, filepath.Base(packagePath))

	dstFile, err := os.Create(targetPath)
	if err != nil {
		return err
	}
	defer dstFile.Close()

	if _, err := io.Copy(dstFile, srcFile); err != nil {
		return err
	}

	return nil
}

// performHealthCheck performs health check on the deployed service
func (d *Deployer) performHealthCheck(ctx context.Context, config *HealthCheckConfig) HealthCheckResult {
	result := HealthCheckResult{
		Healthy:   false,
		CheckTime: time.Now(),
	}

	// Set defaults
	interval := config.Interval
	if interval <= 0 {
		interval = 5000 // 5 seconds default
	}

	maxRetries := config.MaxRetries
	if maxRetries <= 0 {
		maxRetries = 3
	}

	timeout := config.Timeout
	if timeout <= 0 {
		timeout = 30000 // 30 seconds default
	}

	for i := 0; i < maxRetries; i++ {
		result.Retries = i + 1

		healthy, message := d.checkHealth(ctx, config)
		if healthy {
			result.Healthy = true
			result.Status = "healthy"
			result.Message = message
			return result
		}

		result.Message = message

		// Wait before next retry
		if i < maxRetries-1 {
			time.Sleep(time.Duration(interval) * time.Millisecond)
		}
	}

	result.Status = "unhealthy"
	return result
}

// checkHealth performs a single health check
func (d *Deployer) checkHealth(ctx context.Context, config *HealthCheckConfig) (bool, string) {
	switch config.Type {
	case "HTTP":
		return d.checkHTTPHealth(ctx, config)
	case "TCP":
		return d.checkTCPHealth(ctx, config)
	case "COMMAND":
		return d.checkCommandHealth(ctx, config)
	case "PROCESS":
		return d.checkProcessHealth(ctx, config)
	default:
		return false, fmt.Sprintf("unknown health check type: %s", config.Type)
	}
}

// checkHTTPHealth checks HTTP endpoint health
func (d *Deployer) checkHTTPHealth(ctx context.Context, config *HealthCheckConfig) (bool, string) {
	url := fmt.Sprintf("http://localhost:%d%s", config.Port, config.Path)

	timeout := time.Duration(config.Timeout) * time.Millisecond
	if timeout <= 0 {
		timeout = 10 * time.Second
	}

	reqCtx, cancel := context.WithTimeout(ctx, timeout)
	defer cancel()

	req, err := http.NewRequestWithContext(reqCtx, "GET", url, nil)
	if err != nil {
		return false, fmt.Sprintf("failed to create request: %v", err)
	}

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		return false, fmt.Sprintf("request failed: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 200 && resp.StatusCode < 300 {
		return true, fmt.Sprintf("HTTP check passed (status: %d)", resp.StatusCode)
	}

	return false, fmt.Sprintf("HTTP check failed (status: %d)", resp.StatusCode)
}

// checkTCPHealth checks TCP port connectivity
func (d *Deployer) checkTCPHealth(ctx context.Context, config *HealthCheckConfig) (bool, string) {
	address := fmt.Sprintf("localhost:%d", config.Port)

	timeout := time.Duration(config.Timeout) * time.Millisecond
	if timeout <= 0 {
		timeout = 10 * time.Second
	}

	conn, err := net.DialTimeout("tcp", address, timeout)
	if err != nil {
		return false, fmt.Sprintf("TCP connection failed: %v", err)
	}
	conn.Close()

	return true, fmt.Sprintf("TCP check passed (port %d)", config.Port)
}

// checkCommandHealth checks health via command execution
func (d *Deployer) checkCommandHealth(ctx context.Context, config *HealthCheckConfig) (bool, string) {
	cmd := exec.CommandContext(ctx, "sh", "-c", config.Command)
	output, err := cmd.CombinedOutput()

	if err != nil {
		return false, fmt.Sprintf("command failed: %v, output: %s", err, output)
	}

	return true, fmt.Sprintf("command check passed (output: %s)", strings.TrimSpace(string(output)))
}

// checkProcessHealth checks if a process is running
func (d *Deployer) checkProcessHealth(ctx context.Context, config *HealthCheckConfig) (bool, string) {
	// Use pgrep or ps to check for process
	cmd := exec.CommandContext(ctx, "pgrep", "-f", config.ProcessName)
	output, err := cmd.Output()

	if err != nil {
		// Try alternative approach
		cmd = exec.CommandContext(ctx, "sh", "-c",
			fmt.Sprintf("ps aux | grep -v grep | grep '%s'", config.ProcessName))
		output, err = cmd.Output()
		if err != nil || len(output) == 0 {
			return false, fmt.Sprintf("process not found: %s", config.ProcessName)
		}
	}

	if len(output) > 0 {
		return true, fmt.Sprintf("process running: %s (PID: %s)", config.ProcessName, strings.TrimSpace(string(output)))
	}

	return false, fmt.Sprintf("process not found: %s", config.ProcessName)
}

// HealthCheck performs a standalone health check
func (d *Deployer) HealthCheck(ctx context.Context, req HealthCheckRequest) (*HealthCheckResponse, error) {
	result := d.performHealthCheck(ctx, &req.Config)

	response := &HealthCheckResponse{
		RequestID: req.RequestID,
		CheckTime: time.Now(),
	}

	if result.Healthy {
		response.Status = "SUCCESS"
		response.Result = &result
	} else {
		response.Status = "FAILED"
		response.Result = &result
		response.Error = result.Message
	}

	return response, nil
}

// Helper functions

func extractVersionFromPath(path string) string {
	// Try to extract version from filename
	// Common patterns: app-v1.0.0.tar.gz, package-1.0.0.zip
	base := filepath.Base(path)

	// Remove extensions
	for _, ext := range []string{".tar.gz", ".tar", ".zip", ".gz"} {
		base = strings.TrimSuffix(base, ext)
	}

	// Look for version pattern
	parts := strings.Split(base, "-")
	if len(parts) > 1 {
		// Check if last part looks like a version
		last := parts[len(parts)-1]
		if strings.HasPrefix(last, "v") || containsDigit(last) {
			return last
		}
	}

	return "unknown"
}

func containsDigit(s string) bool {
	for _, c := range s {
		if c >= '0' && c <= '9' {
			return true
		}
	}
	return false
}