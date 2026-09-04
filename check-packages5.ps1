$jar = "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-webmvc-test\4.1.1\spring-boot-webmvc-test-4.1.1.jar"
Write-Host "Checking: $jar"
if (Test-Path $jar) {
    jar tf $jar | Select-String -Pattern "WebMvcTest|AutoConfigureMockMvc|MockBean|webmvc" | Select-Object -First 20
} else {
    Write-Host "Not found. Listing:"
    Get-ChildItem "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-webmvc-test\4.1.1" | Select-Object Name
}
