package com.shreeai.os.platform.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <b>ByokSettingsController</b>
 *
 * <p>REST controller for the BYOK (Bring Your Own Key) Settings API.
 * Manages LLM provider credentials.</p>
 *
 * <p><b>Endpoints:</b></p>
 * <pre>
 * POST   /api/settings/providers        — save provider settings
 * GET    /api/settings/providers        — list all providers
 * POST   /api/settings/providers/validate — validate before saving
 * DELETE /api/settings/providers/{provider} — delete provider settings
 * </pre>
 *
 * <p><b>Ownership:</b> Platform Services (v1.0)</p>
 *
 * @since v1.0
 */
@RestController
@RequestMapping("/api/settings/providers")
public class ByokSettingsController {

    private final ByokSettingsService settingsService;

    public ByokSettingsController(ByokSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /**
     * List all configured providers with masked keys.
     */
    @GetMapping
    public ResponseEntity<List<ProviderSettings>> listProviders() {
        return ResponseEntity.ok(settingsService.list());
    }

    /**
     * Save provider settings. Keys are masked before storage.
     */
    @PostMapping
    public ResponseEntity<?> saveProvider(@RequestBody SaveProviderRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body required"));
        }
        ProviderType type = ProviderType.fromString(request.provider());
        if (type == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown provider: " + request.provider()));
        }
        try {
            ProviderSettings masked = settingsService.save(
                    new ProviderSettings(type, request.enabled(), request.apiKey(), request.endpoint())
            );
            return ResponseEntity.ok(masked.toMap());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Validate provider settings without saving.
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateProvider(@RequestBody SaveProviderRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body required"));
        }
        ProviderType type = ProviderType.fromString(request.provider());
        if (type == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown provider: " + request.provider()));
        }
        ProviderSettings draft = new ProviderSettings(type, request.enabled(), request.apiKey(), request.endpoint());
        Optional<String> err = settingsService.validate(draft);
        if (err.isPresent()) {
            return ResponseEntity.ok(Map.of("valid", false, "error", err.get()));
        }
        return ResponseEntity.ok(Map.of("valid", true));
    }

    /**
     * Delete provider settings.
     */
    @DeleteMapping("/{provider}")
    public ResponseEntity<?> deleteProvider(@PathVariable String provider) {
        ProviderType type = ProviderType.fromString(provider);
        if (type == null) {
            return ResponseEntity.notFound().build();
        }
        boolean deleted = settingsService.delete(type);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Request body for save/validate.
     */
    public record SaveProviderRequest(
            String provider,
            boolean enabled,
            String apiKey,
            String endpoint
    ) {}
}
