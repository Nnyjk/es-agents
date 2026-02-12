package main

import (
	"fmt"
	"os"

	"github.com/easy-station/agent/internal/app"
	"github.com/easy-station/agent/internal/config"
	"github.com/spf13/cobra"
)

var cfgFile string

var rootCmd = &cobra.Command{
	Use:   "host-agent",
	Short: "Easy-Station Host Agent",
	Long:  `A lightweight agent that connects to Easy-Station Server to handle tasks.`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("Easy-Station Host Agent starting...")

		cfg, err := config.Load(cfgFile)
		if err != nil {
			fmt.Printf("Error loading config: %v\n", err)
			os.Exit(1)
		}

		if cfg.HostID == "" {
			fmt.Println("Error: host_id is required. Please set it in config.yaml or env var HOST_ID")
			os.Exit(1)
		}

		agent := app.New(cfg)
		if err := agent.Run(); err != nil {
			fmt.Printf("Agent runtime error: %v\n", err)
			os.Exit(1)
		}
	},
}

func init() {
	rootCmd.PersistentFlags().StringVarP(&cfgFile, "config", "c", "", "config file (default is ./config.yaml)")
}

func main() {
	if err := rootCmd.Execute(); err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
}
