Get-ChildItem "$env:USERPROFILE\.m2\repository\org\mockito" -Directory | Select-Object Name
$jar = "$env:USERPROFILE\.m2\repository\org\mockito\mockito-core\5.14.2\mockito-core-5.14.2.jar"
if (Test-Path $jar) {
    jar tf $jar | Select-String -Pattern "MockBean|@interface Mock" | Select-Object -First 10
}

# Check spring-boot-test jar
$jar2 = "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-test\4.1.1\spring-boot-test-4.1.1.jar"
if (Test-Path $jar2) {
    jar tf $jar2 | Select-String -Pattern "MockBean|webmvc" | Select-Object -First 10
}
