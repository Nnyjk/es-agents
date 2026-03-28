package upgrade

import (
	"archive/tar"
	"compress/gzip"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"runtime"
	"time"
)

// Upgrader handles agent upgrade operations
type Upgrader struct {
	CurrentVersion string
	BackupDir      string
	DownloadDir    string
	BinaryPath     string
}

// UpgradeResult represents the result of an upgrade operation
type UpgradeResult struct {
	Success    bool
	Error      string
	NewVersion string
}

// NewUpgrader creates a new Upgrader instance
func NewUpgrader(currentVersion, backupDir, downloadDir, binaryPath string) *Upgrader {
	return &Upgrader{
		CurrentVersion: currentVersion,
		BackupDir:      backupDir,
		DownloadDir:    downloadDir,
		BinaryPath:     binaryPath,
	}
}

// Upgrade downloads, verifies, and installs a new version of the agent
func (u *Upgrader) Upgrade(version, downloadURL, checksum string) (*UpgradeResult, error) {
	result := &UpgradeResult{
		NewVersion: version,
	}

	// Step 1: Backup current version
	if err := u.Backup(); err != nil {
		result.Error = fmt.Sprintf("Backup failed: %v", err)
		return result, err
	}

	// Step 2: Download new version
	downloadedFile, err := u.Download(downloadURL)
	if err != nil {
		result.Error = fmt.Sprintf("Download failed: %v", err)
		return result, err
	}
	defer os.Remove(downloadedFile) // Clean up after extraction

	// Step 3: Verify checksum
	if checksum != "" {
		if err := u.VerifyChecksum(downloadedFile, checksum); err != nil {
			result.Error = fmt.Sprintf("Checksum verification failed: %v", err)
			return result, err
		}
	}

	// Step 4: Extract and install
	if err := u.Install(downloadedFile); err != nil {
		result.Error = fmt.Sprintf("Installation failed: %v", err)
		return result, err
	}

	result.Success = true
	return result, nil
}

// Backup creates a backup of the current agent binary
func (u *Upgrader) Backup() error {
	timestamp := time.Now().Format("20060102_150405")
	backupPath := filepath.Join(u.BackupDir, fmt.Sprintf("agent_backup_%s", timestamp))

	if err := os.MkdirAll(u.BackupDir, 0755); err != nil {
		return fmt.Errorf("failed to create backup directory: %w", err)
	}

	// Copy current binary to backup location
	sourceFile, err := os.Open(u.BinaryPath)
	if err != nil {
		return fmt.Errorf("failed to open current binary: %w", err)
	}
	defer sourceFile.Close()

	destFile, err := os.Create(backupPath)
	if err != nil {
		return fmt.Errorf("failed to create backup file: %w", err)
	}
	defer destFile.Close()

	if _, err := io.Copy(destFile, sourceFile); err != nil {
		return fmt.Errorf("failed to copy binary: %w", err)
	}

	return nil
}

