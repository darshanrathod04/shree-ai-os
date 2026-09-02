package com.shreeai.os.platform.kernels.developer.codegen;

import com.shreeai.os.platform.kernels.developer.codegen.model.*;

import java.util.*;

/**
 * <b>PatchValidator</b> — validates every {@link FilePatch} in a {@link PatchPlan}
 * and returns a {@link ValidationResult}.
 *
 * <p>Validation checks performed per patch:</p>
 * <ul>
 *   <li>Duplicate imports within the same file</li>
 *   <li>Duplicate method signatures</li>
 *   <li>Duplicate field names</li>
 *   <li>Duplicate REST endpoint paths (within ADD_ENDPOINT operations)</li>
 *   <li>Package path consistency (e.g. a class in {@code com.example.controller}
 *       should be in the file path {@code com/example/controller/})</li>
 *   <li>Referenced dependency FQNs are well-formed</li>
 * </ul>
 *
 * <p>The validator does <b>not</b> check whether the referenced classes actually
 * exist on the classpath (that would require the symbol solver). It only checks
 * structural consistency within the patch plan itself.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-15)</p>
 *
 * @since Sprint-15
 */
public final class PatchValidator {

    /**
     * Validates every patch in the plan and returns a consolidated result.
     * @param plan  the patch plan to validate (never null)
     * @return validation result (never null)
     */
    public ValidationResult validate(PatchPlan plan) {
        Objects.requireNonNull(plan, "plan");

        List<ValidationResult.PatchResult> patchResults = new ArrayList<>();
        List<String> allErrors = new ArrayList<>();
        List<String> allWarnings = new ArrayList<>();

        for (FilePatch patch : plan.patches()) {
            ValidationResult.PatchResult pr = validatePatch(patch);
            patchResults.add(pr);
            for (ValidationResult.Issue issue : pr.issues()) {
                String msg = "[" + patch.targetFile() + "] " + issue.message();
                if (issue.severity() == ValidationResult.Status.INVALID) {
                    allErrors.add(msg);
                } else {
                    allWarnings.add(msg);
                }
            }
        }

        return ValidationResult.builder()
                .patchResults(patchResults)
                .errors(allErrors)
                .warnings(allWarnings)
                .build();
    }

    private ValidationResult.PatchResult validatePatch(FilePatch patch) {
        List<ValidationResult.Issue> issues = new ArrayList<>();
        Set<String> seenImports = new LinkedHashSet<>();
        Set<String> seenMethods = new LinkedHashSet<>();
        Set<String> seenFields = new LinkedHashSet<>();
        Set<String> seenEndpoints = new LinkedHashSet<>();

        for (FilePatch.Operation op : patch.operations()) {
            switch (op.kind()) {
                case ADD_IMPORT -> {
                    String imp = op.code().trim();
                    if (imp.isEmpty()) {
                        issues.add(new ValidationResult.Issue(
                                ValidationResult.IssueKind.MALFORMED_SIGNATURE,
                                "Empty import statement",
                                patch.targetFile(),
                                op.signature(),
                                ValidationResult.Status.INVALID
                        ));
                    } else if (seenImports.contains(imp)) {
                        issues.add(new ValidationResult.Issue(
                                ValidationResult.IssueKind.DUPLICATE_IMPORT,
                                "Duplicate import: " + imp,
                                patch.targetFile(),
                                op.signature(),
                                ValidationResult.Status.WARNING
                        ));
                    } else {
                        seenImports.add(imp);
                        // Validate FQN format
                        if (!isValidFqdn(imp)) {
                            issues.add(new ValidationResult.Issue(
                                    ValidationResult.IssueKind.MALFORMED_SIGNATURE,
                                    "Suspicious import (may not be a valid FQN): " + imp,
                                    patch.targetFile(),
                                    op.signature(),
                                    ValidationResult.Status.WARNING
                            ));
                        }
                    }
                }
                case ADD_METHOD, MODIFY_METHOD -> {
                    String sig = op.signature();
                    if (sig.isEmpty()) {
                        issues.add(new ValidationResult.Issue(
                                ValidationResult.IssueKind.MALFORMED_SIGNATURE,
                                "Method signature is empty",
                                patch.targetFile(),
                                sig,
                                ValidationResult.Status.INVALID
                        ));
                    } else if (seenMethods.contains(sig)) {
                        issues.add(new ValidationResult.Issue(
                                ValidationResult.IssueKind.DUPLICATE_METHOD,
                                "Duplicate method signature: " + sig,
                                patch.targetFile(),
                                sig,
                                ValidationResult.Status.INVALID
                        ));
                    } else {
                        seenMethods.add(sig);
                    }
                }
                case ADD_FIELD -> {
                    String fieldSig = op.code().isEmpty() ? op.signature() : op.code();
                    // Extract field name from "Type name" or "Type name = ..."
                    String fieldName = extractFieldName(fieldSig);
                    if (fieldName.isEmpty()) {
                        issues.add(new ValidationResult.Issue(
                                ValidationResult.IssueKind.MALFORMED_SIGNATURE,
                                "Could not parse field name from: " + fieldSig,
                                patch.targetFile(),
                                op.signature(),
                                ValidationResult.Status.WARNING
                        ));
                    } else if (seenFields.contains(fieldName)) {
                        issues.add(new ValidationResult.Issue(
                                ValidationResult.IssueKind.DUPLICATE_FIELD,
                                "Duplicate field name: " + fieldName,
                                patch.targetFile(),
                                op.signature(),
                                ValidationResult.Status.INVALID
                        ));
                    } else {
                        seenFields.add(fieldName);
                    }
                }
                case ADD_ENDPOINT -> {
                    // Check endpoint uniqueness
                    String endpointSig = op.signature();
                    if (seenEndpoints.contains(endpointSig)) {
                        issues.add(new ValidationResult.Issue(
                                ValidationResult.IssueKind.DUPLICATE_ENDPOINT,
                                "Duplicate endpoint: " + endpointSig,
                                patch.targetFile(),
                                endpointSig,
                                ValidationResult.Status.INVALID
                        ));
                    } else {
                        seenEndpoints.add(endpointSig);
                    }
                }
                case CREATE_CLASS, ADD_ENTITY -> {
                    // Validate package consistency
                    String pkg = derivePackage(patch.targetFile());
                    if (!pkg.isEmpty()) {
                        String className = deriveClassName(patch.targetFile());
                        String expected = pkg + "." + className;
                        if (patch.targetClass() != null && !patch.targetClass().isEmpty()) {
                            if (!patch.targetClass().equals(expected) && !patch.targetClass().startsWith(pkg)) {
                                issues.add(new ValidationResult.Issue(
                                        ValidationResult.IssueKind.PACKAGE_MISMATCH,
                                        "Class FQN " + patch.targetClass() + " does not match package " + pkg,
                                        patch.targetFile(),
                                        op.signature(),
                                        ValidationResult.Status.WARNING
                                ));
                            }
                        }
                    }
                }
                default -> { /* no specific checks */ }
            }
        }

        // Validate dependencies reference valid FQNs
        for (String dep : patch.dependencies()) {
            if (!isValidFqdn(dep) && !dep.isEmpty()) {
                issues.add(new ValidationResult.Issue(
                        ValidationResult.IssueKind.MISSING_DEPENDENCY,
                        "Dependency does not look like a valid FQN: " + dep,
                        patch.targetFile(),
                        "",
                        ValidationResult.Status.WARNING
                ));
            }
        }

        ValidationResult.Status status = computePatchStatus(issues);
        return new ValidationResult.PatchResult(patch.targetFile(), status, issues);
    }

