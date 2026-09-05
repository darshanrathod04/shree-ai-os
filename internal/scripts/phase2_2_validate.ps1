#Requires -Version 5.1
$ErrorActionPreference = "Continue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# ============================================================
# Phase 2.2 Batch 2 -- Developer Reasoning Validation
# Shree AI OS -- Evidence Mode
#
# Mission: Validate that every chat response carries:
#   1. Evidence (structured facts from kernel outputs)
#   2. Confidence (derived from evidence quality)
#   3. VerificationTier (VERIFIED_PROJECT / VERIFIED_KB / INFERRED / INSUFFICIENT)
#   4. Non-template answer text
# ============================================================

$base        = "http://127.0.0.1:8080"
$auth        = "user:4c04c2fb-bb64-4067-8b74-80aa2822121f"
$reportFile  = "C:\shree-ai-os\phase2_2_validation_report.json"
$detailsDir  = "C:\shree-ai-os\phase2_2_details"

if (-not (Test-Path $detailsDir)) {
    New-Item -ItemType Directory -Path $detailsDir -Force | Out-Null
}

# ============================================================
# Helper: send a single chat request
# ============================================================
function Send-Chat {
    param(
        [Parameter(Mandatory)]
        [string] $Name,

        [Parameter(Mandatory)]
        [string] $Message,

        [string] $UserId = "phase22-dev"
    )

    $bodyHash  = @{ message = $Message; userId = $UserId }
    $bodyJson  = $bodyHash | ConvertTo-Json -Compress
    $bodyFile  = "C:\shree-ai-os\p22_body_$Name.json"

    Set-Content -Path $bodyFile -Value $bodyJson -Encoding utf8 -NoNewline

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName        = "C:\Windows\System32\curl.exe"
    $psi.Arguments       = (
        "-sS " +
        "-X POST `"$base/api/v1/chat`" " +
        "-u `"$auth`" " +
        "-H `"Content-Type: application/json`" " +
        "--data-binary `"@$bodyFile`" " +
        "--max-time 90 " +
        "-w `"`nHTTPSTATUS:%{http_code}`nHTTPTIME:%{time_total}`n`""
    )
    $psi.UseShellExecute       = $false
    $psi.CreateNoWindow        = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError  = $true
    $psi.StandardOutputEncoding = [System.Text.Encoding]::UTF8

    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $proc   = [System.Diagnostics.Process]::Start($psi)
    $stdout = $proc.StandardOutput.ReadToEndAsync()
    $stderr = $proc.StandardError.ReadToEndAsync()
    $proc.WaitForExit(120000)
    $sw.Stop()
    $exitCode  = $proc.ExitCode
    $stdoutStr = $stdout.Result
    $stderrStr = $proc.StandardError.ReadToEnd()

    Remove-Item $bodyFile -EA SilentlyContinue

    $response = ""
    $status   = 0
    $latency  = $sw.ElapsedMilliseconds
    $errorMsg = $null

    if ($exitCode -eq 0 -and $stdoutStr) {
        if ($stdoutStr -match "HTTPSTATUS:(\d+)") { $status = [int]$Matches[1] }
        $clean = $stdoutStr.TrimEnd()
        if ($clean -match "^(.+})\s*HTTPSTATUS:") {
            $response = $Matches[1].Trim()
        } elseif ($clean -notmatch "\{") {
            $response = ""
        } else {
            $response = $clean
        }
    } else {
        $errorMsg = "curl exit $exitCode"
        if ($stderrStr) {
            $errorMsg += ": $($stderrStr.Substring(0, [Math]::Min(200, $stderrStr.Length)))"
        }
    }

    return [PSCustomObject]@{
        Name     = $Name
        Message  = $Message
        UserId   = $UserId
        Status   = $status
        Latency  = $latency
        Body     = $response
        BodyLen  = $response.Length
        Error    = $errorMsg
    }
}

