$jar = "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-test-autoconfigure\4.1.1\spring-boot-test-autoconfigure-4.1.1.jar"
if (Test-Path $jar) {
    jar tf $jar | Select-String -Pattern "WebMvcTest|webmvc" | Select-Object -First 5
}
Write-Host "---"
Get-ChildItem "$env:USERPROFILE\.m2\repository\org\springframework\boot\spring-boot-test-autoconfigure" -Directory | Select-Object Name
