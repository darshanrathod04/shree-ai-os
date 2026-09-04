package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.kernels.identity.model.IdentityContext;
import com.shreeai.os.platform.runtime.api.Runtime;

import java.util.Map;
import java.util.Objects;

/**
 * Identity SDK Facade
 *
 * Developer-facing entry point for the Identity Kernel.
 * Delegates to ShreeClient for string-routed operations and directly
 * to the Runtime's IdentityService when a Runtime is available.
 *
 * <p><b>Runtime path:</b>
 * {@code IdentitySDK → Runtime.resolveIdentity() → IdentityService.resolveIdentity()}</p>
 */
public final class IdentitySDK {

    private final ShreeClient client;
    private final Runtime runtime;

    IdentitySDK(ShreeClient client) {
        this(client, client != null ? client.runtime() : null);
    }

    IdentitySDK(ShreeClient client, Runtime runtime) {
        this.client = Objects.requireNonNull(client, "client must not be null");
        this.runtime = runtime;
    }

    /**
     * Resolves the Identity context for an incoming request.
     *
     * <p>Delegates directly to the typed
     * {@link com.shreeai.os.platform.runtime.api.Runtime#resolveIdentity}
     * when a Runtime is available. Falls back to string-routing otherwise.</p>
     *
     * @param identityId     the identity identifier (used as requestId)
     * @param sessionId      the session identifier (may be null)
     * @param applicationId  the application identifier (may be null)
     * @param workspaceId    the workspace identifier (may be null)
     * @return SDKResponse with the resolved identity context
     * @throws IllegalArgumentException if identityId is null or blank
     */
    public SDKResponse resolve(
            String identityId,
            String sessionId,
            String applicationId,
            String workspaceId
    ) {
        if (identityId == null || identityId.isBlank()) {
            throw new IllegalArgumentException("identityId must not be null or blank");
        }

        if (runtime != null) {
            IdentityContext ctx = runtime.resolveIdentity(
                    identityId,
                    sessionId,
                    applicationId,
                    workspaceId
            );
            if (ctx != null) {
                java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
                payload.put("identityId", ctx.identityId().value());
                payload.put("identityType", ctx.identityType().name());
                payload.put("sessionId", ctx.sessionId());
                payload.put("applicationId", ctx.applicationId());
                payload.put("workspaceId", ctx.workspaceId());
                payload.put("authenticated", ctx.authenticated());
                payload.put("resolvedAt", ctx.resolvedAt().toString());
                payload.put("_identitySource", "typed-runtime");
                return SDKResponse.builder()
                        .answer("Identity resolved: " + ctx.identityId().value())
                        .structuredPayload(payload)
                        .build();
            }
        }

        // Legacy string-routing fallback.
        SDKRequest request = SDKRequest.builder()
                .message("IDENTITY_RESOLVE")
                .metadata(Map.of(
                        "operation", "RESOLVE_IDENTITY",
                        "identityId", identityId,
                        "sessionId", sessionId != null ? sessionId : "SDK_SESSION",
                        "applicationId", applicationId != null ? applicationId : "SHREE_SDK",
                        "workspaceId", workspaceId != null ? workspaceId : "DEFAULT"
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Creates a logical identity.
     *
     * @param identityId   the identity identifier
     * @param identityType the identity type
     * @param profile      the profile attributes
     * @return SDKResponse with creation result
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
     *
     * @param identityId the identity identifier
     * @return SDKResponse with identity data
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
     *
     * @param identityId the identity identifier
     * @param updates    the profile updates
     * @return SDKResponse with update result
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