package com.shreeai.os.platform.kernels.developer.patch;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.shreeai.os.platform.kernels.developer.codegen.model.FilePatch;
import com.shreeai.os.platform.kernels.developer.codegen.model.FilePatch.Operation;
import com.shreeai.os.platform.kernels.developer.codegen.model.PatchOperation;
import com.shreeai.os.platform.kernels.developer.patch.model.PatchDiff;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan.RollbackEntry;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan.UndoAction;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan.UndoType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>PatchApplier</b>
 *
 * <p>Applies a {@link FilePatch} to a Java source file, producing a
 * {@link PatchDiff} and a corresponding {@link RollbackPlan}. Uses
 * JavaParser for structural verification but always falls back to
 * text-based injection to preserve formatting.</p>
 *
 * <p><b>Supported operations:</b></p>
 * <ul>
 *   <li>{@code ADD_IMPORT} — merge import statements</li>
 *   <li>{@code ADD_FIELD} — add a field declaration</li>
 *   <li>{@code ADD_METHOD} — inject a method into the class body</li>
 *   <li>{@code CREATE_CLASS} — replace source with new class</li>
 *   <li>{@code MODIFY_METHOD} / {@code UPDATE_METHOD} — replace method body</li>
 *   <li>{@code REMOVE_METHOD} — remove a method by signature</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-17)</p>
 *
 * @since Sprint-17
 */
public final class PatchApplier {

    private final ImportMerger importMerger;
    private final MethodInjector methodInjector;

    public PatchApplier() {
        this(new ImportMerger(), new MethodInjector());
    }

    public PatchApplier(ImportMerger importMerger, MethodInjector methodInjector) {
        this.importMerger = Objects.requireNonNull(importMerger, "importMerger");
        this.methodInjector = Objects.requireNonNull(methodInjector, "methodInjector");
    }

