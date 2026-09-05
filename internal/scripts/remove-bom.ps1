$files = @(
    'C:\shree-ai-os\application\shree-developer-intelligence\src\test\java\com\shreeai\os\developer\chat\AiChatControllerTests.java',
    'C:\shree-ai-os\application\shree-developer-intelligence\src\test\java\com\shreeai\os\developer\workspace\WorkspaceControllerTests.java',
    'C:\shree-ai-os\application\shree-developer-intelligence\src\test\java\com\shreeai\os\developer\workflow\DeveloperWorkflowControllerTests.java'
)
foreach ($f in $files) {
    $content = [System.IO.File]::ReadAllText($f)
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($f, $content, $utf8NoBom)
    Write-Host "Fixed: $f"
}
