# Kernel Domain Audit Script
$kernelsPath = "src/main/java/com/shreeai/os/platform/kernels"
$kernels = @("chief", "cognitive", "context", "execution", "identity", "knowledge", "memory", "multiagent", "planning")

Write-Host "=== PLATFORM/KERNELS DOMAIN AUDIT ===" -ForegroundColor Cyan
Write-Host ""

# Create output directory
$outputDir = "kernel_audit_output"
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

foreach ($kernel in $kernels) {
    $kernelPath = Join-Path $kernelsPath $kernel
    if (-not (Test-Path $kernelPath)) { continue }
    
    $files = Get-ChildItem -Path $kernelPath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }
    
    Write-Host "=== KERNEL: $kernel ===" -ForegroundColor Green
    Write-Host "Files: $($files.Count)"
    
    # Count by sub-package
    $subPackages = @{}
    foreach ($file in $files) {
        $dir = Split-Path -Path $file.Directory.FullName
        $dir = $dir -replace [regex]::Escape("$kernelPath\"), ''
        if ($dir -eq $kernelPath) { $dir = "root" }
        else { $dir = Split-Path -Path $dir -Leaf }
        
        if ($subPackages.ContainsKey($dir)) {
            $subPackages[$dir]++
        } else {
            $subPackages[$dir] = 1
        }
    }
    
    Write-Host "Sub-packages:"
    foreach ($pkg in ($subPackages.GetEnumerator() | Sort-Object Name)) {
        Write-Host "  $($pkg.Name): $($pkg.Value) files"
    }
    
    # Count interfaces
    $interfaces = $files | Where-Object { 
        $content = Get-Content $_.FullName -Raw
        $content -match 'interface\s+\w+'
    }
    Write-Host "Interfaces: $($interfaces.Count)"
    
    # Count classes
    $classes = $files | Where-Object { 
        $content = Get-Content $_.FullName -Raw
        $content -match 'class\s+\w+'
    }
    Write-Host "Classes: $($classes.Count)"
    
    # Count exceptions
    $exceptions = $files | Where-Object { 
        $content = Get-Content $_.FullName -Raw
        $content -match 'class\s+\w+.*Exception'
    }
    Write-Host "Exceptions: $($exceptions.Count)"
    
    # Get imports to analyze dependencies
    $imports = @{}
    foreach ($file in $files) {
        $content = Get-Content $file.FullName -Raw
        if ($content -match 'import\s+([^;]+);') {
            $matches = [regex]::Matches($content, 'import\s+([^;]+);')
            foreach ($match in $matches) {
                $import = $match.Groups[1].Value
                if ($import -match 'com\.shreeai\.os\.platform\.core') {
                    if ($imports.ContainsKey("core")) {
                        $imports["core"]++
                    } else {
                        $imports["core"] = 1
                    }
                }
                elseif ($import -match 'com\.shreeai\.os\.platform\.runtime') {
                    if ($imports.ContainsKey("runtime")) {
                        $imports["runtime"]++
                    } else {
                        $imports["runtime"] = 1
                    }
                }
                elseif ($import -match 'com\.shreeai\.os\.platform\.kernels\.([^\.]+)') {
                    $depKernel = $Matches[1]
                    if ($depKernel -ne $kernel) {
                        if ($imports.ContainsKey("kernel:$depKernel")) {
                            $imports["kernel:$depKernel"]++
                        } else {
                            $imports["kernel:$depKernel"] = 1
                        }
                    }
                }
            }
        }
    }
    
    Write-Host "Dependencies:"
    foreach ($dep in ($imports.GetEnumerator() | Sort-Object Value -Descending)) {
        Write-Host "  $($dep.Name): $($dep.Value) references"
    }
    
    Write-Host ""
}

# Generate summary
Write-Host "=== SUMMARY ===" -ForegroundColor Cyan
$totalFiles = 0
$totalInterfaces = 0
$totalClasses = 0
$totalExceptions = 0

foreach ($kernel in $kernels) {
    $kernelPath = Join-Path $kernelsPath $kernel
    if (-not (Test-Path $kernelPath)) { continue }
    
    $files = Get-ChildItem -Path $kernelPath -Filter *.java -Recurse | Where-Object { $_.Name -ne "package-info.java" }
    $totalFiles += $files.Count
    
    $interfaces = $files | Where-Object { 
        $content = Get-Content $_.FullName -Raw
        $content -match 'interface\s+\w+'
    }
    $totalInterfaces += $interfaces.Count
    
    $classes = $files | Where-Object { 
        $content = Get-Content $_.FullName -Raw
        $content -match 'class\s+\w+'
    }
    $totalClasses += $classes.Count
    
    $exceptions = $files | Where-Object { 
        $content = Get-Content $_.FullName -Raw
        $content -match 'class\s+\w+.*Exception'
    }
    $totalExceptions += $exceptions.Count
}

Write-Host "Total files: $totalFiles"
Write-Host "Total interfaces: $totalInterfaces"
Write-Host "Total classes: $totalClasses"
Write-Host "Total exceptions: $totalExceptions"