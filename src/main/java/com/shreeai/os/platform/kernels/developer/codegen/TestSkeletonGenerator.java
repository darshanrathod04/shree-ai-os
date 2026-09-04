package com.shreeai.os.platform.kernels.developer.codegen;

import com.shreeai.os.platform.kernels.developer.analyzer.ImpactReport;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntentType;
import com.shreeai.os.platform.kernels.developer.codegen.model.PatchPlan;
import com.shreeai.os.platform.kernels.developer.codegen.model.TestSkeleton;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;

import java.util.*;

/**
 * <b>TestSkeletonGenerator</b> — deterministic test skeleton descriptor generator.
 *
 * <p>For each intent, produces an ordered list of {@link TestSkeleton} descriptors
 * that describe the test classes to be generated. The actual source code is
 * produced by {@link JavaCodeGenerator} using these descriptors.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-15)</p>
 *
 * @since Sprint-15
 */
public final class TestSkeletonGenerator {

    private static final String DEFAULT_TEST_PACKAGE = "com.example";

    /**
     * Generates test skeleton descriptors for the given intent and patch plan.
     */
    public List<TestSkeleton> generate(DeveloperIntent intent, PatchPlan plan,
                                        List<ProjectClass> allClasses) {
        Objects.requireNonNull(intent, "intent");
        List<TestSkeleton> skeletons = new ArrayList<>();

        // Generate skeleton for the primary entity
        String entityName = intent.entity().isEmpty() ? "Service" : capitalize(intent.entity());

        switch (intent.intent()) {
            case SECURITY -> {
                skeletons.add(buildSecurityTest(entityName));
                skeletons.add(buildJwtServiceTest(entityName));
            }
            case ADD_FEATURE -> {
                skeletons.add(buildUnitTest(entityName + "Service"));
                skeletons.add(buildControllerTest(entityName + "Controller"));
            }
            case CREATE_API -> {
                skeletons.add(buildApiTest(entityName + "Controller"));
                skeletons.add(buildUnitTest(entityName + "Service"));
            }
            case ADD_ENTITY -> {
                skeletons.add(buildEntityRepositoryTest(entityName));
                skeletons.add(buildUnitTest(entityName + "Service"));
                skeletons.add(buildControllerTest(entityName + "Controller"));
            }
            case REFACTOR, FIX_BUG, OPTIMIZE -> {
                skeletons.add(buildUnitTest(entityName));
            }
            case DATABASE -> {
                skeletons.add(buildRepositoryIntegrationTest(entityName));
            }
        }

        // Add skeletons for all affected classes
        if (plan != null) {
            for (String targetFile : plan.newFiles()) {
                String className = extractClassName(targetFile);
                if (!className.isEmpty() && skeletons.stream()
                        .noneMatch(s -> s.testClassName().equals(className + "Test"))) {
                    skeletons.add(buildUnitTest(className));
                }
            }
        }

        return skeletons;
    }

    // ─── Individual skeleton builders ──────────────────────────────────────────

    private TestSkeleton buildUnitTest(String classUnderTest) {
        String testClassName = classUnderTest + "Test";
        String testPkg = DEFAULT_TEST_PACKAGE + ".test";
        String testFilePath = testPkg.replace('.', '/') + "/" + testClassName + ".java";
        return TestSkeleton.builder()
                .classUnderTest(classUnderTest)
                .testClassName(testClassName)
                .testFilePath(testFilePath)
                .framework(TestSkeleton.Framework.JUNIT5)
                .category(TestSkeleton.Category.UNIT)
                .methodSignatures(buildUnitTestMethods(classUnderTest))
                .addImport("org.junit.jupiter.api.Test")
                .addImport("org.junit.jupiter.api.BeforeEach")
                .addImport("org.junit.jupiter.api.DisplayName")
                .build();
    }

    private TestSkeleton buildControllerTest(String classUnderTest) {
        String testClassName = classUnderTest + "Test";
        String testFilePath = DEFAULT_TEST_PACKAGE.replace('.', '/') + "/test/" + testClassName + ".java";
        return TestSkeleton.builder()
                .classUnderTest(classUnderTest)
                .testClassName(testClassName)
                .testFilePath(testFilePath)
                .framework(TestSkeleton.Framework.MOCKMVC)
                .category(TestSkeleton.Category.WEB)
                .methodSignatures(buildWebTestMethods())
                .addImport("org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest")
                .addImport("org.springframework.test.web.servlet.MockMvc")
                .addImport("org.springframework.beans.factory.annotation.Autowired")
                .addImport("com.fasterxml.jackson.databind.ObjectMapper")
                .build();
    }

    private TestSkeleton buildSecurityTest(String entity) {
        String testClassName = entity + "SecurityTest";
        String testFilePath = DEFAULT_TEST_PACKAGE.replace('.', '/') + "/test/security/" + testClassName + ".java";
        return TestSkeleton.builder()
                .classUnderTest(entity)
                .testClassName(testClassName)
                .testFilePath(testFilePath)
                .framework(TestSkeleton.Framework.SPRING_BOOT_TEST)
                .category(TestSkeleton.Category.SECURITY)
                .methodSignatures(buildSecurityTestMethods())
                .addImport("org.springframework.boot.test.context.SpringBootTest")
                .addImport("org.springframework.security.test.context.support.WithMockUser")
                .addImport("org.junit.jupiter.api.Test")
                .build();
    }

