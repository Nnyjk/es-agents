package resource

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"
)

// Fetcher handles resource fetching from various sources
type Fetcher struct {
	httpClient *http.Client
}

// NewFetcher creates a new Fetcher instance
func NewFetcher() *Fetcher {
	return &Fetcher{
		httpClient: &http.Client{
			Timeout: 30 * time.Minute, // Long timeout for large files
		},
	}
}

// Fetch fetches a resource from the specified source
func (f *Fetcher) Fetch(ctx context.Context, req FetchRequest) (*FetchResult, error) {
	started := time.Now()
	result := &FetchResult{
		RequestID: req.RequestID,
		StartedAt: started,
	}

	var resource *ResourceInfo
	var err error

	switch req.Source.Type {
	case SourceTypeLocal:
		resource, err = f.fetchLocal(ctx, req)
	case SourceTypeGit:
		resource, err = f.fetchGit(ctx, req)
	case SourceTypeHTTP, SourceTypeHTTPS:
		resource, err = f.fetchHTTP(ctx, req)
	case SourceTypeDocker:
		resource, err = f.fetchDocker(ctx, req)
	case SourceTypeAliyunOSS:
		resource, err = f.fetchAliyunOSS(ctx, req)
	default:
		err = fmt.Errorf("unsupported source type: %s", req.Source.Type)
	}

	if err != nil {
		result.Status = "FAILED"
		result.Error = err.Error()
		result.FinishedAt = time.Now()
		result.DurationMs = result.FinishedAt.Sub(started).Milliseconds()
		return result, nil
	}

	// Perform validation if requested
	if req.Validate != nil {
		validator := NewValidator()
		valResult := validator.Validate(resource, *req.Validate)
		result.Validation = &valResult
		if !valResult.Valid {
			result.Status = "FAILED"
			result.Error = valResult.Error
			result.FinishedAt = time.Now()
			result.DurationMs = result.FinishedAt.Sub(started).Milliseconds()
			return result, nil
		}
	}

	result.Status = "SUCCESS"
	result.Resource = resource
	result.FinishedAt = time.Now()
	result.DurationMs = result.FinishedAt.Sub(started).Milliseconds()

	return result, nil
}

// fetchLocal handles local file source
func (f *Fetcher) fetchLocal(ctx context.Context, req FetchRequest) (*ResourceInfo, error) {
	var config LocalSourceConfig
	if err := parseConfig(req.Source.Config, &config); err != nil {
		return nil, fmt.Errorf("invalid local source config: %w", err)
	}

	// Check if source path exists
	srcInfo, err := os.Stat(config.Path)
	if err != nil {
		return nil, fmt.Errorf("source path not found: %w", err)
	}

	// Create target directory if needed
	if err := os.MkdirAll(req.TargetDir, 0755); err != nil {
		return nil, fmt.Errorf("failed to create target directory: %w", err)
	}

	targetPath := req.TargetDir
	if srcInfo.IsDir() {
		// Copy directory
		targetPath = filepath.Join(req.TargetDir, filepath.Base(config.Path))
		if err := copyDir(config.Path, targetPath); err != nil {
			return nil, fmt.Errorf("failed to copy directory: %w", err)
		}
	} else {
		// Copy file
		targetPath = filepath.Join(req.TargetDir, filepath.Base(config.Path))
		if err := copyFile(config.Path, targetPath); err != nil {
			return nil, fmt.Errorf("failed to copy file: %w", err)
		}
	}

	// Get file info
	info, err := os.Stat(targetPath)
	if err != nil {
		return nil, fmt.Errorf("failed to stat target: %w", err)
	}

	// Calculate checksum
	checksum, err := calculateChecksum(targetPath, ChecksumSHA256)
	if err != nil {
		return nil, fmt.Errorf("failed to calculate checksum: %w", err)
	}

	return &ResourceInfo{
		Name:      filepath.Base(targetPath),
		Path:      targetPath,
		Size:      info.Size(),
		Checksum:  checksum,
		Algorithm: ChecksumSHA256,
		FetchedAt: time.Now(),
	}, nil
}

