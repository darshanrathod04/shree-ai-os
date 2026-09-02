package com.shreeai.os.platform.kernels.developer.patch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>ImportMerger</b>
 *
 * <p>Utility for merging import statements into Java source files while
 * removing duplicates and preserving formatting. Recognizes both
 * {@code import com.foo.Bar;} and static imports
 * {@code import static org.junit.Assert.assertEquals;}.</p>
 *
 * <p>The merge is performed by:</p>
 * <ol>
 *   <li>Extracting all existing imports from the source</li>
 *   <li>Extracting all package statement(s)</li>
 *   <li>Removing all imports from the source body</li>
 *   <li>Re-inserting a sorted, de-duplicated block of imports right after the package statement</li>
 * </ol>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-17)</p>
 *
 * @since Sprint-17
 */
public final class ImportMerger {

    private static final Pattern IMPORT_PATTERN = Pattern.compile(
            "^[ \\t]*import[ \\t]+(static[ \\t]+)?([\\w.$]+)[ \\t]*;[ \\t]*$",
            Pattern.MULTILINE
    );

    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
            "^[ \\t]*package[ \\t]+([\\w.]+)[ \\t]*;[ \\t]*$",
            Pattern.MULTILINE
    );

    private static final Pattern CLASS_BODY_START = Pattern.compile(
            "^[ \\t]*(@\\w+|public|private|protected|static|final|abstract|sealed|non-sealed|class|interface|enum|record)\\b.*$",
            Pattern.MULTILINE
    );

    /**
     * Result of an import merge.
     */
    public static final class MergeResult {
        private final String source;
        private final List<String> imports;
        private final int added;
        private final int duplicates;

        public MergeResult(String source, List<String> imports, int added, int duplicates) {
            this.source = source;
            this.imports = Collections.unmodifiableList(imports);
            this.added = added;
            this.duplicates = duplicates;
        }

        public String source() { return source; }
        public List<String> imports() { return imports; }
        public int added() { return added; }
        public int duplicates() { return duplicates; }
    }

    /**
     * Merges the given imports into the source code. Duplicate imports
     * are removed. The result is a syntactically valid Java source with
     * sorted imports right after the package statement.
     *
     * @param source     the original Java source
     * @param newImports the imports to merge in
     * @return the merged source and statistics
     */
    public MergeResult merge(String source, List<String> newImports) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(newImports, "newImports");

        // 1) Extract existing imports
        Map<String, String> importsMap = new LinkedHashMap<>();
        for (Matcher m = IMPORT_PATTERN.matcher(source); m.find(); ) {
            String whole = m.group();
            boolean isStatic = m.group(1) != null;
            String fqn = m.group(2);
            String key = (isStatic ? "static:" : "type:") + fqn;
            importsMap.putIfAbsent(key, whole.trim());
        }

        int before = importsMap.size();
        int added = 0;
        int duplicates = 0;

        // 2) Add new imports
        for (String imp : newImports) {
            if (imp == null) continue;
            String trimmed = imp.trim();
            if (trimmed.isEmpty() || !trimmed.startsWith("import")) continue;
            // Normalize: remove comments, ensure ends with ;
            if (!trimmed.endsWith(";")) trimmed = trimmed + ";";
            Matcher m = IMPORT_PATTERN.matcher(trimmed);
            if (m.find()) {
                boolean isStatic = m.group(1) != null;
                String fqn = m.group(2);
                String key = (isStatic ? "static:" : "type:") + fqn;
                if (importsMap.containsKey(key)) {
                    duplicates++;
                } else {
                    importsMap.put(key, trimmed);
                    added++;
                }
            } else {
                // Cannot parse, just keep the raw line
                String key = "raw:" + trimmed;
                if (importsMap.containsKey(key)) {
                    duplicates++;
                } else {
                    importsMap.put(key, trimmed);
                    added++;
                }
            }
        }

        // 3) Sort imports: type imports first, then static, each alphabetically
        List<String> typeImports = new ArrayList<>();
        List<String> staticImports = new ArrayList<>();
        for (Map.Entry<String, String> e : importsMap.entrySet()) {
            if (e.getKey().startsWith("static:")) {
                staticImports.add(e.getValue());
            } else {
                typeImports.add(e.getValue());
            }
        }
        typeImports.sort(String::compareTo);
        staticImports.sort(String::compareTo);

        List<String> allImports = new ArrayList<>();
        allImports.addAll(typeImports);
        allImports.addAll(staticImports);

        // 4) Remove old imports from source body
        String cleanedSource = IMPORT_PATTERN.matcher(source).replaceAll("");

        // 5) Find package statement and insert imports after it
        Matcher packageMatcher = PACKAGE_PATTERN.matcher(cleanedSource);
        String merged;
        if (packageMatcher.find()) {
            int endOfPackage = packageMatcher.end();
            // Find end of line after package
            int lineEnd = cleanedSource.indexOf('\n', endOfPackage);
            if (lineEnd < 0) lineEnd = cleanedSource.length();
            StringBuilder sb = new StringBuilder();
            sb.append(cleanedSource, 0, lineEnd + 1);
            if (!allImports.isEmpty()) {
                sb.append("\n");
                for (String imp : allImports) {
                    sb.append(imp).append("\n");
                }
                sb.append("\n");
            }
            sb.append(cleanedSource, lineEnd + 1, cleanedSource.length());
            merged = sb.toString();
        } else {
            // No package statement — prepend imports
            StringBuilder sb = new StringBuilder();
            for (String imp : allImports) {
                sb.append(imp).append("\n");
            }
            if (!allImports.isEmpty()) sb.append("\n");
            sb.append(cleanedSource);
            merged = sb.toString();
        }

        return new MergeResult(merged, allImports, added, duplicates + (before - added > 0 ? 0 : 0));
    }

    /**
     * Returns the list of imports found in the source.
     */
    public List<String> extract(String source) {
        Objects.requireNonNull(source, "source");
        List<String> result = new ArrayList<>();
        for (Matcher m = IMPORT_PATTERN.matcher(source); m.find(); ) {
            result.add(m.group().trim());
        }
        return result;
    }
}
