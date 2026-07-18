package platform.kernels.memory.model;

/**
 * <b>MemoryType</b>
 *
 * <p>Defines the type of a Memory within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a stable enumeration of Memory types.</li>
 *   <li>Enables type-safe classification of memories.</li>
 *   <li>Supports platform-wide memory categorization.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Memory Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is a pure enumeration with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-201</p>
 */
public enum MemoryType {
    /** Memories related to specific events or experiences */
    EPISODIC,
    /** General knowledge and facts */
    SEMANTIC,
    /** Step-by-step procedures and workflows */
    PROCEDURAL,
    /** Temporary active memory for current tasks */
    WORKING,
    /** Verified factual information */
    FACT,
    /** Document-based memories */
    DOCUMENT,
    /** Goal and objective tracking */
    GOAL,
    /** Conversation history */
    CONVERSATION,
    /** Observations from the environment */
    OBSERVATION,
    /** System-level memories */
    SYSTEM
}