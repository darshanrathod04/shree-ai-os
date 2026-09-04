package com.shreeai.os.platform.kernels.developer.codegen.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <b>FilePatch</b> — a single, fully-described modification of one source file.
 *
 * <p>A {@code FilePatch} is a structured description of what to add or change
 * inside a single Java source file. It contains an ordered list of
 * {@link Operation} entries, each of which carries its own operation type,
 * source code, reason, and the FQNs of the classes it depends on.</p>
 *
 * <p>A patch is <b>never</b> applied to disk. The final apply step is always
 * left under developer control.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-15)</p>
 *
 * @since Sprint-15
 */
public final class FilePatch {

    private final String targetFile;        // e.g. "com/example/security/JwtFilter.java"
    private final String targetClass;       // FQN of the class being modified, may be null for new files
    private final boolean newFile;          // true if the file does not exist yet
    private final List<Operation> operations;
    private final List<String> dependencies; // FQNs of classes this patch depends on
    private final String reason;            // human-readable reason for the patch

    private FilePatch(Builder b) {
        this.targetFile = Objects.requireNonNull(b.targetFile, "targetFile");
        this.targetClass = b.targetClass;
        this.newFile = b.newFile;
        this.operations = List.copyOf(b.operations == null ? List.of() : b.operations);
        this.dependencies = List.copyOf(b.dependencies == null ? List.of() : b.dependencies);
        this.reason = b.reason == null ? "" : b.reason;
    }

    public String targetFile() { return targetFile; }
    public String targetClass() { return targetClass; }
    public boolean isNewFile() { return newFile; }
    public List<Operation> operations() { return operations; }
    public List<String> dependencies() { return dependencies; }
    public String reason() { return reason; }

    public static Builder builder() { return new Builder(); }

    /**
     * <b>Operation</b> — a single concrete code mutation inside a {@link FilePatch}.
     */
    public static final class Operation {
        private final PatchOperation kind;
        private final String signature;   // e.g. "JwtTokenService.createToken(String)"
        private final String code;        // generated source code for this operation
        private final String reason;
        private final List<String> dependencies;

        public Operation(PatchOperation kind,
                         String signature,
                         String code,
                         String reason,
                         List<String> dependencies) {
            this.kind = Objects.requireNonNull(kind, "kind");
            this.signature = signature == null ? "" : signature;
            this.code = code == null ? "" : code;
            this.reason = reason == null ? "" : reason;
            this.dependencies = List.copyOf(dependencies == null ? List.of() : dependencies);
        }

        public PatchOperation kind() { return kind; }
        public String signature() { return signature; }
        public String code() { return code; }
        public String reason() { return reason; }
        public List<String> dependencies() { return dependencies; }
    }

    public static final class Builder {
        private String targetFile;
        private String targetClass;
        private boolean newFile;
        private List<Operation> operations;
        private List<String> dependencies;
        private String reason;

        public Builder targetFile(String v) { this.targetFile = v; return this; }
        public Builder targetClass(String v) { this.targetClass = v; return this; }
        public Builder newFile(boolean v) { this.newFile = v; return this; }
        public Builder operations(List<Operation> v) { this.operations = v; return this; }
        public Builder dependencies(List<String> v) { this.dependencies = v; return this; }
        public Builder reason(String v) { this.reason = v; return this; }

        public Builder addOperation(Operation op) {
            if (this.operations == null) this.operations = new ArrayList<>();
            this.operations.add(op);
            return this;
        }

        public Builder addDependency(String fqn) {
            if (this.dependencies == null) this.dependencies = new ArrayList<>();
            this.dependencies.add(fqn);
            return this;
        }

        public FilePatch build() { return new FilePatch(this); }
    }
}
