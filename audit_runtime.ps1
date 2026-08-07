# Runtime Domain Audit Script
$runtimePath = "src/main/java/com/shreeai/os/platform/runtime"
$files = Get-ChildItem -Path $runtimePath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }

Write-Host "=== PLATFORM/RUNTIME DOMAIN AUDIT ===" -ForegroundColor Cyan
Write-Host ""

# 1. Package Hierarchy
Write-Host "1. PACKAGE HIERARCHY"
Write-Host "====================="
$packages = $files | ForEach-Object { 
    $dir = Split-Path -Path $_.Directory.FullName
    $dir = $dir -replace [regex]::Escape("$runtimePath\"), ''
    if ($dir -eq $runtimePath) { "root" } else { $dir }
} | Sort-Object -Unique

$subPackages = @{}
foreach ($pkg in $packages) {
    if ($subPackages.ContainsKey($pkg)) {
        $subPackages[$pkg]++
    } else {
        $subPackages[$pkg] = 1
    }
}

foreach ($pkg in ($subPackages.GetEnumerator() | Sort-Object Name)) {
    Write-Host "  platform/runtime/$($pkg.Name) ($($pkg.Value) files)"
}
Write-Host ""

# 2. Responsibilities
Write-Host "2. RESPONSIBILITIES"
Write-Host "==================="
$responsibilities = @{
    "root" = "Core runtime abstractions and service definitions"
    "api" = "Public API interfaces for runtime operations"
    "config" = "Runtime configuration and settings"
    "contracts" = "Runtime contracts and agreements"
    "exceptions" = "Runtime-specific exception hierarchy"
    "execution" = "Execution context and pipeline management"
    "internal" = "Internal runtime implementations"
    "lifecycle" = "Runtime lifecycle management"
    "pipeline" = "Execution pipeline and stage management"
}

foreach ($pkg in ($responsibilities.GetEnumerator() | Sort-Object Name)) {
    Write-Host "  $($pkg.Name): $($pkg.Value)"
}
Write-Host ""

# 3. Public APIs
Write-Host "3. PUBLIC APIS"
Write-Host "=============="
$interfaces = $files | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'interface\s+\w+'
}

$publicClasses = $files | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match '^\s*public\s+class\s+\w+'
}

Write-Host "`nInterfaces:"
$interfaceByPackage = @{}
foreach ($iface in $interfaces) {
    $pkg = Split-Path -Path $iface.Directory.FullName
    $pkg = $pkg -replace [regex]::Escape("$runtimePath\"), ''
    if ($pkg -eq $runtimePath) { $pkg = "root" }
    else { $pkg = Split-Path -Path $pkg -Leaf }
    $name = [System.IO.Path]::GetFileNameWithoutExtension($iface.Name)
    if (-not $interfaceByPackage.ContainsKey($pkg)) {
        $interfaceByPackage[$pkg] = @()
    }
    $interfaceByPackage[$pkg] += $name
}

foreach ($pkg in ($interfaceByPackage.GetEnumerator() | Sort-Object Name)) {
    Write-Host "  [$($pkg.Name)]"
    foreach ($iface in $pkg.Value) {
        Write-Host "    - $iface"
    }
}

Write-Host "`nPublic Classes:"
$publicByPackage = @{}
foreach ($cls in $publicClasses) {
    $pkg = Split-Path -Path $cls.Directory.FullName
    $pkg = $pkg -replace [regex]::Escape("$runtimePath\"), ''
    if ($pkg -eq $runtimePath) { $pkg = "root" }
    else { $pkg = Split-Path -Path $pkg -Leaf }
    $name = [System.IO.Path]::GetFileNameWithoutExtension($cls.Name)
    if (-not $publicByPackage.ContainsKey($pkg)) {
        $publicByPackage[$pkg] = @()
    }
    $publicByPackage[$pkg] += $name
}

foreach ($pkg in ($publicByPackage.GetEnumerator() | Sort-Object Name)) {
    Write-Host "  [$($pkg.Name)]"
    foreach ($cls in $pkg.Value) {
        Write-Host "    - $cls"
    }
}
Write-Host ""

# 4. Implementations
Write-Host "4. IMPLEMENTATIONS"
Write-Host "==================="
$implementations = $files | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'class\s+\w+.*implements'
}

$implByPackage = @{}
foreach ($impl in $implementations) {
    $pkg = Split-Path -Path $impl.Directory.FullName
    $pkg = $pkg -replace [regex]::Escape("$runtimePath\"), ''
    if ($pkg -eq $runtimePath) { $pkg = "root" }
    else { $pkg = Split-Path -Path $pkg -Leaf }
    $name = [System.IO.Path]::GetFileNameWithoutExtension($impl.Name)
    if (-not $implByPackage.ContainsKey($pkg)) {
        $implByPackage[$pkg] = @()
    }
    $implByPackage[$pkg] += $name
}

foreach ($pkg in ($implByPackage.GetEnumerator() | Sort-Object Name)) {
    Write-Host "`n  [$($pkg.Name)]"
    foreach ($impl in $pkg.Value) {
        Write-Host "    - $impl"
    }
}
Write-Host ""

# 5. Domain Models
Write-Host "5. DOMAIN MODELS"
Write-Host "================="
$models = $files | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'class\s+\w+'
}

$modelByPackage = @{}
foreach ($model in $models) {
    $pkg = Split-Path -Path $model.Directory.FullName
    $pkg = $pkg -replace [regex]::Escape("$runtimePath\"), ''
    if ($pkg -eq $runtimePath) { $pkg = "root" }
    else { $pkg = Split-Path -Path $pkg -Leaf }
    $name = [System.IO.Path]::GetFileNameWithoutExtension($model.Name)
    if (-not $modelByPackage.ContainsKey($pkg)) {
        $modelByPackage[$pkg] = @()
    }
    $modelByPackage[$pkg] += $name
}

foreach ($pkg in ($modelByPackage.GetEnumerator() | Sort-Object Name)) {
    Write-Host "`n  [$($pkg.Name)]"
    foreach ($model in $pkg.Value) {
        Write-Host "    - $model"
    }
}
Write-Host ""

# 6. Runtime Flow
Write-Host "6. RUNTIME FLOW"
Write-Host "================"
Write-Host "  Request"
Write-Host "     ↓"
Write-Host "  Validation (ExecutionRequest validation)"
Write-Host "     ↓"
Write-Host "  Resolution (Pipeline/Stage resolution)"
Write-Host "     ↓"
Write-Host "  Execution (Pipeline execution with context)"
Write-Host "     ↓"
Write-Host "  Response (ExecutionResult)"
Write-Host ""

# 7. Internal Dependencies
Write-Host "7. INTERNAL DEPENDENCIES"
Write-Host "========================="
$internalDeps = @{}
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match 'import\s+com\.shreeai\.os\.platform\.runtime\.([^;]+);') {
        $matches = [regex]::Matches($content, 'import\s+com\.shreeai\.os\.platform\.runtime\.([^;]+);')
        foreach ($match in $matches) {
            $dep = $match.Groups[1].Value
            $dep = $dep -replace '\.\w+$', ''
            if ($internalDeps.ContainsKey($dep)) {
                $internalDeps[$dep]++
            } else {
                $internalDeps[$dep] = 1
            }
        }
    }
}

