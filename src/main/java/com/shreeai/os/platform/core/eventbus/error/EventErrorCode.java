package com.shreeai.os.platform.core.eventbus.error;

/**
 * <b>EventErrorCode</b>
 *
 * <p>Standardized error codes for Event Bus operations within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides a typed enumeration of all possible Event Bus error conditions.</li>
 *   <li>Enables consistent error reporting across the Event Bus subsystem.</li>
 *   <li>Supports the EventBusException hierarchy.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see EventError
 * @see EventBusException
 */
public enum EventErrorCode {

    /**
     * An event failed structural validation.
     */
    EVENT_INVALID,

    /**
     * Event validation failed due to missing or invalid fields.
     */
    EVENT_VALIDATION_FAILED,

    /**
     * No subscribers found for the event topic.
     */
    EVENT_NO_SUBSCRIBERS,

    /**
     * Event dispatch failed due to an unexpected error.
     */
    EVENT_DISPATCH_FAILED,

    /**
     * The event topic was not found in the registry.
     */
    EVENT_TOPIC_NOT_FOUND,

    /**
     * A subscriber failed to process the event.
     */
    EVENT_SUBSCRIBER_FAILED,

    /**
     * Event publishing failed due to an unexpected error.
     */
    EVENT_PUBLISH_FAILED
}