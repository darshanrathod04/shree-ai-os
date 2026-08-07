# Legacy Memory Audit Script
$legacyMemoryPath = "src/main/java/com/shreeai/os/platform/memory"
$kernelMemoryPath = "src/main/java/com/shreeai/os/platform/kernels/memory"

Write-Host "=== LEGACY MEMORY AUDIT ===" -ForegroundColor Cyan
Write-Host ""

# Analyze legacy memory package
Write-Host "=== LEGACY MEMORY PACKAGE ===" -ForegroundColor Green
$legacyFiles = Get-ChildItem -Path $legacyMemoryPath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }
Write-Host "Total files: $($legacyFiles.Count)"

$legacyInterfaces = $legacyFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'interface\s+\w+'
}
Write-Host "Interfaces: $($legacyInterfaces.Count)"

$legacyClasses = $legacyFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'class\s+\w+'
}
Write-Host "Classes: $($legacyClasses.Count)"

# Analyze kernel memory package
Write-Host "`n=== KERNEL MEMORY PACKAGE (FOR COMPARISON) ===" -ForegroundColor Yellow
$kernelFiles = Get-ChildItem -Path $kernelMemoryPath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }
Write-Host "Total files: $($kernelFiles.Count)"

$kernelInterfaces = $kernelFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'interface\s+\w+'
}
Write-Host "Interfaces: $($kernelInterfaces.Count)"

$kernelClasses = $kernelFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'class\s+\w+'
}
Write-Host "Classes: $($kernelClasses.Count)"

# Get all class names
Write-Host "`n=== CLASS NAME COMPARISON ===" -ForegroundColor Cyan

$legacyClassNames = @{}
foreach ($file in $legacyFiles) {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $legacyClassNames[$name] = $file.FullName
}

$kernelClassNames = @{}
foreach ($file in $kernelFiles) {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $kernelClassNames[$name] = $file.FullName
}

Write-Host "`nClasses in both legacy AND kernel memory:"
$overlap = @{}
foreach ($name in $legacyClassNames.Keys) {
    if ($kernelClassNames.ContainsKey($name)) {
        $overlap[$name] = $true
        Write-Host "  - $name"
    }
}

Write-Host "`nUnique to legacy memory (not in kernel memory):"
foreach ($name in ($legacyClassNames.Keys | Sort-Object)) {
    if (-not $kernelClassNames.ContainsKey($name)) {
        Write-Host "  - $name"
    }
}

Write-Host "`nUnique to kernel memory (not in legacy memory):"
foreach ($name in ($kernelClassNames.Keys | Sort-Object)) {
    if (-not $legacyClassNames.ContainsKey($name)) {
        Write-Host "  - $name"
    }
}

# Analyze dependencies
Write-Host "`n=== DEPENDENCY ANALYSIS ===" -ForegroundColor Cyan

Write-Host "`nLegacy memory dependencies:"
$legacyDeps = @{}
foreach ($file in $legacyFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match 'import\s+([^;]+);') {
        $matches = [regex]::Matches($content, 'import\s+([^;]+);')
        foreach ($match in $matches) {
            $import = $match.Groups[1].Value
            if ($import -match 'com\.shreeai\.os\.platform\.(core|runtime|kernels|brain|cognition|memory)') {
                if ($legacyDeps.ContainsKey($import)) {
                    $legacyDeps[$import]++
                } else {
                    $legacyDeps[$import] = 1
                }
            }
        }
    }
}
foreach ($dep in ($legacyDeps.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 10)) {
    Write-Host "  $($dep.Name): $($dep.Value)"
}

Write-Host "`nKernel memory dependencies:"
$kernelDeps = @{}
foreach ($file in $kernelFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match 'import\s+([^;]+);') {
        $matches = [regex]::Matches($content, 'import\s+([^;]+);')
        foreach ($match in $matches) {
            $import = $match.Groups[1].Value
            if ($import -match 'com\.shreeai\.os\.platform\.(core|runtime|kernels|brain|cognition|memory)') {
                if ($kernelDeps.ContainsKey($import)) {
                    $kernelDeps[$import]++
                } else {
                    $kernelDeps[$import] = 1
                }
            }
        }
    }
}
foreach ($dep in ($kernelDeps.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 10)) {
    Write-Host "  $($dep.Name): $($dep.Value)"
}