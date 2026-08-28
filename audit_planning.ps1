# Legacy Planning Audit Script
$planningPath = "src/main/java/com/shreeai/os/platform/planning"
$plannerPath = "src/main/java/com/shreeai/os/platform/planner"
$autonomyPath = "src/main/java/com/shreeai/os/platform/autonomy"
$kernelPlanningPath = "src/main/java/com/shreeai/os/platform/kernels/planning"
$kernelExecutionPath = "src/main/java/com/shreeai/os/platform/kernels/execution"

Write-Host "=== LEGACY PLANNING AUDIT ===" -ForegroundColor Cyan
Write-Host ""

# Analyze planning package
Write-Host "=== PLANNING PACKAGE ===" -ForegroundColor Green
$planningFiles = Get-ChildItem -Path $planningPath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }
Write-Host "Total files: $($planningFiles.Count)"

$planningInterfaces = $planningFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'interface\s+\w+'
}
Write-Host "Interfaces: $($planningInterfaces.Count)"

$planningClasses = $planningFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'class\s+\w+'
}
Write-Host "Classes: $($planningClasses.Count)"

# Analyze planner package
Write-Host "`n=== PLANNER PACKAGE ===" -ForegroundColor Green
$plannerFiles = Get-ChildItem -Path $plannerPath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }
Write-Host "Total files: $($plannerFiles.Count)"

$plannerInterfaces = $plannerFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'interface\s+\w+'
}
Write-Host "Interfaces: $($plannerInterfaces.Count)"

$plannerClasses = $plannerFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'class\s+\w+'
}
Write-Host "Classes: $($plannerClasses.Count)"

# Analyze autonomy package
Write-Host "`n=== AUTONOMY PACKAGE ===" -ForegroundColor Green
$autonomyFiles = Get-ChildItem -Path $autonomyPath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }
Write-Host "Total files: $($autonomyFiles.Count)"

$autonomyInterfaces = $autonomyFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'interface\s+\w+'
}
Write-Host "Interfaces: $($autonomyInterfaces.Count)"

$autonomyClasses = $autonomyFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'class\s+\w+'
}
Write-Host "Classes: $($autonomyClasses.Count)"

# Analyze kernel planning for comparison
Write-Host "`n=== KERNEL PLANNING (FOR COMPARISON) ===" -ForegroundColor Yellow
$kernelPlanningFiles = Get-ChildItem -Path $kernelPlanningPath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }
Write-Host "Total files: $($kernelPlanningFiles.Count)"

$kernelPlanningInterfaces = $kernelPlanningFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'interface\s+\w+'
}
Write-Host "Interfaces: $($kernelPlanningInterfaces.Count)"

# Analyze kernel execution for comparison
Write-Host "`n=== KERNEL EXECUTION (FOR COMPARISON) ===" -ForegroundColor Yellow
$kernelExecutionFiles = Get-ChildItem -Path $kernelExecutionPath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }
Write-Host "Total files: $($kernelExecutionFiles.Count)"

$kernelExecutionInterfaces = $kernelExecutionFiles | Where-Object { 
    $content = Get-Content $_.FullName -Raw
    $content -match 'interface\s+\w+'
}
Write-Host "Interfaces: $($kernelExecutionInterfaces.Count)"

# Get all class names
Write-Host "`n=== CLASS NAME COMPARISON ===" -ForegroundColor Cyan

$planningClassNames = @{}
foreach ($file in $planningFiles) {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $planningClassNames[$name] = $file.FullName
}

$plannerClassNames = @{}
foreach ($file in $plannerFiles) {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $plannerClassNames[$name] = $file.FullName
}

$autonomyClassNames = @{}
foreach ($file in $autonomyFiles) {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $autonomyClassNames[$name] = $file.FullName
}

$kernelPlanningClassNames = @{}
foreach ($file in $kernelPlanningFiles) {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $kernelPlanningClassNames[$name] = $file.FullName
}

$kernelExecutionClassNames = @{}
foreach ($file in $kernelExecutionFiles) {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $kernelExecutionClassNames[$name] = $file.FullName
}

