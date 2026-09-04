$jar = "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-starter-webmvc-test\4.1.1\spring-boot-starter-webmvc-test-4.1.1.jar"
Write-Host "Checking: $jar"
if (Test-Path $jar) {
    jar tf $jar | Select-String -Pattern "WebMvcTest|AutoConfigureMockMvc|MockBean|web.servlet" | Select-Object -First 15
} else {
    Write-Host "File not found"
}

# Also check spring-boot-test jar
$jar2 = "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-test\4.1.1\spring-boot-test-4.1.1.jar"
Write-Host "Checking: $jar2"
if (Test-Path $jar2) {
    jar tf $jar2 | Select-String -Pattern "WebMvcTest|AutoConfigureMockMvc|MockBean" | Select-Object -First 10
} else {
    Write-Host "File not found"
}
