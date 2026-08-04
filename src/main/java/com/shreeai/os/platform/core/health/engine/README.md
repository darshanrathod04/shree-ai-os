# Health Evaluation Engine

## Architecture

```
platform.core.health.engine
├── HealthEvaluationEngine.java    — Stateless evaluation engine
├── EvaluationResult.java          — Immutable evaluation result
├── package-info.java
└── README.md
```

## Architectural Responsibility

The Health Evaluation Engine performs **runtime health evaluation** for platform components. It is the component that actually determines health status, generates indicators, and creates metrics.

### What the Engine Does

- ✅ Evaluates health of platform components
- ✅ Generates HealthReport with status, indicators, and metrics
- ✅ Returns EvaluationResult with success/failure status
- ✅ Performs actual health checks (CPU, Memory, JVM, etc.)
- ✅ Generates health indicators
- ✅ Calculates health metrics
- ✅ Throws HealthCheckFailedException on failures

### What the Engine Does NOT Do

- ❌ Validate models (that's HealthValidator's job)
- ❌ Coordinate operations (that's HealthService's job)
- ❌ Store components (that's HealthService's job)
- ❌ Own storage or access ConcurrentHashMap
- ❌ Register or unregister components
- ❌ Create threads or schedule jobs
- ❌ Publish events
- ❌ Access Spring or other frameworks
- ❌ Contain business logic

## Evaluation Flow

```
HealthComponent + HealthCheck
    │
    ▼
HealthEvaluationEngine.evaluate()
    │
    ├─→ Perform health evaluation
    │   ├─→ Check CPU
    │   ├─→ Check Memory
    │   ├─→ Check JVM
    │   ├─→ Check services
    │   └─→ ...
    │
    ├─→ Generate HealthStatus
    │
    ├─→ Create HealthIndicators
    │
    ├─→ Calculate HealthMetrics
    │
    └─→ Create HealthReport
    │
    ▼
EvaluationResult
    │
    ├─→ success → EvaluationResult.success(report)
    └─→ failure → throw HealthCheckFailedException
```

## EvaluationResult

Immutable result object with the following fields:

| Field | Type | Description |
|-------|------|-------------|
| success | boolean | Whether evaluation succeeded |
| report | HealthReport | Health report if successful, null if failed |
| failureMessage | String | Failure message if failed, null if successful |
| timestamp | Instant | When the evaluation occurred |

### Factory Methods

#### EvaluationResult.success(HealthReport)

Creates a successful evaluation result:

```java
EvaluationResult result = EvaluationResult.success(report);
```

#### EvaluationResult.failure(String)

Creates a failed evaluation result:

```java
EvaluationResult result = EvaluationResult.failure("Health check failed: Connection timeout");
```

## HealthEvaluationEngine

### evaluate(HealthComponent, HealthCheck)

Evaluates the health of a component and returns an EvaluationResult.

**Parameters:**
- `component` — The health component to evaluate (must not be null)
- `check` — The health check request (must not be null)

**Returns:**
- `EvaluationResult` — The evaluation result

**Throws:**
- `HealthCheckFailedException` — If the health check fails

**Example:**
```java
HealthEvaluationEngine engine = new HealthEvaluationEngine();

HealthComponent component = new HealthComponent(
    new HealthComponentId("event-bus"),
    "Event Bus",
    "Infrastructure"
);

HealthCheck check = new HealthCheck(component, false);

try {
    EvaluationResult result = engine.evaluate(component, check);
    
    if (result.success()) {
        HealthReport report = result.report();
        log.info("Health status: {}", report.status());
    } else {
        log.error("Evaluation failed: {}", result.failureMessage());
    }
} catch (HealthCheckFailedException e) {
    log.error("Health check failed: {}", e.getMessage());
}
```

## Current Sprint: Basic Evaluation

For this sprint (EIO-606), basic evaluation is implemented:

### Basic Evaluation Rules

1. **Health Status**: Always returns HEALTHY
2. **Indicator**: Creates a single "Basic Health" indicator
3. **Metrics**: Generates basic metrics with:
   - availability: 1.0 (100%)
   - responseTime: 0.0
   - uptime: 0.0
   - values: Map with deepCheck and evaluationType

### Example Output

```java
HealthReport {
    component=HealthComponent{id='event-bus', name='Event Bus', category='Infrastructure'},
    status=HEALTHY,
    indicators=[HealthIndicator{name='Basic Health', status=HEALTHY, severity=INFO, message='Basic health check completed successfully'}],
    metrics=HealthMetrics{availability=1.0, responseTime=0.0, uptime=0.0, values={deepCheck=false, evaluationType=basic}},
    timestamp=2024-01-15T10:30:00.123Z
}
```

