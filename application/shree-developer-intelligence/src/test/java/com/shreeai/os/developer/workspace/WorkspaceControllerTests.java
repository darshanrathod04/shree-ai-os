package com.shreeai.os.developer.workspace;

import com.shreeai.os.developer.workspace.WorkspaceController;
import com.shreeai.os.developer.workspace.WorkspaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    @MockitoBean
    private WorkspaceService workspaceService;

    @Autowired
    private MockMvc mvc;


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