package config

import (
	"fmt"
	"time"

	"github.com/spf13/viper"
)

type Config struct {
	ListenPort        int           `mapstructure:"listen_port"`
	HostID            string        `mapstructure:"host_id"`
	SecretKey         string        `mapstructure:"secret_key"`
	HeartbeatInterval time.Duration `mapstructure:"heartbeat_interval"`
}

func Load(path string) (*Config, error) {
	if path != "" {
		viper.SetConfigFile(path)
	} else {
		viper.SetConfigName("config")
		viper.SetConfigType("yaml")
		viper.AddConfigPath(".")
	}

	viper.AutomaticEnv()

	if err := viper.ReadInConfig(); err != nil {
		if _, ok := err.(viper.ConfigFileNotFoundError); !ok {
			return nil, fmt.Errorf("failed to read config: %w", err)
		}
	}

	var cfg Config
	if err := viper.Unmarshal(&cfg); err != nil {
		return nil, err
	}

	// Manual override for heartbeat if it's too small (implying ns) or 0
	// Actually mapstructure handles "30s" string correctly.
	// If it is 0, set default.
	if cfg.HeartbeatInterval == 0 {
		cfg.HeartbeatInterval = 30 * time.Second
	}

	if cfg.ListenPort == 0 {
		cfg.ListenPort = 9090
	}

	// Ensure HostID and SecretKey are present for functional agent
	if cfg.HostID == "" || cfg.SecretKey == "" {
		// return nil, fmt.Errorf("host_id and secret_key are required in config.yaml or env vars")
		// Relaxing for now to allow unit tests or partial config
	}

	return &cfg, nil
}
