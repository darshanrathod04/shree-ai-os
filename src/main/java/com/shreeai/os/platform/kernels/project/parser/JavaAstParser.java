package com.shreeai.os.platform.kernels.project.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.shreeai.os.platform.kernels.project.model.ProjectClass;
import com.shreeai.os.platform.kernels.project.model.ProjectClass.Kind;
import com.shreeai.os.platform.kernels.project.model.ProjectClass.Role;
import com.shreeai.os.platform.kernels.project.model.ProjectField;
import com.shreeai.os.platform.kernels.project.model.ProjectMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class JavaAstParser {
    private static final Set<String> CONTROLLER_ANNOTATIONS = Set.of(
            "RestController", "Controller"
    );
    private static final Set<String> SERVICE_ANNOTATIONS = Set.of(
            "Service", "Component"
    );
    private static final Set<String> REPOSITORY_ANNOTATIONS = Set.of(
            "Repository"
    );
    private static final Set<String> CONFIG_ANNOTATIONS = Set.of(
            "Configuration", "ConfigurationProperties"
    );
    private static final Set<String> ENTITY_ANNOTATIONS = Set.of(
            "Entity", "Document", "Table", "MongoDocument"
    );

    private final JavaParser parser;

    public JavaAstParser() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        this.parser = new JavaParser(config);
    }

    public ProjectClass parse(Path javaFile) throws IOException {
        String source = Files.readString(javaFile);
        return parse(source, javaFile.toString());
    }

    public ProjectClass parse(String source, String filePath) {
        Optional<CompilationUnit> result = parser.parse(source).getResult();
        if (result.isEmpty()) return null;
        CompilationUnit cu = result.get();

        String packageName = cu.getPackageDeclaration()
                .map(p -> p.getNameAsString())
                .orElse("");

        for (TypeDeclaration<?> td : cu.getTypes()) {
            if (td instanceof ClassOrInterfaceDeclaration cid) {
                return buildFromClassOrInterface(cid, packageName, filePath);
            }
            if (td instanceof EnumDeclaration ed) {
                return buildFromEnum(ed, packageName, filePath);
            }
            if (td instanceof RecordDeclaration rd) {
                return buildFromRecord(rd, packageName, filePath);
            }
        }
        return null;
    }

    private ProjectClass buildFromClassOrInterface(ClassOrInterfaceDeclaration decl,
                                                   String packageName, String filePath) {
        String name = decl.getNameAsString();
        String fqn = packageName.isEmpty() ? name : packageName + "." + name;
        Kind kind = decl.isInterface() ? Kind.INTERFACE : Kind.CLASS;
        List<String> annotations = collectAnnotations(decl.getAnnotations());
        Role role = detectRole(annotations);
        String superClass = "";
        if (!decl.getExtendedTypes().isEmpty()) {
            superClass = decl.getExtendedTypes(0).getNameAsString();
        }
        List<String> ifaces = new ArrayList<>();
        decl.getImplementedTypes().forEach(t -> ifaces.add(t.getNameAsString()));
        List<String> modifiers = new ArrayList<>();
        decl.getModifiers().forEach(m -> modifiers.add(m.getKeyword().asString()));
        List<ProjectMethod> methods = new ArrayList<>();
        List<ProjectField> fields = new ArrayList<>();
        for (BodyDeclaration<?> member : decl.getMembers()) {
            if (member instanceof MethodDeclaration md) methods.add(toProjectMethod(md));
            else if (member instanceof FieldDeclaration fd) fields.add(toProjectField(fd));
        }
        return ProjectClass.builder()
                .name(name).fullyQualifiedName(fqn).packageName(packageName).filePath(filePath)
                .kind(kind).role(role).modifiers(modifiers).annotations(annotations)
                .methods(methods).fields(fields)
                .superClass(superClass.isEmpty() ? null : superClass).interfaces(ifaces).build();
    }

    private ProjectClass buildFromEnum(EnumDeclaration decl, String packageName, String filePath) {
        String name = decl.getNameAsString();
        String fqn = packageName.isEmpty() ? name : packageName + "." + name;
        List<String> annotations = collectAnnotations(decl.getAnnotations());
        List<String> modifiers = new ArrayList<>();
        decl.getModifiers().forEach(m -> modifiers.add(m.getKeyword().asString()));
        List<ProjectMethod> methods = new ArrayList<>();
        List<ProjectField> fields = new ArrayList<>();
        for (BodyDeclaration<?> member : decl.getMembers()) {
            if (member instanceof MethodDeclaration md) methods.add(toProjectMethod(md));
            else if (member instanceof FieldDeclaration fd) fields.add(toProjectField(fd));
        }
        List<String> ifaces = new ArrayList<>();
        decl.getImplementedTypes().forEach(t -> ifaces.add(t.getNameAsString()));
        return ProjectClass.builder()
                .name(name).fullyQualifiedName(fqn).packageName(packageName).filePath(filePath)
                .kind(Kind.ENUM).role(Role.NONE).modifiers(modifiers).annotations(annotations)
                .methods(methods).fields(fields).interfaces(ifaces).build();
    }

    private ProjectClass buildFromRecord(RecordDeclaration decl, String packageName, String filePath) {
        String name = decl.getNameAsString();
        String fqn = packageName.isEmpty() ? name : packageName + "." + name;
        List<String> annotations = collectAnnotations(decl.getAnnotations());
        List<String> modifiers = new ArrayList<>();
        decl.getModifiers().forEach(m -> modifiers.add(m.getKeyword().asString()));
        List<ProjectMethod> methods = new ArrayList<>();
        List<ProjectField> fields = new ArrayList<>();
        for (BodyDeclaration<?> member : decl.getMembers()) {
            if (member instanceof MethodDeclaration md) methods.add(toProjectMethod(md));
            else if (member instanceof FieldDeclaration fd) fields.add(toProjectField(fd));
        }
        decl.getParameters().forEach(p -> fields.add(ProjectField.builder()
                .name(p.getNameAsString()).type(p.getTypeAsString()).annotations(List.of()).build()));
        return ProjectClass.builder()
                .name(name).fullyQualifiedName(fqn).packageName(packageName).filePath(filePath)
                .kind(Kind.RECORD).role(Role.NONE).modifiers(modifiers).annotations(annotations)
                .methods(methods).fields(fields).build();
    }

    private List<String> collectAnnotations(List<AnnotationExpr> annotations) {
        List<String> result = new ArrayList<>();
        for (AnnotationExpr ann : annotations) result.add(ann.getNameAsString());
        return result;
    }

    private Role detectRole(List<String> annotations) {
        for (String ann : annotations) if (CONTROLLER_ANNOTATIONS.contains(ann)) return Role.CONTROLLER;
        for (String ann : annotations) if (SERVICE_ANNOTATIONS.contains(ann)) return Role.SERVICE;
        for (String ann : annotations) if (REPOSITORY_ANNOTATIONS.contains(ann)) return Role.REPOSITORY;
        for (String ann : annotations) if (CONFIG_ANNOTATIONS.contains(ann)) return Role.CONFIGURATION;
        for (String ann : annotations) if (ENTITY_ANNOTATIONS.contains(ann)) return Role.ENTITY;
        return Role.NONE;
    }

    private ProjectMethod toProjectMethod(MethodDeclaration decl) {
        String httpMethod = null;
        String httpPath = null;
        for (AnnotationExpr ann : decl.getAnnotations()) {
            String name = ann.getNameAsString();
            if (Set.of("GetMapping", "PostMapping", "PutMapping", "DeleteMapping", "PatchMapping").contains(name)) {
                httpMethod = extractHttpMethod(name);
                httpPath = extractPath(ann);
            } else if ("RequestMapping".equals(name)) {
                httpMethod = extractRequestMethod(ann);
                httpPath = extractPath(ann);
            }
        }
        return ProjectMethod.builder()
                .name(decl.getNameAsString())
                .returnType(decl.getTypeAsString())
                .parameterTypes(decl.getParameters().stream().map(p -> p.getTypeAsString()).toList())
                .annotations(collectAnnotations(decl.getAnnotations()))
                .httpMethod(httpMethod).httpPath(httpPath).build();
    }

    private String extractHttpMethod(String mappingAnnotation) {
        return switch (mappingAnnotation) {
            case "GetMapping" -> "GET";
            case "PostMapping" -> "POST";
            case "PutMapping" -> "PUT";
            case "DeleteMapping" -> "DELETE";
            case "PatchMapping" -> "PATCH";
            default -> null;
        };
    }

    private String extractRequestMethod(AnnotationExpr ann) {
        if (ann instanceof NormalAnnotationExpr ne) {
            for (MemberValuePair pair : ne.getPairs()) {
                if ("method".equals(pair.getNameAsString())) {
                    String val = pair.getValue().toString();
                    if (val.contains("GET")) return "GET";
                    if (val.contains("POST")) return "POST";
                    if (val.contains("PUT")) return "PUT";
                    if (val.contains("DELETE")) return "DELETE";
                    if (val.contains("PATCH")) return "PATCH";
                }
            }
        }
        return "GET";
    }

    private String extractPath(AnnotationExpr ann) {
        if (ann instanceof NormalAnnotationExpr ne) {
            for (MemberValuePair pair : ne.getPairs()) {
                String n = pair.getNameAsString();
                if ("value".equals(n) || "path".equals(n)) {
                    String val = pair.getValue().toString().replace('"', ' ').trim();
                    return val.startsWith("/") ? val : "/" + val;
                }
            }
        }
        if (ann instanceof SingleMemberAnnotationExpr sm) {
            String val = sm.getMemberValue().toString().replace('"', ' ').trim();
            return val.startsWith("/") ? val : "/" + val;
        }
        return "";
    }

    private ProjectField toProjectField(FieldDeclaration decl) {
        List<String> annotations = collectAnnotations(decl.getAnnotations());
        String typeStr = decl.getVariables().isEmpty() ? "Object" : decl.getVariable(0).getTypeAsString();
        String nameStr = decl.getVariables().isEmpty() ? "" : decl.getVariable(0).getNameAsString();
        return ProjectField.builder().name(nameStr).type(typeStr).annotations(annotations).build();
    }
}
