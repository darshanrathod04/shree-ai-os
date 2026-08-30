import os

path = "D:/shree-playground/src/test/java/com/shree/playground/V2_1_AcceptanceTest.java"

# Build the complete file content as a list of lines
L = []
A = L.append

A("package com.shree.playground;")
A("")
A("import com.shree.playground.dto.*;")
A("import com.shreeai.os.platform.runtime.execution.*;")
A("import com.shreeai.os.platform.sdk.ShreeAI;")
A("import com.shreeai.os.platform.sdk.SDKResponse;")
A("import org.junit.jupiter.api.*;")
A("import org.springframework.beans.factory.annotation.Autowired;")
A("import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;")
A("import org.springframework.boot.test.context.SpringBootTest;")
A("import org.springframework.http.MediaType;")
A("import org.springframework.test.web.servlet.MockMvc;")
A("import org.springframework.test.web.servlet.MvcResult;")
A("")
A("import java.util.Map;")
A("")
A("import static org.assertj.core.api.Assertions.assertThat;")
A("import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;")
A("import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;")
A("")
A("@SpringBootTest")
A("@AutoConfigureMockMvc")
A("@TestMethodOrder(MethodOrderer.OrderAnnotation.class)")
A("class V2_1_AcceptanceTest {")
A("")
A("    @Autowired")
A("    private MockMvc mockMvc;")
A("")
A("    @Autowired")
A("    private ShreeAI ai;")
A("")
A("    private static final org.slf4j.Logger LOG =")
A("            org.slf4j.LoggerFactory.getLogger(V2_1_AcceptanceTest.class);")
A("")
A("    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER =")
A("            new com.fasterxml.jackson.databind.ObjectMapper();")
A("")

# Save current state
exec(open("C:/shree-ai-os/_acctest_data.py").read()) if os.path.exists("C:/shree-ai-os/_acctest_data.py") else None
with open("C:/shree-ai-os/_acctest_data.py", "w") as f:
    f.write("L = " + repr(L) + "\n")
print("Part 1 complete, lines:", len(L))