    private TestSkeleton buildJwtServiceTest(String entity) {
        String testClassName = entity + "JwtServiceTest";
        String testFilePath = DEFAULT_TEST_PACKAGE.replace('.', '/') + "/test/security/" + testClassName + ".java";
        return TestSkeleton.builder()
                .classUnderTest(entity + "JwtService")
                .testClassName(testClassName)
                .testFilePath(testFilePath)
                .framework(TestSkeleton.Framework.MOCKITO)
                .category(TestSkeleton.Category.SECURITY)
                .methodSignatures(List.of(
                        "shouldGenerateValidToken()",
                        "shouldRejectExpiredToken()",
                        "shouldExtractClaimsFromToken()"
                ))
                .addImport("org.mockito.Mock")
                .addImport("org.mockito.InjectMocks")
                .addImport("org.junit.jupiter.api.BeforeEach")
                .build();
    }

    private TestSkeleton buildApiTest(String controllerClass) {
        String testClassName = controllerClass + "ApiTest";
        String testFilePath = DEFAULT_TEST_PACKAGE.replace('.', '/') + "/test/api/" + testClassName + ".java";
        return TestSkeleton.builder()
                .classUnderTest(controllerClass)
                .testClassName(testClassName)
                .testFilePath(testFilePath)
                .framework(TestSkeleton.Framework.MOCKMVC)
                .category(TestSkeleton.Category.WEB)
                .methodSignatures(buildApiTestMethods())
                .addImport("org.springframework.test.web.servlet.request.MockMvcRequestBuilders")
                .addImport("org.springframework.test.web.servlet.result.MockMvcResultMatchers")
                .addImport("com.fasterxml.jackson.databind.ObjectMapper")
                .build();
    }

    private TestSkeleton buildEntityRepositoryTest(String entity) {
        String testClassName = entity + "RepositoryTest";
        String testFilePath = DEFAULT_TEST_PACKAGE.replace('.', '/') + "/test/repository/" + testClassName + ".java";
        return TestSkeleton.builder()
                .classUnderTest(entity + "Repository")
                .testClassName(testClassName)
                .testFilePath(testFilePath)
                .framework(TestSkeleton.Framework.SPRING_BOOT_TEST)
                .category(TestSkeleton.Category.INTEGRATION)
                .methodSignatures(List.of(
                        "shouldSaveEntity()",
                        "shouldFindById()",
                        "shouldDeleteById()"
                ))
                .addImport("org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest")
                .addImport("jakarta.persistence.EntityManager")
                .build();
    }

    private TestSkeleton buildRepositoryIntegrationTest(String entity) {
        String testClassName = entity + "RepositoryIntegrationTest";
        String testFilePath = DEFAULT_TEST_PACKAGE.replace('.', '/') + "/test/repository/" + testClassName + ".java";
        return TestSkeleton.builder()
                .classUnderTest(entity + "Repository")
                .testClassName(testClassName)
                .testFilePath(testFilePath)
                .framework(TestSkeleton.Framework.SPRING_BOOT_TEST)
                .category(TestSkeleton.Category.INTEGRATION)
                .methodSignatures(List.of(
                        "shouldPersistAndRetrieve()",
                        "shouldUpdateExisting()",
                        "shouldDeleteById()"
                ))
                .addImport("org.springframework.boot.test.context.SpringBootTest")
                .addImport("jakarta.transaction.Transactional")
                .build();
    }

    // ─── Method list builders ──────────────────────────────────────────────────

    private List<String> buildUnitTestMethods(String className) {
        String base = uncapitalize(className);
        return List.of(
                "shouldInitialize" + className + "()",
                "shouldHandleValidInput()",
                "shouldRejectInvalidInput()",
                "shouldReturnExpectedResult()"
        );
    }

    private List<String> buildWebTestMethods() {
        return List.of(
                "shouldGetAllResources()",
                "shouldGetResourceById()",
                "shouldCreateResource()",
                "shouldReturn404ForMissingResource()"
        );
    }

    private List<String> buildSecurityTestMethods() {
        return List.of(
                "shouldAllowAuthenticatedUser()",
                "shouldRejectUnauthenticatedRequest()",
                "shouldEnforceRoleBasedAccess()"
        );
    }

    private List<String> buildApiTestMethods() {
        return List.of(
                "shouldGetAll()",
                "shouldGetById(Long id)",
                "shouldCreate(Object dto)",
                "shouldUpdate(Long id, Object dto)",
                "shouldDelete(Long id)",
                "shouldReturn400ForInvalidInput()"
        );
    }

    // ─── Utilities ─────────────────────────────────────────────────────────────

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String uncapitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private String extractClassName(String filePath) {
        if (filePath == null || filePath.isEmpty()) return "";
        String name = filePath;
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) name = name.substring(lastSlash + 1);
        int dot = name.lastIndexOf('.');
        if (dot > 0) name = name.substring(0, dot);
        return name;
    }
}
