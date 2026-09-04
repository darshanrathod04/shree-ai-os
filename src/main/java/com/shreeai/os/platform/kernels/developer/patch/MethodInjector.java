package com.shreeai.os.platform.kernels.developer.patch;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>MethodInjector</b>
 *
 * <p>Utility for inserting method bodies into existing Java class source code
 * while preserving formatting and indentation. Methods are injected right before
 * the closing brace of the class body.</p>
 *
 * <p><b>Injection rules:</b></p>
 * <ul>
 *   <li>If the class already has methods, inject before the last closing brace</li>
 *   <li>If the class has no methods but has fields, inject after the last field</li>
 *   <li>If the class body is empty, inject between the class opening and closing brace</li>
 *   <li>Preserves 4-space indentation for injected content</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Developer Workflow (Sprint-17)</p>
 *
 * @since Sprint-17
 */
public final class MethodInjector {

    private static final Pattern CLASS_OPENING = Pattern.compile(
            "(class|interface|enum|record)\\s+\\w+\\s*(?:extends\\s+[\\w.]+)?\\s*(?:implements\\s+[\\w.,. ]+\\s*)?\\{"
    );

    private static final Pattern CLOSING_BRACE = Pattern.compile("\\}");

    /**
     * Result of method injection.
     */
    public static final class InjectionResult {
        private final String source;
        private final boolean injected;
        private final String message;

        public InjectionResult(String source, boolean injected, String message) {
            this.source = source;
            this.injected = injected;
            this.message = message == null ? "" : message;
        }

        public String source() { return source; }
        public boolean injected() { return injected; }
        public String message() { return message; }
    }

    /**
     * Injects the given method code into the class body of the source file.
     * The method is inserted right before the class's closing brace, with
     * 4-space indentation applied to the injected code.
     *
     * @param source      the original Java source
     * @param methodCode  the method body to inject (without extra leading indentation)
     * @return injection result with the modified source
     */
    public InjectionResult inject(String source, String methodCode) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(methodCode, "methodCode must not be null");

        if (methodCode.isBlank()) {
            return new InjectionResult(source, false, "No method code provided");
        }

        // Find the class opening brace
        Matcher classMatcher = CLASS_OPENING.matcher(source);
        if (!classMatcher.find()) {
            return new InjectionResult(source, false, "Could not find class opening brace");
        }

        int classOpenBrace = classMatcher.end() - 1; // position of '{'

        // Find the last closing brace of the file (assuming it's the class close)
        Matcher closeMatcher = CLOSING_BRACE.matcher(source);
        int lastClose = -1;
        while (closeMatcher.find()) {
            lastClose = closeMatcher.end() - 1;
        }

        if (lastClose <= classOpenBrace) {
            return new InjectionResult(source, false, "Could not find class closing brace");
        }

        // Check if there's content between the class brace and the close brace
        String between = source.substring(classOpenBrace + 1, lastClose).trim();
        String indent = detectIndent(source);

        String indentedMethod = indentCode(methodCode, indent);

        String result;
        if (between.isEmpty()) {
            // Empty class body: insert between { and }
            result = source.substring(0, classOpenBrace + 1)
                    + "\n" + indentedMethod + "\n"
                    + source.substring(lastClose);
        } else {
            // Non-empty: insert before the last closing brace
            result = source.substring(0, lastClose)
                    + "\n" + indentedMethod + "\n"
                    + source.substring(lastClose);
        }

        return new InjectionResult(result, true, "Method injected successfully");
    }

    /**
     * Removes a method from the source by its method signature.
     *
     * @param source         the original Java source
     * @param methodSignature e.g. "public void doSomething()"
     * @return injection result
     */
    public InjectionResult remove(String source, String methodSignature) {
        Objects.requireNonNull(source, "source must not be null");

        if (methodSignature == null || methodSignature.isBlank()) {
            return new InjectionResult(source, false, "No method signature provided");
        }

        // Match method with any modifiers, annotations, return type, name, params, throws
        String escapedSig = Pattern.quote(methodSignature.trim());
        Pattern methodPattern = Pattern.compile(
                "[ \\t]*"                    // leading whitespace
                + "(?:@\\w+(?:\\([^)]*\\))?[ \\t]*\\n)*" // annotations
                + escapedSig                 // signature
                + "[^{]*\\{[^}]*\\}"        // body (simplified)
                + "[ \\t]*",                // trailing whitespace
                Pattern.DOTALL
        );

        String result = methodPattern.matcher(source).replaceAll("");
        boolean removed = !result.equals(source);

        return new InjectionResult(result, removed,
                removed ? "Method removed successfully" : "Method not found: " + methodSignature);
    }

    /**
     * Detects the indentation used in the source file (number of spaces).
     */
    private String detectIndent(String source) {
        // Look for the first occurrence of 4 spaces or a tab
        if (source.contains("    ")) return "    ";
        if (source.contains("\t")) return "\t";
        return "    "; // default to 4 spaces
    }

    /**
     * Applies consistent indentation to the code.
     */
    private String indentCode(String code, String indent) {
        StringBuilder sb = new StringBuilder();
        String[] lines = code.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                sb.append(line.isEmpty() ? "" : line); // preserve blank lines as-is
            } else {
                // Check if the line already has some indentation
                int leadingSpaces = 0;
                for (int j = 0; j < line.length(); j++) {
                    char c = line.charAt(j);
                    if (c == ' ') leadingSpaces++;
                    else if (c == '\t') leadingSpaces += 4;
                    else break;
                }
                // Apply indent at the level of the class body (one level in)
                if (leadingSpaces > 0) {
                    sb.append(indent).append(line.substring(leadingSpaces));
                } else {
                    sb.append(indent).append(line);
                }
            }
            if (i < lines.length - 1) sb.append("\n");
        }
        return sb.toString();
    }
}
