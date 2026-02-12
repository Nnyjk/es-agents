package main

import (
	"fmt"
	"os"
	"strings"

	"github.com/easy-station/agent/internal/plugin"
	"github.com/spf13/cobra"
)

var commandStr string
var argsSlice []string

var rootCmd = &cobra.Command{
	Use:   "tool-exec",
	Short: "Easy-Station Tool Executor",
	Long:  "Execute a command with OS-aware handling (cmd/sh/ps1/bat).",
	Run: func(cmd *cobra.Command, args []string) {
		if commandStr == "" {
			fmt.Println("Error: --command is required")
			os.Exit(1)
		}

		flatArgs := make([]string, 0, len(argsSlice))
		for _, a := range argsSlice {
			if a == "" {
				continue
			}
			if strings.Contains(a, ",") {
				flatArgs = append(flatArgs, strings.Split(a, ",")...)
			} else {
				flatArgs = append(flatArgs, a)
			}
		}

		c := plugin.BuildCommand(commandStr, flatArgs)
		c.Stdout = os.Stdout
		c.Stderr = os.Stderr
		if err := c.Run(); err != nil {
			fmt.Printf("Execution failed: %v\n", err)
			os.Exit(1)
		}
	},
}

func init() {
	rootCmd.PersistentFlags().StringVarP(&commandStr, "command", "c", "", "Command or script to execute")
	rootCmd.PersistentFlags().StringSliceVarP(&argsSlice, "args", "a", []string{}, "Arguments (repeat or comma-separated)")
}

func main() {
	if err := rootCmd.Execute(); err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
}

