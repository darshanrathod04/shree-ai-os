$ErrorActionPreference = "Continue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$pass = "4c04c2fb-bb64-4067-8b74-80aa2822121f"
$user = "user"
$base = "http://127.0.0.1:8080"

function Send-Chat {
    param($name, $message, $userId = "trust")
    
    $bodyHash = @{ message = $message; userId = $userId }
    $bodyJson = $bodyHash | ConvertTo-Json -Compress
    $bodyFile = "C:\shree-ai-os\body_$name.json"
    $outFile = "C:\shree-ai-os\resp_$name.txt"
    
    # Remove existing files
    Remove-Item $bodyFile -EA SilentlyContinue
    Remove-Item $outFile -EA SilentlyContinue
    
    # Write body to file
    Set-Content -Path $bodyFile -Value $bodyJson -Encoding utf8 -NoNewline
    
    # Remove -o flag: let curl write body+status to stdout, capture via RedirectStandardOutput
    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = "C:\Windows\System32\curl.exe"
    $psi.Arguments = "-sS -X POST `"$base/api/v1/chat`" -u `"$user`:$pass`" -H `"Content-Type: application/json`" --data-binary `"@$bodyFile`" --max-time 90 -w `"`nHTTPSTATUS:%{http_code}`nHTTPTIME:%{time_total}`n`""
    $psi.UseShellExecute = $false
    $psi.CreateNoWindow = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.StandardOutputEncoding = [System.Text.Encoding]::UTF8
    
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $proc = [System.Diagnostics.Process]::Start($psi)
    $stdout = $proc.StandardOutput.ReadToEndAsync()
    $stderr = $proc.StandardError.ReadToEndAsync()
    $proc.WaitForExit(120000)
    $sw.Stop()
    $exitCode = $proc.ExitCode
    $stdoutStr = $stdout.Result
    $stderrStr = $stderr.Result
    
    $response = ""
    $status = 0
    $latency = $sw.ElapsedMilliseconds
    $errorMsg = $null
    
    if ($exitCode -eq 0 -and $stdoutStr -and $stdoutStr.Length -gt 0) {
        # Parse HTTPSTATUS and HTTPTIME from the combined output
        if ($stdoutStr -match "HTTPSTATUS:(\d+)") {
            $status = [int]$Matches[1]
        }
        if ($stdoutStr -match "HTTPTIME:([0-9.]+)") {
            $latency = [int]([double]$Matches[1] * 1000)
        }
        # Remove the status lines from the body (strip everything after the closing brace + HTTPSTATUS)
        # The body is valid JSON - extract it
        $response = $stdoutStr.TrimEnd()
        if ($response -match "^(.+})\s*HTTPSTATUS:") {
            $response = $Matches[1].Trim()
        } elseif ($response -notmatch "\{") {
            $response = ""
        }
    } else {
        $errorMsg = "curl exit $exitCode"
        if ($stderrStr -and $stderrStr.Length -gt 0) {
            $errorMsg += ": " + $stderrStr.Substring(0, [Math]::Min(150, $stderrStr.Length))
        }
    }
    
    # Cleanup
    Remove-Item $bodyFile -EA SilentlyContinue
    Remove-Item $outFile -EA SilentlyContinue
    
    return [PSCustomObject]@{
        Name = $name
        Message = $message
        Status = $status
        Latency = $latency
        ResponseLength = $response.Length
        ResponsePreview = if ($response.Length -gt 0) { $response.Substring(0, [Math]::Min(400, $response.Length)) } else { "" }
        Error = $errorMsg
    }
}

Write-Host "=============================================="
Write-Host "  Phase 2.1 Batch 1 Trust Validation Report"
Write-Host "  Shree AI OS - $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
Write-Host "=============================================="
Write-Host ""

$scenarios = @(
    @{ Name = "S01"; Message = "Explain WorkspaceController in the codebase" },
    @{ Name = "S02"; Message = "What is ProjectSDK and where is it defined?" },
    @{ Name = "S03"; Message = "List all REST endpoints in the codebase" },
    @{ Name = "S04"; Message = "Which classes depend on DefaultRuntimeService?" },
    @{ Name = "S05"; Message = "Explain DeveloperWorkflowService and its responsibilities" },
    @{ Name = "S06"; Message = "Show the project structure of the shree-ai-os module" },
    @{ Name = "S07"; Message = "Find any circular dependencies in the runtime package" },
    @{ Name = "S08"; Message = "What is the impact of changing ProjectSDK class?" },
    @{ Name = "S09"; Message = "Explain BootManager and how it initializes the system" },
    @{ Name = "S10"; Message = "Compare WorkspaceController and ReviewController" }
)

$results = [System.Collections.Generic.List[PSCustomObject]]::new()
$passCount = 0
$failCount = 0
$totalTimer = [System.Diagnostics.Stopwatch]::StartNew()

foreach ($s in $scenarios) {
    Write-Host "[$($s.Name)] $($s.Message)"
    $r = Send-Chat -name $s.Name -message $s.Message -userId "trust"
    $results.Add($r)
    
    $isPass = $r.Status -ge 200 -and $r.Status -lt 300 -and $r.ResponseLength -gt 0 -and $r.Error -eq $null
    if ($isPass) { $passCount++ } else { $failCount++ }
    
    $sym = if ($isPass) { "PASS" } else { "FAIL" }
    Write-Host "  $sym | Status=$($r.Status) | Latency=$($r.Latency)ms | Len=$($r.ResponseLength)"
    if ($r.Error) {
        Write-Host "  ERROR: $($r.Error)"
    } else {
        $preview = $r.ResponsePreview -replace "`r", "" -replace "`n", " | "
        Write-Host "  Body: $preview"
    }
    Write-Host ""
}

$totalTimer.Stop()
$trustScore = [Math]::Round(($passCount / $results.Count) * 100, 1)

Write-Host "=============================================="
Write-Host "  TRUST SCORE: $trustScore% ($passCount of $($results.Count) scenarios)"
Write-Host "  Total validation time: $($totalTimer.ElapsedMilliseconds)ms"
Write-Host "=============================================="

# Build structured results array
$scenarioResults = @()
foreach ($r in $results) {
    $scenarioResults += [PSCustomObject]@{
        Name = $r.Name
        Message = $r.Message
        Status = $r.Status
        LatencyMs = $r.Latency
        ResponseLength = $r.ResponseLength
        ResponsePreview = $r.ResponsePreview
        Error = $r.Error
        Passed = ($r.Status -ge 200 -and $r.Status -lt 300 -and $r.ResponseLength -gt 0 -and $r.Error -eq $null)
    }
}

$report = [PSCustomObject]@{
    Timestamp = (Get-Date -Format 'yyyy-MM-ddTHH:mm:ss')
    TrustScore = $trustScore
    Passed = $passCount
    Failed = $failCount
    Total = $results.Count
    TotalTimeMs = $totalTimer.ElapsedMilliseconds
    ServerEndpoint = "$base/api/v1/chat"
    Authentication = "user:4c04c2fb-bb64-4067-8b74-80aa2822121f"
    Scenarios = $scenarioResults
}

$report | ConvertTo-Json -Depth 8 | Out-File "C:\shree-ai-os\trust-validation-report.json" -Encoding utf8
Write-Host "Full report: C:\shree-ai-os\trust-validation-report.json"
