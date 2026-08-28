package com.shreeai.os.platform.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Constitutional gate R1 (docs/architecture/LEGACY_MIGRATION_REPORT.md).
 *
 * No canonical (non-legacy) source file may import classes from
 * com.shreeai.os.platform.legacy unless the file is explicitly listed
 * in the shrinking allowlist below. The allowlist MUST be empty at the end of
 * Phase 2. New canonical files are never added to the allowlist.
 */
class CanonicalIsolationTest {

    private static final String LEGACY_IMPORT =
            "import com.shreeai.os.platform.legacy.";

    private static final Set<String> ALLOWED = Set.of(
            "platform/runtime/pipeline/PipelineContext.java",
            "platform/runtime/pipeline/PipelineExecutionState.java",
            "platform/runtime/pipeline/PipelineResult.java",
            "platform/runtime/service/DefaultRuntimeService.java",
            "platform/validation/DecisionValidator.java",
            "platform/validation/ValidationRule.java",
            "platform/validation/rules/CapabilityRule.java",
            "platform/validation/rules/ConfidenceRule.java",
            "platform/validation/rules/ContextRule.java",
            "platform/validation/rules/DecisionExistsRule.java",
            "platform/validation/rules/ExecutionModeRule.java",
            "platform/validation/rules/RiskRule.java",
            "platform/validation/rules/SessionRule.java");

    private static final String FORBIDDEN_MESSAGE =
            "R1 VIOLATION - canonical code must not import platform.legacy. "
                    + "New violations (never add these to the allowlist; "
                    + "migrate via promote-and-delegate instead): ";

    private static final String STALE_MESSAGE =
            "STALE ALLOWLIST - these files no longer import legacy; remove "
                    + "them from CanonicalIsolationTest.ALLOWED: ";

    /**
     * Returns true if the given platform-relative path is inside the legacy
     * quarantine (legacy package itself is exempt from R1).
     */
    private static boolean isLegacyQuarantine(String platformRelativePath) {
        return platformRelativePath.startsWith("platform/legacy/");
    }

    @Test
    void canonicalCodeMustNotImportLegacyOutsideAllowlist() throws IOException {
        Path platformRoot = Paths.get(System.getProperty("user.dir"),
                "src", "main", "java", "com", "shreeai", "os", "platform");
        assertTrue(Files.isDirectory(platformRoot),
                "platform source root not found: " + platformRoot);

        List<String> unexpected = new ArrayList<>();
        List<String> stale = new ArrayList<>(ALLOWED);

        try (Stream<Path> files = Files.walk(platformRoot)) {
            List<Path> javaFiles = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());

            for (Path file : javaFiles) {
                String relative = platformRoot.relativize(file)
                        .toString().replace('\\', '/');
                String prefixed = "platform/" + relative;

                if (isLegacyQuarantine(prefixed)) {
                    continue;
                }

                String source = Files.readString(file);
                if (source.contains(LEGACY_IMPORT)) {
                    if (ALLOWED.contains(prefixed)) {
                        stale.remove(prefixed);
                    } else {
                        unexpected.add(prefixed);
                    }
                }
            }
        }

        assertTrue(unexpected.isEmpty(), FORBIDDEN_MESSAGE + unexpected);
        assertTrue(stale.isEmpty(), STALE_MESSAGE + stale);
    }
}