    private ValidationResult.Status computePatchStatus(List<ValidationResult.Issue> issues) {
        boolean hasInvalid = issues.stream()
                .anyMatch(i -> i.severity() == ValidationResult.Status.INVALID);
        boolean hasWarning = issues.stream()
                .anyMatch(i -> i.severity() == ValidationResult.Status.WARNING);
        if (hasInvalid) return ValidationResult.Status.INVALID;
        if (hasWarning) return ValidationResult.Status.WARNING;
        return ValidationResult.Status.SAFE;
    }

    private boolean isValidFqdn(String fqn) {
        if (fqn == null || fqn.isEmpty()) return false;
        // A valid FQN: starts with lowercase, contains dots, no spaces
        if (fqn.contains(" ") || fqn.startsWith(".")) return false;
        // Check it looks like a Java package/class name
        String[] parts = fqn.split("\\.");
        if (parts.length < 2) {
            // Could be a primitive or simple name — accept for now
            return true;
        }
        return Arrays.stream(parts).allMatch(p -> !p.isEmpty() && p.matches("[a-zA-Z_][a-zA-Z0-9_]*"));
    }

    private String extractFieldName(String fieldSpec) {
        if (fieldSpec == null || fieldSpec.isEmpty()) return "";
        // "private String userName = null;" -> "userName"
        // "String userName" -> "userName"
        String trimmed = fieldSpec.trim();
        // Split on common delimiters
        String[] tokens = trimmed.split("\\s+");
        if (tokens.length == 0) return "";
        String last = tokens[tokens.length - 1];
        // Remove = and everything after
        int eq = last.indexOf('=');
        if (eq > 0) last = last.substring(0, eq);
        // Remove trailing ; if present
        if (last.endsWith(";")) last = last.substring(0, last.length() - 1);
        // Remove array brackets and generics
        last = last.replaceAll("[\\[\\]<>].*", "");
        return last.trim();
    }

    private String derivePackage(String filePath) {
        if (filePath == null || filePath.isEmpty()) return "";
        String[] segments = filePath.split("/");
        StringBuilder pkg = new StringBuilder();
        for (int i = 0; i < segments.length - 1; i++) {
            String seg = segments[i];
            if (seg.equals("java") || seg.equals("test") || seg.equals("src")) continue;
            if (pkg.length() > 0) pkg.append('.');
            pkg.append(seg);
        }
        return pkg.toString();
    }

    private String deriveClassName(String filePath) {
        if (filePath == null || filePath.isEmpty()) return "";
        String name = filePath;
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) name = name.substring(lastSlash + 1);
        int dot = name.lastIndexOf('.');
        if (dot > 0) name = name.substring(0, dot);
        return name;
    }
}
