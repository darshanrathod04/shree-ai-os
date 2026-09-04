package com.shreeai.os.platform.kernels.project.model;

import java.util.List;
import java.util.Objects;

/**
 * <b>ProjectModule</b> — represents a top-level module or package group
 * in a project (e.g. "Runtime", "Knowledge", "Memory").
 */
public final class ProjectModule {

    public enum Kind {
        ROOT_PACKAGE, MAVEN_MODULE, GRADLE_MODULE
    }

    private final String name;
    private final Kind kind;
    private final String path;     // root package or module path
    private final List<String> subPackages;

    private ProjectModule(String name, Kind kind, String path, List<String> subPackages) {
        this.name = Objects.requireNonNull(name);
        this.kind = kind == null ? Kind.ROOT_PACKAGE : kind;
        this.path = path == null ? "" : path;
        this.subPackages = List.copyOf(subPackages == null ? List.of() : subPackages);
    }

    public String name() { return name; }
    public Kind kind() { return kind; }
    public String path() { return path; }
    public List<String> subPackages() { return subPackages; }

    public static ProjectModule of(String name, Kind kind, String path, List<String> subPackages) {
        return new ProjectModule(name, kind, path, subPackages);
    }
}
