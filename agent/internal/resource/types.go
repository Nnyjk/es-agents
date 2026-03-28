package resource

import (
	"encoding/json"
	"time"
)

// SourceType represents the type of resource source
type SourceType string

const (
	SourceTypeLocal     SourceType = "LOCAL"
	SourceTypeGit       SourceType = "GIT"
	SourceTypeHTTP      SourceType = "HTTP"
	SourceTypeHTTPS     SourceType = "HTTPS"
	SourceTypeDocker    SourceType = "DOCKER"
	SourceTypeAliyunOSS SourceType = "ALIYUN"
)

// PackageType represents the type of package
type PackageType string

const (
	PackageTypeTarGz PackageType = "TAR_GZ"
	PackageTypeZip   PackageType = "ZIP"
	PackageTypeDocker PackageType = "DOCKER"
)

// DeploymentStatus represents the status of a deployment
type DeploymentStatus string

const (
	DeploymentStatusPending    DeploymentStatus = "PENDING"
	DeploymentStatusInProgress DeploymentStatus = "IN_PROGRESS"
	DeploymentStatusSuccess    DeploymentStatus = "SUCCESS"
	DeploymentStatusFailed     DeploymentStatus = "FAILED"
	DeploymentStatusRollback   DeploymentStatus = "ROLLBACK"
)

// ChecksumAlgorithm represents the algorithm used for checksum verification
type ChecksumAlgorithm string

const (
	ChecksumMD5    ChecksumAlgorithm = "MD5"
	ChecksumSHA256 ChecksumAlgorithm = "SHA256"
	ChecksumSHA512 ChecksumAlgorithm = "SHA512"
)

// ResourceSource represents a resource source configuration
type ResourceSource struct {
	Type       SourceType              `json:"type"`
	Config     json.RawMessage         `json:"config"`
	Credential *CredentialConfig       `json:"credential,omitempty"`
}

// LocalSourceConfig represents local file source configuration
type LocalSourceConfig struct {
	Path string `json:"path"`
}

// GitSourceConfig represents Git repository source configuration
type GitSourceConfig struct {
	URL      string `json:"url"`
	Branch   string `json:"branch,omitempty"`
	Commit   string `json:"commit,omitempty"`
	Depth    int    `json:"depth,omitempty"`
	Username string `json:"username,omitempty"`
	Password string `json:"password,omitempty"`
}

// HTTPSourceConfig represents HTTP/HTTPS download source configuration
type HTTPSourceConfig struct {
	URL      string            `json:"url"`
	Headers  map[string]string `json:"headers,omitempty"`
	Username string            `json:"username,omitempty"`
	Password string `json:"password,omitempty"`
}

// DockerSourceConfig represents Docker registry source configuration
type DockerSourceConfig struct {
	Image     string `json:"image"`
	Tag       string `json:"tag,omitempty"`
	Registry  string `json:"registry,omitempty"`
	Username  string `json:"username,omitempty"`
	Password  string `json:"password,omitempty"`
	Platform  string `json:"platform,omitempty"`
}

// AliyunOSSSourceConfig represents Aliyun OSS source configuration
type AliyunOSSSourceConfig struct {
	Endpoint        string `json:"endpoint"`
	Bucket          string `json:"bucket"`
	ObjectKey       string `json:"objectKey"`
	AccessKeyID     string `json:"accessKeyId"`
	AccessKeySecret string `json:"accessKeySecret"`
}

// CredentialConfig represents credential configuration
type CredentialConfig struct {
	Type     string `json:"type"` // SSH_KEY, USERNAME_PASSWORD, API_TOKEN
	Key      string `json:"key,omitempty"`
	Username string `json:"username,omitempty"`
	Password string `json:"password,omitempty"`
	Token    string `json:"token,omitempty"`
}

// ResourceInfo represents information about a fetched resource
type ResourceInfo struct {
	Name       string    `json:"name"`
	Path       string    `json:"path"`
	Size       int64     `json:"size"`
	Checksum   string    `json:"checksum,omitempty"`
	Algorithm  ChecksumAlgorithm `json:"algorithm,omitempty"`
	Version    string    `json:"version,omitempty"`
	FetchedAt  time.Time `json:"fetchedAt"`
}

// ValidationConfig represents validation configuration
type ValidationConfig struct {
	Version     string            `json:"version,omitempty"`
	Checksum    string            `json:"checksum,omitempty"`
	Algorithm   ChecksumAlgorithm `json:"algorithm,omitempty"`
	PublicKey   string            `json:"publicKey,omitempty"`   // For signature verification
	Signature   string            `json:"signature,omitempty"`  // For signature verification
}

// ValidationResult represents the result of resource validation
type ValidationResult struct {
	Valid       bool   `json:"valid"`
	VersionOK   bool   `json:"versionOk"`
	ChecksumOK  bool   `json:"checksumOk"`
	SignatureOK bool   `json:"signatureOk"`
	Error       string `json:"error,omitempty"`
}

// PackageConfig represents package build configuration
type PackageConfig struct {
	Name          string            `json:"name"`
	Type          PackageType       `json:"type"`
	SourcePath    string            `json:"sourcePath"`
	OutputDir     string            `json:"outputDir"`
	BuildCommand  string            `json:"buildCommand,omitempty"`
	EnvVariables  map[string]string `json:"envVariables,omitempty"`
	ExcludePatterns []string        `json:"excludePatterns,omitempty"`
}

