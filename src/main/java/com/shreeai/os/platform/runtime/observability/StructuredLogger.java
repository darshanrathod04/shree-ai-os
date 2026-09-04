package com.shreeai.os.platform.runtime.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * <b>StructuredLogger</b>
 *
 * <p>Thin wrapper around SLF4J that provides structured key-value logging
 * with automatic MDC (Mapped Diagnostic Context) management for correlation
 * IDs, capability tracking, and execution lifecycle events.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Ensures every log line carries a correlation ID for distributed tracing.</li>
 *   <li>Provides structured KV output: {@code key1=value1 key2=value2 message}.</li>
 *   <li>Manages MDC lifecycle (put/clear) automatically.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel — Observability</p>
 * <p><b>Version:</b> 2.2</p>
 *
 * @since 2.2
 */
public final class StructuredLogger {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String CAPABILITY_KEY = "capability";
    private static final String EXECUTION_ID_KEY = "executionId";
    private static final String PHASE_KEY = "phase";

    private final Logger delegate;

    private StructuredLogger(Logger delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
    }

    /**
     * Creates a structured logger for the given class.
     *
     * @param clazz the owning class
     * @return a new structured logger
     */
    public static StructuredLogger of(Class<?> clazz) {
        return new StructuredLogger(LoggerFactory.getLogger(clazz));
    }

    /**
     * Creates a structured logger with the given name.
     *
     * @param name the logger name
     * @return a new structured logger
     */
    public static StructuredLogger of(String name) {
        return new StructuredLogger(LoggerFactory.getLogger(name));
    }

    // ==========================================================
    // Correlation ID Management
    // ==========================================================

    /**
     * Sets the correlation ID in MDC for the current thread.
     *
     * @param correlationId the correlation ID (generated if null)
     * @return this logger for chaining
     */
    public StructuredLogger withCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID_KEY, correlationId != null ? correlationId : generateId());
        return this;
    }

    /**
     * Sets the execution ID in MDC.
     *
     * @param executionId the execution ID
     * @return this logger for chaining
     */
    public StructuredLogger withExecutionId(String executionId) {
        if (executionId != null) {
            MDC.put(EXECUTION_ID_KEY, executionId);
        }
        return this;
    }

    /**
     * Sets the capability in MDC.
     *
     * @param capability the capability name
     * @return this logger for chaining
     */
    public StructuredLogger withCapability(String capability) {
        if (capability != null) {
            MDC.put(CAPABILITY_KEY, capability);
        }
        return this;
    }

    /**
     * Sets the execution phase in MDC.
     *
     * @param phase the current phase
     * @return this logger for chaining
     */
    public StructuredLogger withPhase(String phase) {
        if (phase != null) {
            MDC.put(PHASE_KEY, phase);
        }
        return this;
    }

    /**
     * Clears all MDC keys set by this logger.
     */
    public void clearContext() {
        MDC.remove(CORRELATION_ID_KEY);
        MDC.remove(CAPABILITY_KEY);
        MDC.remove(EXECUTION_ID_KEY);
        MDC.remove(PHASE_KEY);
    }

    // ==========================================================
    // Structured Logging Methods
    // ==========================================================

    /**
     * Logs an INFO message with structured key-value pairs.
     *
     * @param message the log message
     * @param keyValue pairs of key-value (must be even number of args)
     */
    public void info(String message, String... keyValue) {
        logWithKV("INFO", message, keyValue);
        delegate.info(appendKV(message, keyValue));
    }

    /**
     * Logs a WARN message with structured key-value pairs.
     *
     * @param message the log message
     * @param keyValue pairs of key-value (must be even number of args)
     */
    public void warn(String message, String... keyValue) {
        delegate.warn(appendKV(message, keyValue));
    }

    /**
     * Logs an ERROR message with structured key-value pairs.
     *
     * @param message the log message
     * @param keyValue pairs of key-value (must be even number of args)
     */
    public void error(String message, String... keyValue) {
        delegate.error(appendKV(message, keyValue));
    }

    /**
     * Logs a DEBUG message with structured key-value pairs.
     *
     * @param message the log message
     * @param keyValue pairs of key-value (must be even number of args)
     */
    public void debug(String message, String... keyValue) {
        if (delegate.isDebugEnabled()) {
            delegate.debug(appendKV(message, keyValue));
        }
    }

    /**
     * Logs an execution lifecycle event (start, complete, fail).
     *
     * @param event      the lifecycle event name
     * @param capability the capability being executed
     * @param durationMs the duration in milliseconds (negative if not applicable)
     * @param keyValue   additional key-value pairs
     */
    public void executionEvent(String event, String capability, long durationMs, String... keyValue) {
        StringBuilder sb = new StringBuilder("event=").append(event);
        sb.append(" capability=").append(capability);
        if (durationMs >= 0) {
            sb.append(" durationMs=").append(durationMs);
        }
        if (keyValue != null && keyValue.length > 0) {
            sb.append(" ");
            appendKV(sb, keyValue);
        }
        MDC.put("logEventType", event);
        delegate.info(sb.toString());
        MDC.remove("logEventType");
    }

    // ==========================================================
    // Internal Helpers
    // ==========================================================

    private String appendKV(String message, String... keyValue) {
        if (keyValue == null || keyValue.length == 0) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message);
        sb.append(" ");
        appendKV(sb, keyValue);
        return sb.toString().trim();
    }

    private void appendKV(StringBuilder sb, String... keyValue) {
        if (keyValue == null) return;
        for (int i = 0; i + 1 < keyValue.length; i += 2) {
            sb.append(keyValue[i]).append("=").append(keyValue[i + 1]).append(" ");
        }
    }

    private void logWithKV(String level, String message, String... keyValue) {
        // KV already appended in appendKV, this hook allows future structured JSON appenders
    }

    private static String generateId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
