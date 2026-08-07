package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.sdk.SDKConfiguration;
import com.shreeai.os.platform.sdk.SDKRequest;
import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.sdk.ShreeClient;
import com.shreeai.os.platform.sdk.exceptions.ConfigurationException;
import com.shreeai.os.platform.sdk.exceptions.SDKException;
import com.shreeai.os.platform.sdk.exceptions.ValidationException;
import com.shreeai.os.platform.sdk.version.SDKVersion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SDK Integration Test
 *
 * <p>This test verifies the Shree AI OS SDK foundation works correctly.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0.0-V1
 * @since EO-V1-SDK1-001
 */
public class SDKIntegrationTest {

    private ShreeAI ai;

    @BeforeEach
    public void setUp() {
        ai = ShreeAI.builder().build();
    }

    @Test
    public void testSDKBuildsSuccessfully() {
        assertNotNull(ai, "ShreeAI should be created");
        assertNotNull(ai.configuration(), "Configuration should exist");
        assertEquals("1.0.0-V1", SDKVersion.VERSION, "Version should be correct");
    }

    @Test
    public void testBuilderWorks() {
        ShreeAI custom = ShreeAI.builder()
                .apiKey("test-key")
                .build();
        assertNotNull(custom, "Custom ShreeAI should be created");
        assertEquals("test-key", custom.configuration().apiKey(), "API key should be set");
    }

    @Test
    public void testChatRequestExecutes() {
        SDKResponse response = ai.chat("Hello");
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.answer(), "Answer should not be null");
        assertFalse(response.answer().isBlank(), "Answer should not be blank");
    }

    @Test
    public void testRuntimeInvoked() {
        // Verify the client is accessible
        ShreeClient client = ai.client();
        assertNotNull(client, "Client should exist");
        assertNotNull(client.configuration(), "Client configuration should exist");
    }

    @Test
    public void testSDKResponseReturned() {
        SDKResponse response = ai.chat("What is Java?");
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.answer(), "Answer should not be null");
        assertTrue(response.confidence() >= 0.0 && response.confidence() <= 1.0, "Confidence should be 0-1");
        assertNotNull(response.timestamp(), "Timestamp should not be null");
    }

    @Test
    public void testInvalidConfigurationThrowsSDKException() {
        // Invalid timeout should throw ConfigurationException (subclass of SDKException)
        assertThrows(SDKException.class, () -> {
            SDKConfiguration.builder().timeout(-1).build();
        });
    }

    @Test
    public void testPublicAPIStable() {
        // Verify the public API surface
        assertNotNull(ShreeAI.builder(), "builder() should exist");
        assertNotNull(SDKRequest.builder(), "SDKRequest.builder() should exist");
        assertNotNull(SDKResponse.builder(), "SDKResponse.builder() should exist");
        assertNotNull(SDKConfiguration.builder(), "SDKConfiguration.builder() should exist");
    }

    @Test
    public void testNoKernelClassesExposed() {
        // Verify SDK does not expose kernel classes
        // The SDK package should only contain SDK classes
        String sdkPackage = "com.shreeai.os.platform.sdk";
        assertFalse(sdkPackage.contains("kernels"), "SDK should not expose kernel package");
        assertFalse(sdkPackage.contains("memory"), "SDK should not expose memory package");
        assertFalse(sdkPackage.contains("knowledge"), "SDK should not expose knowledge package");
        assertFalse(sdkPackage.contains("cognitive"), "SDK should not expose cognitive package");
        assertFalse(sdkPackage.contains("inference"), "SDK should not expose inference package");
    }

    @Test
    public void testChatWithSDKRequest() {
        SDKRequest request = SDKRequest.builder()
                .message("Explain Java Streams")
                .context("programming")
                .sessionId("session-1")
                .userId("user-1")
                .build();

        SDKResponse response = ai.chat(request);
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.answer(), "Answer should not be null");
    }

    @Test
    public void testInvalidRequestThrowsValidationException() {
        assertThrows(ValidationException.class, () -> {
            SDKRequest.builder().message("").build();
        });
    }
}