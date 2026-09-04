package com.shreeai.os.platform.runtime.storage;

/**
 * <b>StorageRuntimeException</b>
 *
 * <p>Runtime failure raised by storage adapters (knowledge graph store,
 * embedding repository). Provider-specific failures are translated to this
 * type at the adapter boundary.</p>
 *
 * <p><b>Ownership:</b> Runtime — Storage</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public class StorageRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public StorageRuntimeException(String message) {
        super(message);
    }

    public StorageRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
