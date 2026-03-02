package com.easystation.infra.service;

import com.easystation.agent.domain.enums.OsType;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ApplicationScoped
public class HostAgentPackageBuilder {

    public void writePackage(
            OutputStream output,
            HostAgentResourceResolver.ResolvedHostAgentResource resource,
            InputStream binaryStream,
            String configContent
    ) throws IOException {
        Map<String, String> scripts = resource.osType() == OsType.WINDOWS
                ? windowsScripts(resource.fileName())
                : linuxScripts(resource.fileName());

        if (resource.osType() == OsType.WINDOWS) {
            try (ZipOutputStream zos = new ZipOutputStream(output)) {
                addZipFile(zos, resource.fileName(), binaryStream);
                addZipText(zos, "config.yaml", configContent);

                for (Map.Entry<String, String> script : scripts.entrySet()) {
                    addZipText(zos, script.getKey(), script.getValue());
                }
            }
            return;
        }

        try (GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(output);
             TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(gzipOutputStream)) {
            tarOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

            addTarFile(tarOutputStream, resource.fileName(), binaryStream, 0755);
            addTarText(tarOutputStream, "config.yaml", configContent, 0644);

            for (Map.Entry<String, String> script : scripts.entrySet()) {
                addTarText(tarOutputStream, script.getKey(), script.getValue(), 0755);
            }
        }
    }

