$jar = "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-test\4.1.1\spring-boot-test-4.1.1.jar"
if (Test-Path $jar) {
    jar tf $jar | Select-Object -First 30
} else {
    Write-Host "File not found"
    Get-ChildItem "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-test\4.1.1"
}
