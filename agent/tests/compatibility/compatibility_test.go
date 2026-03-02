package compatibility_test

import (
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"runtime"
	"strings"
	"testing"
)

var glibcVersionPattern = regexp.MustCompile(`GLIBC_(\d+\.\d+)`)

func TestHostAgentLinuxBuildIsStaticAndGlibcFree(t *testing.T) {
	if runtime.GOOS != "linux" {
		t.Skip("linux compatibility verification runs on linux only")
	}

	requireCommands(t, "go", "file", "ldd", "readelf", "objdump", "strings")

	tmpDir := t.TempDir()
	output := filepath.Join(tmpDir, "host-agent-linux-amd64")

	build := exec.Command("go", "build", "-trimpath", "-tags", "netgo osusergo", "-o", output, "./cmd/host-agent")
	build.Dir = filepath.Join("..", "..")
	build.Env = append(os.Environ(), "CGO_ENABLED=0", "GOOS=linux", "GOARCH=amd64")
	if out, err := build.CombinedOutput(); err != nil {
		t.Fatalf("static host-agent build failed: %v\n%s", err, out)
	}

	assertELFMetadata(t, output)
	assertStaticLinking(t, output)
	assertNoDynamicLoader(t, output)
	assertNoGLIBCSymbols(t, output)
}

func requireCommands(t *testing.T, commands ...string) {
	t.Helper()

	for _, command := range commands {
		if _, err := exec.LookPath(command); err != nil {
			t.Skipf("required command %q is unavailable: %v", command, err)
		}
	}
}

func assertELFMetadata(t *testing.T, binaryPath string) {
	t.Helper()

	fileOutput := mustRun(t, exec.Command("file", binaryPath))
	if !strings.Contains(fileOutput, "ELF 64-bit") || !strings.Contains(fileOutput, "x86-64") {
		t.Fatalf("unexpected file metadata for %s:\n%s", binaryPath, fileOutput)
	}
}

func assertStaticLinking(t *testing.T, binaryPath string) {
	t.Helper()

	lddOutput := runAllowFailure(exec.Command("ldd", binaryPath))
	if strings.Contains(lddOutput, "not found") {
		t.Fatalf("ldd reported unresolved runtime dependencies:\n%s", lddOutput)
	}

	if strings.Contains(lddOutput, "not a dynamic executable") || strings.Contains(lddOutput, "statically linked") {
		return
	}

	t.Fatalf("host-agent must be statically linked:\n%s", lddOutput)
}

func assertNoDynamicLoader(t *testing.T, binaryPath string) {
	t.Helper()

	programHeaders := mustRun(t, exec.Command("readelf", "-l", binaryPath))
	if strings.Contains(programHeaders, "INTERP") {
		t.Fatalf("static binary unexpectedly exposes INTERP program header:\n%s", programHeaders)
	}

	dynamicSection := runAllowFailure(exec.Command("readelf", "-d", binaryPath))
	objdumpHeaders := runAllowFailure(exec.Command("objdump", "-p", binaryPath))
	if strings.Contains(dynamicSection, "(NEEDED)") || strings.Contains(objdumpHeaders, "NEEDED") {
		t.Fatalf("static binary unexpectedly exposes dynamic dependencies:\nreadelf -d:\n%s\nobjdump -p:\n%s", dynamicSection, objdumpHeaders)
	}
}

func assertNoGLIBCSymbols(t *testing.T, binaryPath string) {
	t.Helper()

	outputs := []string{
		mustRun(t, exec.Command("strings", binaryPath)),
		runAllowFailure(exec.Command("readelf", "--version-info", binaryPath)),
		runAllowFailure(exec.Command("objdump", "-T", binaryPath)),
	}

	if matches := glibcVersionPattern.FindAllString(strings.Join(outputs, "\n"), -1); len(matches) > 0 {
		t.Fatalf("detected unexpected GLIBC symbol references: %s", strings.Join(matches, ", "))
	}
}

func mustRun(t *testing.T, cmd *exec.Cmd) string {
	t.Helper()

	output, err := cmd.CombinedOutput()
	if err != nil {
		t.Fatalf("%q failed: %v\n%s", strings.Join(cmd.Args, " "), err, output)
	}
	return string(output)
}

func runAllowFailure(cmd *exec.Cmd) string {
	output, _ := cmd.CombinedOutput()
	return string(output)
}
