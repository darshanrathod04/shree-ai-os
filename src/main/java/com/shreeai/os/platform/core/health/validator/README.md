# Health Validation Layer

## Architecture

```
platform.core.health.validator
├── HealthValidator.java    — Stateless validation utility
├── package-info.java
└── README.md
```

## Architectural Responsibility

The Health Validator is a **stateless validation layer** that protects the Platform Language by ensuring all Health models meet structural requirements before being used by the Health Engine.

### What the Validator Does

- ✅ Validates HealthComponentId structure
- ✅ Validates HealthComponent structure
- ✅ Validates HealthIndicator structure
- ✅ Validates HealthMetrics structure
- ✅ Validates HealthCheck structure
- ✅ Validates HealthReport structure
- ✅ Returns ValidationResult with errors and warnings
- ✅ Enforces architectural invariants

### What the Validator Does NOT Do

- ❌ Evaluate health (that's the Health Engine's job)
- ❌ Ping services or monitor CPU
- ❌ Access Registry, Lifecycle, Event Bus, Configuration
- ❌ Access databases or external services
- ❌ Create threads or schedule tasks
- ❌ Cache results
- ❌ Throw exceptions for validation failures (returns ValidationResult instead)
- ❌ Mutate models
- ❌ Execute health checks

## Validation Flow

```
Health Model
    │
    ▼
HealthValidator
    │
    ▼
ValidationResult
    │
    ├── isValid() → true/false
    ├── errors() → List<String>
    └── warnings() → List<String>
    │
    ▼
Health Engine (consumes ValidationResult)
```

## Validation Rules

### HealthComponentId

| Field | Rule | Error Message |
|-------|------|---------------|
| componentId | Must not be null | "HealthComponentId must not be null" |
| value | Must not be null or blank | "HealthComponentId value must not be null or blank" |

### HealthComponent

| Field | Rule | Error Message |
|-------|------|---------------|
| component | Must not be null | "HealthComponent must not be null" |
| id | Must not be null | "HealthComponentId must not be null" |
| name | Must not be null or blank | "HealthComponent name must not be null or blank" |
| category | Must not be null or blank | "HealthComponent category must not be null or blank" |

### HealthIndicator

| Field | Rule | Error Message |
|-------|------|---------------|
| indicator | Must not be null | "HealthIndicator must not be null" |
| name | Must not be null or blank | "HealthIndicator name must not be null or blank" |
| status | Must not be null | "HealthIndicator status must not be null" |
| severity | Must not be null | "HealthIndicator severity must not be null" |
| message | Must not be null or blank | "HealthIndicator message must not be null or blank" |

### HealthMetrics

| Field | Rule | Error Message |
|-------|------|---------------|
| metrics | Must not be null | "HealthMetrics must not be null" |
| availability | Must be >= 0 | "HealthMetrics availability must be greater than or equal to 0" |
| responseTime | Must be >= 0 | "HealthMetrics responseTime must be greater than or equal to 0" |
| uptime | Must be >= 0 | "HealthMetrics uptime must be greater than or equal to 0" |
| values | Must not be null | "HealthMetrics values map must not be null" |

**Note:** The validator does not calculate metrics. It only validates that provided values meet structural requirements.

### HealthCheck

| Field | Rule | Error Message |
|-------|------|---------------|
| check | Must not be null | "HealthCheck must not be null" |
| component | Must not be null | "HealthCheck component must not be null" |

**Note:** The validator does not execute health checks. It only validates the structural integrity of the request.

### HealthReport

| Field | Rule | Error Message |
|-------|------|---------------|
| report | Must not be null | "HealthReport must not be null" |
| component | Must not be null | "HealthReport component must not be null" |
| status | Must not be null | "HealthReport status must not be null" |
| indicators | Must not be null | "HealthReport indicators list must not be null" |
| metrics | Must not be null | "HealthReport metrics must not be null" |
| timestamp | Must not be null | "HealthReport timestamp must not be null" |

**Note:** The validator does not inspect health logic. It only validates the structural integrity of the report.

## Usage Examples

### Example 1: Validate HealthComponentId

