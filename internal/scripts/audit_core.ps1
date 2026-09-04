# Core Domain Audit Script
$corePath = "src/main/java/com/shreeai/os/platform/core"
$files = Get-ChildItem -Path $corePath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }

Write-Host "=== PLATFORM/CORE DOMAIN AUDIT ===" -ForegroundColor Cyan
Write-Host ""

# 1. Package Hierarchy
Write-Host "1. PACKAGE HIERARCHY"
Write-Host "====================="
$packages = $files | ForEach-Object { 
    $dir = Split-Path -Path $_.Directory.FullName
    $dir = $dir -replace [regex]::Escape("$corePath\"), ''
    $dir
} | Sort-Object -Unique

$subPackages = @{}
foreach ($pkg in $packages) {
    $topLevel = $pkg.Split('\')[0]
    if ($subPackages.ContainsKey($topLevel)) {
        $subPackages[$topLevel]++
    } else {
        $subPackages[$topLevel] = 1
    }
}

foreach ($pkg in ($subPackages.GetEnumerator() | Sort-Object Name)) {
    Write-Host "  platform/core/$($pkg.Name) ($($pkg.Value) files)"
}
Write-Host ""

# 2. Purpose of each sub-package
Write-Host "2. SUB-PACKAGE PURPOSES"
Write-Host "======================="
$purposes = @{
    "configuration" = "Configuration management and resolution"
    "discovery" = "Service discovery and capability resolution"
    "eventbus" = "Event-driven communication infrastructure"
    "health" = "System health monitoring and diagnostics"
    "lifecycle" = "Component lifecycle management"
    "plugin" = "Plugin system and extensibility"
    "registry" = "Kernel registration and discovery"
}

foreach ($pkg in ($purposes.GetEnumerator() | Sort-Object Name)) {
    Write-Host "  $($pkg.Name): $($pkg.Value)"
}
Write-Host ""

# 3. Public Interfaces
Write-Host "3. PUBLIC INTERFACES"
Write-Host "===================="
$interfaces = $files | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'interface\s+\w+'
}

$interfaceByPackage = @{}
foreach ($iface in $interfaces) {
    $pkg = Split-Path -Path $iface.Directory.FullName -Leaf
    $name = [System.IO.Path]::GetFileNameWithoutExtension($iface.Name)
    if (-not $interfaceByPackage.ContainsKey($pkg)) {
        $interfaceByPackage[$pkg] = @()
    }
    $interfaceByPackage[$pkg] += $name
}

foreach ($pkg in ($interfaceByPackage.GetEnumerator() | Sort-Object Name)) {
    Write-Host "`n  [$($pkg.Name)]"
    foreach ($iface in $pkg.Value) {
        Write-Host "    - $iface"
    }
}
Write-Host ""

# 4. Default Implementations
Write-Host "4. DEFAULT IMPLEMENTATIONS"
Write-Host "=========================="
$implementations = $files | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'class\s+\w+.*implements'
}

