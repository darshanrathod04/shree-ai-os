$jar = "$env:USERPROFILE\.m2\repository\org\springframework\spring-test\7.0.9\spring-test-7.0.9.jar"
if (Test-Path $jar) {
    Write-Host "Found spring-test 7.0.9"
    jar tf $jar | Select-String -Pattern "MockBean|MockitoBean" | Select-Object -First 10
} else {
    Write-Host "Not found"
    Get-ChildItem "$env:USERPROFILE\.m2\repository\org\springframework\spring-test"
}