// PackageInfo represents information about a built package
type PackageInfo struct {
	Name       string    `json:"name"`
	Path       string    `json:"path"`
	Type       PackageType `json:"type"`
	Size       int64     `json:"size"`
	Checksum   string    `json:"checksum"`
	Version    string    `json:"version,omitempty"`
	BuiltAt    time.Time `json:"builtAt"`
}

// DeploymentConfig represents deployment configuration
type DeploymentConfig struct {
	TargetHosts   []string        `json:"targetHosts"`
	PackagePath   string          `json:"packagePath"`
	DeployDir     string          `json:"deployDir"`
	PreDeployCmd  string          `json:"preDeployCommand,omitempty"`
	PostDeployCmd string          `json:"postDeployCommand,omitempty"`
	StartCmd      string          `json:"startCommand,omitempty"`
	StopCmd       string          `json:"stopCommand,omitempty"`
	HealthCheck   *HealthCheckConfig `json:"healthCheck,omitempty"`
	Timeout       int64           `json:"timeout,omitempty"` // milliseconds
}

// HealthCheckConfig represents health check configuration
type HealthCheckConfig struct {
	Type        string `json:"type"` // HTTP, TCP, COMMAND, PROCESS
	Port        int    `json:"port,omitempty"`
	Path        string `json:"path,omitempty"`
	Command     string `json:"command,omitempty"`
	ProcessName string `json:"processName,omitempty"`
	Interval    int64  `json:"interval,omitempty"`  // milliseconds
	Timeout     int64  `json:"timeout,omitempty"`   // milliseconds
	MaxRetries  int    `json:"maxRetries,omitempty"`
}

// HealthCheckResult represents the result of a health check
type HealthCheckResult struct {
	Healthy   bool   `json:"healthy"`
	Status    string `json:"status"`
	Message   string `json:"message,omitempty"`
	Retries   int    `json:"retries"`
	CheckTime time.Time `json:"checkTime"`
}

// DeploymentResult represents the result of a deployment
type DeploymentResult struct {
	Status      DeploymentStatus `json:"status"`
	Version     string           `json:"version,omitempty"`
	StartedAt   time.Time        `json:"startedAt"`
	FinishedAt  time.Time        `json:"finishedAt,omitempty"`
	HostResults []HostDeployResult `json:"hostResults,omitempty"`
	Error       string           `json:"error,omitempty"`
}

// HostDeployResult represents deployment result for a single host
type HostDeployResult struct {
	Host        string           `json:"host"`
	Status      DeploymentStatus `json:"status"`
	StartedAt   time.Time        `json:"startedAt"`
	FinishedAt  time.Time        `json:"finishedAt,omitempty"`
	HealthCheck *HealthCheckResult `json:"healthCheck,omitempty"`
	Error       string           `json:"error,omitempty"`
}

// FetchRequest represents a resource fetch request
type FetchRequest struct {
	RequestID string          `json:"requestId"`
	Source    ResourceSource  `json:"source"`
	TargetDir string          `json:"targetDir"`
	Validate  *ValidationConfig `json:"validate,omitempty"`
}

// FetchResult represents the result of a fetch operation
type FetchResult struct {
	RequestID    string           `json:"requestId"`
	Status       string           `json:"status"` // SUCCESS, FAILED, TIMEOUT
	Resource     *ResourceInfo    `json:"resource,omitempty"`
	Validation   *ValidationResult `json:"validation,omitempty"`
	StartedAt    time.Time        `json:"startedAt"`
	FinishedAt   time.Time        `json:"finishedAt"`
	DurationMs   int64            `json:"durationMs"`
	Error        string           `json:"error,omitempty"`
}

// BuildRequest represents a package build request
type BuildRequest struct {
	RequestID string         `json:"requestId"`
	Config    PackageConfig  `json:"config"`
}

// BuildResult represents the result of a build operation
type BuildResult struct {
	RequestID  string        `json:"requestId"`
	Status     string        `json:"status"` // SUCCESS, FAILED
	Package    *PackageInfo  `json:"package,omitempty"`
	StartedAt  time.Time     `json:"startedAt"`
	FinishedAt time.Time     `json:"finishedAt"`
	DurationMs int64         `json:"durationMs"`
	Error      string        `json:"error,omitempty"`
}

// DeployRequest represents a deployment request
type DeployRequest struct {
	RequestID string           `json:"requestId"`
	Config    DeploymentConfig `json:"config"`
}

// DeployResult represents the result of a deploy operation
type DeployResult struct {
	RequestID  string            `json:"requestId"`
	Status     string            `json:"status"` // SUCCESS, FAILED, IN_PROGRESS
	Result     *DeploymentResult `json:"result,omitempty"`
	StartedAt  time.Time         `json:"startedAt"`
	FinishedAt time.Time         `json:"finishedAt,omitempty"`
	DurationMs int64             `json:"durationMs,omitempty"`
	Error      string            `json:"error,omitempty"`
}

// HealthCheckRequest represents a health check request
type HealthCheckRequest struct {
	RequestID string            `json:"requestId"`
	Config    HealthCheckConfig `json:"config"`
}

// HealthCheckResponse represents the response of a health check request
type HealthCheckResponse struct {
	RequestID  string            `json:"requestId"`
	Status     string            `json:"status"` // SUCCESS, FAILED
	Result     *HealthCheckResult `json:"result,omitempty"`
	CheckTime  time.Time         `json:"checkTime"`
	Error      string            `json:"error,omitempty"`
}