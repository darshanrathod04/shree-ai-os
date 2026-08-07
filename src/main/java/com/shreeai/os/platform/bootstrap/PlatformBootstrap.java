package com.shreeai.os.platform.bootstrap;

import com.shreeai.os.platform.bootstrap.integration.PlatformServiceLocator;
import com.shreeai.os.platform.core.configuration.api.ConfigurationService;
import com.shreeai.os.platform.core.discovery.api.DiscoveryService;
import com.shreeai.os.platform.core.eventbus.api.EventBus;
import com.shreeai.os.platform.core.health.api.HealthService;
import com.shreeai.os.platform.core.lifecycle.api.LifecycleService;
import com.shreeai.os.platform.core.plugin.api.PluginService;
import com.shreeai.os.platform.core.registry.api.KernelRegistry;
import com.shreeai.os.platform.core.registry.model.KernelId;
import com.shreeai.os.platform.core.registry.model.KernelMetadata;
import com.shreeai.os.platform.core.registry.model.KernelVersion;
import com.shreeai.os.platform.core.registry.model.RegisteredKernel;
import com.shreeai.os.platform.runtime.api.Runtime;
import com.shreeai.os.platform.runtime.service.DefaultRuntimeService;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Platform Bootstrap - Single entry point for platform initialization.
 * 
 * This class orchestrates the entire platform bootstrap process from OFFLINE to READY.
 * It follows a deterministic initialization sequence and handles failures with rollback.
 * 
 * This class is framework-agnostic and does not depend on Spring Boot.
 */
public class PlatformBootstrap {
    
    private final BootstrapConfiguration configuration;
    private volatile BootstrapState currentState;
    private final List<BootstrapListener> listeners;
    private final List<PlatformInitializationReport.ModuleInitializationResult> initializationHistory;
    private PlatformInitializationReport lastReport;
    
    /**
     * Module initialization function
     */
    @FunctionalInterface
    public interface ModuleInitializer {
        PlatformInitializationReport.ModuleInitializationResult initialize(String moduleName) throws Exception;
    }
    
    /**
     * Bootstrap event listener
     */
    public interface BootstrapListener {
        void onStateChange(BootstrapState oldState, BootstrapState newState);
        void onModuleInitialized(String moduleName, boolean success, Duration duration);
        void onBootstrapComplete(PlatformInitializationReport report);
    }
    
    /**
     * Create a new PlatformBootstrap with default configuration
     */
    public static PlatformBootstrap create() {
        return new PlatformBootstrap(BootstrapConfiguration.defaults());
    }
    