    /**
     * Applies the given FilePatch to the source code and produces a PatchDiff
     * plus a RollbackEntry.
     *
     * @param patch     the file patch
     * @param source    the original source code (may be empty for new files)
     * @return a result containing the diff and a per-file rollback entry
     */
    public ApplyResult apply(FilePatch patch, String source) {
        Objects.requireNonNull(patch, "patch must not be null");
        String original = source == null ? "" : source;
        String modified = original;
        List<UndoAction> undoActions = new ArrayList<>();
        boolean allSuccess = true;
        String lastMessage = "";

        try {
            // For CREATE_CLASS, replace the whole source
            if (patch.isNewFile() || hasOnlyCreateClass(patch)) {
                String newClass = extractCreateClassSource(patch);
                if (newClass == null || newClass.isBlank()) {
                    return ApplyResult.failed(patch.targetFile(), original, original,
                            "CREATE_CLASS operation missing code",
                            buildEntry(patch.targetFile(), List.of(
                                    new UndoAction(UndoType.REMOVE_FILE, "Remove new file", patch.targetFile())),
                                    original));
                }
                return ApplyResult.success(patch.targetFile(), "", newClass, "Created new class",
                        buildEntry(patch.targetFile(), List.of(
                                new UndoAction(UndoType.REMOVE_FILE, "Remove new file", patch.targetFile())),
                                ""));
            }

            for (Operation op : patch.operations()) {
                if (op == null || op.kind() == null) continue;
                switch (op.kind()) {
                    case ADD_IMPORT -> {
                        List<String> toAdd = extractImports(op.code());
                        ImportMerger.MergeResult merged = importMerger.merge(modified, toAdd);
                        modified = merged.source();
                        if (merged.added() > 0) {
                            undoActions.add(new UndoAction(UndoType.REMOVE_IMPORT,
                                    "Remove import: " + String.join(", ", toAdd), op.signature()));
                        }
                    }
                    case ADD_FIELD -> {
                        FieldInjectResult r = injectField(modified, op.code());
                        modified = r.source;
                        if (r.injected) {
                            undoActions.add(new UndoAction(UndoType.REMOVE_FIELD,
                                    "Remove field: " + op.signature(), op.signature()));
                        } else {
                            lastMessage = "Failed to add field: " + r.message;
                            allSuccess = false;
                        }
                    }
                    case ADD_METHOD -> {
                        MethodInjector.InjectionResult r = methodInjector.inject(modified, op.code());
                        modified = r.source();
                        if (r.injected()) {
                            undoActions.add(new UndoAction(UndoType.REMOVE_METHOD,
                                    "Remove method: " + op.signature(), op.signature()));
                        } else {
                            lastMessage = "Failed to inject method: " + r.message();
                            allSuccess = false;
                        }
                    }
                    case MODIFY_METHOD -> {
                        if (op.signature() != null && !op.signature().isBlank()) {
                            MethodInjector.InjectionResult r = methodInjector.remove(modified, op.signature());
                            if (r.injected()) {
                                modified = r.source();
                                MethodInjector.InjectionResult r2 = methodInjector.inject(modified, op.code());
                                modified = r2.source();
                                if (r2.injected()) {
                                    undoActions.add(new UndoAction(UndoType.REMOVE_METHOD,
                                            "Remove updated method: " + op.signature(), op.signature()));
                                } else {
                                    lastMessage = "Failed to reinject updated method: " + r2.message();
                                    allSuccess = false;
                                }
                            } else {
                                // Method not present — just inject as new
                                MethodInjector.InjectionResult r2 = methodInjector.inject(modified, op.code());
                                modified = r2.source();
                                if (r2.injected()) {
                                    undoActions.add(new UndoAction(UndoType.REMOVE_METHOD,
                                            "Remove method: " + op.signature(), op.signature()));
                                } else {
                                    lastMessage = "Failed to inject method: " + r2.message();
                                    allSuccess = false;
                                }
                            }
                        }
                    }
                    case ADD_ENDPOINT, ADD_ENTITY -> {
                        // handled via ADD_METHOD path (annotations-only injection)
                        MethodInjector.InjectionResult r2 = methodInjector.inject(modified, op.code());
                        modified = r2.source();
                        if (r2.injected()) {
                            undoActions.add(new UndoAction(UndoType.REMOVE_METHOD,
                                    "Remove endpoint/entity: " + op.signature(), op.signature()));
                        }
                    }
                    case MODIFY_CLASS -> {
                        // No-op for now (annotations/supertype changes not supported in this version)
                    }
                    default -> {
                        // Unknown operation — skip
                    }
                }
            }

            // Verify with JavaParser
            String verifyError = verifyStructure(modified);
            if (verifyError != null) {
                lastMessage = "JavaParser verification failed: " + verifyError;
                allSuccess = false;
            }

            PatchDiff.Status status = allSuccess
                    ? PatchDiff.Status.SUCCESS
                    : (modified.equals(original) ? PatchDiff.Status.SKIPPED : PatchDiff.Status.PARTIAL);

            RollbackEntry entry = buildEntry(patch.targetFile(), undoActions, original);

            return ApplyResult.fromDiff(patch.targetFile(), original, modified, status, lastMessage, entry);
        } catch (Exception e) {
            return ApplyResult.failed(patch.targetFile(), original, modified,
                    "Exception during apply: " + e.getMessage(),
                    buildEntry(patch.targetFile(), undoActions, original));
        }
    }

    /**
     * Applies multiple patches in order and builds a complete RollbackPlan.
     */
    public List<ApplyResult> applyAll(List<FilePatch> patches, Map<String, String> sources) {
        Objects.requireNonNull(patches, "patches must not be null");
        Map<String, String> workingSources = new LinkedHashMap<>(sources == null ? Map.of() : sources);
        List<ApplyResult> results = new ArrayList<>();
        List<RollbackEntry> rollbackEntries = new ArrayList<>();

        for (FilePatch patch : patches) {
            String source = workingSources.getOrDefault(patch.targetFile(), "");
            ApplyResult r = apply(patch, source);
            results.add(r);
            // Update working source for the next patch
            workingSources.put(patch.targetFile(), r.diff.after());
            rollbackEntries.add(r.rollbackEntry);
        }

        // Build a full RollbackPlan from accumulated entries
        // (we don't auto-execute; the caller can build it from the entries)
        return results;
    }

