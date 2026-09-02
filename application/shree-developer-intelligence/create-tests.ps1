$workspaceDir = "C:\shree-ai-os\application\shree-developer-intelligence\src\test\java\com\shreeai\os\developer\workspace"
$chatDir = "C:\shree-ai-os\application\shree-developer-intelligence\src\test\java\com\shreeai\os\developer\chat"
$workflowDir = "C:\shree-ai-os\application\shree-developer-intelligence\src\test\java\com\shreeai\os\developer\workflow"

$workspaceContent = @"
package com.shreeai.os.developer.workspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkspaceController.class)
@AutoConfigureMockMvc
@SpringJUnitConfig
class WorkspaceControllerTests {

    @MockBean
    private WorkspaceService workspaceService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        when(workspaceService.getAllSessions()).thenReturn(List.of());
    }

    @Test
    void listSessions_returnsOk() throws Exception {
        mvc.perform(get("/api/developer/workspace/sessions")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void open_withNullBody_returnsBadRequest() throws Exception {
        mvc.perform(post("/api/developer/workspace/open")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void open_withBlankPath_returnsBadRequest() throws Exception {
        mvc.perform(post("/api/developer/workspace/open")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"path\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSession_returns404_whenNotFound() throws Exception {
        mvc.perform(get("/api/developer/workspace/nonexistent-id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void close_returns404_whenSessionNotFound() throws Exception {
        when(workspaceService.closeSession("nonexistent-id")).thenReturn(false);
        mvc.perform(delete("/api/developer/workspace/nonexistent-id"))
                .andExpect(status().isNotFound());
    }
}
"@

$chatContent = @"
package com.shreeai.os.developer.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiChatController.class)
@AutoConfigureMockMvc
@SpringJUnitConfig
class AiChatControllerTests {

    @MockBean
    private AiChatService chatService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void ask_withNullBody_returnsBadRequest() throws Exception {
        mvc.perform(post("/api/developer/chat/ask")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ask_withNullSessionId_returnsBadRequest() throws Exception {
        mvc.perform(post("/api/developer/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"question\": \"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("sessionId is required"));
    }

    @Test
    void ask_withEmptyQuestion_returnsBadRequest() throws Exception {
        mvc.perform(post("/api/developer/chat/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\": \"test\", \"question\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("question is required"));
    }

    @Test
    void remember_withValidData_returnsOk() throws Exception {
        mvc.perform(post("/api/developer/chat/remember")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\": \"test\", \"title\": \"Naming Rule\", \"content\": \"Use Controller suffix\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("stored"));
    }

    @Test
    void recall_withValidData_returnsOk() throws Exception {
        mvc.perform(post("/api/developer/chat/recall")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\": \"test\", \"query\": \"convention\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confidence").exists());
    }
}
"@

$workflowContent = @"
package com.shreeai.os.developer.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.developer.workspace.WorkspaceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeveloperWorkflowController.class)
@AutoConfigureMockMvc
@SpringJUnitConfig
class DeveloperWorkflowControllerTests {

    @MockBean
    private DeveloperWorkflowService workflowService;

    @MockBean
    private WorkspaceService workspaceService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void build_withNullBody_returnsBadRequest() throws Exception {
        mvc.perform(post("/api/developer/workflow/build")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void build_withNoSessionOrPath_returnsBadRequest() throws Exception {
        mvc.perform(post("/api/developer/workflow/build")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"instruction\": \"Add JWT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("sessionId or projectPath is required"));
    }

    @Test
    void build_withEmptyInstruction_returnsBadRequest() throws Exception {
        mvc.perform(post("/api/developer/workflow/build")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectPath\": \"/test\", \"instruction\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("instruction is required"));
    }
}
"@

$workspaceFile = Join-Path $workspaceDir "WorkspaceControllerTests.java"
$chatFile = Join-Path $chatDir "AiChatControllerTests.java"
$workflowFile = Join-Path $workflowDir "DeveloperWorkflowControllerTests.java"

# Create directories if they don't exist
New-Item -ItemType Directory -Path $workspaceDir -Force | Out-Null
New-Item -ItemType Directory -Path $chatDir -Force | Out-Null
New-Item -ItemType Directory -Path $workflowDir -Force | Out-Null

# Write files with BOM for UTF-8
$utf8Bom = New-Object System.Text.UTF8Encoding $true
[System.IO.File]::WriteAllText($workspaceFile, $workspaceContent, $utf8Bom)
[System.IO.File]::WriteAllText($chatFile, $chatContent, $utf8Bom)
[System.IO.File]::WriteAllText($workflowFile, $workflowContent, $utf8Bom)

Write-Host "Files written:"
Get-ChildItem $workspaceDir
Get-ChildItem $chatDir
Get-ChildItem $workflowDir
