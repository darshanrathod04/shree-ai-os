$jar = "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-webmvc-test\4.1.1\spring-boot-webmvc-test-4.1.1.jar"
Write-Host "All classes in webmvc-test:"
jar tf $jar | Select-String -Pattern "\.class$" | Select-Object -First 30