$sortedDeps = $internalDeps.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 15
foreach ($dep in $sortedDeps) {
    Write-Host "  $($dep.Name) ($($dep.Value) references)"
}
Write-Host ""

# 8. External Dependencies
Write-Host "8. EXTERNAL DEPENDENCIES"
Write-Host "========================="
$imports = @{}
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match 'import\s+([^;]+);') {
        $matches = [regex]::Matches($content, 'import\s+([^;]+);')
        foreach ($match in $matches) {
            $import = $match.Groups[1].Value
            if ($import -notmatch '^com\.shreeai\.os') {
                if ($import -notmatch '^(java|javax|org\.springframework)') {
                    if ($imports.ContainsKey($import)) {
                        $imports[$import]++
                    } else {
                        $imports[$import] = 1
                    }
                }
            }
        }
    }
}

$sortedImports = $imports.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 20
foreach ($imp in $sortedImports) {
    Write-Host "  $($imp.Name) ($($imp.Value) files)"
}
Write-Host ""

# 9. Shared Concepts
Write-Host "9. SHARED CONCEPTS"
Write-Host "==================="
Write-Host "  Classes with conceptually similar names across domains:"
Write-Host "  - ExecutionContext (runtime.execution) - Similar to DecisionContext, ContextProcessingEngine"
Write-Host "  - RuntimeState (runtime.lifecycle) - Similar to CognitiveState, KernelState"
Write-Host "  - ExecutionPipeline (runtime.pipeline) - Similar to DefaultExecutionPipeline"
Write-Host "  - ExecutionRequest (runtime.execution) - Similar to ReasoningRequest, AgentRequest"
Write-Host "  - ExecutionResult (runtime.execution) - Similar to ValidationResult, DiscoveryResult"
Write-Host ""

# 10. Architecture Observations
Write-Host "10. ARCHITECTURE OBSERVATIONS"
Write-Host "=============================="

$totalFiles = $files.Count
$totalInterfaces = ($interfaces | Measure-Object | Select-Object -ExpandProperty Count)
$totalPublicClasses = ($publicClasses | Measure-Object | Select-Object -ExpandProperty Count)
$totalImpl = ($implementations | Measure-Object | Select-Object -ExpandProperty Count)

Write-Host "  Total files analyzed: $totalFiles"
Write-Host "  Interfaces: $totalInterfaces"
Write-Host "  Public Classes: $totalPublicClasses"
Write-Host "  Implementations: $totalImpl"
Write-Host ""
Write-Host "  Architecture Pattern: Pipeline-based Execution Architecture"
Write-Host "  Key Components:"
Write-Host "    - Runtime: Main entry point for execution"
Write-Host "    - RuntimeBuilder: Fluent builder for runtime configuration"
Write-Host "    - ExecutionPipeline: Pipeline for executing requests"
Write-Host "    - ExecutionContext: Context for execution"
Write-Host "    - ExecutionRequest/Result: Request/Response pattern"
Write-Host "    - RuntimeLifecycle: Lifecycle management"
Write-Host "    - Pipeline stages: Modular execution steps"
Write-Host ""
Write-Host "  Key Characteristics:"
Write-Host "    - Pipeline-based execution model"
Write-Host "    - Strong separation between API and implementation"
Write-Host "    - Context-driven execution"
Write-Host "    - Lifecycle-aware components"
Write-Host "    - Extensible pipeline stages"
Write-Host "    - State management throughout execution"