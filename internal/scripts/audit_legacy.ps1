# Legacy Intelligence Audit Script
$brainPath = "src/main/java/com/shreeai/os/platform/brain"
$cognitionPath = "src/main/java/com/shreeai/os/platform/cognition"
$kernelCognitivePath = "src/main/java/com/shreeai/os/platform/kernels/cognitive"

Write-Host "=== LEGACY INTELLIGENCE AUDIT ===" -ForegroundColor Cyan
Write-Host ""

# Analyze brain package
Write-Host "=== BRAIN PACKAGE ===" -ForegroundColor Green
$brainFiles = Get-ChildItem -Path $brainPath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }
Write-Host "Total files: $($brainFiles.Count)"

$brainInterfaces = $brainFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'interface\s+\w+'
}
Write-Host "Interfaces: $($brainInterfaces.Count)"

$brainClasses = $brainFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'class\s+\w+'
}
Write-Host "Classes: $($brainClasses.Count)"

# Analyze cognition package
Write-Host "`n=== COGNITION PACKAGE ===" -ForegroundColor Green
$cognitionFiles = Get-ChildItem -Path $cognitionPath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }
Write-Host "Total files: $($cognitionFiles.Count)"

$cognitionInterfaces = $cognitionFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'interface\s+\w+'
}
Write-Host "Interfaces: $($cognitionInterfaces.Count)"

$cognitionClasses = $cognitionFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'class\s+\w+'
}
Write-Host "Classes: $($cognitionClasses.Count)"

# Analyze kernels/cognitive for comparison
Write-Host "`n=== KERNELS/COGNITIVE (FOR COMPARISON) ===" -ForegroundColor Yellow
$kernelFiles = Get-ChildItem -Path $kernelCognitivePath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }
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

# Get all class names from each package
Write-Host "`n=== CLASS NAME COMPARISON ===" -ForegroundColor Cyan

$brainClassNames = @{}
foreach ($file in $brainFiles) {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $brainClassNames[$name] = $file.FullName
}

$cognitionClassNames = @{}
foreach ($file in $cognitionFiles) {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $cognitionClassNames[$name] = $file.FullName
}

$kernelClassNames = @{}
foreach ($file in $kernelFiles) {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $kernelClassNames[$name] = $file.FullName
}

# Find overlaps
Write-Host "`nClasses in both brain AND cognition:"
$brainCognitionOverlap = @{}
foreach ($name in $brainClassNames.Keys) {
    if ($cognitionClassNames.ContainsKey($name)) {
        $brainCognitionOverlap[$name] = $true
        Write-Host "  - $name"
    }
}

Write-Host "`nClasses in both cognition AND kernels/cognitive:"
$cognitionKernelOverlap = @{}
foreach ($name in $cognitionClassNames.Keys) {
    if ($kernelClassNames.ContainsKey($name)) {
        $cognitionKernelOverlap[$name] = $true
        Write-Host "  - $name"
    }
}

Write-Host "`nClasses in both brain AND kernels/cognitive:"
$brainKernelOverlap = @{}
foreach ($name in $brainClassNames.Keys) {
    if ($kernelClassNames.ContainsKey($name)) {
        $brainKernelOverlap[$name] = $true
        Write-Host "  - $name"
    }
}

# Analyze dependencies
Write-Host "`n=== DEPENDENCY ANALYSIS ===" -ForegroundColor Cyan

Write-Host "`nBrain package dependencies:"
$brainDeps = @{}
foreach ($file in $brainFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match 'import\s+([^;]+);') {
        $matches = [regex]::Matches($content, 'import\s+([^;]+);')
        foreach ($match in $matches) {
            $import = $match.Groups[1].Value
            if ($import -match 'com\.shreeai\.os\.platform\.(core|runtime|kernels|cognition)') {
                if ($brainDeps.ContainsKey($import)) {
                    $brainDeps[$import]++
                } else {
                    $brainDeps[$import] = 1
                }
            }
        }
    }
}
foreach ($dep in ($brainDeps.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 10)) {
    Write-Host "  $($dep.Name): $($dep.Value)"
}

Write-Host "`nCognition package dependencies:"
$cognitionDeps = @{}
foreach ($file in $cognitionFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match 'import\s+([^;]+);') {
        $matches = [regex]::Matches($content, 'import\s+([^;]+);')
        foreach ($match in $matches) {
            $import = $match.Groups[1].Value
            if ($import -match 'com\.shreeai\.os\.platform\.(core|runtime|kernels|brain)') {
                if ($cognitionDeps.ContainsKey($import)) {
                    $cognitionDeps[$import]++
                } else {
                    $cognitionDeps[$import] = 1
                }
            }
        }
    }
}
foreach ($dep in ($cognitionDeps.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 10)) {
    Write-Host "  $($dep.Name): $($dep.Value)"
}

# Identify unique capabilities
Write-Host "`n=== UNIQUE CAPABILITIES ===" -ForegroundColor Cyan

Write-Host "`nUnique to brain (not in cognition or kernels/cognitive):"
foreach ($name in ($brainClassNames.Keys | Sort-Object)) {
    if (-not $cognitionClassNames.ContainsKey($name) -and -not $kernelClassNames.ContainsKey($name)) {
        Write-Host "  - $name"
    }
}

Write-Host "`nUnique to cognition (not in brain or kernels/cognitive):"
foreach ($name in ($cognitionClassNames.Keys | Sort-Object)) {
    if (-not $brainClassNames.ContainsKey($name) -and -not $kernelClassNames.ContainsKey($name)) {
        Write-Host "  - $name"
    }
}

Write-Host "`nUnique to kernels/cognitive (not in brain or cognition):"
foreach ($name in ($kernelClassNames.Keys | Sort-Object)) {
    if (-not $brainClassNames.ContainsKey($name) -and -not $cognitionClassNames.ContainsKey($name)) {
        Write-Host "  - $name"
    }
}