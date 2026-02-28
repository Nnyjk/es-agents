package compatibility_test

import (
	"os/exec"
	"path/filepath"
	"runtime"
	"testing"
)

func TestLinuxBuildDoesNotRequireGLIBC234(t *testing.T) {
	if runtime.GOOS != "linux" {
		t.Skip("linux compatibility verification runs on linux only")
	}

	tmpDir := t.TempDir()
	output := filepath.Join(tmpDir, "host-agent-linux-amd64")
	script := filepath.Join("scripts", "verify-linux-compat.sh")

	cmd := exec.Command("bash", script, output)
	cmd.Dir = filepath.Join("..", "..")
	if out, err := cmd.CombinedOutput(); err != nil {
		t.Fatalf("compatibility verification failed: %v\n%s", err, string(out))
	}
}
