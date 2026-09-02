package com.shreeai.os.developer.chat;

import com.shreeai.os.developer.chat.AiChatController;
import com.shreeai.os.developer.chat.AiChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiChatController.class)
@AutoConfigureMockMvc
@SpringJUnitConfig
class AiChatControllerTests {

    @MockitoBean
    private AiChatService chatService;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        // Setup mocks for tests that need them
        when(chatService.recall("test", "convention"))
                .thenReturn(com.shreeai.os.platform.sdk.SDKResponse.builder()
                        .answer("Use Controller suffix")
                        .confidence(0.85)
                        .build());
    }

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