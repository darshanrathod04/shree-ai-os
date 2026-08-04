# Health Domain Models

## Architecture

```
platform.core.health.model
├── HealthComponentId.java    — Immutable health component identifier
├── HealthComponent.java      — Immutable health component
├── HealthStatus.java         — Enum of health states
├── HealthSeverity.java       — Enum of severity levels
├── HealthIndicator.java      — Immutable health observation
├── HealthMetrics.java        — Immutable health metrics
├── HealthCheck.java          — Immutable health check request
├── HealthReport.java         — Immutable health check result
├── package-info.java
└── README.md
```

## Model Descriptions

### HealthComponentId
Immutable value object representing a unique identifier for a health component.
- **Field:** `String value()`
- **Validation:** Non-null and non-blank
- **Usage:** Primary key for health component registration

### HealthComponent
Immutable value object representing a platform component for health monitoring.
- **Fields:**
  - `HealthComponentId id()` — Unique identifier
  - `String name()` — Human-readable name
  - `String category()` — Component category (e.g., "Kernel", "Service", "AI Provider")
- **Validation:** All fields non-null
- **Usage:** Represents kernels, infrastructure services, or platform components

### HealthStatus (Enum)
Represents the overall health state of a component.
- **Values:**
  - `HEALTHY` — Component is healthy and operating normally
  - `DEGRADED` — Component is degraded but still operational
  - `UNHEALTHY` — Component is unhealthy and not operating correctly
  - `UNKNOWN` — Component health is unknown or has not been checked

### HealthSeverity (Enum)
Represents the severity of health findings.
- **Values:**
  - `INFO` — Informational finding, no action required
  - `WARNING` — Warning finding, attention may be required
  - `ERROR` — Error finding, action is required
  - `CRITICAL` — Critical finding, immediate action is required

### HealthIndicator
Immutable value object representing a single health observation.
- **Fields:**
  - `String name()` — Indicator name (e.g., "CPU", "Memory", "Database")
  - `HealthStatus status()` — Health status of this indicator
  - `HealthSeverity severity()` — Severity level
  - `String message()` — Detailed message
- **Validation:** All fields non-null
- **Usage:** Granular health reporting for specific aspects of a component

### HealthMetrics
Immutable value object representing quantitative health metrics.
- **Fields:**
  - `double availability()` — Availability ratio (0.0 to 1.0)
  - `double responseTime()` — Response time in milliseconds
  - `double uptime()` — Uptime in seconds
  - `Map<String, Object> values()` — Additional metric values (unmodifiable)
- **Validation:** Values map non-null
- **Usage:** Pure model for quantitative health assessment, no calculations

### HealthCheck
Immutable value object representing a health check request.
- **Fields:**
  - `HealthComponent component()` — Component to check
  - `boolean deep()` — Whether to perform a deep health check
- **Validation:** Component non-null
- **Usage:** Request object for health check operations

### HealthReport
Immutable value object representing the complete result of a health check.
- **Fields:**
  - `HealthComponent component()` — Checked component
  - `HealthStatus status()` — Overall health status
  - `List<HealthIndicator> indicators()` — Detailed indicators (unmodifiable)
  - `HealthMetrics metrics()` — Health metrics
  - `Instant timestamp()` — Check timestamp
- **Validation:** All fields non-null, indicators list is unmodifiable
- **Usage:** Complete health check result returned by HealthService

## Future Extensions

The Health Domain Models are designed to support enterprise-grade observability for:

- **Runtime Kernel** — JVM health, thread pools, garbage collection
- **Memory Kernel** — Heap usage, memory leaks, allocation rates
- **Knowledge Kernel** — Graph database health, vector store status
- **Event Bus** — Message throughput, queue depth, consumer lag
- **Registry** — Service registration health, discovery latency
- **Discovery** — Service discovery health, cache hit rates
- **Lifecycle** — Component lifecycle state transitions
- **Configuration** — Configuration source health, reload times
- **AI Providers** — Ollama, OpenAI, Groq API health and latency
- **Vector Database** — Index health, query performance
- **SQL Database** — Connection pool health, query performance
- **Cache** — Hit rates, eviction rates, memory usage
- **Plugins** — Plugin health, dependency resolution
- **REST APIs** — Endpoint health, response times
- **Scheduler** — Job execution health, queue depth
- **External Services** — Third-party service health, timeout rates

## Design Principles

1. **Immutability** — All models are immutable value objects with final fields
2. **No Setters** — All fields set via constructor validation
3. **No Lombok** — Explicit constructors, getters, equals, hashCode, toString
4. **No Service Logic** — Models are pure data carriers
5. **No Validation Logic** — Validation belongs to the service layer
6. **No Persistence** — No ORM annotations
7. **No Spring** — Framework-agnostic
8. **No Records** — Uses explicit classes for compatibility
9. **Defensive Copying** — Collections are unmodifiable to preserve immutability
10. **Constructor Validation** — All invariants enforced at construction time

## Usage Example

```java
// Create a health component
HealthComponentId componentId = new HealthComponentId("event-bus");
HealthComponent component = new HealthComponent(componentId, "Event Bus", "Infrastructure");

// Create health indicators
HealthIndicator cpuIndicator = new HealthIndicator(
    "CPU",
    HealthStatus.HEALTHY,
    HealthSeverity.INFO,
    "CPU usage at 45%"
);

HealthIndicator memoryIndicator = new HealthIndicator(
    "Memory",
    HealthStatus.DEGRADED,
    HealthSeverity.WARNING,
    "Memory usage at 85%"
);

List<HealthIndicator> indicators = List.of(cpuIndicator, memoryIndicator);

// Create health metrics
Map<String, Object> values = Map.of("heapUsed", 1024, "threadCount", 42);
HealthMetrics metrics = new HealthMetrics(0.99, 12.5, 86400.0, values);

// Create health report
HealthReport report = new HealthReport(
    component,
    HealthStatus.DEGRADED,
    indicators,
    metrics,
    Instant.now()
);
```

## Constitutional Authority

- ADD-PLT-202: Platform Language immutability requirements
- ADD-PLT-205: Domain model package structure
- ADD-PLT-206: Platform Language standards

## Ownership

**Platform Core** — Health Domain Models are owned and maintained by the Platform Core team.

## Version

1.0