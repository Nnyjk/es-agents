package resource

import (
	"archive/tar"
	"archive/zip"
	"compress/gzip"
	"context"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"io"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"
)

// Packer handles package building
type Packer struct{}

// NewPacker creates a new Packer instance
func NewPacker() *Packer {
	return &Packer{}
}

// Build builds a package from the given configuration
func (p *Packer) Build(ctx context.Context, req BuildRequest) (*BuildResult, error) {
	started := time.Now()
	result := &BuildResult{
		RequestID: req.RequestID,
		StartedAt: started,
	}

	// Validate configuration
	if err := p.validateConfig(&req.Config); err != nil {
		result.Status = "FAILED"
		result.Error = err.Error()
		result.FinishedAt = time.Now()
		result.DurationMs = result.FinishedAt.Sub(started).Milliseconds()
		return result, nil
	}

	// Check source path exists
	if _, err := os.Stat(req.Config.SourcePath); err != nil {
		result.Status = "FAILED"
		result.Error = fmt.Sprintf("source path not found: %v", err)
		result.FinishedAt = time.Now()
		result.DurationMs = result.FinishedAt.Sub(started).Milliseconds()
		return result, nil
	}

	// Create output directory
	if err := os.MkdirAll(req.Config.OutputDir, 0755); err != nil {
		result.Status = "FAILED"
		result.Error = fmt.Sprintf("failed to create output directory: %v", err)
		result.FinishedAt = time.Now()
		result.DurationMs = result.FinishedAt.Sub(started).Milliseconds()
		return result, nil
	}

	// Run build command if specified
	if req.Config.BuildCommand != "" {
		if err := p.runBuildCommand(ctx, &req.Config); err != nil {
			result.Status = "FAILED"
			result.Error = fmt.Sprintf("build command failed: %v", err)
			result.FinishedAt = time.Now()
			result.DurationMs = result.FinishedAt.Sub(started).Milliseconds()
			return result, nil
		}
	}

	// Create package based on type
	packageInfo, err := p.createPackage(ctx, &req.Config)
	if err != nil {
		result.Status = "FAILED"
		result.Error = err.Error()
		result.FinishedAt = time.Now()
		result.DurationMs = result.FinishedAt.Sub(started).Milliseconds()
		return result, nil
	}

	result.Status = "SUCCESS"
	result.Package = packageInfo
	result.FinishedAt = time.Now()
	result.DurationMs = result.FinishedAt.Sub(started).Milliseconds()

	return result, nil
}

// validateConfig validates the package configuration
func (p *Packer) validateConfig(config *PackageConfig) error {
	if config.Name == "" {
		return fmt.Errorf("package name is required")
	}
	if config.SourcePath == "" {
		return fmt.Errorf("source path is required")
	}
	if config.OutputDir == "" {
		config.OutputDir = "." // Default to current directory
	}
	if config.Type == "" {
		config.Type = PackageTypeTarGz // Default type
	}
	return nil
}

// runBuildCommand executes the build command
func (p *Packer) runBuildCommand(ctx context.Context, config *PackageConfig) error {
	cmd := exec.CommandContext(ctx, "sh", "-c", config.BuildCommand)
	cmd.Dir = config.SourcePath

	// Set environment variables
	env := os.Environ()
	for key, value := range config.EnvVariables {
		env = append(env, fmt.Sprintf("%s=%s", key, value))
	}
	cmd.Env = env

	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf("build command failed: %w, output: %s", err, output)
	}

	return nil
}

// createPackage creates the package file
func (p *Packer) createPackage(ctx context.Context, config *PackageConfig) (*PackageInfo, error) {
	switch config.Type {
	case PackageTypeTarGz:
		return p.createTarGz(config)
	case PackageTypeZip:
		return p.createZip(config)
	case PackageTypeDocker:
		return p.createDockerImage(ctx, config)
	default:
		return p.createTarGz(config) // Default to tar.gz
	}
}

// createTarGz creates a tar.gz archive
func (p *Packer) createTarGz(config *PackageConfig) (*PackageInfo, error) {
	packageFile := filepath.Join(config.OutputDir, config.Name+".tar.gz")

	file, err := os.Create(packageFile)
	if err != nil {
		return nil, fmt.Errorf("failed to create package file: %w", err)
	}
	defer file.Close()

	gzipWriter := gzip.NewWriter(file)
	defer gzipWriter.Close()

	tarWriter := tar.NewWriter(gzipWriter)
	defer tarWriter.Close()

	// Walk source directory and add files
	err = filepath.Walk(config.SourcePath, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}

		// Check exclusion patterns
		for _, pattern := range config.ExcludePatterns {
			if matchPattern(pattern, path, config.SourcePath) {
				if info.IsDir() {
					return filepath.SkipDir
				}
				return nil
			}
		}

		// Create tar header
		header, err := tar.FileInfoHeader(info, "")
		if err != nil {
			return err
		}

		// Set relative path
		relPath, err := filepath.Rel(config.SourcePath, path)
		if err != nil {
			return err
		}
		header.Name = relPath

		// Write header
		if err := tarWriter.WriteHeader(header); err != nil {
			return err
		}

		// Write file content (skip directories)
		if !info.IsDir() {
			srcFile, err := os.Open(path)
			if err != nil {
				return err
			}
			defer srcFile.Close()

			if _, err := io.Copy(tarWriter, srcFile); err != nil {
				return err
			}
		}

		return nil
	})

	if err != nil {
		os.Remove(packageFile)
		return nil, fmt.Errorf("failed to create archive: %w", err)
	}

	// Calculate checksum and size
	checksum, size, err := calculateFileChecksumAndSize(packageFile)
	if err != nil {
		os.Remove(packageFile)
		return nil, fmt.Errorf("failed to calculate checksum: %w", err)
	}

	return &PackageInfo{
		Name:     config.Name,
		Path:     packageFile,
		Type:     PackageTypeTarGz,
		Size:     size,
		Checksum: checksum,
		BuiltAt:  time.Now(),
	}, nil
}

