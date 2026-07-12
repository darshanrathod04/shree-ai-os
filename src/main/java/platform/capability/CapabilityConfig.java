package platform.capability;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Capability Registry configuration.
 * Registers all capabilities on application startup.
 *
 * SHADOW MODE — capabilities are registered for observation only.
 * Production routing remains in AgentBrain switch().
 */
@Configuration
public class CapabilityConfig {

    @Bean
    public CapabilityRegistry capabilityRegistry() {
        CapabilityRegistry registry = new CapabilityRegistry();

        // Register all capabilities
        registry.register(new ChatCapability());
        registry.register(new LearningCapability());
        registry.register(new QuizCapability());
        registry.register(new RoadmapCapability());

        // Detect duplicate intents
        registry.detectDuplicateIntents();

        return registry;
    }
}