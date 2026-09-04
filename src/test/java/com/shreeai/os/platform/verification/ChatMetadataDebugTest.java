package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class ChatMetadataDebugTest {

    @Test
    public void debugChat() {
        ShreeAI ai = ShreeAI.builder().apiKey("local").build();
        ai.knowledge().ingest("darshan", "Darshan is founder of Shree AI OS");

        SDKResponse chat = ai.chat("who is darshan");
        System.out.println("CHAT ANSWER: '" + chat.answer() + "'");
        System.out.println("CHAT CONFIDENCE: " + chat.confidence());

        Map<?, ?> payload = chat.structuredPayload();
        System.out.println("CHAT METADATA: " + payload);

        // Check response field
        Object response = payload != null ? payload.get("response") : null;
        System.out.println("RESPONSE: " + response);
        if (response != null) {
            String responseStr = response.toString();
            System.out.println("HAS # darshan: " + responseStr.contains("# darshan"));
            System.out.println("HAS who is darshan: " + responseStr.contains("who is darshan"));
            System.out.println("HAS knowledgeTitle: " + responseStr.contains("knowledgeTitle"));
        }

        // Also debug search
        SDKResponse search = ai.knowledge().search("darshan");
        System.out.println("SEARCH ANSWER: '" + search.answer() + "'");
    }
}