// createZip creates a zip archive
func (p *Packer) createZip(config *PackageConfig) (*PackageInfo, error) {
	packageFile := filepath.Join(config.OutputDir, config.Name+".zip")

	file, err := os.Create(packageFile)
	if err != nil {
		return nil, fmt.Errorf("failed to create package file: %w", err)
	}
	defer file.Close()

	zipWriter := zip.NewWriter(file)
	defer zipWriter.Close()

	// Walk source directory and add files
	err = filepath.Walk(config.SourcePath, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}

		// Check exclusion patterns
		for _, pattern := range config.ExcludePatterns {
			if matchPattern(pattern, path, config.SourcePath) {
				if info.IsDir() {
					return filepath.SkipDir
				}
				return nil
			}
		}

		// Create zip header
		header, err := zip.FileInfoHeader(info)
		if err != nil {
			return err
		}

		// Set relative path
		relPath, err := filepath.Rel(config.SourcePath, path)
		if err != nil {
			return err
		}
		header.Name = relPath

		// Write header
		writer, err := zipWriter.CreateHeader(header)
		if err != nil {
			return err
		}

		// Write file content (skip directories)
		if !info.IsDir() {
			srcFile, err := os.Open(path)
			if err != nil {
				return err
			}
			defer srcFile.Close()

			if _, err := io.Copy(writer, srcFile); err != nil {
				return err
			}
		}

		return nil
	})

	if err != nil {
		os.Remove(packageFile)
		return nil, fmt.Errorf("failed to create archive: %w", err)
	}

	// Calculate checksum and size
	checksum, size, err := calculateFileChecksumAndSize(packageFile)
	if err != nil {
		os.Remove(packageFile)
		return nil, fmt.Errorf("failed to calculate checksum: %w", err)
	}

	return &PackageInfo{
		Name:     config.Name,
		Path:     packageFile,
		Type:     PackageTypeZip,
		Size:     size,
		Checksum: checksum,
		BuiltAt:  time.Now(),
	}, nil
}

// createDockerImage creates a Docker image
func (p *Packer) createDockerImage(ctx context.Context, config *PackageConfig) (*PackageInfo, error) {
	// Build Docker image
	imageName := config.Name

	args := []string{"build", "-t", imageName, config.SourcePath}
	cmd := exec.CommandContext(ctx, "docker", args...)

	output, err := cmd.CombinedOutput()
	if err != nil {
		return nil, fmt.Errorf("docker build failed: %w, output: %s", err, output)
	}

	// Get image ID
	cmd = exec.CommandContext(ctx, "docker", "images", "-q", imageName)
	output, err = cmd.Output()
	imageID := strings.TrimSpace(string(output))
	if err != nil {
		imageID = "unknown"
	}

	// Save image to file for distribution
	packageFile := filepath.Join(config.OutputDir, config.Name+".tar")
	args = []string{"save", "-o", packageFile, imageName}
	cmd = exec.CommandContext(ctx, "docker", args...)
	output, err = cmd.CombinedOutput()
	if err != nil {
		return nil, fmt.Errorf("docker save failed: %w, output: %s", err, output)
	}

	// Calculate checksum and size
	checksum, size, err := calculateFileChecksumAndSize(packageFile)
	if err != nil {
		os.Remove(packageFile)
		return nil, fmt.Errorf("failed to calculate checksum: %w", err)
	}

	return &PackageInfo{
		Name:     config.Name,
		Path:     packageFile,
		Type:     PackageTypeDocker,
		Size:     size,
		Checksum: checksum,
		Version:  imageID,
		BuiltAt:  time.Now(),
	}, nil
}

// Helper functions

func calculateFileChecksumAndSize(path string) (string, int64, error) {
	file, err := os.Open(path)
	if err != nil {
		return "", 0, err
	}
	defer file.Close()

	hasher := sha256.New()
	size, err := io.Copy(hasher, file)
	if err != nil {
		return "", 0, err
	}

	checksum := hex.EncodeToString(hasher.Sum(nil))
	return checksum, size, nil
}

func matchPattern(pattern, path, basePath string) bool {
	relPath, err := filepath.Rel(basePath, path)
	if err != nil {
		return false
	}

	// Simple pattern matching
	if pattern == "*" {
		return true
	}

	// Check for exact match or prefix match
	if strings.HasPrefix(pattern, "*.") {
		ext := strings.TrimPrefix(pattern, "*.")
		return strings.HasSuffix(relPath, ext)
	}

	if strings.HasSuffix(pattern, "/*") {
		dir := strings.TrimSuffix(pattern, "/*")
		return strings.HasPrefix(relPath, dir+"/") || relPath == dir
	}

	return relPath == pattern || strings.HasPrefix(relPath, pattern+"/")
}