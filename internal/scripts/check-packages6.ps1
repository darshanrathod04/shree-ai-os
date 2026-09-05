$jar = "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-webmvc-test\4.1.1\spring-boot-webmvc-test-4.1.1.jar"
jar tf $jar | Select-String -Pattern "WebMvcTest|WebMvc" | Select-Object -First 10

# Also check mockito
$jar2 = "$env:USERPROFILE\.m2\repository\org\mockito\mockito-core\"
Get-ChildItem $jar2 -Directory | Select-Object -Last 1 Name
$mockitoJar = "$env:USERPROFILE\.m2\repository\org\mockito\mockito-core\5.14.2\mockito-core-5.14.2.jar"
if (Test-Path $mockitoJar) {
    jar tf $mockitoJar | Select-String -Pattern "MockBean|Mock" | Select-Object -First 5
}