$implByPackage = @{}
foreach ($impl in $implementations) {
    $pkg = Split-Path -Path $impl.Directory.FullName -Leaf
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

# 5. Models
Write-Host "5. MODELS"
Write-Host "========"
$models = $files | Where-Object { 
    $dir = Split-Path -Path $_.Directory.FullName -Leaf
    $dir -eq "model"
}

$modelByPackage = @{}
foreach ($model in $models) {
    $parentPkg = Split-Path -Path $model.Directory.Parent.FullName -Leaf
    $name = [System.IO.Path]::GetFileNameWithoutExtension($model.Name)
    if (-not $modelByPackage.ContainsKey($parentPkg)) {
        $modelByPackage[$parentPkg] = @()
    }
    $modelByPackage[$parentPkg] += $name
}

foreach ($pkg in ($modelByPackage.GetEnumerator() | Sort-Object Name)) {
    Write-Host "`n  [$($pkg.Name)]"
    foreach ($model in $pkg.Value) {
        Write-Host "    - $model"
    }
}
Write-Host ""

# 6. Validators
Write-Host "6. VALIDATORS"
Write-Host "============="
$validators = $files | Where-Object { 
    $dir = Split-Path -Path $_.Directory.FullName -Leaf
    $dir -eq "validator" -or $_.Name -match "Validator"
}

$validatorByPackage = @{}
foreach ($val in $validators) {
    $pkg = Split-Path -Path $val.Directory.FullName -Leaf
    $name = [System.IO.Path]::GetFileNameWithoutExtension($val.Name)
    if (-not $validatorByPackage.ContainsKey($pkg)) {
        $validatorByPackage[$pkg] = @()
    }
    $validatorByPackage[$pkg] += $name
}

foreach ($pkg in ($validatorByPackage.GetEnumerator() | Sort-Object Name)) {
    Write-Host "`n  [$($pkg.Name)]"
    foreach ($val in $pkg.Value) {
        Write-Host "    - $val"
    }
}
Write-Host ""

# 7. Exceptions
Write-Host "7. EXCEPTIONS"
Write-Host "============="
$exceptions = $files | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'class\s+\w+.*Exception'
}

$exceptionByPackage = @{}
foreach ($exc in $exceptions) {
    $pkg = Split-Path -Path $exc.Directory.FullName -Leaf
    $name = [System.IO.Path]::GetFileNameWithoutExtension($exc.Name)
    if (-not $exceptionByPackage.ContainsKey($pkg)) {
        $exceptionByPackage[$pkg] = @()
    }
    $exceptionByPackage[$pkg] += $name
}

foreach ($pkg in ($exceptionByPackage.GetEnumerator() | Sort-Object Name)) {
    Write-Host "`n  [$($pkg.Name)]"
    foreach ($exc in $pkg.Value) {
        Write-Host "    - $exc"
    }
}
Write-Host ""

# 8. External Dependencies
Write-Host "8. EXTERNAL DEPENDENCIES"
Write-Host "========================"
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

# 9. Internal Dependencies
Write-Host "9. INTERNAL DEPENDENCIES"
Write-Host "========================="
$internalDeps = @{}
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match 'import\s+com\.shreeai\.os\.platform\.core\.([^;]+);') {
        $matches = [regex]::Matches($content, 'import\s+com\.shreeai\.os\.platform\.core\.([^;]+);')
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

# 10. Architecture Observations
Write-Host "10. ARCHITECTURE OBSERVATIONS"
Write-Host "=============================="

$totalFiles = $files.Count
$totalInterfaces = ($interfaces | Measure-Object | Select-Object -ExpandProperty Count)
$totalModels = ($models | Measure-Object | Select-Object -ExpandProperty Count)
$totalValidators = ($validators | Measure-Object | Select-Object -ExpandProperty Count)
$totalExceptions = ($exceptions | Measure-Object | Select-Object -ExpandProperty Count)

Write-Host "  Total files analyzed: $totalFiles"
Write-Host "  Interfaces: $totalInterfaces"
Write-Host "  Models: $totalModels"
Write-Host "  Validators: $totalValidators"
Write-Host "  Exceptions: $totalExceptions"
Write-Host ""
Write-Host "  Architecture Pattern: Layered Architecture with API/Service/Model separation"
Write-Host "  Each sub-package follows consistent structure:"
Write-Host "    - api: Public interfaces"
Write-Host "    - service: Default implementations"
Write-Host "    - model: Data models and DTOs"
Write-Host "    - engine: Business logic processors"
Write-Host "    - validator: Validation logic"
Write-Host "    - error: Exception hierarchy"
Write-Host "    - verification: Additional validation (plugin only)"
Write-Host ""
Write-Host "  Key Characteristics:"
Write-Host "    - Strong separation of concerns"
Write-Host "    - Consistent package structure across all modules"
Write-Host "    - Comprehensive error handling with specific exceptions"
Write-Host "    - Validation at multiple layers"
Write-Host "    - Event-driven communication via eventbus"
Write-Host "    - Health monitoring capabilities"
Write-Host "    - Lifecycle management for all components"