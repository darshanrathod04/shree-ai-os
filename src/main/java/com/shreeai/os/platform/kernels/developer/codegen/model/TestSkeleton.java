package com.shreeai.os.platform.kernels.developer.codegen.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>TestSkeleton</b> — deterministic, ready-to-fill test class description
 * produced by {@link com.shreeai.os.platform.kernels.developer.codegen.TestSkeletonGenerator}.
 *
 * <p>A {@code TestSkeleton} carries only structural information:
 * the target test class FQN, the framework flavor (JUnit 5 / Spring Boot Test
 * / MockMvc / Mockito), and an ordered list of method signatures to be
 * included in the generated class. The actual source code is produced
 * by the {@link com.shreeai.os.platform.kernels.developer.codegen.JavaCodeGenerator}.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-15)</p>
 *
 * @since Sprint-15
 */
public final class TestSkeleton {

    /** Test framework to assume. */
    public enum Framework {
        JUNIT5,             // pure JUnit 5
        SPRING_BOOT_TEST,   // @SpringBootTest with MockMvc
        MOCKMVC,            // @WebMvcTest with MockMvc
        MOCKITO             // JUnit 5 + Mockito
    }

    public enum Category { UNIT, INTEGRATION, SECURITY, WEB }

    private final String classUnderTest;   // FQN of the class being tested
    private final String testClassName;    // e.g. "UserServiceTest"
    private final String testClassFqn;     // FQN of the generated test class
    private final String testFilePath;     // source file path
    private final Framework framework;
    private final Category category;
    private final List<String> methodSignatures; // e.g. ["shouldCreateUser()", ...]
    private final List<String> imports;          // imports to include

    private TestSkeleton(Builder b) {
        this.classUnderTest = Objects.requireNonNull(b.classUnderTest, "classUnderTest");
        this.testClassName = Objects.requireNonNull(b.testClassName, "testClassName");
        this.testClassFqn = b.testClassFqn == null
                ? classUnderTest + "Test" : b.testClassFqn;
        this.testFilePath = b.testFilePath == null ? "" : b.testFilePath;
        this.framework = b.framework == null ? Framework.JUNIT5 : b.framework;
        this.category = b.category == null ? Category.UNIT : b.category;
        this.methodSignatures = List.copyOf(b.methodSignatures == null ? List.of() : b.methodSignatures);
        this.imports = List.copyOf(b.imports == null ? List.of() : b.imports);
    }

    public String classUnderTest() { return classUnderTest; }
    public String testClassName() { return testClassName; }
    public String testClassFqn() { return testClassFqn; }
    public String testFilePath() { return testFilePath; }
    public Framework framework() { return framework; }
    public Category category() { return category; }
    public List<String> methodSignatures() { return methodSignatures; }
    public List<String> imports() { return imports; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String classUnderTest;
        private String testClassName;
        private String testClassFqn;
        private String testFilePath;
        private Framework framework;
        private Category category;
        private List<String> methodSignatures;
        private List<String> imports;

        public Builder classUnderTest(String v) { this.classUnderTest = v; return this; }
        public Builder testClassName(String v) { this.testClassName = v; return this; }
        public Builder testClassFqn(String v) { this.testClassFqn = v; return this; }
        public Builder testFilePath(String v) { this.testFilePath = v; return this; }
        public Builder framework(Framework v) { this.framework = v; return this; }
        public Builder category(Category v) { this.category = v; return this; }
        public Builder methodSignatures(List<String> v) { this.methodSignatures = v; return this; }
        public Builder imports(List<String> v) { this.imports = v; return this; }
        public Builder addMethod(String signature) {
            if (this.methodSignatures == null) this.methodSignatures = new java.util.ArrayList<>();
            this.methodSignatures.add(signature);
            return this;
        }
        public Builder addImport(String imp) {
            if (this.imports == null) this.imports = new java.util.ArrayList<>();
            this.imports.add(imp);
            return this;
        }

        public TestSkeleton build() { return new TestSkeleton(this); }
    }
}
