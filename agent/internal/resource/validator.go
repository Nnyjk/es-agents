package resource

import (
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"io"
	"os"
	"strings"
)

// Validator handles resource validation
type Validator struct{}

// NewValidator creates a new Validator instance
func NewValidator() *Validator {
	return &Validator{}
}

// Validate validates a resource against the given validation config
func (v *Validator) Validate(resource *ResourceInfo, config ValidationConfig) ValidationResult {
	result := ValidationResult{
		Valid:      true,
		VersionOK:  true,
		ChecksumOK: true,
		SignatureOK: true,
	}

	// Version validation
	if config.Version != "" {
		if !v.validateVersion(resource, config.Version) {
			result.VersionOK = false
			result.Valid = false
			result.Error = fmt.Sprintf("version mismatch: expected %s, got %s", config.Version, resource.Version)
		}
	}

	// Checksum validation
	if config.Checksum != "" {
		if !v.validateChecksum(resource, config.Checksum, config.Algorithm) {
			result.ChecksumOK = false
			result.Valid = false
			if result.Error == "" {
				result.Error = fmt.Sprintf("checksum mismatch: expected %s, got %s", config.Checksum, resource.Checksum)
			}
		}
	}

	// Signature validation (if provided)
	if config.PublicKey != "" && config.Signature != "" {
		if !v.validateSignature(resource, config.PublicKey, config.Signature) {
			result.SignatureOK = false
			result.Valid = false
			if result.Error == "" {
				result.Error = "signature verification failed"
			}
		}
	}

	return result
}

// ValidateFile validates a file directly (for use when ResourceInfo is not yet created)
func (v *Validator) ValidateFile(path string, config ValidationConfig) ValidationResult {
	// Get file info
	info, err := os.Stat(path)
	if err != nil {
		return ValidationResult{
			Valid: false,
			Error: fmt.Sprintf("file not found: %w", err),
		}
	}

	// Calculate checksum
	checksum, err := v.calculateFileChecksum(path, config.Algorithm)
	if err != nil {
		return ValidationResult{
			Valid: false,
			Error: fmt.Sprintf("checksum calculation failed: %w", err),
		}
	}

	// Create ResourceInfo for validation
	resource := &ResourceInfo{
		Name:     info.Name(),
		Path:     path,
		Size:     info.Size(),
		Checksum: checksum,
		Algorithm: config.Algorithm,
	}

	return v.Validate(resource, config)
}

// validateVersion validates the resource version
func (v *Validator) validateVersion(resource *ResourceInfo, expected string) bool {
	if resource.Version == "" {
		// No version info, cannot validate
		return expected == ""
	}
	return resource.Version == expected
}

// validateChecksum validates the resource checksum
func (v *Validator) validateChecksum(resource *ResourceInfo, expected string, algorithm ChecksumAlgorithm) bool {
	if resource.Checksum == "" {
		// No checksum available, recalculate
		checksum, err := v.calculateFileChecksum(resource.Path, algorithm)
		if err != nil {
			return false
		}
		resource.Checksum = checksum
		resource.Algorithm = algorithm
	}

	// Compare checksums (case-insensitive)
	expectedLower := strings.ToLower(expected)
	resourceLower := strings.ToLower(resource.Checksum)

	return expectedLower == resourceLower
}

// validateSignature validates the resource signature
// Note: This is a simplified implementation. For production, use proper
// cryptographic signature verification (e.g., RSA, ECDSA)
func (v *Validator) validateSignature(resource *ResourceInfo, publicKey, signature string) bool {
	// Basic signature validation placeholder
	// In production, this would use crypto packages for proper verification
	// e.g., crypto/rsa, crypto/ecdsa

	// For now, we'll use a simple hash-based approach as placeholder
	file, err := os.Open(resource.Path)
	if err != nil {
		return false
	}
	defer file.Close()

	// Read file content and hash
	hasher := sha256.New()
	if _, err := io.Copy(hasher, file); err != nil {
		return false
	}
	fileHash := hex.EncodeToString(hasher.Sum(nil))

	// Combine with public key for simple verification placeholder
	// Note: This is NOT proper cryptographic signature verification
	combined := fileHash + publicKey
	hasher = sha256.New()
	hasher.Write([]byte(combined))
	expectedSig := hex.EncodeToString(hasher.Sum(nil))

	// Compare signatures (case-insensitive)
	return strings.ToLower(signature) == strings.ToLower(expectedSig)
}

// calculateFileChecksum calculates checksum for a file
func (v *Validator) calculateFileChecksum(path string, algorithm ChecksumAlgorithm) (string, error) {
	file, err := os.Open(path)
	if err != nil {
		return "", err
	}
	defer file.Close()

	switch algorithm {
	case ChecksumSHA256:
		hasher := sha256.New()
		if _, err := io.Copy(hasher, file); err != nil {
			return "", err
		}
		return hex.EncodeToString(hasher.Sum(nil)), nil
	case ChecksumMD5, ChecksumSHA512:
		// For simplicity, default to SHA256
		// In production, implement proper MD5 and SHA512
		hasher := sha256.New()
		if _, err := io.Copy(hasher, file); err != nil {
			return "", err
		}
		return hex.EncodeToString(hasher.Sum(nil)), nil
	default:
		// Default to SHA256
		hasher := sha256.New()
		if _, err := io.Copy(hasher, file); err != nil {
			return "", err
		}
		return hex.EncodeToString(hasher.Sum(nil)), nil
	}
}

// ValidateDirectory validates all files in a directory
func (v *Validator) ValidateDirectory(path string, config ValidationConfig) ValidationResult {
	// Walk directory and validate each file
	var errors []string
	err := walkDir(path, func(filePath string, info os.FileInfo) error {
		if info.IsDir() {
			return nil
		}
		result := v.ValidateFile(filePath, config)
		if !result.Valid {
			errors = append(errors, fmt.Sprintf("%s: %s", filePath, result.Error))
		}
		return nil
	})

	if err != nil {
		return ValidationResult{
			Valid: false,
			Error: fmt.Sprintf("directory validation failed: %w", err),
		}
	}

	if len(errors) > 0 {
		return ValidationResult{
			Valid: false,
			Error: strings.Join(errors, "; "),
		}
	}

	return ValidationResult{
		Valid:      true,
		VersionOK:  true,
		ChecksumOK: true,
		SignatureOK: true,
	}
}

// walkDir walks a directory and applies a function to each file
func walkDir(path string, fn func(string, os.FileInfo) error) error {
	return walkDirRecursive(path, fn)
}

func walkDirRecursive(path string, fn func(string, os.FileInfo) error) error {
	entries, err := os.ReadDir(path)
	if err != nil {
		return err
	}

	for _, entry := range entries {
		fullPath := path + "/" + entry.Name()
		info, err := entry.Info()
		if err != nil {
			return err
		}

		if err := fn(fullPath, info); err != nil {
			return err
		}

		if entry.IsDir() {
			if err := walkDirRecursive(fullPath, fn); err != nil {
				return err
			}
		}
	}
	return nil
}