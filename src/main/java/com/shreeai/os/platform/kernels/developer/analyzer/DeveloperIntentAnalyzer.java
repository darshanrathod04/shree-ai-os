package com.shreeai.os.platform.kernels.developer.analyzer;

import com.shreeai.os.platform.kernels.developer.api.DeveloperIntent;
import com.shreeai.os.platform.kernels.developer.api.DeveloperIntentType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class DeveloperIntentAnalyzer {

    private static final List<Pattern> ADD_FEATURE_PATTERNS = List.of(
            Pattern.compile("(?i)\\badd\\s+(jwt|authentication|authorization|feature)\\b"),
            Pattern.compile("(?i)\\bimplement\\s+"),
            Pattern.compile("(?i)\\badd\\s+(user|product|customer|order)\\s+"),
            Pattern.compile("(?i)\\bintroduce\\s+"),
            Pattern.compile("(?i)\\benable\\s+")
    );

    private static final List<Pattern> REFACTOR_PATTERNS = List.of(
            Pattern.compile("(?i)\\brefactor(ing|ed)?\\b"),
            Pattern.compile("(?i)\\brename\\s+"),
            Pattern.compile("(?i)\\bextract\\s+(service|layer|method)\\b"),
            Pattern.compile("(?i)\\bmove\\s+(class|method)\\b"),
            Pattern.compile("(?i)\\breorganize\\b"),
            Pattern.compile("(?i)\\brestructure\\b"),
            Pattern.compile("(?i)\\bclean\\s+up\\b"),
            Pattern.compile("(?i)\\bsimplify\\b"),
            Pattern.compile("(?i)\\bdecouple\\b")
    );

    private static final List<Pattern> FIX_BUG_PATTERNS = List.of(
            Pattern.compile("(?i)\\bfix(ed|ing)?\\b"),
            Pattern.compile("(?i)\\bpatch\\b"),
            Pattern.compile("(?i)\\bhotfix\\b"),
            Pattern.compile("(?i)\\bhandle\\s+(null|exception|error)\\b"),
            Pattern.compile("(?i)\\bnull\\s*pointer\\b"),
            Pattern.compile("(?i)\\brace\\s+condition\\b"),
            Pattern.compile("(?i)\\bresolve\\s+(the\\s+)?issue\\b")
    );

    private static final List<Pattern> OPTIMIZE_PATTERNS = List.of(
            Pattern.compile("(?i)\\boptimi[sz](e[sd]?|ing)?\\b"),
            Pattern.compile("(?i)\\bperformance\\b"),
            Pattern.compile("(?i)\\bcach(e[sd]?|ing)\\b"),
            Pattern.compile("(?i)\\blazy\\s+load\\b"),
            Pattern.compile("(?i)\\bspeed\\s+up\\b"),
            Pattern.compile("(?i)\\basync\\b")
    );

    private static final List<Pattern> CREATE_API_PATTERNS = List.of(
            Pattern.compile("(?i)\\bcreate\\s+(a\\s+)?(new\\s+)?(rest|api|endpoint|graphql)\\b"),
            Pattern.compile("(?i)\\badd\\s+(a\\s+)?(new\\s+)?(rest|api|endpoint)\\b"),
            Pattern.compile("(?i)\\bexpose\\s+(a\\s+)?(new\\s+)?(rest|api|endpoint)\\b"),
            Pattern.compile("(?i)\\bnew\\s+controller\\b"),
            Pattern.compile("(?i)\\b(rest|api|endpoint)\\b")
    );

    private static final List<Pattern> ADD_ENTITY_PATTERNS = List.of(
            Pattern.compile("(?i)\\badd\\s+(a\\s+)?entity\\b"),
            Pattern.compile("(?i)\\bnew\\s+model\\b"),
            Pattern.compile("(?i)\\bcreate\\s+(a\\s+)?(product|user|order|entity)\\b"),
            Pattern.compile("(?i)\\bdomain\\s+object\\b"),
            Pattern.compile("(?i)\\badd\\s+(a\\s+)?dto\\b")
    );

    private static final List<Pattern> SECURITY_PATTERNS = List.of(
            Pattern.compile("(?i)\\bjwt\\b"),
            Pattern.compile("(?i)\\boauth\\b"),
            Pattern.compile("(?i)\\bsecurity\\b"),
            Pattern.compile("(?i)\\bauthentication\\b"),
            Pattern.compile("(?i)\\bauthorization\\b"),
            Pattern.compile("(?i)\\bcors\\b"),
            Pattern.compile("(?i)\\bsecure\\b")
    );

    private static final List<Pattern> DATABASE_PATTERNS = List.of(
            Pattern.compile("(?i)\\bdatabase\\b"),
            Pattern.compile("(?i)\\brepository\\b"),
            Pattern.compile("(?i)\\bjpa\\b"),
            Pattern.compile("(?i)\\bmigration\\b"),
            Pattern.compile("(?i)\\bschema\\b")
    );

    private static final Set<String> STOP_WORDS = Set.of(
            "REST", "API", "DAO", "DTO", "VO", "POJO",
            "HTTP", "SQL", "JSON", "XML", "OAuth", "CORS", "CRUD", "JPA",
            "Refactor", "Add", "Create", "Fix", "Patch", "Hotfix",
            "Implement", "Enable", "Support", "Introduce", "Optimize",
            "Secure", "Rename", "Extract", "Move", "Clean", "Simplify"
    );

    private static final Pattern CAMEL_CASE = Pattern.compile(
            "\\b[A-Z][a-z]+(?:[A-Z][a-z]+)*\\b|\\b[A-Z]{2,}\\b");

    private static final Map<String, String> DOMAIN_MAP = Map.ofEntries(
            Map.entry("(?i)\\bspring\\s*security\\b", "Spring Security"),
            Map.entry("(?i)\\bspring\\b", "Spring"),
            Map.entry("(?i)\\bjpa\\b", "Persistence"),
            Map.entry("(?i)\\brest\\b", "REST API"),
            Map.entry("(?i)\\bgraphql\\b", "GraphQL API"),
            Map.entry("(?i)\\bjwt\\b", "Authentication"),
            Map.entry("(?i)\\boauth\\b", "OAuth2"),
            Map.entry("(?i)\\bmongodb\\b", "MongoDB"),
            Map.entry("(?i)\\bredis\\b", "Redis")
    );

    public DeveloperIntent analyze(String request) {
        if (request == null || request.isBlank()) {
            return DeveloperIntent.builder()
                    .originalRequest("")
                    .intent(DeveloperIntentType.ADD_FEATURE)
                    .confidence(0.0)
                    .build();
        }
        String input = request.trim();
        Map<DeveloperIntentType, Integer> scores = new LinkedHashMap<>();
        for (DeveloperIntentType type : DeveloperIntentType.values()) {
            scores.put(type, score(type, input));
        }
        DeveloperIntentType primary = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DeveloperIntentType.ADD_FEATURE);
        String entity = extractEntity(input);
        String domain = extractDomain(input);
        List<String> tokens = extractTokens(input);
        double confidence = computeConfidence(scores.get(primary), scores.values());
        return DeveloperIntent.builder()
                .originalRequest(input)
                .intent(primary)
                .action(primary.name())
                .entity(entity)
                .domain(domain)
                .tokens(tokens)
                .attributes(buildAttributes(input, primary, entity))
                .confidence(confidence)
                .build();
    }

    private int score(DeveloperIntentType type, String input) {
        List<Pattern> patterns = patternsFor(type);
        int s = 0;
        for (Pattern p : patterns) {
            if (p.matcher(input).find()) s++;
        }
        if (type == DeveloperIntentType.SECURITY) {
            if (Pattern.compile("(?i)\\bjwt\\b").matcher(input).find()) s += 2;
            if (Pattern.compile("(?i)\\boauth\\b").matcher(input).find()) s += 2;
        }
        if (type == DeveloperIntentType.DATABASE) {
            if (Pattern.compile("(?i)\\brepository\\b").matcher(input).find()) s += 2;
            if (Pattern.compile("(?i)\\bjpa\\b").matcher(input).find()) s += 2;
        }
        return s;
    }

    private List<Pattern> patternsFor(DeveloperIntentType type) {
        return switch (type) {
            case ADD_FEATURE -> ADD_FEATURE_PATTERNS;
            case REFACTOR -> REFACTOR_PATTERNS;
            case FIX_BUG -> FIX_BUG_PATTERNS;
            case OPTIMIZE -> OPTIMIZE_PATTERNS;
            case CREATE_API -> CREATE_API_PATTERNS;
            case ADD_ENTITY -> ADD_ENTITY_PATTERNS;
            case SECURITY -> SECURITY_PATTERNS;
            case DATABASE -> DATABASE_PATTERNS;
        };
    }

    private String extractEntity(String input) {
        java.util.regex.Matcher m = CAMEL_CASE.matcher(input);
        while (m.find()) {
            String token = m.group();
            if (!STOP_WORDS.contains(token)) return token;
        }
        return "";
    }

    private String extractDomain(String input) {
        for (Map.Entry<String, String> entry : DOMAIN_MAP.entrySet()) {
            if (Pattern.compile(entry.getKey()).matcher(input).find()) {
                return entry.getValue();
            }
        }
        return "General";
    }

    private List<String> extractTokens(String input) {
        List<String> tokens = new ArrayList<>();
        java.util.regex.Matcher m = CAMEL_CASE.matcher(input);
        while (m.find()) {
            String token = m.group();
            if (!STOP_WORDS.contains(token)) tokens.add(token);
        }
        return tokens;
    }

    private Map<String, String> buildAttributes(String input, DeveloperIntentType type, String entity) {
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("intent", type.name());
        if (!entity.isEmpty()) attrs.put("entity", entity);
        if (Pattern.compile("(?i)\\brequire\\s+").matcher(input).find())
            attrs.put("requiresNewFiles", "true");
        if (Pattern.compile("(?i)\\brefactor\\b").matcher(input).find())
            attrs.put("modifiesExistingFiles", "true");
        if (Pattern.compile("(?i)\\btest\\b").matcher(input).find())
            attrs.put("includesTests", "true");
        return attrs;
    }

    private double computeConfidence(int topScore, Iterable<Integer> allScores) {
        if (topScore == 0) return 0.3;
        int runnerUp = 0;
        for (int s : allScores) {
            if (s < topScore && s > runnerUp) runnerUp = s;
        }
        double spread = topScore - runnerUp;
        return Math.min(0.95, 0.5 + (spread * 0.1));
    }
}