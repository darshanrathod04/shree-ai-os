package com.shreeai.os.platform.sdk;

import java.util.Map;
import java.util.Objects;

/**
 * Identity SDK Facade
 *
 * Developer-facing entry point for the Identity Kernel.
 * Contains no business logic; delegates to ShreeClient.
 */
public final class IdentitySDK {

    private final ShreeClient client;

    IdentitySDK(ShreeClient client) {
        this.client = Objects.requireNonNull(client);
    }

    /**
     * Creates a logical identity.
     */
    public SDKResponse createIdentity(
            String identityId,
            String identityType,
            Map<String, Object> profile
    ) {

        SDKRequest request = SDKRequest.builder()
                .message("IDENTITY_CREATE")
                .metadata(Map.of(
                        "operation", "CREATE_IDENTITY",
                        "identityId", identityId,
                        "identityType", identityType,
                        "profile", profile
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Retrieves identity information.
     */
    public SDKResponse getIdentity(String identityId) {

        SDKRequest request = SDKRequest.builder()
                .message("IDENTITY_GET")
                .metadata(Map.of(
                        "operation", "GET_IDENTITY",
                        "identityId", identityId
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Updates identity profile.
     */
    public SDKResponse updateProfile(
            String identityId,
            Map<String, Object> updates
    ) {

        SDKRequest request = SDKRequest.builder()
                .message("IDENTITY_UPDATE")
                .metadata(Map.of(
                        "operation", "UPDATE_PROFILE",
                        "identityId", identityId,
                        "updates", updates
                ))
                .build();

        return client.chat(request);
    }
}