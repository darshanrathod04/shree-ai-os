package com.shreeai.os.platform.kernels.project.model;

import java.util.Objects;

/**
 * <b>ProjectEndpoint</b> — represents a REST endpoint discovered in
 * a Spring controller. Includes the full dependency chain
 * (controller → service → repository → entity) when resolvable.
 */
public final class ProjectEndpoint {

    private final String httpMethod;   // GET, POST, PUT, DELETE, PATCH
    private final String path;         // e.g. /users/{id}
    private final String controllerClass;
    private final String methodName;
    private final String requestDto;   // may be null
    private final String responseDto;  // may be null
    private final String service;      // may be null
    private final String repository;    // may be null
    private final String entity;       // may be null

    private ProjectEndpoint(Builder b) {
        this.httpMethod = Objects.requireNonNull(b.httpMethod).toUpperCase();
        this.path = Objects.requireNonNull(b.path);
        this.controllerClass = Objects.requireNonNull(b.controllerClass);
        this.methodName = b.methodName == null ? "" : b.methodName;
        this.requestDto = b.requestDto;
        this.responseDto = b.responseDto;
        this.service = b.service;
        this.repository = b.repository;
        this.entity = b.entity;
    }

    public String httpMethod() { return httpMethod; }
    public String path() { return path; }
    public String controllerClass() { return controllerClass; }
    public String methodName() { return methodName; }
    public String requestDto() { return requestDto; }
    public String responseDto() { return responseDto; }
    public String service() { return service; }
    public String repository() { return repository; }
    public String entity() { return entity; }

    public String signature() {
        return httpMethod + " " + path;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String httpMethod;
        private String path;
        private String controllerClass;
        private String methodName;
        private String requestDto;
        private String responseDto;
        private String service;
        private String repository;
        private String entity;

        public Builder httpMethod(String v) { this.httpMethod = v; return this; }
        public Builder path(String v) { this.path = v; return this; }
        public Builder controllerClass(String v) { this.controllerClass = v; return this; }
        public Builder methodName(String v) { this.methodName = v; return this; }
        public Builder requestDto(String v) { this.requestDto = v; return this; }
        public Builder responseDto(String v) { this.responseDto = v; return this; }
        public Builder service(String v) { this.service = v; return this; }
        public Builder repository(String v) { this.repository = v; return this; }
        public Builder entity(String v) { this.entity = v; return this; }

        public ProjectEndpoint build() { return new ProjectEndpoint(this); }
    }
}
