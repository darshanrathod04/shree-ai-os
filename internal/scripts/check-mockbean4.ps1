$jar = "$env:USERPROFILE\.m2\repository\org\mockito\mockito-junit-jupiter\5.14.2\mockito-junit-jupiter-5.14.2.jar"
if (Test-Path $jar) {
    jar tf $jar | Select-String -Pattern "MockBean|MockExtension" | Select-Object -First 10
}

Get-ChildItem "$env:USERPROFILE\.m2\repository\org\mockito\mockito-junit-jupiter" -Directory | Select-Object Name
