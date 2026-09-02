Get-ChildItem "$env:USERPROFILE\.m2\repository\org\mockito\mockito-core" -Directory | Select-Object Name
$jar = "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-test\4.1.1\spring-boot-test-4.1.1.jar"
if (Test-Path $jar) {
    jar tf $jar | Select-String -Pattern "MockBean|webmvc" | Select-Object -First 10
} else {
    Get-ChildItem "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-test" -Directory | Select-Object Name
}
