package com.shreeai.os.platform.kernels.developer.analyzer;

import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntentType;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectEndpoint;
import com.shreeai.os.platform.kernels.project.model.ProjectEntity;

import java.util.*;

/**
 * <b>TestStrategyGenerator</b>
 *
 * <p>Generates a deterministic testing checklist based on the developer intent,
 * impact report, and implementation plan. No code is generated — only a
 * structured testing strategy that guides manual or tool-assisted test creation.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-14)</p>
 *
 * @since Sprint-14
 */
public final class TestStrategyGenerator {

    public static final class TestStrategy {
        private final String request;
        private final List<String> unitTests;
        private final List<String> integrationTests;
        private final List<String> securityTests;
        private final List<String> apiTests;
        private final List<String> regressionTests;
        private final Map<String, Object> metadata;

        private TestStrategy(Builder b) {
            this.request = b.request;
            this.unitTests = List.copyOf(b.unitTests == null ? List.of() : b.unitTests);
            this.integrationTests = List.copyOf(b.integrationTests == null ? List.of() : b.integrationTests);
            this.securityTests = List.copyOf(b.securityTests == null ? List.of() : b.securityTests);
            this.apiTests = List.copyOf(b.apiTests == null ? List.of() : b.apiTests);
            this.regressionTests = List.copyOf(b.regressionTests == null ? List.of() : b.regressionTests);
            this.metadata = Map.copyOf(b.metadata == null ? Map.of() : b.metadata);
        }

        public String request() { return request; }
        public List<String> unitTests() { return unitTests; }
        public List<String> integrationTests() { return integrationTests; }
        public List<String> securityTests() { return securityTests; }
        public List<String> apiTests() { return apiTests; }
        public List<String> regressionTests() { return regressionTests; }
        public Map<String, Object> metadata() { return metadata; }

        public int totalTests() {
            return unitTests.size() + integrationTests.size()
                    + securityTests.size() + apiTests.size() + regressionTests.size();
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private String request;
            private List<String> unitTests;
            private List<String> integrationTests;
            private List<String> securityTests;
            private List<String> apiTests;
            private List<String> regressionTests;
            private Map<String, Object> metadata;

            public Builder request(String v) { this.request = v; return this; }
            public Builder unitTests(List<String> v) { this.unitTests = v; return this; }
            public Builder integrationTests(List<String> v) { this.integrationTests = v; return this; }
            public Builder securityTests(List<String> v) { this.securityTests = v; return this; }
            public Builder apiTests(List<String> v) { this.apiTests = v; return this; }
            public Builder regressionTests(List<String> v) { this.regressionTests = v; return this; }
            public Builder metadata(Map<String, Object> v) { this.metadata = v; return this; }

            public TestStrategy build() { return new TestStrategy(this); }
        }
    }

    /**
     * Generates a complete testing strategy for the given context.
     */
    public TestStrategy generate(DeveloperIntent intent, ImpactReport impact,
                                  ImplementationPlan plan) {
        List<String> unitTests = buildUnitTests(intent, impact);
        List<String> integrationTests = buildIntegrationTests(intent, impact);
        List<String> securityTests = buildSecurityTests(intent, impact);
        List<String> apiTests = buildApiTests(intent, impact);
        List<String> regressionTests = buildRegressionTests(intent, impact);

        return TestStrategy.builder()
                .request(intent.originalRequest())
                .unitTests(unitTests)
                .integrationTests(integrationTests)
                .securityTests(securityTests)
                .apiTests(apiTests)
                .regressionTests(regressionTests)
                .metadata(Map.of(
                        "intentType", intent.intent().name(),
                        "totalTestCount", unitTests.size() + integrationTests.size()
                                + securityTests.size() + apiTests.size() + regressionTests.size(),
                        "affectedControllers", impact.affectedControllers().size(),
                        "affectedServices", impact.affectedServices().size(),
                        "affectedEndpoints", impact.affectedEndpoints().size()
                ))
                .build();
    }

    // ─── Unit tests ─────────────────────────────────────────────────────────

    private List<String> buildUnitTests(DeveloperIntent intent, ImpactReport impact) {
        List<String> tests = new ArrayList<>();

        // Service tests
        for (ProjectClass svc : impact.affectedServices()) {
            tests.add(svc.name() + "Test — happy path");
            tests.add(svc.name() + "Test — null input handling");
            tests.add(svc.name() + "Test — edge cases");
        }

        // Entity tests
        for (ProjectEntity entity : impact.affectedEntities()) {
            tests.add(entity.name() + "Test — field validation");
            tests.add(entity.name() + "Test — relationship mapping");
        }

        // Domain-specific tests
        switch (intent.intent()) {
            case ADD_FEATURE -> {
                tests.add("FeatureServiceTest — business logic validation");
            }
            case SECURITY -> {
                tests.add("JwtTokenServiceTest — token generation");
                tests.add("JwtTokenServiceTest — token validation");
                tests.add("SecurityConfigTest — access rules");
            }
            case ADD_ENTITY -> {
                tests.add("EntityMappingTest — JPA annotations");
                tests.add("EntityMappingTest — relationships");
            }
            case FIX_BUG -> {
                tests.add("BugFixTest — reproduces the original bug");
                tests.add("BugFixTest — boundary conditions");
            }
            case OPTIMIZE -> {
                tests.add("PerformanceTest — baseline benchmark");
                tests.add("PerformanceTest — optimized benchmark");
            }
            default -> { }
        }

        return tests;
    }

