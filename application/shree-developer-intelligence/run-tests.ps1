Set-Location C:\shree-ai-os\application\shree-developer-intelligence
.\mvnw.cmd -B test 2>&1 | Out-File -FilePath C:\shree-ai-os\dev_test.log -Encoding utf8
exit $LASTEXITCODE