// fetchGit handles Git repository source
func (f *Fetcher) fetchGit(ctx context.Context, req FetchRequest) (*ResourceInfo, error) {
	var config GitSourceConfig
	if err := parseConfig(req.Source.Config, &config); err != nil {
		return nil, fmt.Errorf("invalid git source config: %w", err)
	}

	// Create target directory
	if err := os.MkdirAll(req.TargetDir, 0755); err != nil {
		return nil, fmt.Errorf("failed to create target directory: %w", err)
	}

	repoName := extractRepoName(config.URL)
	targetPath := filepath.Join(req.TargetDir, repoName)

	// Remove existing directory if present
	if _, err := os.Stat(targetPath); err == nil {
		os.RemoveAll(targetPath)
	}

	// Build git clone command
	args := []string{"clone"}
	if config.Depth > 0 {
		args = append(args, "--depth", fmt.Sprintf("%d", config.Depth))
	}
	if config.Branch != "" {
		args = append(args, "--branch", config.Branch)
	}
	args = append(args, config.URL, targetPath)

	cmd := exec.CommandContext(ctx, "git", args...)

	// Set credentials if provided
	if config.Username != "" || config.Password != "" {
		// Use credential helper approach
		env := os.Environ()
		if config.Username != "" && config.Password != "" {
			// Construct URL with credentials for HTTPS
			if strings.HasPrefix(config.URL, "https://") || strings.HasPrefix(config.URL, "http://") {
				authenticatedURL := strings.Replace(config.URL, "://",
					fmt.Sprintf("://%s:%s@", config.Username, config.Password), 1)
				cmd.Args[len(cmd.Args)-2] = authenticatedURL
			}
		}
		cmd.Env = env
	}

	output, err := cmd.CombinedOutput()
	if err != nil {
		return nil, fmt.Errorf("git clone failed: %w, output: %s", err, output)
	}

	// Checkout specific commit if specified
	if config.Commit != "" {
		cmd := exec.CommandContext(ctx, "git", "-C", targetPath, "checkout", config.Commit)
		output, err := cmd.CombinedOutput()
		if err != nil {
			return nil, fmt.Errorf("git checkout failed: %w, output: %s", err, output)
		}
	}

	// Get version info (commit hash)
	cmd = exec.CommandContext(ctx, "git", "-C", targetPath, "rev-parse", "HEAD")
	output, err = cmd.Output()
	version := strings.TrimSpace(string(output))
	if err != nil {
		version = "unknown"
	}

	// Calculate size
	size, err := dirSize(targetPath)
	if err != nil {
		size = 0
	}

	return &ResourceInfo{
		Name:      repoName,
		Path:      targetPath,
		Size:      size,
		Version:   version,
		FetchedAt: time.Now(),
	}, nil
}

