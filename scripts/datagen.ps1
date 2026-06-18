# Run NeoForge data generation and auto-terminate.
#
# Why: NeoForge's dev `runData` generates everything in well under a second, but its
# JVM never exits — Minecraft's bootstrap/IO executor pools (`pool-1-*`, `pool-2-*`)
# are non-daemon threads that the dev datagen path never shuts down, so the process
# lingers forever. This wrapper runs the task, waits for the "All providers took"
# completion marker in the run log, then kills the lingering JVM. Generation output
# is already fully written by that point.
#
# Usage:  pwsh -File scripts/datagen.ps1     (or right-click > Run with PowerShell)

$ErrorActionPreference = "Stop"
$repo = Split-Path $PSScriptRoot -Parent
Set-Location $repo

$runLog = Join-Path $repo "neoforge\run\logs\latest.log"
$gradleLog = Join-Path $env:TEMP "theleadage-datagen.log"
Remove-Item $runLog -ErrorAction SilentlyContinue

$startTime = Get-Date
$gradle = Start-Process -FilePath (Join-Path $repo "gradlew.bat") `
    -ArgumentList ":neoforge:runData", "--console=plain" `
    -RedirectStandardOutput $gradleLog -RedirectStandardError "$gradleLog.err" `
    -PassThru -WindowStyle Hidden

Write-Host "Running :neoforge:runData (auto-exits when generation finishes)..."

$ok = $null
while ($null -eq $ok) {
    Start-Sleep -Seconds 2
    if ((Test-Path $runLog) -and (Select-String -Path $runLog -Pattern "All providers took" -SimpleMatch -Quiet)) {
        $ok = $true
    }
    elseif ((Test-Path $gradleLog) -and (Select-String -Path $gradleLog -Pattern "BUILD FAILED" -SimpleMatch -Quiet)) {
        $ok = $false
    }
    elseif ($gradle.HasExited -and -not (Test-Path $runLog)) {
        $ok = $false
    }
}

# Terminate the gradle launcher tree, plus the lingering data-gen client JVM (a
# fresh, large java process the daemon spawned that won't self-exit).
& taskkill /F /T /PID $gradle.Id 2>$null | Out-Null
Get-Process java -ErrorAction SilentlyContinue |
    Where-Object { $_.StartTime -ge $startTime -and $_.WorkingSet64 -gt 700MB } |
    ForEach-Object { Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue }

if ($ok) {
    Write-Host "Data generation complete." -ForegroundColor Green
}
else {
    Write-Host "Data generation FAILED:" -ForegroundColor Red
    Get-Content $gradleLog, "$gradleLog.err" -ErrorAction SilentlyContinue |
        Select-String -Pattern "error:|FAILURE|BUILD FAILED" | Select-Object -First 12 |
        ForEach-Object { $_.Line }
    exit 1
}
