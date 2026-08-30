package com.shree.playground;

import com.shree.playground.dto.*;
import com.shreeai.os.platform.runtime.execution.*;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.sdk.SDKResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class V2_1_AcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShreeAI ai;

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(V2_1_AcceptanceTest.class);

                        private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    // S1
    @Test

            
