$jar = "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-test-autoconfigure\4.1.1\spring-boot-test-autoconfigure-4.1.1.jar"
Write-Host "Checking: $jar"
jar tf $jar | Select-String -Pattern "WebMvcTest|AutoConfigureMockMvc|MockBean" | Select-Object -First 10
