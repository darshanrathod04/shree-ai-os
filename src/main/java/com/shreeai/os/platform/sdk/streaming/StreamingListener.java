package com.shreeai.os.platform.sdk.streaming;

/**
 * <b>StreamingListener</b>
 *
 * <p>Developer-facing callback interface for receiving streamed responses
 * from Shree AI OS.</p>
 *
 * <p>This is the canonical streaming contract used by the SDK. The Runtime
 * remains independent of the transport mechanism and may later provide true
 * token-level streaming.</p>
 *
 * <p><b>Lifecycle:</b></p>
 * <pre>
 * onStart()
 * onToken(...)
 * onToken(...)
 * ...
 * onComplete(...)
 * </pre>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface StreamingListener {

    /**
     * Called once before streaming begins.
     */
    default void onStart() {
    }

    /**
     * Receives the next streamed token or text chunk.
     *
     * @param token incremental output
     */
    void onToken(String token);

    /**
     * Called once when streaming finishes successfully.
     *
     * @param completeResponse complete accumulated response
     */
    default void onComplete(String completeResponse) {
    }

    /**
     * Called if streaming fails.
     *
     * @param throwable failure cause
     */
    default void onError(Throwable throwable) {
    }
}