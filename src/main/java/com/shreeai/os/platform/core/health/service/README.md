# Health Service Implementation

## Architecture

```
platform.core.health.service
├── DefaultHealthService.java    — Default service implementation
├── package-info.java
└── README.md
```

## Architectural Responsibility

The Health Service Implementation provides the **default in-memory implementation** of the HealthService contract. It coordinates validation and health evaluation while owning the component storage.

### What the Service Does

- ✅ Implements the HealthService API contract
- ✅ Owns health component storage (in-memory ConcurrentHashMap)
- ✅ Coordinates validation via HealthValidator
- ✅ Coordinates evaluation via HealthEvaluationEngine
- ✅ Returns immutable collections
- ✅ Ensures thread-safe health management
- ✅ Handles registration, checking, and unregistration

### What the Service Does NOT Do

- ❌ Perform health evaluation directly (delegates to HealthEvaluationEngine)
- ❌ Validate models directly (delegates to HealthValidator)
- ❌ Inspect CPU, memory, or services
- ❌ Ping databases or external services
- ❌ Create threads or schedule work
- ❌ Publish events
- ❌ Access Lifecycle, Registry, Event Bus, or Configuration directly
- ❌ Contain business logic

## Internal Flow

### register(HealthComponent)

```
register(component)
    │
    ├─→ Validate component (HealthValidator)
    │   └─→ Invalid? → throw InvalidHealthComponentException
    │
    ├─→ Check for duplicates
    │   └─→ Duplicate? → throw HealthException (HEALTH_ALREADY_REGISTERED)
    │
    ├─→ Store component (ConcurrentHashMap)
    │
    └─→ Return true
```

### check(HealthComponent)

```
check(component)
    │
    ├─→ Validate component (HealthValidator)
    │   └─→ Invalid? → throw InvalidHealthComponentException
    │
    ├─→ Lookup component
    │   └─→ Not found? → return Optional.empty()
    │
    ├─→ Delegate to HealthEvaluationEngine.evaluate(component, false)
    │
    └─→ Return Optional<HealthReport>
```

### checkAll()

```
checkAll()
    │
    ├─→ Iterate registered components
    │
    ├─→ Delegate each to HealthEvaluationEngine.evaluate(component, false)
    │   └─→ Failure? → Log and continue
    │
    ├─→ Collect reports
    │
    └─→ Return unmodifiable collection
```

### unregister(HealthComponent)

```
unregister(component)
    │
    ├─→ Validate component (HealthValidator)
    │   └─→ Invalid? → return false
    │
    ├─→ Remove component (ConcurrentHashMap.remove)
    │
    └─→ Return success (true if removed, false otherwise)
```

### exists(HealthComponent)

```
exists(component)
    │
    └─→ Lookup only (ConcurrentHashMap.containsKey)
        └─→ Return true/false
```

## Storage

### In-Memory Storage

The service uses `ConcurrentHashMap<HealthComponentId, HealthComponent>` for thread-safe storage:

```java
private final Map<HealthComponentId, HealthComponent> components = new ConcurrentHashMap<>();
```

**Characteristics:**
- Thread-safe — ConcurrentHashMap handles concurrent access
- In-memory only — no persistence
- Keyed by HealthComponentId — fast lookups
- Stores HealthComponent — the full component definition

## Constructor Injection

The service uses constructor injection only — no static state:

```java
public DefaultHealthService(HealthValidator validator, HealthEvaluationEngine engine) {
    this.validator = Objects.requireNonNull(validator, "HealthValidator must not be null");
    this.engine = Objects.requireNonNull(engine, "HealthEvaluationEngine must not be null");
    this.components = new ConcurrentHashMap<>();
}
```

**Dependencies:**
- `HealthValidator` — validates components
- `HealthEvaluationEngine` — evaluates health

## Usage Examples

### Example 1: Creating the Service

```java
// Create dependencies
HealthValidator validator = new HealthValidator();
HealthEvaluationEngine engine = new DefaultHealthEvaluationEngine();

// Create service
DefaultHealthService healthService = new DefaultHealthService(validator, engine);
```

