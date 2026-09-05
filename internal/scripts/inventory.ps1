$files = Get-ChildItem -Path src/main/java -Filter *.java -Recurse | Select-Object -ExpandProperty FullName

# Count total files
$totalFiles = $files.Count
Write-Host "Total Java files: $totalFiles"

# Count unique packages
$packages = $files | ForEach-Object { 
    $dir = Split-Path -Path $_ -Parent
    $dir = $dir -replace [regex]::Escape('src/main/java/'), ''
    $dir = $dir -replace '\\', '.'
    $dir
} | Sort-Object -Unique
Write-Host "`nTotal packages: $($packages.Count)"

# Count interfaces, classes, enums, records
$interfaces = 0
$classes = 0
$enums = 0
$records = 0

foreach ($file in $files) {
    $content = Get-Content $file -Raw
    if ($content -match '\binterface\s+\w+') { $interfaces++ }
    if ($content -match '\benum\s+\w+') { $enums++ }
    if ($content -match '\brecord\s+\w+') { $records++ }
    if ($content -match '\bclass\s+\w+') { $classes++ }
}

Write-Host "`nTotal interfaces: $interfaces"
Write-Host "Total classes: $classes"
Write-Host "Total enums: $enums"
Write-Host "Total records: $records"

# Find duplicate class names
$classNames = @{}
foreach ($file in $files) {
    $name = [System.IO.Path]::GetFileNameWithoutExtension($file)
    if ($classNames.ContainsKey($name)) {
        $classNames[$name]++
    } else {
        $classNames[$name] = 1
    }
}

$duplicates = $classNames.GetEnumerator() | Where-Object { $_.Value -gt 1 } | Sort-Object Name
Write-Host "`nDuplicate class names:"
if ($duplicates) {
    foreach ($dup in $duplicates) {
        Write-Host "  $($dup.Name): $($dup.Value) occurrences"
    }
} else {
    Write-Host "  None found"
}

# Analyze package responsibilities - look for similar domain packages
Write-Host "`nPackage responsibility analysis:"
$packageDomains = @{}
foreach ($package in $packages) {
    # Extract domain from package name (e.g., cognition, memory, runtime, etc.)
    # Package format: com.shreeai.os.platform.XXX
    if ($package -match '^com\.shreeai\.os\.platform\.([^\.]+)') {
        $domain = $Matches[1]
        if ($packageDomains.ContainsKey($domain)) {
            $packageDomains[$domain]++
        } else {
            $packageDomains[$domain] = 1
        }
    }
}

$domainCounts = $packageDomains.GetEnumerator() | Sort-Object Value -Descending
Write-Host "`nPackage domains by frequency:"
foreach ($domain in $domainCounts) {
    Write-Host "  $($domain.Name): $($domain.Value) packages"
}

# Identify potential duplicate responsibilities
Write-Host "`nPotential duplicate responsibilities (domains with multiple packages):"
$duplicateDomains = $domainCounts | Where-Object { $_.Value -gt 1 }
if ($duplicateDomains) {
    foreach ($domain in $duplicateDomains) {
        $relatedPackages = $packages | Where-Object { $_ -match "^com\.shreeai\.os\.platform\.$($domain.Name)(\.|$)" }
        if ($relatedPackages.Count -gt 1) {
            Write-Host "`n  Domain: $($domain.Name) ($($domain.Value) packages)"
            foreach ($pkg in $relatedPackages) {
                Write-Host "    - $pkg"
            }
        }
    }
} else {
    Write-Host "  None found"
}