```java
HealthComponentId componentId = new HealthComponentId("event-bus");
ValidationResult result = HealthValidator.validateComponentId(componentId);

if (result.isValid()) {
    // ComponentId is valid, proceed with registration
} else {
    // Handle validation errors
    for (String error : result.errors()) {
        System.err.println("Validation error: " + error);
    }
}
```

### Example 2: Validate HealthComponent

```java
HealthComponentId componentId = new HealthComponentId("event-bus");
HealthComponent component = new HealthComponent(componentId, "Event Bus", "Infrastructure");

ValidationResult result = HealthValidator.validateComponent(component);

if (result.isValid()) {
    // Component is valid, proceed with health check
} else {
    // Handle validation errors
    result.errors().forEach(error -> log.error("Invalid component: {}", error));
}
```

### Example 3: Validate HealthIndicator

```java
HealthIndicator indicator = new HealthIndicator(
    "CPU",
    HealthStatus.HEALTHY,
    HealthSeverity.INFO,
    "CPU usage at 45%"
);

ValidationResult result = HealthValidator.validateIndicator(indicator);

if (result.isValid()) {
    // Indicator is valid, add to report
} else {
    // Handle validation errors
}
```

### Example 4: Validate HealthMetrics

```java
Map<String, Object> values = Map.of("heapUsed", 1024, "threadCount", 42);
HealthMetrics metrics = new HealthMetrics(0.99, 12.5, 86400.0, values);

ValidationResult result = HealthValidator.validateMetrics(metrics);

if (result.isValid()) {
    // Metrics are valid
} else {
    // Handle validation errors
    result.errors().forEach(error -> log.error("Invalid metrics: {}", error));
}
```

### Example 5: Validate HealthCheck

```java
HealthCheck check = new HealthCheck(component, true);
ValidationResult result = HealthValidator.validateCheck(check);

if (result.isValid()) {
    // Check is valid, execute health check
    Optional<HealthReport> report = healthService.check(check.component());
} else {
    // Handle validation errors
}
```

### Example 6: Validate HealthReport

```java
HealthReport report = new HealthReport(
    component,
    HealthStatus.HEALTHY,
    indicators,
    metrics,
    Instant.now()
);

ValidationResult result = HealthValidator.validateReport(report);

if (result.isValid()) {
    // Report is valid, process results
} else {
    // Handle validation errors
}
```

### Example 7: Batch Validation

```java
// Validate multiple models
ValidationResult componentResult = HealthValidator.validateComponent(component);
ValidationResult indicatorResult = HealthValidator.validateIndicator(indicator);
ValidationResult metricsResult = HealthValidator.validateMetrics(metrics);

// Combine results
ValidationResult combined = ValidationResult.builder()
    .addErrors(componentResult.errors())
    .addWarnings(componentResult.warnings())
    .addErrors(indicatorResult.errors())
    .addWarnings(indicatorResult.warnings())
    .addErrors(metricsResult.errors())
    .addWarnings(metricsResult.warnings())
    .build();

if (combined.isValid()) {
    // All models are valid
}
```

## Design Principles

1. **Stateless** — All state is passed as method parameters. No instance fields.
2. **Deterministic** — Same inputs always produce the same result.
3. **Thread-Safe** — No mutable shared state. Safe for concurrent use.
4. **No Exceptions** — Validation failures return ValidationResult, not exceptions.
5. **No Health Evaluation** — Never pings services, monitors CPU, or evaluates health.
6. **No External Access** — Never accesses Registry, Lifecycle, Event Bus, Configuration, or databases.
7. **No Business Logic** — Validation rules only. No calculations or transformations.
8. **No Model Mutation** — Models are never modified during validation.

## Architectural Separation

```
┌─────────────────────────────────────────────────────────────┐
│                    Health Subsystem                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  HealthValidator          Health Engine        HealthService │
│  ────────────────        ──────────────        ──────────── │
│  • Validates structure    • Evaluates health    • Coordinates│
│  • Enforces invariants    • Pings services      • Orchestrates│
│  • Returns ValidationResult • Monitors CPU     • Manages lifecycle│
│  • Never evaluates health • Calculates metrics  • Exposes API │
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

**Platform Core** — Health Validation Layer is owned and maintained by the Platform Core team.

## Version

1.0