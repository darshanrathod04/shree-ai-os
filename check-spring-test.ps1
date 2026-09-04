Get-ChildItem "$env:USERPROFILE\.m2\repository\org\springframework\spring-test" -Directory | Select-Object Name

$latest = "$env:USERPROFILE\.m2\repository\org\springframework\spring-test\6.1.18"
if (Test-Path $latest) {
    $jar = "$latest\spring-test-6.1.18.jar"
    if (Test-Path $jar) {
        jar tf $jar | Select-String -Pattern "MockBean" | Select-Object -First 10
    }
}
