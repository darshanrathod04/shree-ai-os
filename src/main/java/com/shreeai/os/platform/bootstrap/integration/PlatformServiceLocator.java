package com.shreeai.os.platform.bootstrap.integration;

import com.shreeai.os.platform.core.configuration.api.ConfigurationService;
import com.shreeai.os.platform.core.configuration.service.DefaultConfigurationService;
import com.shreeai.os.platform.core.discovery.api.DiscoveryService;
import com.shreeai.os.platform.core.discovery.service.DefaultDiscoveryService;
import com.shreeai.os.platform.core.discovery.validator.DiscoveryValidator;
import com.shreeai.os.platform.core.eventbus.api.EventBus;
import com.shreeai.os.platform.core.eventbus.engine.DefaultEventDispatchEngine;
import com.shreeai.os.platform.core.eventbus.engine.EventDispatchEngine;
import com.shreeai.os.platform.core.eventbus.service.DefaultEventBusService;
import com.shreeai.os.platform.core.eventbus.validator.EventValidator;
import com.shreeai.os.platform.core.health.api.HealthService;
import com.shreeai.os.platform.core.health.engine.HealthEvaluationEngine;
import com.shreeai.os.platform.core.health.service.DefaultHealthService;
import com.shreeai.os.platform.core.health.validator.HealthValidator;
import com.shreeai.os.platform.core.lifecycle.api.LifecycleService;
import com.shreeai.os.platform.core.lifecycle.engine.LifecycleTransitionEngine;
import com.shreeai.os.platform.core.lifecycle.service.DefaultLifecycleService;
import com.shreeai.os.platform.core.lifecycle.validator.LifecycleValidator;
import com.shreeai.os.platform.core.plugin.api.PluginService;
import com.shreeai.os.platform.core.plugin.engine.PluginLifecycleEngine;
import com.shreeai.os.platform.core.plugin.service.DefaultPluginService;
import com.shreeai.os.platform.core.plugin.validator.PluginValidator;
import com.shreeai.os.platform.core.registry.api.KernelRegistry;
import com.shreeai.os.platform.core.registry.service.DefaultKernelRegistry;
import com.shreeai.os.platform.core.registry.validator.KernelRegistrationValidator;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.config.RuntimeConfiguration;
import com.shreeai.os.platform.runtime.contracts.RuntimeContract;
import com.shreeai.os.platform.runtime.service.DefaultRuntimeService;

/**
 * Service locator for platform services during bootstrap.
 * 
 * This class provides access to platform service instances during bootstrap.
 * It acts as a bridge between the bootstrap layer and the platform core/runtime.
 * 
 * This class is framework-agnostic and does not depend on Spring Boot.
 */
public class PlatformServiceLocator {
    
    private static PlatformServiceLocator instance;
    
    private final ConfigurationService configurationService;
    private final KernelRegistry<?> kernelRegistry;
    private final DiscoveryService discoveryService;
    private final EventBus eventBus;
    private final HealthService healthService;
    private final PluginService pluginService;
    private final LifecycleService lifecycleService;
    private final Runtime runtime;
    
    /**
     * Private constructor - use getInstance()
     * 
     * Instantiates all platform services in dependency order:
     * Configuration → Registry → Discovery → Lifecycle → EventBus → Health → Plugin → Runtime
     */
    private PlatformServiceLocator() {
        // Initialize Configuration service (no-arg constructor)
        this.configurationService = new DefaultConfigurationService();
        
        // Initialize Registry with validator
        KernelRegistrationValidator registryValidator = new KernelRegistrationValidator();
        this.kernelRegistry = new DefaultKernelRegistry(registryValidator);
        
        // Initialize Discovery service (depends on KernelRegistry)
        DiscoveryValidator discoveryValidator = new DiscoveryValidator();
        this.discoveryService = new DefaultDiscoveryService(
            (KernelRegistry<?>) this.kernelRegistry, 
            discoveryValidator
        );
        
        // Initialize Lifecycle service (depends on KernelRegistry)
        LifecycleValidator lifecycleValidator = new LifecycleValidator();
        LifecycleTransitionEngine lifecycleTransitionEngine = new LifecycleTransitionEngine(lifecycleValidator);
        this.lifecycleService = new DefaultLifecycleService(
            (KernelRegistry<?>) this.kernelRegistry,
            lifecycleValidator,
            lifecycleTransitionEngine
        );
        
        // Initialize Health service (no platform dependencies)
        HealthValidator healthValidator = new HealthValidator();
        HealthEvaluationEngine healthEvaluationEngine = new HealthEvaluationEngine();
        this.healthService = new DefaultHealthService(healthValidator, healthEvaluationEngine);
        
        // Initialize Plugin service (no platform dependencies)
        PluginValidator pluginValidator = new PluginValidator();
        PluginLifecycleEngine pluginLifecycleEngine = new PluginLifecycleEngine();
        this.pluginService = new DefaultPluginService(pluginValidator, pluginLifecycleEngine);
        
        // Initialize EventBus (depends on LifecycleService)
        EventValidator eventValidator = new EventValidator();
        EventDispatchEngine eventDispatchEngine = new DefaultEventDispatchEngine();
        this.eventBus = new DefaultEventBusService(
            eventValidator,
            this.lifecycleService,
            lifecycleTransitionEngine,
            eventDispatchEngine
        );
        
        // Initialize Runtime with default configuration and contract
        RuntimeConfiguration runtimeConfig = RuntimeConfiguration.builder().build();
        RuntimeContract runtimeContract = RuntimeContract.builder().build();
        this.runtime = new DefaultRuntimeService(runtimeConfig, runtimeContract);
    }
    
    /**
     * Get the singleton instance
     * 
     * @return service locator instance
     */
    public static synchronized PlatformServiceLocator getInstance() {
        if (instance == null) {
            instance = new PlatformServiceLocator();
        }
        return instance;
    }
    
    /**
     * Get the configuration service
     * 
     * @return configuration service
     */
    public ConfigurationService getConfigurationService() {
        return configurationService;
    }
    
    /**
     * Get the kernel registry
     * 
     * @return kernel registry
     */
    public KernelRegistry<?> getKernelRegistry() {
        return kernelRegistry;
    }
    
    /**
     * Get the discovery service
     * 
     * @return discovery service
     */
    public DiscoveryService getDiscoveryService() {
        return discoveryService;
    }
    
    /**
     * Get the event bus
     * 
     * @return event bus
     */
    public EventBus getEventBus() {
        return eventBus;
    }
    
    /**
     * Get the health service
     * 
     * @return health service
     */
    public HealthService getHealthService() {
        return healthService;
    }
    
    /**
     * Get the plugin service
     * 
     * @return plugin service
     */
    public PluginService getPluginService() {
        return pluginService;
    }
    
    /**
     * Get the lifecycle service
     * 
     * @return lifecycle service
     */
    public LifecycleService getLifecycleService() {
        return lifecycleService;
    }
    
    /**
     * Get the runtime
     * 
     * @return runtime
     */
    public Runtime getRuntime() {
        return runtime;
    }
    
    /**
     * Reset the service locator (for testing)
     */
    public static synchronized void reset() {
        instance = null;
    }
}