# ============================================================
# Helper: extract a top-level double-quoted string field
# Uses proper char-by-char parsing to handle embedded quotes
# ============================================================
function Extract-Field {
    param([string] $Json, [string] $FieldName)

    $idx = $Json.IndexOf("`"$FieldName`":")
    if ($idx -lt 0) { return $null }
    $colon = $idx + $FieldName.Length + 3  # skip "fieldName":
    # Skip whitespace
    while ($colon -lt $Json.Length -and $Json[$colon] -eq ' ') { $colon++ }
    if ($colon -ge $Json.Length -or $Json[$colon] -ne '"') { return $null }
    $colon++  # skip opening "
    $i = $colon
    $escaped = $false
    while ($i -lt $Json.Length) {
        $ch = $Json[$i]
        if ($escaped) {
            $escaped = $false
            $i++
        } elseif ($ch -eq '\') {
            $escaped = $true
            $i++
        } elseif ($ch -eq '"') {
            return $Json.Substring($colon, $i - $colon) -replace '\\"', '"' -replace '\\n', "`n"
        } else {
            $i++
        }
    }
    return $null
}

# ============================================================
# Helper: extract a top-level number field
# ============================================================
function Extract-Number {
    param([string] $Json, [string] $FieldName)

    $pattern = "`"$FieldName`"\s*:\s*([0-9.]+)"
    if ($Json -match $pattern) {
        return [double]$Matches[1]
    }
    return $null
}

# ============================================================
# Helper: extract the evidence array from structuredPayload
# Uses proper nested bracket counting with string-skip logic
# ============================================================
function Extract-EvidenceArray {
    param([string] $Json)

    $start = $Json.IndexOf('"evidence":')
    if ($start -lt 0) { return @() }

    $bracket = $Json.IndexOf('[', $start)
    if ($bracket -lt 0) { return @() }

    $depth = 0
    for ($i = $bracket; $i -lt $Json.Length; $i++) {
        $ch = $Json[$i]
        if ($ch -eq '"') {
            # Skip string content
            $i++
            while ($i -lt $Json.Length) {
                if ($Json[$i] -eq '\') { $i++; $i++; continue }
                if ($Json[$i] -eq '"') { break }
                $i++
            }
        } elseif ($ch -eq '[') {
            $depth++
        } elseif ($ch -eq ']') {
            $depth--
            if ($depth -eq 0) {
                $arrJson = $Json.Substring($bracket, $i - $bracket + 1)
                return Parse-EvidenceArray $arrJson
            }
        }
    }
    return @()
}

function Parse-EvidenceArray {
    param([string] $ArrJson)

    $items = @()
    if ($ArrJson -eq "[]") { return $items }

    $i = 0
    while ($i -lt $ArrJson.Length) {
        # Skip whitespace and commas
        while ($i -lt $ArrJson.Length -and ($ArrJson[$i] -eq ' ' -or $ArrJson[$i] -eq "`t" -or $ArrJson[$i] -eq "`n" -or $ArrJson[$i] -eq "`r" -or $ArrJson[$i] -eq ',')) { $i++ }
        if ($i -ge $ArrJson.Length) { break }
        if ($ArrJson[$i] -ne '{') { $i++; continue }

        # Find the matching } with string-skip logic
        $depth = 0
        $objStart = $i
        for ($j = $i; $j -lt $ArrJson.Length; $j++) {
            $ch = $ArrJson[$j]
            if ($ch -eq '"') {
                # Skip string content
                $j++
                while ($j -lt $ArrJson.Length) {
                    if ($ArrJson[$j] -eq '\') { $j++; $j++; continue }
                    if ($ArrJson[$j] -eq '"') { break }
                    $j++
                }
            } elseif ($ch -eq '{') {
                $depth++
            } elseif ($ch -eq '}') {
                $depth--
                if ($depth -eq 0) {
                    $objJson = $ArrJson.Substring($objStart, $j - $objStart + 1)
                    $item = Parse-SimpleJson $objJson
                    if ($null -ne $item) { $items += $item }
                    $i = $j + 1
                    break
                }
            }
        }
        if ($depth -ne 0) { break }
    }
    return $items
}

