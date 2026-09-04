package com.shreeai.os.platform.runtime.vector;

/**
 * <b>VectorRuntimeException</b>
 *
 * <p>Runtime failure raised by the vector subsystem (store or search engine).
 * All provider-specific failures are translated to this type at the adapter
 * boundary so kernels never see persistence technology.</p>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public class VectorRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public VectorRuntimeException(String message) {
        super(message);
    }

    public VectorRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
