$jar = "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-test\4.1.1\spring-boot-test-4.1.1.jar"
if (Test-Path $jar) {
    jar tf $jar | Select-String -Pattern "MockBean" | Select-Object -First 10
} else {
    Write-Host "File not found, listing versions:"
    Get-ChildItem "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-test" -Directory | Select-Object Name
}
