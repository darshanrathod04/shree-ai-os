# Run full test suite and save results
$ErrorActionPreference = "Continue"
$start = Get-Date
Write-Host "Starting full test suite at $start"
C:\shree-ai-os\mvnw.cmd test -o -T 4 *> C:\shree-ai-os\full_test_results.log
$exitCode = $LASTEXITCODE
$end = Get-Date
$duration = $end - $start
Write-Host "Test suite completed at $end"
Write-Host "Duration: $($duration.TotalMinutes) minutes"
Write-Host "Exit code: $exitCode"
# Extract summary
Select-String -Path C:\shree-ai-os\full_test_results.log -Pattern "Tests run:" | Select-Object -Last 1 >> C:\shree-ai-os\test_summary.log
