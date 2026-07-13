package platform.core.eventbus.engine;

import platform.core.eventbus.model.Event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DispatchResult</b>
 *
 * <p>Represents the outcome of an event dispatch operation within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Records the outcome of event delivery to subscribers.</li>
 *   <li>Collects success/failure statistics and failure messages.</li>
 *   <li>Provides an immutable snapshot of the dispatch operation.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> All fields are non-null. Failure messages list may be empty but never null.</p>
 */
public final class DispatchResult {

    private final boolean success;
    private final Event event;
    private final int subscribersAttempted;
    private final int subscribersSucceeded;
    private final int subscribersFailed;
    private final List<String> failureMessages;
    private final Instant timestamp;

    /**
     * Constructs a new {@code DispatchResult} with the given parameters.
     *
     * @param success              whether the dispatch succeeded overall
     * @param event                the event that was dispatched (must not be null)
     * @param subscribersAttempted the number of subscribers attempted
     * @param subscribersSucceeded the number of subscribers that succeeded
     * @param subscribersFailed     the number of subscribers that failed
     * @param failureMessages      list of failure messages (must not be null)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public DispatchResult(boolean success,
                          Event event,
                          int subscribersAttempted,
                          int subscribersSucceeded,
                          int subscribersFailed,
                          List<String> failureMessages) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        if (failureMessages == null) {
            throw new IllegalArgumentException("Failure messages must not be null");
        }
        if (subscribersAttempted < 0) {
            throw new IllegalArgumentException("Subscribers attempted must not be negative");
        }
        if (subscribersSucceeded < 0) {
            throw new IllegalArgumentException("Subscribers succeeded must not be negative");
        }
        if (subscribersFailed < 0) {
            throw new IllegalArgumentException("Subscribers failed must not be negative");
        }
        if (subscribersAttempted != subscribersSucceeded + subscribersFailed) {
            throw new IllegalArgumentException("Subscribers attempted must equal succeeded + failed");
        }

        this.success = success;
        this.event = event;
        this.subscribersAttempted = subscribersAttempted;
        this.subscribersSucceeded = subscribersSucceeded;
        this.subscribersFailed = subscribersFailed;
        this.failureMessages = Collections.unmodifiableList(new ArrayList<>(failureMessages));
        this.timestamp = Instant.now();
    }

    /**
     * Returns whether the dispatch succeeded overall.
     *
     * <p>A dispatch is considered successful if all subscribers processed the event
     * without errors. Individual subscriber failures do not necessarily mean the
     * overall dispatch failed.</p>
     *
     * @return {@code true} if the dispatch succeeded, {@code false} otherwise
     */
    public boolean success() {
        return success;
    }

    /**
     * Returns the event that was dispatched.
     *
     * @return the event
     */
    public Event event() {
        return event;
    }

    /**
     * Returns the number of subscribers that were attempted.
     *
     * @return the number of subscribers attempted
     */
    public int subscribersAttempted() {
        return subscribersAttempted;
    }

    /**
     * Returns the number of subscribers that succeeded.
     *
     * @return the number of subscribers that succeeded
     */
    public int subscribersSucceeded() {
        return subscribersSucceeded;
    }

    /**
     * Returns the number of subscribers that failed.
     *
     * @return the number of subscribers that failed
     */
    public int subscribersFailed() {
        return subscribersFailed;
    }

    /**
     * Returns an unmodifiable list of failure messages.
     *
     * @return the list of failure messages (empty if no failures)
     */
    public List<String> failureMessages() {
        return failureMessages;
    }

    /**
     * Returns the timestamp when the dispatch result was created.
     *
     * @return the timestamp
     */
    public Instant timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispatchResult that = (DispatchResult) o;
        return success == that.success
                && subscribersAttempted == that.subscribersAttempted
                && subscribersSucceeded == that.subscribersSucceeded
                && subscribersFailed == that.subscribersFailed
                && event.equals(that.event)
                && failureMessages.equals(that.failureMessages)
                && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, event, subscribersAttempted, subscribersSucceeded, subscribersFailed, failureMessages, timestamp);
    }

    @Override
    public String toString() {
        return "DispatchResult{"
                + "success=" + success
                + ", event=" + event
                + ", subscribersAttempted=" + subscribersAttempted
                + ", subscribersSucceeded=" + subscribersSucceeded
                + ", subscribersFailed=" + subscribersFailed
                + ", failureMessages=" + failureMessages
                + ", timestamp=" + timestamp
                + '}';
    }
}