// fetchHTTP handles HTTP/HTTPS download source
func (f *Fetcher) fetchHTTP(ctx context.Context, req FetchRequest) (*ResourceInfo, error) {
	var config HTTPSourceConfig
	if err := parseConfig(req.Source.Config, &config); err != nil {
		return nil, fmt.Errorf("invalid http source config: %w", err)
	}

	// Create target directory
	if err := os.MkdirAll(req.TargetDir, 0755); err != nil {
		return nil, fmt.Errorf("failed to create target directory: %w", err)
	}

	// Create HTTP request
	httpReq, err := http.NewRequestWithContext(ctx, "GET", config.URL, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	// Set headers
	for key, value := range config.Headers {
		httpReq.Header.Set(key, value)
	}

	// Set authentication
	if config.Username != "" || config.Password != "" {
		httpReq.SetBasicAuth(config.Username, config.Password)
	}

	// Execute request
	resp, err := f.httpClient.Do(httpReq)
	if err != nil {
		return nil, fmt.Errorf("http request failed: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("http request returned status: %d", resp.StatusCode)
	}

	// Determine filename from URL or Content-Disposition header
	filename := extractFilenameFromURL(config.URL)
	if disp := resp.Header.Get("Content-Disposition"); disp != "" {
		if name := extractFilenameFromDisposition(disp); name != "" {
			filename = name
		}
	}

	targetPath := filepath.Join(req.TargetDir, filename)

	// Create target file
	file, err := os.Create(targetPath)
	if err != nil {
		return nil, fmt.Errorf("failed to create file: %w", err)
	}

	// Calculate checksum while downloading
	hasher := sha256.New()
	writer := io.MultiWriter(file, hasher)

	// Download file
	size, err := io.Copy(writer, resp.Body)
	file.Close()
	if err != nil {
		os.Remove(targetPath)
		return nil, fmt.Errorf("failed to download file: %w", err)
	}

	checksum := hex.EncodeToString(hasher.Sum(nil))

	return &ResourceInfo{
		Name:      filename,
		Path:      targetPath,
		Size:      size,
		Checksum:  checksum,
		Algorithm: ChecksumSHA256,
		FetchedAt: time.Now(),
	}, nil
}

// fetchDocker handles Docker registry source
func (f *Fetcher) fetchDocker(ctx context.Context, req FetchRequest) (*ResourceInfo, error) {
	var config DockerSourceConfig
	if err := parseConfig(req.Source.Config, &config); err != nil {
		return nil, fmt.Errorf("invalid docker source config: %w", err)
	}

	// Create target directory
	if err := os.MkdirAll(req.TargetDir, 0755); err != nil {
		return nil, fmt.Errorf("failed to create target directory: %w", err)
	}

	// Build image reference
	imageRef := config.Image
	if config.Tag != "" {
		imageRef = fmt.Sprintf("%s:%s", config.Image, config.Tag)
	}
	if config.Registry != "" {
		imageRef = fmt.Sprintf("%s/%s", config.Registry, imageRef)
	}

	// Pull docker image
	args := []string{"pull", imageRef}
	cmd := exec.CommandContext(ctx, "docker", args...)

	if config.Username != "" && config.Password != "" {
		// Login to registry first if credentials provided
		loginArgs := []string{"login"}
		if config.Registry != "" {
			loginArgs = append(loginArgs, config.Registry)
		}
		loginArgs = append(loginArgs, "-u", config.Username, "-p", config.Password)
		loginCmd := exec.CommandContext(ctx, "docker", loginArgs...)
		loginOutput, err := loginCmd.CombinedOutput()
		if err != nil {
			return nil, fmt.Errorf("docker login failed: %w, output: %s", err, loginOutput)
		}
	}

	output, err := cmd.CombinedOutput()
	if err != nil {
		return nil, fmt.Errorf("docker pull failed: %w, output: %s", err, output)
	}

	// Save image to tar file
	filename := fmt.Sprintf("%s.tar", strings.ReplaceAll(imageRef, "/", "_"))
	filename = strings.ReplaceAll(filename, ":", "_")
	targetPath := filepath.Join(req.TargetDir, filename)

	args = []string{"save", "-o", targetPath, imageRef}
	cmd = exec.CommandContext(ctx, "docker", args...)
	output, err = cmd.CombinedOutput()
	if err != nil {
		return nil, fmt.Errorf("docker save failed: %w, output: %s", err, output)
	}

	// Get file info
	info, err := os.Stat(targetPath)
	if err != nil {
		return nil, fmt.Errorf("failed to stat target: %w", err)
	}

	// Calculate checksum
	checksum, err := calculateChecksum(targetPath, ChecksumSHA256)
	if err != nil {
		return nil, fmt.Errorf("failed to calculate checksum: %w", err)
	}

	return &ResourceInfo{
		Name:      filename,
		Path:      targetPath,
		Size:      info.Size(),
		Checksum:  checksum,
		Algorithm: ChecksumSHA256,
		Version:   config.Tag,
		FetchedAt: time.Now(),
	}, nil
}

// fetchAliyunOSS handles Aliyun OSS source
// Note: This implementation uses a command-line approach or HTTP download
// For production, consider using the official Aliyun OSS SDK
func (f *Fetcher) fetchAliyunOSS(ctx context.Context, req FetchRequest) (*ResourceInfo, error) {
	var config AliyunOSSSourceConfig
	if err := parseConfig(req.Source.Config, &config); err != nil {
		return nil, fmt.Errorf("invalid aliyun oss source config: %w", err)
	}

	// Create target directory
	if err := os.MkdirAll(req.TargetDir, 0755); err != nil {
		return nil, fmt.Errorf("failed to create target directory: %w", err)
	}

	// Construct download URL
	// For public buckets, direct URL access works
	// For private buckets, we need to generate a signed URL or use ossutil
	downloadURL := fmt.Sprintf("https://%s.%s/%s", config.Bucket, config.Endpoint, config.ObjectKey)

	// Try using ossutil command if available
	if hasCommand("ossutil") {
		args := []string{"cp", fmt.Sprintf("oss://%s/%s", config.Bucket, config.ObjectKey),
			filepath.Join(req.TargetDir, filepath.Base(config.ObjectKey))}
		if config.AccessKeyID != "" && config.AccessKeySecret != "" {
			args = append([]string{"-e", config.Endpoint, "-i", config.AccessKeyID, "-k", config.AccessKeySecret}, args...)
		}
		cmd := exec.CommandContext(ctx, "ossutil", args...)
		_, err := cmd.CombinedOutput()
		if err != nil {
			// Fallback to HTTP download
			return f.downloadAliyunHTTP(ctx, req, downloadURL, config)
		}
		// Successfully used ossutil
		targetPath := filepath.Join(req.TargetDir, filepath.Base(config.ObjectKey))
		info, err := os.Stat(targetPath)
		if err != nil {
			return nil, fmt.Errorf("failed to stat target: %w", err)
		}
		checksum, err := calculateChecksum(targetPath, ChecksumSHA256)
		if err != nil {
			return nil, fmt.Errorf("failed to calculate checksum: %w", err)
		}
		return &ResourceInfo{
			Name:      filepath.Base(config.ObjectKey),
			Path:      targetPath,
			Size:      info.Size(),
			Checksum:  checksum,
			Algorithm: ChecksumSHA256,
			FetchedAt: time.Now(),
		}, nil
	}

	// Fallback to HTTP download (works for public objects or signed URLs)
	return f.downloadAliyunHTTP(ctx, req, downloadURL, config)
}

// downloadAliyunHTTP downloads from Aliyun OSS via HTTP
func (f *Fetcher) downloadAliyunHTTP(ctx context.Context, req FetchRequest, url string, config AliyunOSSSourceConfig) (*ResourceInfo, error) {
	// Use HTTP download approach
	httpReq, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	// Note: For private buckets, you'd need to add OSS-specific headers
	// This simplified version works for public buckets

	resp, err := f.httpClient.Do(httpReq)
	if err != nil {
		return nil, fmt.Errorf("oss download failed: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("oss download returned status: %d", resp.StatusCode)
	}

	filename := filepath.Base(config.ObjectKey)
	targetPath := filepath.Join(req.TargetDir, filename)

	file, err := os.Create(targetPath)
	if err != nil {
		return nil, fmt.Errorf("failed to create file: %w", err)
	}

	hasher := sha256.New()
	writer := io.MultiWriter(file, hasher)

	size, err := io.Copy(writer, resp.Body)
	file.Close()
	if err != nil {
		os.Remove(targetPath)
		return nil, fmt.Errorf("failed to download file: %w", err)
	}

	checksum := hex.EncodeToString(hasher.Sum(nil))

	return &ResourceInfo{
		Name:      filename,
		Path:      targetPath,
		Size:      size,
		Checksum:  checksum,
		Algorithm: ChecksumSHA256,
		FetchedAt: time.Now(),
	}, nil
}

// Helper functions

func parseConfig(raw json.RawMessage, target interface{}) error {
	return json.Unmarshal(raw, target)
}

func copyFile(src, dst string) error {
	srcFile, err := os.Open(src)
	if err != nil {
		return err
	}
	defer srcFile.Close()

	dstFile, err := os.Create(dst)
	if err != nil {
		return err
	}
	defer dstFile.Close()

	_, err = io.Copy(dstFile, srcFile)
	if err != nil {
		return err
	}

	// Copy permissions
	srcInfo, err := os.Stat(src)
	if err != nil {
		return err
	}
	return os.Chmod(dst, srcInfo.Mode())
}

func copyDir(src, dst string) error {
	srcInfo, err := os.Stat(src)
	if err != nil {
		return err
	}

	if err := os.MkdirAll(dst, srcInfo.Mode()); err != nil {
		return err
	}

	entries, err := os.ReadDir(src)
	if err != nil {
		return err
	}

	for _, entry := range entries {
		srcPath := filepath.Join(src, entry.Name())
		dstPath := filepath.Join(dst, entry.Name())

		if entry.IsDir() {
			if err := copyDir(srcPath, dstPath); err != nil {
				return err
			}
		} else {
			if err := copyFile(srcPath, dstPath); err != nil {
				return err
			}
		}
	}
	return nil
}

func calculateChecksum(path string, algorithm ChecksumAlgorithm) (string, error) {
	file, err := os.Open(path)
	if err != nil {
		return "", err
	}
	defer file.Close()

	switch algorithm {
	case ChecksumMD5:
		// Not implemented for simplicity, use SHA256
	case ChecksumSHA256:
		hasher := sha256.New()
		if _, err := io.Copy(hasher, file); err != nil {
			return "", err
		}
		return hex.EncodeToString(hasher.Sum(nil)), nil
	case ChecksumSHA512:
		// Not implemented for simplicity
	}

	// Default to SHA256
	hasher := sha256.New()
	if _, err := io.Copy(hasher, file); err != nil {
		return "", err
	}
	return hex.EncodeToString(hasher.Sum(nil)), nil
}

func dirSize(path string) (int64, error) {
	var size int64
	err := filepath.Walk(path, func(_ string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if !info.IsDir() {
			size += info.Size()
		}
		return nil
	})
	return size, err
}

func extractRepoName(url string) string {
	// Extract repository name from URL
	// e.g., https://github.com/user/repo.git -> repo
	parts := strings.Split(url, "/")
	if len(parts) > 0 {
		name := parts[len(parts)-1]
		name = strings.TrimSuffix(name, ".git")
		return name
	}
	return "repo"
}

func extractFilenameFromURL(url string) string {
	parts := strings.Split(url, "/")
	if len(parts) > 0 {
		return parts[len(parts)-1]
	}
	return "download"
}

func extractFilenameFromDisposition(disp string) string {
	// Parse Content-Disposition header
	// e.g., attachment; filename="file.txt"
	if strings.Contains(disp, "filename=") {
		parts := strings.Split(disp, "filename=")
		if len(parts) > 1 {
			name := strings.TrimSpace(parts[1])
			name = strings.Trim(name, "\"")
			return name
		}
	}
	return ""
}

func hasCommand(cmd string) bool {
	_, err := exec.LookPath(cmd)
	return err == nil
}