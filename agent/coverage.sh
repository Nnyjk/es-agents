#!/bin/bash

# Agent Test Coverage Script
# Generates detailed coverage report in cobertura format for CI

set -e

echo "Running tests with coverage..."

# Create coverage output directory
COVERAGE_DIR="coverage"
mkdir -p "$COVERAGE_DIR"

# Collect coverage for all packages
go test ./... -coverprofile="$COVERAGE_DIR/coverage.out" -covermode=atomic -json > "$COVERAGE_DIR/test_output.json" 2>&1 || true

# Show summary
echo ""
echo "=== Coverage Summary ==="
go tool cover -func="$COVERAGE_DIR/coverage.out"

# Generate HTML report
echo ""
echo "Generating HTML coverage report..."
go tool cover -html="$COVERAGE_DIR/coverage.out" -o="$COVERAGE_DIR/coverage.html"

# Generate cobertura XML format for CI
echo ""
echo "Generating cobertura XML report..."

# Convert Go coverage to cobertura format
cat > "$COVERAGE_DIR/gocover-cobertura.go" << 'GOCODE'
package main

import (
	"encoding/xml"
	"fmt"
	"go/coverage"
	"os"
	"path/filepath"
	"strings"
	"time"
)

type Cobertura struct {
	XMLName     xml.Name `xml:"coverage"`
	Version     string   `xml:"version,attr"`
	Timestamp   int64    `xml:"timestamp,attr"`
	LineRate    float64  `xml:"line-rate,attr"`
	BranchRate  float64  `xml:"branch-rate,attr"`
	Complexity  float64  `xml:"complexity,attr"`
	Packages    []Package `xml:"packages>package"`
}

type Package struct {
	Name     string   `xml:"name,attr"`
	Complexity float64 `xml:"complexity,attr"`
	LineRate float64  `xml:"line-rate,attr"`
	BranchRate float64 `xml:"branch-rate,attr"`
	Classes  []Class  `xml:"classes>class"`
}

type Class struct {
	Name       string  `xml:"name,attr"`
	Filename   string  `xml:"filename,attr"`
	LineRate   float64 `xml:"line-rate,attr"`
	BranchRate float64 `xml:"branch-rate,attr"`
	Complexity float64 `xml:"complexity,attr"`
	Lines      []Line  `xml:"lines>line"`
}

type Line struct {
	Number int     `xml:"number,attr"`
	Hits   int     `xml:"hits,attr"`
}

func main() {
	if len(os.Args) < 2 {
		fmt.Println("Usage: gocover-cobertura <coverage.out>")
		os.Exit(1)
	}

	profile, err := coverage.ParseProfileFile(os.Args[1])
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error parsing coverage file: %v\n", err)
		os.Exit(1)
	}

	cobertura := Cobertura{
		Version:    "1.0",
		Timestamp:  time.Now().Unix(),
		Complexity: 0,
		Packages:   []Package{},
	}

	// Group by package
	packageMap := make(map[string]*Package)

	for _, block := range profile.Profiles[0].Blocks {
		pkg := filepath.Dir(block.FileName)
		if pkg == "." {
			pkg = "root"
		}

		if _, exists := packageMap[pkg]; !exists {
			packageMap[pkg] = &Package{
				Name:       pkg,
				Complexity: 0,
				Classes:    []Class{},
			}
		}

		// Find or create class for this file
		className := strings.TrimSuffix(filepath.Base(block.FileName), ".go")
		var class *Class
		for i := range packageMap[pkg].Classes {
			if packageMap[pkg].Classes[i].Filename == block.FileName {
				class = &packageMap[pkg].Classes[i]
				break
			}
		}

		if class == nil {
			class = &Class{
				Name:       className,
				Filename:   block.FileName,
				LineRate:   0,
				BranchRate: 0,
				Complexity: 0,
				Lines:      []Line{},
			}
			packageMap[pkg].Classes = append(packageMap[pkg].Classes, *class)
			class = &packageMap[pkg].Classes[len(packageMap[pkg].Classes)-1]
		}

		// Add lines
		for lineNo := block.StartLine; lineNo <= block.EndLine; lineNo++ {
			class.Lines = append(class.Lines, Line{
				Number: lineNo,
				Hits:   block.Count,
			})
		}
	}

	// Calculate rates
	totalLines := 0
	coveredLines := 0
	for _, pkg := range packageMap {
		for i := range pkg.Classes {
			class := &pkg.Classes[i]
			classLines := len(class.Lines)
			classCovered := 0
			for _, line := range class.Lines {
				if line.Hits > 0 {
					classCovered++
				}
			}
			if classLines > 0 {
				class.LineRate = float64(classCovered) / float64(classLines)
			}
			totalLines += classLines
			coveredLines += classCovered
		}
		pkg.LineRate = 0 // Will calculate from total
		cobertura.Packages = append(cobertura.Packages, *pkg)
	}

	if totalLines > 0 {
		cobertura.LineRate = float64(coveredLines) / float64(totalLines)
	}

	// Output XML
	output, _ := xml.MarshalIndent(cobertura, "", "  ")
	fmt.Println(xml.Header + string(output))
}
GOCODE