    // ─── Integration tests ──────────────────────────────────────────────────

    private List<String> buildIntegrationTests(DeveloperIntent intent, ImpactReport impact) {
        List<String> tests = new ArrayList<>();

        for (ProjectEndpoint ep : impact.affectedEndpoints()) {
            tests.add("IntegrationTest — " + ep.httpMethod() + " " + ep.path() + " — success");
            tests.add("IntegrationTest — " + ep.httpMethod() + " " + ep.path() + " — not found");
            tests.add("IntegrationTest — " + ep.httpMethod() + " " + ep.path() + " — bad request");
        }

        for (ProjectClass ctrl : impact.affectedControllers()) {
            tests.add(ctrl.name() + "IntegrationTest — full flow");
        }

        switch (intent.intent()) {
            case ADD_ENTITY -> {
                tests.add("RepositoryIntegrationTest — CRUD operations");
                tests.add("RepositoryIntegrationTest — custom queries");
                tests.add("MigrationIntegrationTest — schema creation");
            }
            case CREATE_API -> {
                tests.add("ApiIntegrationTest — full CRUD");
                tests.add("ApiIntegrationTest — pagination");
                tests.add("ApiIntegrationTest — filtering");
            }
            default -> { }
        }

        return tests;
    }

    // ─── Security tests ─────────────────────────────────────────────────────

    private List<String> buildSecurityTests(DeveloperIntent intent, ImpactReport impact) {
        List<String> tests = new ArrayList<>();

        if (intent.intent() == DeveloperIntentType.SECURITY
                || intent.intent() == DeveloperIntentType.ADD_FEATURE) {
            tests.add("SecurityTest — unauthorized access returns 401");
            tests.add("SecurityTest — valid JWT grants access");
            tests.add("SecurityTest — expired JWT returns 401");
            tests.add("SecurityTest — invalid signature returns 401");
            tests.add("SecurityTest — missing token returns 401");
            tests.add("SecurityTest — role-based access control");
            tests.add("SecurityTest — CORS headers present");

            for (ProjectEndpoint ep : impact.affectedEndpoints()) {
                tests.add("SecurityTest — " + ep.httpMethod() + " " + ep.path() + " requires auth");
            }
        }

        if (intent.intent() == DeveloperIntentType.ADD_FEATURE) {
            tests.add("SecurityTest — input validation on all endpoints");
            tests.add("SecurityTest — SQL injection prevention");
            tests.add("SecurityTest — XSS prevention");
        }

        return tests;
    }

    // ─── API tests ──────────────────────────────────────────────────────────

    private List<String> buildApiTests(DeveloperIntent intent, ImpactReport impact) {
        List<String> tests = new ArrayList<>();

        for (ProjectEndpoint ep : impact.affectedEndpoints()) {
            tests.add("ApiTest — " + ep.httpMethod() + " " + ep.path() + " — returns 200");
            tests.add("ApiTest — " + ep.httpMethod() + " " + ep.path() + " — returns 404");
            tests.add("ApiTest — " + ep.httpMethod() + " " + ep.path() + " — returns 400 for invalid input");
            if ("POST".equals(ep.httpMethod()) || "PUT".equals(ep.httpMethod())) {
                tests.add("ApiTest — " + ep.httpMethod() + " " + ep.path() + " — returns 422 for validation error");
            }
        }

        return tests;
    }

    // ─── Regression tests ───────────────────────────────────────────────────

    private List<String> buildRegressionTests(DeveloperIntent intent, ImpactReport impact) {
        List<String> tests = new ArrayList<>();

        tests.add("RegressionTest — full test suite passes");
        tests.add("RegressionTest — existing endpoints unchanged");

        if (!impact.affectedEndpoints().isEmpty()) {
            for (ProjectEndpoint ep : impact.affectedEndpoints()) {
                tests.add("RegressionTest — " + ep.httpMethod() + " " + ep.path() + " backward compatibility");
            }
        }

        for (ProjectClass ctrl : impact.affectedControllers()) {
            tests.add("RegressionTest — " + ctrl.name() + " — existing behavior preserved");
        }

        for (ProjectClass svc : impact.affectedServices()) {
            tests.add("RegressionTest — " + svc.name() + " — existing contracts preserved");
        }

        return tests;
    }
}