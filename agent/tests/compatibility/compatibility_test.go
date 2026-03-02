package compatibility_test

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"runtime"
	"slices"
	"strings"
	"testing"
)

const targetGLIBCBaseline = "2.34"

var glibcVersionPattern = regexp.MustCompile(`GLIBC_(\d+\.\d+)`)

func TestLinuxBuildMeetsGLIBCBaseline(t *testing.T) {
	if runtime.GOOS != "linux" {
		t.Skip("linux compatibility verification runs on linux only")
	}

	requireCommands(t, "bash", "ldd", "readelf", "objdump", "strings", "file")

	tmpDir := t.TempDir()
	output := filepath.Join(tmpDir, "host-agent-linux-amd64")
	rootDir := filepath.Join("..", "..")
	script := filepath.Join("scripts", "verify-linux-compat.sh")

	cmd := exec.Command("bash", script, output)
	cmd.Dir = rootDir
	cmd.Env = append(os.Environ(), "TARGET_GLIBC_VERSION="+targetGLIBCBaseline)
	if out, err := cmd.CombinedOutput(); err != nil {
		t.Fatalf("compatibility verification failed: %v\n%s", err, out)
	}

	assertELFMetadata(t, output)
	assertLddBaseline(t, output)
	assertReadelfAndObjdumpBaseline(t, output)
	assertGLIBCBaseline(t, output, targetGLIBCBaseline)
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

func assertLddBaseline(t *testing.T, binaryPath string) {
	t.Helper()

	lddOutput := runAllowFailure(t, exec.Command("ldd", binaryPath))
	if strings.Contains(lddOutput, "not found") {
		t.Fatalf("ldd reported unresolved runtime dependencies:\n%s", lddOutput)
	}

	if strings.Contains(lddOutput, "not a dynamic executable") || strings.Contains(lddOutput, "statically linked") {
		return
	}

	if !strings.Contains(lddOutput, "=>") && !strings.Contains(lddOutput, "/lib") {
		t.Fatalf("unexpected ldd output:\n%s", lddOutput)
	}
}

func assertReadelfAndObjdumpBaseline(t *testing.T, binaryPath string) {
	t.Helper()

	programHeaders := mustRun(t, exec.Command("readelf", "-l", binaryPath))
	dynamicSection := runAllowFailure(t, exec.Command("readelf", "-d", binaryPath))
	objdumpHeaders := runAllowFailure(t, exec.Command("objdump", "-p", binaryPath))

	isDynamic := strings.Contains(programHeaders, "INTERP")
	hasNeededEntries := strings.Contains(dynamicSection, "(NEEDED)") || strings.Contains(objdumpHeaders, "NEEDED")

	if isDynamic && !hasNeededEntries {
		t.Fatalf("dynamic binary is missing NEEDED entries:\nreadelf -d:\n%s\nobjdump -p:\n%s", dynamicSection, objdumpHeaders)
	}

	if !isDynamic && hasNeededEntries {
		t.Fatalf("static binary unexpectedly exposes NEEDED entries:\nreadelf -d:\n%s\nobjdump -p:\n%s", dynamicSection, objdumpHeaders)
	}
}

func assertGLIBCBaseline(t *testing.T, binaryPath string, targetVersion string) {
	t.Helper()

	outputs := []string{
		mustRun(t, exec.Command("strings", binaryPath)),
		runAllowFailure(t, exec.Command("readelf", "--version-info", binaryPath)),
		runAllowFailure(t, exec.Command("objdump", "-T", binaryPath)),
	}

	versions := collectGLIBCVersions(strings.Join(outputs, "\n"))
	for _, version := range versions {
		if compareGLIBCVersion(version, targetVersion) > 0 {
			t.Fatalf("detected unsupported GLIBC version %s; allowed baseline is %s\n%s", version, targetVersion, strings.Join(outputs, "\n"))
		}
	}
}

func collectGLIBCVersions(output string) []string {
	matches := glibcVersionPattern.FindAllStringSubmatch(output, -1)
	if len(matches) == 0 {
		return nil
	}

	unique := make(map[string]struct{}, len(matches))
	for _, match := range matches {
		if len(match) > 1 {
			unique[match[1]] = struct{}{}
		}
	}

	versions := make([]string, 0, len(unique))
	for version := range unique {
		versions = append(versions, version)
	}
	slices.SortFunc(versions, compareGLIBCVersion)
	return versions
}

func compareGLIBCVersion(a, b string) int {
	var majorA, minorA int
	var majorB, minorB int

	fmt.Sscanf(a, "%d.%d", &majorA, &minorA)
	fmt.Sscanf(b, "%d.%d", &majorB, &minorB)

	switch {
	case majorA != majorB:
		return majorA - majorB
	default:
		return minorA - minorB
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

func runAllowFailure(t *testing.T, cmd *exec.Cmd) string {
	t.Helper()

	output, err := cmd.CombinedOutput()
	if err != nil {
		return string(output)
	}
	return string(output)
}
