package config

import (
	"os"
	"path/filepath"
	"testing"
	"time"
)

func TestLoad(t *testing.T) {
	tests := []struct {
		name     string
		setup    func() string // returns config file path
		wantPort int
		wantErr  bool
	}{
		{
			name: "valid config file",
			setup: func() string {
				content := `
listen_port: 8080
host_id: test-host-id
secret_key: test-secret-key
heartbeat_interval: 30s
`
				tmpDir := t.TempDir()
				configPath := filepath.Join(tmpDir, "config.yaml")
				os.WriteFile(configPath, []byte(content), 0644)
				return configPath
			},
			wantPort: 8080,
			wantErr:  false,
		},
		{
			name: "config with missing heartbeat uses default",
			setup: func() string {
				content := `
listen_port: 9091
host_id: test-host-id
secret_key: test-secret-key
`
				tmpDir := t.TempDir()
				configPath := filepath.Join(tmpDir, "config.yaml")
				os.WriteFile(configPath, []byte(content), 0644)
				return configPath
			},
			wantPort: 9091,
			wantErr:  false,
		},
		{
			name: "config with zero heartbeat uses default",
			setup: func() string {
				content := `
listen_port: 9092
host_id: test-host-id
secret_key: test-secret-key
heartbeat_interval: 0s
`
				tmpDir := t.TempDir()
				configPath := filepath.Join(tmpDir, "config.yaml")
				os.WriteFile(configPath, []byte(content), 0644)
				return configPath
			},
			wantPort: 9092,
			wantErr:  false,
		},
		{
			name: "config with custom port",
			setup: func() string {
				content := `
listen_port: 9999
host_id: test-host-id
secret_key: test-secret-key
heartbeat_interval: 60s
`
				tmpDir := t.TempDir()
				configPath := filepath.Join(tmpDir, "config.yaml")
				os.WriteFile(configPath, []byte(content), 0644)
				return configPath
			},
			wantPort: 9999,
			wantErr:  false,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			configPath := tt.setup()
			cfg, err := Load(configPath)

			if (err != nil) != tt.wantErr {
				t.Errorf("Load() error = %v, wantErr %v", err, tt.wantErr)
				return
			}

			if cfg != nil && cfg.ListenPort != tt.wantPort {
				t.Errorf("Load().ListenPort = %v, want %v", cfg.ListenPort, tt.wantPort)
			}
		})
	}
}

func TestLoad_HeartbeatInterval(t *testing.T) {
	tests := []struct {
		name     string
		content  string
		wantDur  time.Duration
	}{
		{
			name: "30 seconds",
			content: `heartbeat_interval: 30s`,
			wantDur: 30 * time.Second,
		},
		{
			name: "1 minute",
			content: `heartbeat_interval: 1m`,
			wantDur: 1 * time.Minute,
		},
		{
			name: "default when missing",
			content: `listen_port: 9090`,
			wantDur: 30 * time.Second,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			tmpDir := t.TempDir()
			configPath := filepath.Join(tmpDir, "config.yaml")
			os.WriteFile(configPath, []byte(tt.content), 0644)

			cfg, err := Load(configPath)
			if err != nil {
				t.Fatalf("Load() error = %v", err)
			}

			if cfg.HeartbeatInterval != tt.wantDur {
				t.Errorf("HeartbeatInterval = %v, want %v", cfg.HeartbeatInterval, tt.wantDur)
			}
		})
	}
}

func TestLoad_Defaults(t *testing.T) {
	// Test with empty config
	tmpDir := t.TempDir()
	configPath := filepath.Join(tmpDir, "config.yaml")
	os.WriteFile(configPath, []byte(""), 0644)

	cfg, err := Load(configPath)
	if err != nil {
		t.Fatalf("Load() error = %v", err)
	}

	// Check defaults are applied
	if cfg.ListenPort != 9090 {
		t.Errorf("default ListenPort should be 9090, got %d", cfg.ListenPort)
	}

	if cfg.HeartbeatInterval != 30*time.Second {
		t.Errorf("default HeartbeatInterval should be 30s, got %v", cfg.HeartbeatInterval)
	}
}

func TestLoad_InvalidYAML(t *testing.T) {
	tmpDir := t.TempDir()
	configPath := filepath.Join(tmpDir, "config.yaml")
	os.WriteFile(configPath, []byte("invalid: yaml: content: ["), 0644)

	_, err := Load(configPath)
	if err == nil {
		t.Error("Load() should return error for invalid YAML")
	}
}

func TestConfig_Structure(t *testing.T) {
	// Verify Config struct has expected fields
	cfg := Config{
		ListenPort:        9090,
		HostID:            "test-host",
		SecretKey:         "test-secret",
		HeartbeatInterval: 30 * time.Second,
		Version:           "1.0.0",
	}

	if cfg.ListenPort != 9090 {
		t.Errorf("ListenPort = %d, want 9090", cfg.ListenPort)
	}
	if cfg.HostID != "test-host" {
		t.Errorf("HostID = %s, want test-host", cfg.HostID)
	}
	if cfg.SecretKey != "test-secret" {
		t.Errorf("SecretKey = %s, want test-secret", cfg.SecretKey)
	}
	if cfg.HeartbeatInterval != 30*time.Second {
		t.Errorf("HeartbeatInterval = %v, want 30s", cfg.HeartbeatInterval)
	}
}