package com.shreeai.os.platform.kernels.project.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * <b>RepositoryScanner</b>
 *
 * <p>Recursively discovers Java source files and configuration files
 * in a project root. Ignores build outputs, version control, and
 * dependency caches.</p>
 *
 * <p><b>Ownership:</b> Project Intelligence (Sprint-13)</p>
 */
public final class RepositoryScanner {

    private static final Set<String> IGNORED_DIRS = Set.of(
            "target", "build", "out", ".git", ".idea", ".vscode",
            "node_modules", ".gradle", "dist", "bin"
    );

    private static final List<String> CONFIG_FILE_NAMES = List.of(
            "pom.xml", "build.gradle", "build.gradle.kts", "settings.gradle",
            "application.yml", "application.yaml", "application.properties"
    );

    private final Path projectRoot;

    public RepositoryScanner(Path projectRoot) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
    }

    /**
     * Returns the absolute paths of all Java source files in the project.
     */
    public List<Path> findJavaFiles() throws IOException {
        return findJavaFiles(this.projectRoot, new ArrayList<>());
    }

    /**
     * Returns the absolute paths of all Java source files under {@code root}.
     * Skips ignored directories.
     */
    public List<Path> findJavaFiles(Path root, List<Path> sink) throws IOException {
        if (!Files.isDirectory(root)) return sink;
        try (Stream<Path> stream = Files.list(root)) {
            stream.forEach(entry -> {
                String name = entry.getFileName().toString();
                if (Files.isDirectory(entry)) {
                    if (!IGNORED_DIRS.contains(name)) {
                        try {
                            findJavaFiles(entry, sink);
                        } catch (IOException e) {
                            // best-effort; skip
                        }
                    }
                } else if (name.endsWith(".java")) {
                    sink.add(entry);
                }
            });
        }
        return sink;
    }

    /**
     * Returns the absolute paths of known configuration files
     * (pom.xml, application.yml, etc.).
     */
    public List<Path> findConfigFiles() throws IOException {
        List<Path> result = new ArrayList<>();
        if (!Files.isDirectory(projectRoot)) return result;
        try (Stream<Path> stream = Files.walk(projectRoot, 6)) {
            stream.forEach(entry -> {
                if (Files.isRegularFile(entry)) {
                    String name = entry.getFileName().toString();
                    if (CONFIG_FILE_NAMES.contains(name)) {
                        result.add(entry);
                    }
                }
            });
        }
        return result;
    }

    /**
     * Returns all resource files (non-Java, non-config) in
     * {@code src/main/resources}.
     */
    public List<Path> findResourceFiles() throws IOException {
        Path resourcesDir = projectRoot.resolve("src").resolve("main").resolve("resources");
        if (!Files.isDirectory(resourcesDir)) return List.of();
        List<Path> result = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(resourcesDir)) {
            stream.forEach(entry -> {
                if (Files.isRegularFile(entry)) {
                    result.add(entry);
                }
            });
        }
        return result;
    }

    public Path getProjectRoot() {
        return projectRoot;
    }

    /**
     * Returns the top-level group of packages found under
     * {@code src/main/java}, used to derive "modules".
     */
    public List<String> findTopLevelPackages() throws IOException {
        Path javaRoot = projectRoot.resolve("src").resolve("main").resolve("java");
        if (!Files.isDirectory(javaRoot)) return List.of();
        List<String> groups = new ArrayList<>();
        try (Stream<Path> stream = Files.list(javaRoot)) {
            stream.forEach(entry -> {
                if (Files.isDirectory(entry)) {
                    groups.add(entry.getFileName().toString());
                }
            });
        }
        return groups;
    }

    public List<String> findSubPackages(String topLevelPackage) throws IOException {
        Path javaRoot = projectRoot.resolve("src").resolve("main").resolve("java");
        Path base = javaRoot.resolve(topLevelPackage.replace(".", "/"));
        if (!Files.isDirectory(base)) return List.of();
        List<String> subs = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(base, 4)) {
            stream.filter(Files::isDirectory).forEach(p -> {
                String rel = javaRoot.relativize(p).toString().replace(java.io.File.separatorChar, '.');
                if (!rel.isEmpty()) subs.add(rel);
            });
        }
        return subs;
    }

    public static Set<String> ignoredDirectories() {
        return Set.copyOf(IGNORED_DIRS);
    }
}