### Example 2: Registering Components

```java
// Create component
HealthComponentId componentId = new HealthComponentId("event-bus");
HealthComponent component = new HealthComponent(componentId, "Event Bus", "Infrastructure");

// Register component
boolean registered = healthService.register(component);

if (registered) {
    log.info("Component registered: {}", component.name());
}
```

### Example 3: Checking Health

```java
// Check specific component
HealthComponent component = new HealthComponent(componentId, "Event Bus", "Infrastructure");
Optional<HealthReport> report = healthService.check(component);

if (report.isPresent()) {
    HealthStatus status = report.get().status();
    log.info("Health status: {}", status);
} else {
    log.warn("Component not found");
}
```

### Example 4: Checking All Components

```java
// Check all registered components
Collection<HealthReport> reports = healthService.checkAll();

for (HealthReport report : reports) {
    log.info("Component: {}, Status: {}", 
        report.component().name(), 
        report.status());
}
```

### Example 5: Unregistering Components

```java
// Unregister component
boolean unregistered = healthService.unregister(component);

if (unregistered) {
    log.info("Component unregistered: {}", component.name());
}
```

### Example 6: Checking Existence

```java
// Check if component exists
boolean exists = healthService.exists(component);

if (exists) {
    log.info("Component is registered");
} else {
    log.info("Component is not registered");
}
```

## Error Handling

### InvalidHealthComponentException

Thrown when component validation fails:

```java
try {
    healthService.register(invalidComponent);
} catch (InvalidHealthComponentException e) {
    log.error("Invalid component: {}", e.getMessage());
    for (String error : e.error().details().values()) {
        log.error("  - {}", error);
    }
}
```

### HealthException (ALREADY_REGISTERED)

Thrown when attempting to register a duplicate:

```java
try {
    healthService.register(component);
    healthService.register(component); // Duplicate
} catch (HealthException e) {
    log.error("Registration failed: {}", e.getMessage());
}
```

### HealthCheckFailedException

Thrown when health evaluation fails:

```java
try {
    Optional<HealthReport> report = healthService.check(component);
    report.ifPresent(r -> log.info("Status: {}", r.status()));
} catch (HealthCheckFailedException e) {
    log.error("Health check failed: {}", e.getMessage());
}
```

## Thread Safety

The service is fully thread-safe:

- **ConcurrentHashMap** — thread-safe storage
- **Immutable models** — all models are immutable
- **No shared mutable state** — all state is in ConcurrentHashMap
- **Constructor injection** — no static state

## Design Principles

1. **Coordination Only** — Service coordinates, doesn't evaluate or validate
2. **Constructor Injection** — No static state, all dependencies injected
3. **Thread-Safe** — ConcurrentHashMap for storage
4. **Immutable Collections** — Returns unmodifiable collections
5. **Delegation** — Delegates validation and evaluation to specialized components
6. **Error Handling** — Uses structured HealthError for failures
7. **No Business Logic** — Pure coordination logic only

## Architectural Separation

```
┌─────────────────────────────────────────────────────────────┐
│                    Health Subsystem                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  DefaultHealthService                                       │
│  ────────────────────                                       │
│  • Coordinates                                              │
│  • Stores components                                        │
│  • Delegates validation                                     │
│  • Delegates evaluation                                     │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐ │
│  │ Validator    │  │ Engine       │  │ Error            │ │
│  │ ──────────── │  │ ──────────── │  │ ──────────────── │ │
│  │ • Validates  │  │ • Evaluates  │  │ • Reports        │ │
│  │ • Protects   │  │ • Pings      │  │ • Structures     │ │
│  │ • Enforces   │  │ • Monitors   │  │ • Throws         │ │
│  └──────────────┘  └──────────────┘  └──────────────────┘ │
│                                                             │
│  These responsibilities shall remain independent forever.   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Constitutional Authority

- ADD-PLT-202: Platform Language immutability requirements
- ADD-PLT-205: Domain model package structure
- ADD-PLT-206: Platform Language standards

## Ownership

**Platform Core** — Health Service Implementation is owned and maintained by the Platform Core team.

## Version

1.0