Write-Host "`nClasses in both planning AND planner:"
foreach ($name in $planningClassNames.Keys) {
    if ($plannerClassNames.ContainsKey($name)) {
        Write-Host "  - $name"
    }
}

Write-Host "`nClasses in both planning AND autonomy:"
foreach ($name in $planningClassNames.Keys) {
    if ($autonomyClassNames.ContainsKey($name)) {
        Write-Host "  - $name"
    }
}

Write-Host "`nClasses in both planner AND autonomy:"
foreach ($name in $plannerClassNames.Keys) {
    if ($autonomyClassNames.ContainsKey($name)) {
        Write-Host "  - $name"
    }
}

Write-Host "`nClasses in legacy AND kernel planning:"
foreach ($name in $planningClassNames.Keys) {
    if ($kernelPlanningClassNames.ContainsKey($name)) {
        Write-Host "  - $name"
    }
}
foreach ($name in $plannerClassNames.Keys) {
    if ($kernelPlanningClassNames.ContainsKey($name) -and -not $planningClassNames.ContainsKey($name)) {
        Write-Host "  - $name"
    }
}

Write-Host "`nClasses in legacy AND kernel execution:"
foreach ($name in $planningClassNames.Keys) {
    if ($kernelExecutionClassNames.ContainsKey($name)) {
        Write-Host "  - $name"
    }
}
foreach ($name in $plannerClassNames.Keys) {
    if ($kernelExecutionClassNames.ContainsKey($name) -and -not $planningClassNames.ContainsKey($name)) {
        Write-Host "  - $name"
    }
}

# Analyze dependencies
Write-Host "`n=== DEPENDENCY ANALYSIS ===" -ForegroundColor Cyan

Write-Host "`nPlanning package dependencies:"
$planningDeps = @{}
foreach ($file in $planningFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match 'import\s+([^;]+);') {
        $matches = [regex]::Matches($content, 'import\s+([^;]+);')
        foreach ($match in $matches) {
            $import = $match.Groups[1].Value
            if ($import -match 'com\.shreeai\.os\.platform\.(core|runtime|kernels|brain|cognition|memory|planning|planner|autonomy)') {
                if ($planningDeps.ContainsKey($import)) {
                    $planningDeps[$import]++
                } else {
                    $planningDeps[$import] = 1
                }
            }
        }
    }
}
foreach ($dep in ($planningDeps.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 10)) {
    Write-Host "  $($dep.Name): $($dep.Value)"
}

Write-Host "`nPlanner package dependencies:"
$plannerDeps = @{}
foreach ($file in $plannerFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match 'import\s+([^;]+);') {
        $matches = [regex]::Matches($content, 'import\s+([^;]+);')
        foreach ($match in $matches) {
            $import = $match.Groups[1].Value
            if ($import -match 'com\.shreeai\.os\.platform\.(core|runtime|kernels|brain|cognition|memory|planning|planner|autonomy)') {
                if ($plannerDeps.ContainsKey($import)) {
                    $plannerDeps[$import]++
                } else {
                    $plannerDeps[$import] = 1
                }
            }
        }
    }
}
foreach ($dep in ($plannerDeps.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 10)) {
    Write-Host "  $($dep.Name): $($dep.Value)"
}

Write-Host "`nAutonomy package dependencies:"
$autonomyDeps = @{}
foreach ($file in $autonomyFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match 'import\s+([^;]+);') {
        $matches = [regex]::Matches($content, 'import\s+([^;]+);')
        foreach ($match in $matches) {
            $import = $match.Groups[1].Value
            if ($import -match 'com\.shreeai\.os\.platform\.(core|runtime|kernels|brain|cognition|memory|planning|planner|autonomy)') {
                if ($autonomyDeps.ContainsKey($import)) {
                    $autonomyDeps[$import]++
                } else {
                    $autonomyDeps[$import] = 1
                }
            }
        }
    }
}
foreach ($dep in ($autonomyDeps.GetEnumerator() | Sort-Object Value -Descending | Select-Object -First 10)) {
    Write-Host "  $($dep.Name): $($dep.Value)"
}