## Future Sprint: Comprehensive Evaluation

Future sprints will add comprehensive evaluation for:

### Sprint EIO-607: Runtime Kernel
- CPU usage and load
- Memory usage and leaks
- Thread pool health
- Garbage collection metrics

### Sprint EIO-608: Infrastructure Services
- Database connectivity and performance
- Event Bus message throughput
- Registry service health
- Discovery service health

### Sprint EIO-609: AI Providers
- Ollama API health
- OpenAI API health
- Groq API health
- Model availability

### Sprint EIO-610: Data Stores
- Vector Database health
- SQL Database health
- Cache hit rates and memory
- Connection pool health

### Sprint EIO-611: Platform Components
- REST API endpoint health
- Plugin health
- Scheduler health
- External service health

## Usage Examples

### Example 1: Basic Evaluation

```java
// Create engine
HealthEvaluationEngine engine = new HealthEvaluationEngine();

// Create component
HealthComponentId id = new HealthComponentId("event-bus");
HealthComponent component = new HealthComponent(id, "Event Bus", "Infrastructure");

// Create check
HealthCheck check = new HealthCheck(component, false);

// Evaluate
EvaluationResult result = engine.evaluate(component, check);

if (result.success()) {
    HealthReport report = result.report();
    log.info("Component: {}, Status: {}", 
        report.component().name(), 
        report.status());
}
```

### Example 2: Deep Health Check

```java
// Create deep check
HealthCheck deepCheck = new HealthCheck(component, true);

// Evaluate with deep check
EvaluationResult result = engine.evaluate(component, deepCheck);

if (result.success()) {
    HealthReport report = result.report();
    boolean deepCheckPerformed = (boolean) report.metrics().values().get("deepCheck");
    log.info("Deep check performed: {}", deepCheckPerformed);
}
```

### Example 3: Handling Failures

```java
try {
    EvaluationResult result = engine.evaluate(component, check);
    
    if (result.success()) {
        // Process successful result
        HealthReport report = result.report();
        processReport(report);
    } else {
        // Handle failure
        log.error("Evaluation failed: {}", result.failureMessage());
    }
} catch (HealthCheckFailedException e) {
    // Handle exception
    log.error("Health check failed: {}", e.getMessage());
    HealthError error = e.error();
    log.error("Error code: {}", error.code());
    log.error("Details: {}", error.details());
}
```

### Example 4: Integration with HealthService

```java
// The service delegates to the engine
public Optional<HealthReport> check(HealthComponent component) {
    // Validate component
    var validationResult = validator.validateComponent(component);
    if (!validationResult.isValid()) {
        throw new InvalidHealthComponentException(component, ...);
    }
    
    // Lookup component
    if (!components.containsKey(component.id())) {
        return Optional.empty();
    }
    
    // Delegate to engine
    HealthCheck check = new HealthCheck(component, false);
    EvaluationResult result = engine.evaluate(component, check);
    
    // Return report
    if (result.success()) {
        return Optional.of(result.report());
    } else {
        throw new HealthCheckFailedException(component, result.failureMessage());
    }
}
```

## Design Principles

1. **Stateless** — No instance fields, all state passed as parameters
2. **No Storage** — Never owns or accesses ConcurrentHashMap
3. **No Validation** — Delegates to HealthValidator
4. **No Coordination** — Delegates to HealthService
5. **Pure Evaluation** — Only evaluates health, nothing else
6. **Immutability** — Returns immutable EvaluationResult
7. **Exception-Based Errors** — Throws HealthCheckFailedException on failures
8. **Framework-Agnostic** — No Spring or other framework dependencies

## Thread Safety

The engine is stateless and thread-safe:

- **No instance fields** — No mutable state
- **No shared state** — All state passed as parameters
- **Immutable results** — EvaluationResult is immutable
- **Safe for concurrent use** — Multiple threads can call evaluate() simultaneously

## Architectural Separation

```
┌─────────────────────────────────────────────────────────────┐
│                    Health Subsystem                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  HealthService          HealthEvaluationEngine              │
│  ──────────────────     ────────────────────────           │
│  • Coordinates          • Evaluates health                  │
│  • Stores components    • Generates reports                 │
│  • Delegates            • Performs checks                   │
│  • Validates            • Stateless                         │
│                                                             │
│  The service owns orchestration.                            │
│  The engine owns evaluation.                                 │
│  These responsibilities shall never overlap.                 │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Constitutional Authority

- ADD-PLT-202: Platform Language immutability requirements
- ADD-PLT-205: Domain model package structure
- ADD-PLT-206: Platform Language standards

## Ownership

**Platform Core** — Health Evaluation Engine is owned and maintained by the Platform Core team.

## Version

1.0