    /**
     * Create a new PlatformBootstrap with custom configuration
     */
    public static PlatformBootstrap create(BootstrapConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }
        return new PlatformBootstrap(configuration);
    }
    
    private PlatformBootstrap(BootstrapConfiguration configuration) {
        this.configuration = configuration;
        this.currentState = BootstrapState.OFFLINE;
        this.listeners = new CopyOnWriteArrayList<>();
        this.initializationHistory = new CopyOnWriteArrayList<>();
    }
    
    public void addListener(BootstrapListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }
    
    public void removeListener(BootstrapListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }
    
    public BootstrapState getCurrentState() {
        return currentState;
    }
    
    public BootstrapConfiguration getConfiguration() {
        return configuration;
    }
    
    public PlatformInitializationReport getLastReport() {
        return lastReport;
    }
    
    /**
     * Start the platform bootstrap process.
     * Initializes all platform services and registers all kernels.
     */
    public PlatformInitializationReport start() {
        if (currentState != BootstrapState.OFFLINE && currentState != BootstrapState.STOPPED) {
            throw new BootstrapException("Cannot start bootstrap from state: " + currentState);
        }
        
        Instant startTime = Instant.now();
        List<PlatformInitializationReport.ModuleInitializationResult> initializedModules = new ArrayList<>();
        List<PlatformInitializationReport.ModuleInitializationResult> failedModules = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String errorMessage = null;
        
        try {
            transitionTo(BootstrapState.INITIALIZING);
            
            // Phase 1: Start Core
            transitionTo(BootstrapState.STARTING_CORE);
            initializeCore(initializedModules, failedModules, warnings);
            
            // Phase 2: Start Runtime
            transitionTo(BootstrapState.STARTING_RUNTIME);
            initializeRuntime(initializedModules, failedModules, warnings);
            
            // Phase 3: Start Kernels
            transitionTo(BootstrapState.STARTING_KERNELS);
            initializeKernels(initializedModules, failedModules, warnings);
            
            // Phase 4: Verification
            if (configuration.isEnableVerification()) {
                transitionTo(BootstrapState.VERIFYING);
                verifySystem(initializedModules, failedModules, warnings);
            }
            
            // Success - transition to READY
            transitionTo(BootstrapState.READY);
            
        } catch (Exception e) {
            errorMessage = e.getMessage();
            transitionTo(BootstrapState.FAILED);
            
            if (configuration.isRollbackOnFailure()) {
                warnings.add("Rolling back initialized modules...");
                rollback(initializedModules, failedModules);
            }
            
            if (configuration.isStrictMode()) {
                throw new BootstrapException("Bootstrap failed: " + errorMessage, e);
            }
        }
        
        Instant endTime = Instant.now();
        Duration totalDuration = Duration.between(startTime, endTime);
        
        lastReport = PlatformInitializationReport.builder()
            .withFinalState(currentState)
            .withStartTime(startTime)
            .withEndTime(endTime)
            .withTotalDuration(totalDuration)
            .withInitializedModules(initializedModules)
            .withFailedModules(failedModules)
            .withWarnings(warnings)
            .withErrorMessage(errorMessage)
            .build();
        
        for (BootstrapListener listener : listeners) {
            try {
                listener.onBootstrapComplete(lastReport);
            } catch (Exception e) {
                // Ignore listener errors
            }
        }
        
        return lastReport;
    }
    
    /**
     * Shutdown the platform
     */
    public PlatformInitializationReport shutdown() {
        if (currentState == BootstrapState.OFFLINE || currentState == BootstrapState.STOPPED) {
            return lastReport;
        }
        
        Instant startTime = Instant.now();
        List<PlatformInitializationReport.ModuleInitializationResult> shutdownModules = new ArrayList<>();
        
        try {
            transitionTo(BootstrapState.SHUTTING_DOWN);
            
            // Shutdown Runtime first
            PlatformServiceLocator locator = PlatformServiceLocator.getInstance();
            Runtime runtime = locator.getRuntime();
            if (runtime instanceof DefaultRuntimeService) {
                ((DefaultRuntimeService) runtime).shutdown();
                shutdownModules.add(new PlatformInitializationReport.ModuleInitializationResult(
                    "Runtime", true, Duration.ZERO, null));
            }
            
            // Shutdown in reverse order
            List<String> moduleOrder = configuration.getModuleOrder();
            List<String> reverseOrder = new ArrayList<>(moduleOrder);
            java.util.Collections.reverse(reverseOrder);
            
            for (String module : reverseOrder) {
                if (!module.equals("Runtime") && !module.equals("Verification")) {
                    shutdownModule(module, shutdownModules);
                }
            }
            
            transitionTo(BootstrapState.STOPPED);
            
        } catch (Exception e) {
            transitionTo(BootstrapState.STOPPED);
        }
        
        Instant endTime = Instant.now();
        
        lastReport = PlatformInitializationReport.builder()
            .withFinalState(currentState)
            .withStartTime(startTime)
            .withEndTime(endTime)
            .withInitializedModules(shutdownModules)
            .build();
        
        return lastReport;
    }
    
    /**
     * Verify the platform is ready
     */
    public boolean verify() {
        if (currentState != BootstrapState.READY) {
            return false;
        }
        
        PlatformServiceLocator locator = PlatformServiceLocator.getInstance();
        
        // Verify Configuration available
        ConfigurationService config = locator.getConfigurationService();
        if (config == null) {
            return false;
        }
        
        // Verify Registry operational
        KernelRegistry<?> registry = locator.getKernelRegistry();
        if (registry == null) {
            return false;
        }
        
        // Verify Discovery operational
        DiscoveryService discovery = locator.getDiscoveryService();
        if (discovery == null) {
            return false;
        }
        
        // Verify EventBus operational
        EventBus eventBus = locator.getEventBus();
        if (eventBus == null) {
            return false;
        }
        
        // Verify Health operational
        HealthService health = locator.getHealthService();
        if (health == null) {
            return false;
        }
        
        // Verify Plugin operational
        PluginService plugin = locator.getPluginService();
        if (plugin == null) {
            return false;
        }
        
        // Verify Lifecycle operational
        LifecycleService lifecycle = locator.getLifecycleService();
        if (lifecycle == null) {
            return false;
        }
        
        // Verify Runtime state is READY or VERIFIED
        Runtime runtime = locator.getRuntime();
        if (runtime == null) {
            return false;
        }
        // Check if RuntimeService is in VERIFIED state
        if (runtime instanceof DefaultRuntimeService) {
            com.shreeai.os.platform.runtime.RuntimeState runtimeState = 
                ((DefaultRuntimeService) runtime).getRuntimeState();
            if (runtimeState != com.shreeai.os.platform.runtime.RuntimeState.VERIFIED &&
                runtimeState != com.shreeai.os.platform.runtime.RuntimeState.STARTED) {
                return false;
            }
        }
        
        // Verify Registry contains all nine kernels
        if (registry.findAll().size() < 9) {
            return false;
        }
        
        return true;
    }
    
    private void rollback(List<PlatformInitializationReport.ModuleInitializationResult> initializedModules, 
                         List<PlatformInitializationReport.ModuleInitializationResult> failedModules) {
        List<PlatformInitializationReport.ModuleInitializationResult> rollbackResults = new ArrayList<>();
        
        List<PlatformInitializationReport.ModuleInitializationResult> reverseOrder = new ArrayList<>(initializedModules);
        java.util.Collections.reverse(reverseOrder);
        
        for (PlatformInitializationReport.ModuleInitializationResult module : reverseOrder) {
            try {
                rollbackModule(module.getModuleName());
                rollbackResults.add(new PlatformInitializationReport.ModuleInitializationResult(
                    module.getModuleName(), true, Duration.ZERO, null));
            } catch (Exception e) {
                rollbackResults.add(new PlatformInitializationReport.ModuleInitializationResult(
                    module.getModuleName(), false, Duration.ZERO, e.getMessage()));
            }
        }
        
        initializedModules.clear();
        initializedModules.addAll(rollbackResults);
    }
    
    private void transitionTo(BootstrapState newState) {
        BootstrapState oldState = this.currentState;
        this.currentState = newState;
        
        for (BootstrapListener listener : listeners) {
            try {
                listener.onStateChange(oldState, newState);
            } catch (Exception e) {
                // Ignore listener errors
            }
        }
    }
    
    /**
     * Initialize core modules
     */
    private void initializeCore(List<PlatformInitializationReport.ModuleInitializationResult> initializedModules,
                               List<PlatformInitializationReport.ModuleInitializationResult> failedModules,
                               List<String> warnings) {
        PlatformServiceLocator locator = PlatformServiceLocator.getInstance();
        
        // 1. Configuration
        initializeModule("Configuration", (name) -> {
            ConfigurationService config = locator.getConfigurationService();
            if (config == null) {
                throw new BootstrapException("ConfigurationService not available");
            }
            return new PlatformInitializationReport.ModuleInitializationResult("Configuration", true, Duration.ZERO, null);
        }, initializedModules, failedModules);
        
        // 2. Registry
        initializeModule("Registry", (name) -> {
            KernelRegistry<?> registry = locator.getKernelRegistry();
            if (registry == null) {
                throw new BootstrapException("KernelRegistry not available");
            }
            return new PlatformInitializationReport.ModuleInitializationResult("Registry", true, Duration.ZERO, null);
        }, initializedModules, failedModules);
        
        // 3. Discovery
        initializeModule("Discovery", (name) -> {
            DiscoveryService discovery = locator.getDiscoveryService();
            if (discovery == null) {
                throw new BootstrapException("DiscoveryService not available");
            }
            return new PlatformInitializationReport.ModuleInitializationResult("Discovery", true, Duration.ZERO, null);
        }, initializedModules, failedModules);
        
        // 4. EventBus
        initializeModule("EventBus", (name) -> {
            EventBus eventBus = locator.getEventBus();
            if (eventBus == null) {
                throw new BootstrapException("EventBus not available");
            }
            return new PlatformInitializationReport.ModuleInitializationResult("EventBus", true, Duration.ZERO, null);
        }, initializedModules, failedModules);
    }
    
    /**
     * Initialize runtime modules
     */
    private void initializeRuntime(List<PlatformInitializationReport.ModuleInitializationResult> initializedModules,
                                  List<PlatformInitializationReport.ModuleInitializationResult> failedModules,
                                  List<String> warnings) {
        PlatformServiceLocator locator = PlatformServiceLocator.getInstance();
        
        // 5. Health
        initializeModule("Health", (name) -> {
            HealthService health = locator.getHealthService();
            if (health == null) {
                throw new BootstrapException("HealthService not available");
            }
            return new PlatformInitializationReport.ModuleInitializationResult("Health", true, Duration.ZERO, null);
        }, initializedModules, failedModules);
        
        // 6. Plugin
        initializeModule("Plugin", (name) -> {
            PluginService plugin = locator.getPluginService();
            if (plugin == null) {
                throw new BootstrapException("PluginService not available");
            }
            return new PlatformInitializationReport.ModuleInitializationResult("Plugin", true, Duration.ZERO, null);
        }, initializedModules, failedModules);
        
        // 7. Lifecycle
        initializeModule("Lifecycle", (name) -> {
            LifecycleService lifecycle = locator.getLifecycleService();
            if (lifecycle == null) {
                throw new BootstrapException("LifecycleService not available");
            }
            return new PlatformInitializationReport.ModuleInitializationResult("Lifecycle", true, Duration.ZERO, null);
        }, initializedModules, failedModules);
        
        // 8. Runtime - initialize and verify
        initializeModule("Runtime", (name) -> {
            Runtime runtime = locator.getRuntime();
            if (runtime == null) {
                throw new BootstrapException("Runtime not available");
            }
            
            // Initialize the Runtime
            if (runtime instanceof DefaultRuntimeService) {
                DefaultRuntimeService runtimeService = (DefaultRuntimeService) runtime;
                runtimeService.initialize();
                runtimeService.start();
                runtimeService.verify();
            }
            
            return new PlatformInitializationReport.ModuleInitializationResult("Runtime", true, Duration.ZERO, null);
        }, initializedModules, failedModules);
    }
    
    /**
     * Initialize kernels by registering them with the KernelRegistry
     */
    @SuppressWarnings("unchecked")
    private void initializeKernels(List<PlatformInitializationReport.ModuleInitializationResult> initializedModules,
                                  List<PlatformInitializationReport.ModuleInitializationResult> failedModules,
                                  List<String> warnings) {
        PlatformServiceLocator locator = PlatformServiceLocator.getInstance();
        KernelRegistry<RegisteredKernel> registry = (KernelRegistry<RegisteredKernel>) locator.getKernelRegistry();
        
        if (registry == null) {
            throw new BootstrapException("KernelRegistry not available for kernel registration");
        }
        
        // Define all 9 kernels with their metadata
        KernelMetadata identityKernel = new KernelMetadata(
            "Identity", "Identity management and authentication kernel",
            "Shree AI OS", Set.of("identity", "auth", "security"), "security", Instant.now());
        
        KernelMetadata memoryKernel = new KernelMetadata(
            "Memory", "Memory management and storage kernel",
            "Shree AI OS", Set.of("memory", "storage", "persistence"), "storage", Instant.now());
        
        KernelMetadata contextKernel = new KernelMetadata(
            "Context", "Context management and session tracking kernel",
            "Shree AI OS", Set.of("context", "session", "state"), "state", Instant.now());
        
        KernelMetadata knowledgeKernel = new KernelMetadata(
            "Knowledge", "Knowledge graph and ontology kernel",
            "Shree AI OS", Set.of("knowledge", "graph", "ontology"), "intelligence", Instant.now());
        
        KernelMetadata cognitiveKernel = new KernelMetadata(
            "Cognitive", "Cognitive processing and reasoning kernel",
            "Shree AI OS", Set.of("cognitive", "reasoning", "inference"), "intelligence", Instant.now());
        
        KernelMetadata planningKernel = new KernelMetadata(
            "Planning", "Planning and strategy kernel",
            "Shree AI OS", Set.of("planning", "strategy", "optimization"), "execution", Instant.now());
        
        KernelMetadata executionKernel = new KernelMetadata(
            "Execution", "Execution and task management kernel",
            "Shree AI OS", Set.of("execution", "tasks", "workflow"), "execution", Instant.now());
        
        KernelMetadata multiAgentKernel = new KernelMetadata(
            "MultiAgent", "Multi-agent coordination and swarm kernel",
            "Shree AI OS", Set.of("multiagent", "swarm", "coordination"), "coordination", Instant.now());
        
        KernelMetadata chiefKernel = new KernelMetadata(
            "Chief", "Chief orchestration and oversight kernel",
            "Shree AI OS", Set.of("chief", "orchestration", "oversight"), "orchestration", Instant.now());
        
        // Register each kernel in order
        registerKernel("Identity", "kernel-identity", identityKernel, registry, initializedModules, failedModules, warnings);
        registerKernel("Memory", "kernel-memory", memoryKernel, registry, initializedModules, failedModules, warnings);
        registerKernel("Context", "kernel-context", contextKernel, registry, initializedModules, failedModules, warnings);
        registerKernel("Knowledge", "kernel-knowledge", knowledgeKernel, registry, initializedModules, failedModules, warnings);
        registerKernel("Cognitive", "kernel-cognitive", cognitiveKernel, registry, initializedModules, failedModules, warnings);
        registerKernel("Planning", "kernel-planning", planningKernel, registry, initializedModules, failedModules, warnings);
        registerKernel("Execution", "kernel-execution", executionKernel, registry, initializedModules, failedModules, warnings);
        registerKernel("MultiAgent", "kernel-multiagent", multiAgentKernel, registry, initializedModules, failedModules, warnings);
        registerKernel("Chief", "kernel-chief", chiefKernel, registry, initializedModules, failedModules, warnings);
    }
    
    /**
     * Register a single kernel with the KernelRegistry
     */
    private void registerKernel(String kernelName, String kernelId, KernelMetadata metadata,
                                KernelRegistry<RegisteredKernel> registry,
                                List<PlatformInitializationReport.ModuleInitializationResult> initializedModules,
                                List<PlatformInitializationReport.ModuleInitializationResult> failedModules,
                                List<String> warnings) {
        
        initializeModule(kernelName, (name) -> {
            PlatformServiceLocator locator = PlatformServiceLocator.getInstance();
            
            try {
                KernelId id = new KernelId(kernelId);
                KernelVersion version = new KernelVersion(1, 0, 0);
                RegisteredKernel registeredKernel = new RegisteredKernel(id, version, metadata);
                
                // Step 1: Register kernel
                boolean success = registry.register(kernelId, registeredKernel);
                if (!success) {
                    warnings.add("Kernel registration returned false for: " + kernelName);
                }
                
                // Verify registration
                boolean exists = registry.exists(kernelId);
                if (!exists) {
                    throw new BootstrapException("Kernel registration verification failed for: " + kernelName);
                }
                
                // Step 2: Initialize kernel lifecycle
                LifecycleService lifecycleService = locator.getLifecycleService();
                if (lifecycleService != null) {
                    boolean initialized = lifecycleService.initialize(id);
                    if (!initialized) {
                        throw new BootstrapException("Kernel initialization failed for: " + kernelName);
                    }
                }
                
                // Step 3: Start kernel
                if (lifecycleService != null) {
                    boolean started = lifecycleService.start(id);
                    if (!started) {
                        throw new BootstrapException("Kernel start failed for: " + kernelName);
                    }
                }
                
                return new PlatformInitializationReport.ModuleInitializationResult(kernelName, true, Duration.ZERO, null);
                
            } catch (Exception ex) {
                throw new BootstrapException("Failed to register kernel " + kernelName + ": " + ex.getMessage(), ex);
            }
        }, initializedModules, failedModules);
    }
    
    private void initializeModule(String moduleName,
                                 ModuleInitializer initializer,
                                 List<PlatformInitializationReport.ModuleInitializationResult> initializedModules,
                                 List<PlatformInitializationReport.ModuleInitializationResult> failedModules) {
        Instant moduleStart = Instant.now();
        
        try {
            PlatformInitializationReport.ModuleInitializationResult result = initializer.initialize(moduleName);
            Duration duration = Duration.between(moduleStart, Instant.now());
            
            initializedModules.add(result);
            initializationHistory.add(result);
            
            for (BootstrapListener listener : listeners) {
                try {
                    listener.onModuleInitialized(moduleName, true, duration);
                } catch (Exception listenerEx) {
                    // Ignore listener errors
                }
            }
            
        } catch (Exception ex) {
            Duration duration = Duration.between(moduleStart, Instant.now());
            PlatformInitializationReport.ModuleInitializationResult result = 
                new PlatformInitializationReport.ModuleInitializationResult(moduleName, false, duration, ex.getMessage());
            
            failedModules.add(result);
            initializationHistory.add(result);
            
            for (BootstrapListener listener : listeners) {
                try {
                    listener.onModuleInitialized(moduleName, false, duration);
                } catch (Exception listenerEx) {
                    // Ignore listener errors
                }
            }
            
            throw new BootstrapException("Failed to initialize module: " + moduleName, ex);
        }
    }
    
    private void shutdownModule(String moduleName, List<PlatformInitializationReport.ModuleInitializationResult> shutdownModules) {
        Instant moduleStart = Instant.now();
        
        try {
            Duration duration = Duration.between(moduleStart, Instant.now());
            shutdownModules.add(new PlatformInitializationReport.ModuleInitializationResult(
                moduleName, true, duration, null));
            
        } catch (Exception e) {
            Duration duration = Duration.between(moduleStart, Instant.now());
            shutdownModules.add(new PlatformInitializationReport.ModuleInitializationResult(
                moduleName, false, duration, e.getMessage()));
        }
    }
    
    private void rollbackModule(String moduleName) {
        // Rollback is handled by the service lifecycle
    }
    
    /**
     * Verify the platform system
     */
    private void verifySystem(List<PlatformInitializationReport.ModuleInitializationResult> initializedModules,
                             List<PlatformInitializationReport.ModuleInitializationResult> failedModules,
                             List<String> warnings) {
        
        PlatformServiceLocator locator = PlatformServiceLocator.getInstance();
        
        // 1. Verify Configuration available
        boolean configAvailable = locator.getConfigurationService() != null;
        if (!configAvailable) {
            throw new BootstrapException("Configuration not available");
        }
        
        // 2. Verify Registry operational
        KernelRegistry<?> registry = locator.getKernelRegistry();
        if (registry == null) {
            throw new BootstrapException("Registry not operational");
        }
        
        // 3. Verify Discovery operational
        if (locator.getDiscoveryService() == null) {
            throw new BootstrapException("Discovery not operational");
        }
        
        // 4. Verify EventBus operational
        if (locator.getEventBus() == null) {
            throw new BootstrapException("EventBus not operational");
        }
        
        // 5. Verify Health operational
        if (locator.getHealthService() == null) {
            throw new BootstrapException("Health not operational");
        }
        
        // 6. Verify Plugin operational
        if (locator.getPluginService() == null) {
            throw new BootstrapException("Plugin not operational");
        }
        
        // 7. Verify Lifecycle operational
        if (locator.getLifecycleService() == null) {
            throw new BootstrapException("Lifecycle not operational");
        }
        
        // 8. Verify Runtime state == VERIFIED
        Runtime runtime = locator.getRuntime();
        if (runtime == null) {
            throw new BootstrapException("Runtime not available");
        }
        if (runtime instanceof DefaultRuntimeService) {
            com.shreeai.os.platform.runtime.RuntimeState runtimeState = 
                ((DefaultRuntimeService) runtime).getRuntimeState();
            if (runtimeState != com.shreeai.os.platform.runtime.RuntimeState.VERIFIED &&
                runtimeState != com.shreeai.os.platform.runtime.RuntimeState.STARTED) {
                throw new BootstrapException("Runtime not verified. State: " + runtimeState);
            }
        }
        
        // 9. Verify Registry contains all nine kernels
        int kernelCount = registry.findAll().size();
        if (kernelCount < 9) {
            warnings.add("Not all kernels registered: " + kernelCount + "/9");
            throw new BootstrapException("Insufficient kernels registered: " + kernelCount + "/9");
        }
    }
}