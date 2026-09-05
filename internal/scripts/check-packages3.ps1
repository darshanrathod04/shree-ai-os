$jar = "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-starter-webmvc-test\4.1.1\spring-boot-starter-webmvc-test-4.1.1.jar"
Write-Host "Checking: $jar"
if (Test-Path $jar) {
    jar tf $jar
} else {
    Write-Host "File not found - listing directory:"
    Get-ChildItem "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-starter-webmvc-test\4.1.1" | Select-Object Name
}