# ============================================================
# Helper: parse a JSON object into a hashtable
# Fully handles nested objects/arrays and escaped strings
# ============================================================
function Parse-SimpleJson {
    param([string] $ObjJson)

    if ($ObjJson.Length -lt 2) { return $null }
    $result = @{}
    $i = 1  # skip opening {

    while ($i -lt $ObjJson.Length) {
        # Skip whitespace
        while ($i -lt $ObjJson.Length -and [char]::IsWhiteSpace($ObjJson[$i])) { $i++ }
        if ($i -ge $ObjJson.Length) { break }
        if ($ObjJson[$i] -eq '}') { break }

        # Must have a key
        if ($ObjJson[$i] -ne '"') { $i++; continue }
        $i++  # skip opening "
        $keyEnd = $i
        while ($keyEnd -lt $ObjJson.Length -and $ObjJson[$keyEnd] -ne '"') {
            if ($ObjJson[$keyEnd] -eq '\') { $keyEnd++ }
            $keyEnd++
        }
        $key = $ObjJson.Substring($i, $keyEnd - $i) -replace '\\"', '"'
        $i = $keyEnd + 1

        # Skip to colon
        while ($i -lt $ObjJson.Length -and $ObjJson[$i] -ne ':') { $i++ }
        $i++

        # Skip whitespace
        while ($i -lt $ObjJson.Length -and [char]::IsWhiteSpace($ObjJson[$i])) { $i++ }
        if ($i -ge $ObjJson.Length) { break }

        $ch = $ObjJson[$i]

        if ($ch -eq '"') {
            # String value
            $i++
            $valStart = $i
            $escaped = $false
            while ($i -lt $ObjJson.Length) {
                if ($escaped) { $escaped = $false; $i++; continue }
                if ($ObjJson[$i] -eq '\') { $escaped = $true; $i++; continue }
                if ($ObjJson[$i] -eq '"') { break }
                $i++
            }
            $val = $ObjJson.Substring($valStart, $i - $valStart) -replace '\\"', '"'
            $result[$key] = $val
            $i++  # skip closing "
        }
        elseif ($ch -eq '[') {
            # Array - parse it properly
            $depth = 1
            $arrStart = $i
            $i++
            while ($i -lt $ObjJson.Length -and $depth -gt 0) {
                if ($ObjJson[$i] -eq '"') {
                    $i++
                    while ($i -lt $ObjJson.Length) {
                        if ($ObjJson[$i] -eq '\') { $i++; $i++; continue }
                        if ($ObjJson[$i] -eq '"') { break }
                        $i++
                    }
                } elseif ($ObjJson[$i] -eq '[') { $depth++; $i++ }
                elseif ($ObjJson[$i] -eq ']') { $depth--; if ($depth -eq 0) { $i++; break } else { $i++ } }
                else { $i++ }
            }
            $arrJson = $ObjJson.Substring($arrStart, $i - $arrStart)
            $result[$key] = Parse-EvidenceArray $arrJson
        }
        elseif ($ch -eq '{') {
            # Object - parse it properly
            $depth = 1
            $objStart = $i
            $i++
            while ($i -lt $ObjJson.Length -and $depth -gt 0) {
                if ($ObjJson[$i] -eq '"') {
                    $i++
                    while ($i -lt $ObjJson.Length) {
                        if ($ObjJson[$i] -eq '\') { $i++; $i++; continue }
                        if ($ObjJson[$i] -eq '"') { break }
                        $i++
                    }
                } elseif ($ObjJson[$i] -eq '{') { $depth++; $i++ }
                elseif ($ObjJson[$i] -eq '}') { $depth--; if ($depth -eq 0) { $i++; break } else { $i++ } }
                else { $i++ }
            }
            $objJson = $ObjJson.Substring($objStart, $i - $objStart)
            $result[$key] = Parse-SimpleJson $objJson
        }
        elseif ($ch -eq 't' -or $ch -eq 'f') {
            # true / false
            $valEnd = $i
            while ($valEnd -lt $ObjJson.Length -and $ObjJson[$valEnd] -ne ',' -and $ObjJson[$valEnd] -ne '}') { $valEnd++ }
            $val = $ObjJson.Substring($i, $valEnd - $i)
            $result[$key] = ($val.Trim() -eq 'true')
            $i = $valEnd
        }
        else {
            # Number
            $valEnd = $i
            while ($valEnd -lt $ObjJson.Length -and $ObjJson[$valEnd] -match '[0-9.eE+-]') { $valEnd++ }
            $val = $ObjJson.Substring($i, $valEnd - $i)
            if ($val -match '^[0-9]+(\.[0-9]+)?$') { $result[$key] = [double]$val }
            $i = $valEnd
        }

        # Skip to comma or }
        while ($i -lt $ObjJson.Length -and $ObjJson[$i] -ne ',' -and $ObjJson[$i] -ne '}') { $i++ }
        if ($i -lt $ObjJson.Length -and $ObjJson[$i] -eq ',') { $i++ }
    }
    return $result
}

# ============================================================
# Helper: validate a single scenario response
# ============================================================
function Test-Scenario {
    param(
        [Parameter(Mandatory)]
        [string] $ScenarioId,

        [Parameter(Mandatory)]
        [string] $ScenarioMessage,

        [Parameter(Mandatory)]
        [object] $Response
    )

    $checks = [System.Collections.Generic.List[PSCustomObject]]::new()
    $allPass = $true

    # C1: HTTP 200
    $c1 = $Response.Status -ge 200 -and $Response.Status -lt 300
    $checks.Add([PSCustomObject]@{ Check = "C1-HTTP-200"; Pass = $c1; Detail = "Status=$($Response.Status)" })
    if (-not $c1) { $allPass = $false }

    # C2: Non-empty body
    $c2 = $Response.BodyLen -gt 10
    $checks.Add([PSCustomObject]@{ Check = "C2-Body-NonEmpty"; Pass = $c2; Detail = "Len=$($Response.BodyLen)" })
    if (-not $c2) { $allPass = $false }

    if (-not $c1 -or -not $c2) {
        return [PSCustomObject]@{
            ScenarioId       = $ScenarioId
            Message          = $ScenarioMessage
            Status          = $Response.Status
            LatencyMs        = $Response.Latency
            Pass             = $false
            Checks           = $checks
            Answer           = ""
            Evidence         = @()
            Confidence       = $null
            VerificationTier = ""
            Error            = $Response.Error
            RuntimeClass     = "NetworkError - body=$($Response.BodyLen) status=$($Response.Status)"
        }
    }

    $body = $Response.Body

    # C3: structuredPayload present
    $spStart = $body.IndexOf('"structuredPayload":')
    $c3 = $spStart -gt 0
    $checks.Add([PSCustomObject]@{ Check = "C3-StructuredPayload"; Pass = $c3; Detail = $(if ($c3) { "Present" } else { "MISSING" }) })
    if (-not $c3) { $allPass = $false }

    # C4: evidence array present in structuredPayload
    $evidence = Extract-EvidenceArray $body
    # Normalize - if parser returned single hashtable with no sourceType, treat as failed parse
    $evidenceCount = 0
    if ($evidence -is [array] -or $evidence -is [System.Collections.ArrayList]) {
        $evidenceCount = $evidence.Count
    } elseif ($evidence -is [hashtable]) {
        $evidenceCount = 1
    }
    $c4 = ($evidenceCount -gt 0)
    $detail4 = if ($c4) { "$evidenceCount item(s)" } else { "MISSING or empty" }
    $checks.Add([PSCustomObject]@{ Check = "C4-Evidence-Present"; Pass = $c4; Detail = $detail4 })
    if (-not $c4) { $allPass = $false }

    # C5: evidence has sourceType
    $c5 = $false
    $srcTypeDetail = "MISSING"
    # $evidence is returned by Extract-EvidenceArray which returns an array of hashtables
    # But if the parser merged everything into one hashtable, check it directly
    if ($evidence -is [System.Collections.ArrayList] -or $evidence -is [object[]]) {
        $first = $null
        if ($evidence.Count -gt 0) { $first = $evidence[0] }
        if ($null -ne $first -and $first -is [hashtable] -and $first.ContainsKey("sourceType")) {
            $c5 = $true
            $srcTypeDetail = "$($first.sourceType)"
        }
    } elseif ($evidence -is [hashtable]) {
        # Parser may have merged into single hashtable
        if ($evidence.ContainsKey("sourceType")) {
            $c5 = $true
            $srcTypeDetail = "$($evidence.sourceType)"
        }
    }
    $checks.Add([PSCustomObject]@{ Check = "C5-Evidence-SourceType"; Pass = $c5; Detail = $srcTypeDetail })
    if (-not $c5) { $allPass = $false }

    # C6: confidence field present (top-level or in structuredPayload)
    $confidence = Extract-Number $body "confidence"
    $c6 = ($null -ne $confidence -and $confidence -ge 0 -and $confidence -le 1)
    if (-not $c6) {
        # Try structuredPayload.confidence
        if ($spStart -gt 0) {
            $spEnd = $body.IndexOf('}', $spStart)
            $substr = $body.Substring($spStart, [Math]::Min(5000, $body.Length - $spStart))
            $conf2 = Extract-Number $substr "verificationConfidence"
            if ($null -ne $conf2 -and $conf2 -ge 0 -and $conf2 -le 1) {
                $confidence = $conf2
                $c6 = $true
            }
        }
    }
    $detail6 = if ($c6) { [string]::Format("{0:P0}", $confidence) } else { "MISSING" }
    $checks.Add([PSCustomObject]@{ Check = "C6-Confidence-Valid"; Pass = $c6; Detail = $detail6 })
    if (-not $c6) { $allPass = $false }

    # C7: verificationTier present and valid
    $tier = $null
    # Search for verificationTier near structuredPayload
    $searchStart = if ($spStart -gt 0) { $spStart } else { 0 }
    $searchEnd = [Math]::Min($body.Length, $searchStart + 5000)
    $substr = $body.Substring($searchStart, $searchEnd - $searchStart)
    $tier = Extract-Field $substr "verificationTier"
    $c7 = $false
    $tierDetail = "MISSING"
    if ($tier -match "^(VERIFIED_PROJECT|VERIFIED_KB|INFERRED|INSUFFICIENT)$") {
        $c7 = $true
        $tierDetail = $tier
    }
    $checks.Add([PSCustomObject]@{ Check = "C7-VerificationTier"; Pass = $c7; Detail = $tierDetail })
    if (-not $c7) { $allPass = $false }

    # C8: answer field present
    $answer = Extract-Field $body "answer"
    $c8 = ($null -ne $answer -and $answer.Length -gt 5)
    $checks.Add([PSCustomObject]@{ Check = "C8-Answer-Present"; Pass = $c8; Detail = $(if ($c8) { "$($answer.Length) chars" } else { "MISSING" }) })
    if (-not $c8) { $allPass = $false }

    # C9: answer is NOT template text
    $c9 = $true
    $templatePhrases = @(
        "No structured evidence available",
        "No evidence available",
        "I don.t have enough verified information",
        "Based on my general knowledge"
    )
    foreach ($phrase in $templatePhrases) {
        if ($answer -and $answer.Contains($phrase)) {
            $c9 = $false
            $checks.Add([PSCustomObject]@{ Check = "C9-Answer-NotTemplate"; Pass = $false; Detail = "Template phrase found: $phrase" })
            $allPass = $false
            break
        }
    }
    if ($c9) {
        $checks.Add([PSCustomObject]@{ Check = "C9-Answer-NotTemplate"; Pass = $true; Detail = "Evidence-grounded answer" })
    }

    # C10: evidence item has non-empty content
    $c10 = $false
    $contentDetail = "MISSING"
    if ($evidence -and $evidence.Count -gt 0) {
        $first = $null
        if ($evidence -is [hashtable]) { $first = $evidence } else { $first = $evidence[0] }
        if ($first -and $first.ContainsKey("content")) {
            $content = $first.content
            if ($content -and $content.Length -gt 5) {
                $c10 = $true
                $contentDetail = $content.Substring(0, [Math]::Min(80, $content.Length)) -replace "`n", " "
            }
        }
    }
    $checks.Add([PSCustomObject]@{ Check = "C10-Evidence-Content"; Pass = $c10; Detail = $contentDetail })
    if (-not $c10) { $allPass = $false }

    # Runtime class identification
    $runtimeClass = ""
    if (-not $allPass) {
        foreach ($check in $checks) {
            if (-not $check.Pass) {
                $ck = $check.Check
                if ($ck -match "^C4|^C5|^C10") {
                    $runtimeClass = "EvidenceAgent.extractFromMetadata() - pipeline state metadata missing expected evidence keys"
                } elseif ($ck -match "^C6|^C7") {
                    $runtimeClass = "NaturalResponseAgent.generate() - verification report not in structuredPayload"
                } elseif ($ck -eq "C9") {
                    $runtimeClass = "NaturalResponseAgent.generateFromEvidence() - returns template text instead of evidence-grounded answer"
                } else {
                    $runtimeClass = "Unknown - $ck`: $($check.Detail)"
                }
                break
            }
        }
    } else {
        $runtimeClass = "N/A (all checks passed)"
    }

    return [PSCustomObject]@{
        ScenarioId       = $ScenarioId
        Message          = $ScenarioMessage
        Status          = $Response.Status
        LatencyMs        = $Response.Latency
        Pass             = $allPass
        Checks           = $checks
        Answer           = if ($answer) { $answer } else { "" }
        Evidence         = $evidence
        Confidence       = $confidence
        VerificationTier = if ($tier) { $tier } else { "" }
        Error            = $Response.Error
        RuntimeClass     = $runtimeClass
    }
}

# ============================================================
# D01-D10 Developer Reasoning Scenarios
# ============================================================
$scenarios = @(
    @{ Name = "D01"; Message = "Explain WorkspaceController in the codebase" },
    @{ Name = "D02"; Message = "What is ProjectSDK and where is it defined?" },
    @{ Name = "D03"; Message = "List all REST controllers in the codebase" },
    @{ Name = "D04"; Message = "Which classes depend on DefaultRuntimeService?" },
    @{ Name = "D05"; Message = "Show the project structure of the shree-ai-os module" },
    @{ Name = "D06"; Message = "What would happen if we removed the Knowledge kernel?" },
    @{ Name = "D07"; Message = "Identify the most complex class in the platform" },
    @{ Name = "D08"; Message = "What is the impact of changing ShreeClient?" },
    @{ Name = "D09"; Message = "Explain the autonomous intelligence layer and its agents" },
    @{ Name = "D10"; Message = "Compare AiChatController and SdkDiagnosticsController" }
)

# ============================================================
# Header
# ============================================================
Write-Host ""
Write-Host ("=" * 80)
Write-Host "  Phase 2.2 Batch 2 - Developer Reasoning Validation"
Write-Host "  Shree AI OS - Evidence Mode"
Write-Host "  $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Host ("=" * 80)
Write-Host ""
Write-Host "Scenarios : $($scenarios.Count)"
Write-Host "Endpoint  : $base/api/v1/chat"
Write-Host ""

# ============================================================
# Execute scenarios
# ============================================================
$results     = [System.Collections.Generic.List[PSCustomObject]]::new()
$passCount  = 0
$failCount  = 0
$totalTimer = [System.Diagnostics.Stopwatch]::StartNew()

foreach ($s in $scenarios) {
    $sn  = $s.Name
    $msg = $s.Message

    Write-Host "[$sn] $msg" -ForegroundColor Cyan

    $httpResp = Send-Chat -Name $sn -Message $msg -UserId "phase22-$sn"
    $result   = Test-Scenario -ScenarioId $sn -ScenarioMessage $msg -Response $httpResp
    $results.Add($result)

    if ($result.Pass) { $passCount++ } else { $failCount++ }

    $sym   = if ($result.Pass) { "PASS" } else { "FAIL" }
    $color = if ($result.Pass) { "Green" } else { "Red" }
    $conf  = if ($null -ne $result.Confidence) { [Math]::Round($result.Confidence * 100) } else { 0 }
    Write-Host "  $sym | Status=$($result.Status) | Latency=$($result.LatencyMs)ms | Confidence=$conf% | Tier=$($result.VerificationTier)" -ForegroundColor $color

    foreach ($check in $result.Checks) {
        $ck = if ($check.Pass) { "  ok " } else { "  X  " }
        $cc = if ($check.Pass) { "Gray" } else { "Yellow" }
        Write-Host "  $ck $($check.Check): $($check.Detail)" -ForegroundColor $cc
    }

    if ($result.Error) {
        Write-Host "  ERROR: $($result.Error)" -ForegroundColor Red
    }

    if (-not $result.Pass) {
        Write-Host "  RuntimeClass: $($result.RuntimeClass)" -ForegroundColor DarkYellow
    }

    Write-Host ""

    $detailFile = Join-Path $detailsDir "$sn.json"
    $result | ConvertTo-Json -Depth 10 | Out-File $detailFile -Encoding utf8
}

$totalTimer.Stop()

# ============================================================
# Summary
# ============================================================
$trustScore = if ($results.Count -gt 0) {
    [Math]::Round(($passCount / $results.Count) * 100, 1)
} else { 0 }

Write-Host ("=" * 80)
Write-Host "  TRUST SCORE: $trustScore% ($passCount of $($results.Count) scenarios)"
Write-Host "  Total validation time: $($totalTimer.ElapsedMilliseconds)ms"
Write-Host ("=" * 80)
Write-Host ""

# Per-check breakdown
$checkSummary = @{}
foreach ($r in $results) {
    foreach ($c in $r.Checks) {
        $key = $c.Check
        if (-not $checkSummary.ContainsKey($key)) {
            $checkSummary[$key] = @{ Pass = 0; Fail = 0 }
        }
        if ($c.Pass) { $checkSummary[$key].Pass++ } else { $checkSummary[$key].Fail++ }
    }
}

Write-Host "Per-Check Breakdown:" -ForegroundColor White
foreach ($key in ($checkSummary.Keys | Sort-Object)) {
    $s = $checkSummary[$key]
    $total = $s.Pass + $s.Fail
    $pct   = [Math]::Round(($s.Pass / $total) * 100, 0)
    if ($pct -eq 100) { $c = "Green" } else { $c = "Yellow" }
    Write-Host "  $key : $($s.Pass)/$total ($pct%)" -ForegroundColor $c
}
Write-Host ""

# ============================================================
# Build report
# ============================================================
$scenarioResults = @()
foreach ($r in $results) {
    $ansPreview = if ($r.Answer) { $r.Answer.Substring(0, [Math]::Min(200, $r.Answer.Length)) } else { "" }
    $scenarioResults += [PSCustomObject]@{
        ScenarioId       = $r.ScenarioId
        Message          = $r.Message
        Status           = $r.Status
        LatencyMs        = $r.LatencyMs
        Pass             = $r.Pass
        Confidence       = $r.Confidence
        VerificationTier = $r.VerificationTier
        EvidenceCount    = $(if ($r.Evidence) { $r.Evidence.Count } else { 0 })
        Checks           = $r.Checks
        RuntimeClass     = $r.RuntimeClass
        Error            = $r.Error
        AnswerPreview    = $ansPreview
    }
}

$report = [PSCustomObject]@{
    Timestamp          = (Get-Date -Format 'yyyy-MM-ddTHH:mm:ss')
    TrustScore         = $trustScore
    Passed             = $passCount
    Failed             = $failCount
    Total              = $results.Count
    TotalTimeMs        = $totalTimer.ElapsedMilliseconds
    ServerEndpoint     = "$base/api/v1/chat"
    Phase              = "Phase 2.2 Batch 2"
    ValidationMode     = "Evidence Mode"
    Scenarios          = $scenarioResults
    PerCheckSummary    = $checkSummary
}

$report | ConvertTo-Json -Depth 12 | Out-File $reportFile -Encoding utf8

Write-Host "Report saved: $reportFile" -ForegroundColor Gray
Write-Host "Detail files: $detailsDir" -ForegroundColor Gray
Write-Host ""

if ($failCount -gt 0) {
    Write-Host "VALIDATION FAILED - $failCount scenario(s) did not pass." -ForegroundColor Red
    exit 1
} else {
    Write-Host "ALL SCENARIOS PASSED - $trustScore% Trust Score." -ForegroundColor Green
    exit 0
}
