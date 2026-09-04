$files = @(
    'C:\shree-ai-os\application\shree-developer-intelligence\src\test\java\com\shreeai\os\developer\workspace\WorkspaceControllerTests.java',
    'C:\shree-ai-os\application\shree-developer-intelligence\src\test\java\com\shreeai\os\developer\chat\AiChatControllerTests.java',
    'C:\shree-ai-os\application\shree-developer-intelligence\src\test\java\com\shreeai\os\developer\workflow\DeveloperWorkflowControllerTests.java'
)
foreach ($f in $files) {
    $content = [System.IO.File]::ReadAllText($f)
    # Remove unused ObjectMapper injection (we don't use it for assertions)
    $content = $content -replace 'import com\.fasterxml\.jackson\.databind\.ObjectMapper;\r?\n', ''
    $content = $content -replace '    @Autowired\r?\n    private ObjectMapper mapper;\r?\n', ''
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($f, $content, $utf8NoBom)
    Write-Host "Fixed: $f"
}