// Download downloads the agent package from the given URL
func (u *Upgrader) Download(url string) (string, error) {
	if err := os.MkdirAll(u.DownloadDir, 0755); err != nil {
		return "", fmt.Errorf("failed to create download directory: %w", err)
	}

	resp, err := http.Get(url)
	if err != nil {
		return "", fmt.Errorf("failed to download: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("download failed with status: %d", resp.StatusCode)
	}

	filename := filepath.Join(u.DownloadDir, fmt.Sprintf("agent_%s.tar.gz", time.Now().Format("20060102_150405")))
	outFile, err := os.Create(filename)
	if err != nil {
		return "", fmt.Errorf("failed to create file: %w", err)
	}
	defer outFile.Close()

	if _, err := io.Copy(outFile, resp.Body); err != nil {
		return "", fmt.Errorf("failed to write file: %w", err)
	}

	return filename, nil
}

// VerifyChecksum verifies the SHA256 checksum of the downloaded file
func (u *Upgrader) VerifyChecksum(filePath, expectedChecksum string) error {
	file, err := os.Open(filePath)
	if err != nil {
		return fmt.Errorf("failed to open file: %w", err)
	}
	defer file.Close()

	hasher := sha256.New()
	if _, err := io.Copy(hasher, file); err != nil {
		return fmt.Errorf("failed to calculate hash: %w", err)
	}

	actualChecksum := hex.EncodeToString(hasher.Sum(nil))
	
	// Remove "sha256:" prefix if present
	expected := expectedChecksum
	if len(expected) > 7 && expected[:7] == "sha256:" {
		expected = expected[7:]
	}

	if actualChecksum != expected {
		return fmt.Errorf("checksum mismatch: expected %s, got %s", expected, actualChecksum)
	}

	return nil
}

// Install extracts and installs the new agent binary
func (u *Upgrader) Install(archivePath string) error {
	// Extract tar.gz
	if err := u.extractTarGz(archivePath, u.DownloadDir); err != nil {
		return fmt.Errorf("failed to extract archive: %w", err)
	}

	// Find the agent binary in extracted files
	agentBinary := filepath.Join(u.DownloadDir, "agent")
	if runtime.GOOS == "windows" {
		agentBinary = filepath.Join(u.DownloadDir, "agent.exe")
	}

	if _, err := os.Stat(agentBinary); os.IsNotExist(err) {
		return fmt.Errorf("agent binary not found in archive")
	}

	// Replace current binary
	if err := u.replaceBinary(agentBinary); err != nil {
		return fmt.Errorf("failed to replace binary: %w", err)
	}

	return nil
}

// extractTarGz extracts a tar.gz archive to the destination directory
func (u *Upgrader) extractTarGz(archivePath, destDir string) error {
	file, err := os.Open(archivePath)
	if err != nil {
		return err
	}
	defer file.Close()

	gzr, err := gzip.NewReader(file)
	if err != nil {
		return err
	}
	defer gzr.Close()

	tr := tar.NewReader(gzr)

	for {
		header, err := tr.Next()
		if err == io.EOF {
			break
		}
		if err != nil {
			return err
		}

		target := filepath.Join(destDir, header.Name)

		switch header.Typeflag {
		case tar.TypeDir:
			if err := os.MkdirAll(target, 0755); err != nil {
				return err
			}
		case tar.TypeReg:
			outFile, err := os.Create(target)
			if err != nil {
				return err
			}
			if _, err := io.Copy(outFile, tr); err != nil {
				outFile.Close()
				return err
			}
			outFile.Close()
			// Set executable permissions
			if err := os.Chmod(target, 0755); err != nil {
				return err
			}
		default:
			return fmt.Errorf("unknown type: %b in %s", header.Typeflag, header.Name)
		}
	}

	return nil
}

// replaceBinary replaces the current agent binary with the new one
func (u *Upgrader) replaceBinary(newBinaryPath string) error {
	// On Linux, we can't replace a running binary directly
	// Strategy: rename current binary, copy new one, then restart
	
	// Create a temporary name for the old binary
	oldBinaryPath := u.BinaryPath + ".old"
	
	// Rename current binary
	if err := os.Rename(u.BinaryPath, oldBinaryPath); err != nil {
		return fmt.Errorf("failed to rename old binary: %w", err)
	}

	// Copy new binary
	newFile, err := os.Open(newBinaryPath)
	if err != nil {
		// Restore old binary
		os.Rename(oldBinaryPath, u.BinaryPath)
		return fmt.Errorf("failed to open new binary: %w", err)
	}
	defer newFile.Close()

	oldFile, err := os.Create(u.BinaryPath)
	if err != nil {
		// Restore old binary
		os.Rename(oldBinaryPath, u.BinaryPath)
		return fmt.Errorf("failed to create new binary: %w", err)
	}
	defer oldFile.Close()

	if _, err := io.Copy(oldFile, newFile); err != nil {
		// Restore old binary
		os.Rename(oldBinaryPath, u.BinaryPath)
		return fmt.Errorf("failed to copy new binary: %w", err)
	}

	// Set executable permissions
	if err := os.Chmod(u.BinaryPath, 0755); err != nil {
		// Restore old binary
		os.Rename(oldBinaryPath, u.BinaryPath)
		return fmt.Errorf("failed to set permissions: %w", err)
	}

	// Remove old backup after successful replacement
	os.Remove(oldBinaryPath)

	return nil
}

// Rollback restores the agent to the previous version
func (u *Upgrader) Rollback() error {
	// Find the latest backup
	files, err := os.ReadDir(u.BackupDir)
	if err != nil {
		return fmt.Errorf("failed to read backup directory: %w", err)
	}

	if len(files) == 0 {
		return fmt.Errorf("no backups found")
	}

	// Get the most recent backup (assuming sorted by name with timestamp)
	var latestBackup string
	for _, file := range files {
		if !file.IsDir() && filepath.Ext(file.Name()) == "" {
			latestBackup = filepath.Join(u.BackupDir, file.Name())
		}
	}

	if latestBackup == "" {
		return fmt.Errorf("no valid backup found")
	}

	// Restore from backup
	backupFile, err := os.Open(latestBackup)
	if err != nil {
		return fmt.Errorf("failed to open backup: %w", err)
	}
	defer backupFile.Close()

	currentFile, err := os.Create(u.BinaryPath)
	if err != nil {
		return fmt.Errorf("failed to create current binary: %w", err)
	}
	defer currentFile.Close()

	if _, err := io.Copy(currentFile, backupFile); err != nil {
		return fmt.Errorf("failed to restore binary: %w", err)
	}

	if err := os.Chmod(u.BinaryPath, 0755); err != nil {
		return fmt.Errorf("failed to set permissions: %w", err)
	}

	return nil
}
