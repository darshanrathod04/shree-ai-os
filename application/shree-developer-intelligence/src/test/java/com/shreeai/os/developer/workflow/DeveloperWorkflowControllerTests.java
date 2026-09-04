package com.shreeai.os.developer.workflow;

import com.shreeai.os.developer.workspace.WorkspaceService;
import com.shreeai.os.developer.workflow.DeveloperWorkflowController;
import com.shreeai.os.developer.workflow.DeveloperWorkflowService;
import com.shreeai.os.developer.review.ReviewController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeveloperWorkflowController.class)
@AutoConfigureMockMvc
@SpringJUnitConfig
class DeveloperWorkflowControllerTests {

    @MockitoBean
    private DeveloperWorkflowService workflowService;

    @MockitoBean
    private WorkspaceService workspaceService;

    @MockitoBean
    private ReviewController reviewController;

    @Autowired
    private MockMvc mvc;


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