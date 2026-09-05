$repo = "$env:USERPROFILE\.m2\repository\org\springframework\boot"
$jars = Get-ChildItem $repo -Recurse -Filter "*.jar" | Where-Object { $_.Name -notmatch "sources|javadoc" }
$found = @()
foreach ($jar in $jars) {
    $classes = jar tf $jar.FullName 2>$null
    if ($classes -match "MockBean") {
        $found += "$($jar.Name) in $($jar.Directory.Name)"
    }
}
$found | Select-Object -First 10