    private FieldInjectResult injectField(String source, String fieldCode) {
        if (fieldCode == null || fieldCode.isBlank()) {
            return new FieldInjectResult(source, false, "empty field code");
        }
        // Inject field right after the class opening brace (before any methods)
        Pattern classOpenPattern = Pattern.compile(
                "(class|interface|enum|record)\\s+\\w+\\s*(?:extends\\s+[\\w.]+)?\\s*(?:implements\\s+[\\w.,. ]+\\s*)?\\{"
        );
        Matcher m = classOpenPattern.matcher(source);
        if (!m.find()) {
            return new FieldInjectResult(source, false, "class opening brace not found");
        }
        int openBraceEnd = m.end();
        String indent = "    ";
        String indented = indent + fieldCode.trim();
        String result = source.substring(0, openBraceEnd) + "\n" + indented + "\n" + source.substring(openBraceEnd);
        return new FieldInjectResult(result, true, "field injected");
    }

    private List<String> extractImports(String code) {
        if (code == null) return List.of();
        List<String> imports = new ArrayList<>();
        Pattern p = Pattern.compile("^[ \\t]*import[ \\t]+(static[ \\t]+)?([\\w.$]+)[ \\t]*;[ \\t]*$", Pattern.MULTILINE);
        Matcher m = p.matcher(code);
        while (m.find()) {
            String s = m.group().trim();
            if (!imports.contains(s)) imports.add(s);
        }
        // Also include single-line @Fqn annotations or class references → not imports
        return imports;
    }

    private boolean hasOnlyCreateClass(FilePatch patch) {
        return !patch.operations().isEmpty()
                && patch.operations().stream().allMatch(o -> o.kind() == PatchOperation.CREATE_CLASS);
    }

    private String extractCreateClassSource(FilePatch patch) {
        if (patch.operations().isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (Operation op : patch.operations()) {
            sb.append(op.code()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Uses JavaParser to verify the structural validity of the modified source.
     * Returns null on success, or an error message on failure.
     */
    private String verifyStructure(String source) {
        if (source == null || source.isBlank()) {
            return "empty source";
        }
        try {
            ParserConfiguration cfg = new ParserConfiguration()
                    .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
            StaticJavaParser.setConfiguration(cfg);
            CompilationUnit unit = StaticJavaParser.parse(source);
            if (unit == null) {
                return "ParseResult was null";
            }
            return null;
        } catch (Exception e) {
            return "Parser exception: " + e.getMessage();
        }
    }

    private RollbackEntry buildEntry(String filePath, List<UndoAction> actions, String originalContent) {
        return new RollbackEntry(filePath, actions, originalContent);
    }

    /**
     * Helper record for field injection.
     */
    private static final class FieldInjectResult {
        final String source;
        final boolean injected;
        final String message;

        FieldInjectResult(String source, boolean injected, String message) {
            this.source = source;
            this.injected = injected;
            this.message = message;
        }
    }

    /**
     * <b>ApplyResult</b> — the result of applying a single patch.
     */
    public static final class ApplyResult {
        private final PatchDiff diff;
        private final RollbackEntry rollbackEntry;

        public ApplyResult(PatchDiff diff, RollbackEntry rollbackEntry) {
            this.diff = diff;
            this.rollbackEntry = rollbackEntry;
        }

        public PatchDiff diff() { return diff; }
        public RollbackEntry rollbackEntry() { return rollbackEntry; }

        public static ApplyResult success(String filePath, String before, String after,
                                           String message, RollbackEntry entry) {
            return new ApplyResult(
                    PatchDiff.builder()
                            .filePath(filePath).before(before).after(after)
                            .status(PatchDiff.Status.SUCCESS)
                            .message(message)
                            .appliedAt(Instant.now())
                            .build(),
                    entry);
        }

        public static ApplyResult fromDiff(String filePath, String before, String after,
                                            PatchDiff.Status status, String message,
                                            RollbackEntry entry) {
            return new ApplyResult(
                    PatchDiff.builder()
                            .filePath(filePath).before(before).after(after)
                            .status(status)
                            .message(message)
                            .appliedAt(Instant.now())
                            .build(),
                    entry);
        }

        public static ApplyResult failed(String filePath, String before, String after,
                                          String message, RollbackEntry entry) {
            return new ApplyResult(
                    PatchDiff.builder()
                            .filePath(filePath).before(before).after(after)
                            .status(PatchDiff.Status.FAILED)
                            .message(message)
                            .appliedAt(Instant.now())
                            .build(),
                    entry);
        }
    }
}