    private void addZipFile(ZipOutputStream zos, String name, InputStream content) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        content.transferTo(zos);
        zos.closeEntry();
    }

    private void addZipText(ZipOutputStream zos, String name, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private void addTarFile(TarArchiveOutputStream tarOutputStream, String name, InputStream content, int mode)
            throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        byte[] data = content.readAllBytes();
        entry.setSize(data.length);
        entry.setMode(mode);
        tarOutputStream.putArchiveEntry(entry);
        tarOutputStream.write(data);
        tarOutputStream.closeArchiveEntry();
    }

    private void addTarText(TarArchiveOutputStream tarOutputStream, String name, String content, int mode)
            throws IOException {
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(data.length);
        entry.setMode(mode);
        tarOutputStream.putArchiveEntry(entry);
        tarOutputStream.write(data);
        tarOutputStream.closeArchiveEntry();
    }

    private Map<String, String> linuxScripts(String binaryName) {
        Map<String, String> scripts = new LinkedHashMap<>();
        scripts.put("install.sh", linuxInstallScript());
        scripts.put("start.sh", linuxStartScript(binaryName));
        scripts.put("stop.sh", linuxStopScript());
        scripts.put("update.sh", linuxUpdateScript(binaryName));
        return scripts;
    }

    private String linuxInstallScript() {
        return """
                #!/usr/bin/env bash
                set -eu

                SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
                "$SCRIPT_DIR/start.sh"
                """;
    }

    private String linuxStartScript(String binaryName) {
        return """
                #!/usr/bin/env bash
                set -eu

                SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
                PID_FILE="$SCRIPT_DIR/host-agent.pid"
                LOG_DIR="$SCRIPT_DIR/logs"
                LOG_FILE="$LOG_DIR/host-agent.log"
                BINARY_PATH="$SCRIPT_DIR/%s"
                CONFIG_PATH="$SCRIPT_DIR/config.yaml"

                mkdir -p "$LOG_DIR"
                chmod +x "$BINARY_PATH"

                if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
                  echo "HostAgent already running with PID $(cat "$PID_FILE")"
                  exit 0
                fi

                nohup "$BINARY_PATH" --config "$CONFIG_PATH" >> "$LOG_FILE" 2>&1 &
                printf '%%s' "$!" > "$PID_FILE"
                echo "HostAgent started in background. PID: $(cat "$PID_FILE")"
                echo "Log file: $LOG_FILE"
                """.formatted(binaryName);
    }

    private String linuxStopScript() {
        return """
                #!/usr/bin/env bash
                set -eu

                SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
                PID_FILE="$SCRIPT_DIR/host-agent.pid"

                if [ ! -f "$PID_FILE" ]; then
                  echo "HostAgent is not running."
                  exit 0
                fi

                PID="$(cat "$PID_FILE")"
                if kill -0 "$PID" 2>/dev/null; then
                  kill "$PID"
                  echo "HostAgent stopped."
                else
                  echo "HostAgent process not found, removing stale PID file."
                fi
                rm -f "$PID_FILE"
                """;
    }

    private String linuxUpdateScript(String binaryName) {
        return """
                #!/usr/bin/env bash
                set -eu

                SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
                SOURCE_DIR="${1:-$SCRIPT_DIR}"
                BINARY_NAME="%s"

                # Extract and replace binaries and scripts (except config.yaml)
                for FILE in "$BINARY_NAME" install.sh start.sh stop.sh update.sh; do
                  if [ ! -f "$SOURCE_DIR/$FILE" ]; then
                    continue
                  fi
                  if [ "$SOURCE_DIR/$FILE" = "$SCRIPT_DIR/$FILE" ]; then
                    continue
                  fi
                  cp "$SOURCE_DIR/$FILE" "$SCRIPT_DIR/$FILE"
                done

                chmod +x "$SCRIPT_DIR/$BINARY_NAME" "$SCRIPT_DIR/install.sh" "$SCRIPT_DIR/start.sh" "$SCRIPT_DIR/stop.sh" "$SCRIPT_DIR/update.sh"
                echo "HostAgent binaries and scripts updated."
                echo "config.yaml preserved at $SCRIPT_DIR/config.yaml"
                echo "Run ./start.sh to start the new version."
                """.formatted(binaryName);
    }

    private Map<String, String> windowsScripts(String binaryName) {
        Map<String, String> scripts = new LinkedHashMap<>();
        scripts.put("install.bat", windowsInstallScript());
        scripts.put("start.bat", windowsStartScript(binaryName));
        scripts.put("stop.bat", windowsStopScript());
        scripts.put("update.bat", windowsUpdateScript(binaryName));
        return scripts;
    }

    private String windowsInstallScript() {
        return """
                @echo off
                setlocal
                call "%~dp0start.bat"
                """;
    }

    private String windowsStartScript(String binaryName) {
        return """
                @echo off
                setlocal
                set "SCRIPT_DIR=%~dp0"
                set "PID_FILE=%SCRIPT_DIR%host-agent.pid"
                set "LOG_DIR=%SCRIPT_DIR%logs"
                set "LOG_FILE=%LOG_DIR%host-agent.log"

                if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

                if exist "%PID_FILE%" (
                  for /f "usebackq delims=" %%P in ("%PID_FILE%") do (
                    tasklist /FI "PID eq %%P" | find "%%P" >nul
                    if not errorlevel 1 (
                      echo HostAgent already running with PID %%P
                      exit /b 0
                    )
                  )
                )

                powershell -NoProfile -ExecutionPolicy Bypass -Command ^
                  "$binary = Join-Path $env:SCRIPT_DIR '__BINARY__';" ^
                  "$config = Join-Path $env:SCRIPT_DIR 'config.yaml';" ^
                  "$pidFile = Join-Path $env:SCRIPT_DIR 'host-agent.pid';" ^
                  "$logFile = Join-Path (Join-Path $env:SCRIPT_DIR 'logs') 'host-agent.log';" ^
                  "$proc = Start-Process -FilePath $binary -ArgumentList '--config', $config -RedirectStandardOutput $logFile -RedirectStandardError $logFile -WindowStyle Hidden -PassThru;" ^
                  "Set-Content -Path $pidFile -Value $proc.Id;" ^
                  "Write-Output ('HostAgent started in background. PID: ' + $proc.Id);" ^
                  "Write-Output ('Log file: ' + $logFile)"
                """.replace("__BINARY__", binaryName);
    }

    private String windowsStopScript() {
        return """
                @echo off
                setlocal
                set "PID_FILE=%~dp0host-agent.pid"

                if not exist "%PID_FILE%" (
                  echo HostAgent is not running.
                  exit /b 0
                )

                for /f "usebackq delims=" %%P in ("%PID_FILE%") do (
                  powershell -NoProfile -ExecutionPolicy Bypass -Command "Stop-Process -Id %%P -Force -ErrorAction SilentlyContinue"
                  echo HostAgent stopped.
                )
                del /q "%PID_FILE%" >nul 2>nul
                """;
    }

    private String windowsUpdateScript(String binaryName) {
        return """
                @echo off
                setlocal
                set "SCRIPT_DIR=%~dp0"
                set "SOURCE_DIR=%~1"
                if "%SOURCE_DIR%"=="" set "SOURCE_DIR=%SCRIPT_DIR%"

                if exist "%SCRIPT_DIR%stop.bat" call "%SCRIPT_DIR%stop.bat"

                for %%F in (__BINARY__ install.bat start.bat stop.bat update.bat) do (
                  if exist "%SOURCE_DIR%\\%%F" (
                    if /I not "%SOURCE_DIR%\\%%F"=="%SCRIPT_DIR%\\%%F" copy /Y "%SOURCE_DIR%\\%%F" "%SCRIPT_DIR%\\%%F" >nul
                  )
                )

                echo HostAgent binaries and scripts updated.
                echo config.yaml preserved at %SCRIPT_DIR%config.yaml
                echo Run start.bat to start the new version.
                """.replace("__BINARY__", binaryName);
    }
}