# Try to generate cobertura format using gocov-xml if available
if command -v gocov &> /dev/null && command -v gocov-xml &> /dev/null; then
    gocov test ./... | gocov-xml > "$COVERAGE_DIR/cobertura.xml"
    echo "Cobertura XML generated using gocov-xml"
else
    # Fallback: generate simple cobertura format manually
    echo "gocov-xml not available, generating simplified cobertura format..."
    
    # Extract coverage data and create basic cobertura XML
    TOTAL_LINES=$(awk -F: '{sum+=$3} END {print sum}' "$COVERAGE_DIR/coverage.out" 2>/dev/null || echo "0")
    COVERED_LINES=$(awk -F: '{sum+=$4} END {print sum}' "$COVERAGE_DIR/coverage.out" 2>/dev/null || echo "0")
    
    if [ "$TOTAL_LINES" -gt 0 ]; then
        LINE_RATE=$(echo "scale=4; $COVERED_LINES / $TOTAL_LINES" | bc 2>/dev/null || echo "0")
    else
        LINE_RATE="0"
    fi
    
    cat > "$COVERAGE_DIR/cobertura.xml" << XMLHEADER
<?xml version="1.0" ?>
<coverage version="1.0" timestamp="$(date +%s)" lines-valid="$TOTAL_LINES" lines-covered="$COVERED_LINES" line-rate="$LINE_RATE" branch-rate="0" complexity="0">
  <sources>
    <source>.</source>
  </sources>
  <packages>
XMLHEADER

    # Add packages
    awk -F: '
    {
        file = $1
        gsub(/^[^:]*:/, "", file)
        pkg = file
        gsub(/\/[^\/]*$/, "", pkg)
        if (pkg == "" || pkg == ".") pkg = "root"
        
        total[file] += $3
        covered[file] += $4
        pkg_files[pkg] = pkg_files[pkg] " " file
    }
    END {
        for (pkg in pkg_files) {
            printf "    <package name=\"%s\" line-rate=\"1\" branch-rate=\"0\" complexity=\"0\">\n", pkg
            printf "      <classes>\n"
            n = split(pkg_files[pkg], files, " ")
            for (i = 1; i <= n; i++) {
                f = files[i]
                if (f == "") continue
                fname = f
                gsub(/.*\//, "", fname)
                gsub(/\.go$/, "", fname)
                rate = (total[f] > 0) ? covered[f] / total[f] : 0
                printf "        <class name=\"%s\" filename=\"%s\" line-rate=\"%.4f\" branch-rate=\"0\" complexity=\"0\">\n", fname, f, rate
                printf "          <lines>\n"
                printf "          </lines>\n"
                printf "        </class>\n"
            }
            printf "      </classes>\n"
            printf "    </package>\n"
        }
    }
    ' "$COVERAGE_DIR/coverage.out" >> "$COVERAGE_DIR/cobertura.xml"

    cat >> "$COVERAGE_DIR/cobertura.xml" << XMLFOOTER
  </packages>
</coverage>
XMLFOOTER

    echo "Simplified cobertura XML generated at: $COVERAGE_DIR/cobertura.xml"
fi

# Cleanup temporary Go file
rm -f "$COVERAGE_DIR/gocover-cobertura.go"

echo ""
echo "=== Coverage Files Generated ==="
echo "  - $COVERAGE_DIR/coverage.out (Go native format)"
echo "  - $COVERAGE_DIR/coverage.html (HTML report)"
echo "  - $COVERAGE_DIR/cobertura.xml (Cobertura XML for CI)"
echo ""
