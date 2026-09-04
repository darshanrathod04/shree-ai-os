$jar = "$env:USERPROFILE\.m2\repository\org\mockito\mockito-junit-jupiter\5.14.2\mockito-junit-jupiter-5.14.2.jar"
if (Test-Path $jar) {
    jar tf $jar | Select-Object -First 20
} else {
    Write-Host "Not found"
}
