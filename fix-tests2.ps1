$files = @(
    'C:\shree-ai-os\application\shree-developer-intelligence\src\test\java\com\shreeai\os\developer\workspace\WorkspaceControllerTests.java',
    'C:\shree-ai-os\application\shree-developer-intelligence\src\test\java\com\shreeai\os\developer\chat\AiChatControllerTests.java',
    'C:\shree-ai-os\application\shree-developer-intelligence\src\test\java\com\shreeai\os\developer\workflow\DeveloperWorkflowControllerTests.java'
)
foreach ($f in $files) {
    $content = [System.IO.File]::ReadAllText($f)
    # Spring Boot 4: WebMvcTest is in webmvc.test.autoconfigure, not test.autoconfigure.web.servlet
    $content = $content -replace 'org\.springframework\.boot\.test\.autoconfigure\.web\.servlet\.WebMvcTest', 'org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest'
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($f, $content, $utf8NoBom)
    Write-Host "Fixed: $f"
}
