package com.shreeai.os.platform.kernels.planning.analyzer;

import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult.Complexity;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult.Domain;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult.PlanningType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Deterministic planning intent analyzer.
 *
 * <p>Extracts structured planning metadata from user input without LLM:
 * domain, planning type, complexity, duration estimate, keywords, and constraints.</p>
 *
 * <p>Supported domains: JAVA, SPRING, AI, SAAS, FITNESS, EDUCATION, GENERAL</p>
 *
 * @since Sprint-11
 */
public final class PlanningAnalyzer {

    // Domain detection patterns
    private static final Map<Domain, List<Pattern>> DOMAIN_PATTERNS = Map.of(
            Domain.JAVA,   List.of(
                    Pattern.compile("\\bjava\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bjdk\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bjvm\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bjava\\s+developer\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bjakarta\\b", Pattern.CASE_INSENSITIVE)
            ),
            Domain.SPRING, List.of(
                    Pattern.compile("\\bspring\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bspringboot\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bspring\\s+boot\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bspring\\s+cloud\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bspring\\s+security\\b", Pattern.CASE_INSENSITIVE)
            ),
            Domain.AI,     List.of(
                    Pattern.compile("\\bai\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bmachine\\s+learning\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bml\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bllm\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bneural\\s+network\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bdeep\\s+learning\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bai\\s+agent\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bai\\s+assistant\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bai\\s+os\\b", Pattern.CASE_INSENSITIVE)
            ),
            Domain.SAAS,   List.of(
                    Pattern.compile("\\bsaas\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bsoftware\\s+as\\s+a\\s+service\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bproduct\\s+roadmap\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bsubscription\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bmulti.?tenant\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bmarketplace\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bmrr\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bchurn\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bstartup\\b", Pattern.CASE_INSENSITIVE)
            ),
            Domain.FITNESS, List.of(
                    Pattern.compile("\\bfitness\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bworkout\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bgym\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\btraining\\s+plan\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bweight\\s*loss\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bmuscle\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bstrength\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bmarathon\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bendurance\\b", Pattern.CASE_INSENSITIVE)
            ),
            Domain.EDUCATION, List.of(
                    Pattern.compile("\\beducation\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bprofessor\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bcourse\\s+plan\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\blearning\\s+plan\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bacademic\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bteaching\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bstudent\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\buniversity\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bcurriculum\\b", Pattern.CASE_INSENSITIVE)
            ),
            Domain.GENERAL, List.of()
    );

    // Planning type detection
    private static final Map<PlanningType, List<Pattern>> TYPE_PATTERNS = Map.of(
            PlanningType.ROADMAP, List.of(
                    Pattern.compile("\\broadmap\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\blearning\\s+path\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bcareer\\s+path\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bjourney\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bfrom\\s+beginner\\b", Pattern.CASE_INSENSITIVE)
            ),
            PlanningType.PROJECT, List.of(
                    Pattern.compile("\\bproject\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bbuild\\s+a\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bcreate\\s+a\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bapp\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bapplication\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bsystem\\b", Pattern.CASE_INSENSITIVE)
            ),
            PlanningType.LEARNING, List.of(
                    Pattern.compile("\\blearn\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bmaster\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bstudy\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bskill\\s+up\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bupskill\\b", Pattern.CASE_INSENSITIVE)
            ),
            PlanningType.CAREER, List.of(
                    Pattern.compile("\\bcareer\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bjob\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\binterview\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bdeveloper\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bengineer\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bsenior\\b", Pattern.CASE_INSENSITIVE)
            ),
            PlanningType.FITNESS, List.of(
                    Pattern.compile("\\bfitness\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bworkout\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\btraining\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bgym\\b", Pattern.CASE_INSENSITIVE)
            ),
            PlanningType.BUSINESS, List.of(
                    Pattern.compile("\\bsaas\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bstartup\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bbusiness\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bproduct\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\brevenue\\b", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\bgo.?to.?market\\b", Pattern.CASE_INSENSITIVE)
            ),
            PlanningType.GENERAL, List.of()
    );

    // Complexity indicators
    private static final List<Pattern> HIGH_COMPLEXITY_PATTERNS = List.of(
            Pattern.compile("\\badvanced\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bexpert\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bsenior\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bmastery\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bprofessional\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\benterprise\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\barchitecture\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bproduction\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bcomprehensive\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bfull.?stack\\b", Pattern.CASE_INSENSITIVE)
    );

    private static final List<Pattern> LOW_COMPLEXITY_PATTERNS = List.of(
            Pattern.compile("\\bbeginner\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bintro\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bgetting\\s+started\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bbasics\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bfundamentals\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bquick\\s+start\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bhello\\s+world\\b", Pattern.CASE_INSENSITIVE)
    );

    // Duration indicators (weeks)
    private static final Pattern WEEKS_PATTERN = Pattern.compile(
            "(\\d+)[\\s\\-]*(?:weeks?|wks?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MONTHS_PATTERN = Pattern.compile(
            "(\\d+)\\s*(?:months?|mos?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DAYS_PATTERN = Pattern.compile(
            "(\\d+)\\s*(?:days?)\\s*(?:plan)?", Pattern.CASE_INSENSITIVE);

    // Timeline indicators (default estimates)
    private static final Map<Domain, Integer> DOMAIN_DEFAULT_WEEKS = Map.of(
            Domain.JAVA, 12,
            Domain.SPRING, 8,
            Domain.AI, 16,
            Domain.SAAS, 24,
            Domain.FITNESS, 12,
            Domain.EDUCATION, 16,
            Domain.GENERAL, 8
    );

    // Keyword extraction patterns
    private static final Pattern KEYWORD_PATTERN = Pattern.compile(
            "\\b[a-z][a-z0-9+#.\\-]{2,30}\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Words to exclude from keywords (no duplicates)
    private static final java.util.Set<String> STOP_WORDS = Set.of(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "as", "is", "was", "are", "were", "be",
            "been", "being", "have", "has", "had", "do", "does", "did", "will",
            "would", "could", "should", "may", "might", "must", "shall", "can",
            "need", "dare", "ought", "used", "create", "build", "make", "plan",
            "design", "develop", "generate", "prepare", "set", "up", "organize",
            "organise", "how", "what", "when", "where", "why", "which", "who",
            "roadmap", "learning", "path", "guide", "overview", "complete",
            "full", "new", "get", "want", "like", "help", "looking", "trying",
            "use", "using", "based", "know", "into", "also", "via", "per",
            "each", "all"
    );

    /**
     * Analyzes the given planning input and returns structured metadata.
     *
     * @param input the user's planning request text
     * @return immutable planning analysis result
     */
    public PlanningAnalysisResult analyze(String input) {
        if (input == null || input.isBlank()) {
            return new PlanningAnalysisResult(
                    Domain.GENERAL, PlanningType.GENERAL,
                    Complexity.MEDIUM, 4, List.of(), List.of(), input, Map.of()
            );
        }

        String text = input.trim();

        Domain domain = detectDomain(text);
        PlanningType planningType = detectPlanningType(text, domain);
        Complexity complexity = detectComplexity(text);
        int weeks = detectDuration(text, domain);
        List<String> keywords = extractKeywords(text);
        List<String> constraints = extractConstraints(text);

        Map<String, Object> meta = Map.of(
                "originalInput", text,
                "analyzerVersion", "1.0"
        );

        return new PlanningAnalysisResult(
                domain, planningType, complexity, weeks,
                keywords, constraints, text, meta
        );
    }

    private Domain detectDomain(String text) {
        int maxMatches = 0;
        Domain bestDomain = Domain.GENERAL;

        for (Map.Entry<Domain, List<Pattern>> entry : DOMAIN_PATTERNS.entrySet()) {
            if (entry.getKey() == Domain.GENERAL) continue;
            int matches = countMatches(text, entry.getValue());
            if (matches > maxMatches) {
                maxMatches = matches;
                bestDomain = entry.getKey();
            }
        }
        return bestDomain;
    }

    private PlanningType detectPlanningType(String text, Domain domain) {
        // Domain-specific defaults
        if (domain == Domain.FITNESS) return PlanningType.FITNESS;
        if (domain == Domain.SAAS) return PlanningType.BUSINESS;

        // ROADMAP detection has highest priority — a "roadmap" keyword
        // in the input should always produce ROADMAP planning type
        if (countMatches(text, TYPE_PATTERNS.get(PlanningType.ROADMAP)) > 0) {
            return PlanningType.ROADMAP;
        }

        int maxMatches = 0;
        PlanningType bestType = PlanningType.GENERAL;

        for (Map.Entry<PlanningType, List<Pattern>> entry : TYPE_PATTERNS.entrySet()) {
            if (entry.getKey() == PlanningType.GENERAL) continue;
            if (entry.getKey() == PlanningType.ROADMAP) continue; // already checked
            int matches = countMatches(text, entry.getValue());
            if (matches > maxMatches) {
                maxMatches = matches;
                bestType = entry.getKey();
            }
        }
        return bestType;
    }

    private Complexity detectComplexity(String text) {
        int highMatches = countMatches(text, HIGH_COMPLEXITY_PATTERNS);
        int lowMatches = countMatches(text, LOW_COMPLEXITY_PATTERNS);

        if (highMatches > lowMatches && highMatches >= 2) return Complexity.HIGH;
        if (lowMatches > highMatches && lowMatches >= 1) return Complexity.LOW;
        return Complexity.MEDIUM;
    }

    private int detectDuration(String text, Domain domain) {
        var weeksMatcher = WEEKS_PATTERN.matcher(text);
        if (weeksMatcher.find()) {
            return Math.max(1, Integer.parseInt(weeksMatcher.group(1)));
        }

        var monthsMatcher = MONTHS_PATTERN.matcher(text);
        if (monthsMatcher.find()) {
            return Math.max(4, Integer.parseInt(monthsMatcher.group(1)) * 4);
        }

        var daysMatcher = DAYS_PATTERN.matcher(text);
        if (daysMatcher.find()) {
            return Math.max(1, Integer.parseInt(daysMatcher.group(1)) / 7);
        }

        return DOMAIN_DEFAULT_WEEKS.getOrDefault(domain, 8);
    }

    private List<String> extractKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        var matcher = KEYWORD_PATTERN.matcher(text.toLowerCase(Locale.ROOT));

        while (matcher.find()) {
            String word = matcher.group().toLowerCase(Locale.ROOT);
            // Allow "ai", "ml", "llm" as special 2-3 char tech keywords
            if (word.length() >= 2 && !STOP_WORDS.contains(word)) {
                if (!keywords.contains(word)) {
                    keywords.add(word);
                }
            }
        }

        return List.copyOf(keywords);
    }

    private List<String> extractConstraints(String text) {
        List<String> constraints = new ArrayList<>();

        if (text.contains("without")) constraints.add("Constraint: exclude " + extractAfter(text, "without"));
        if (text.contains("only")) constraints.add("Constraint: only " + extractAfter(text, "only"));
        if (text.contains("budget")) constraints.add("Budget constraint detected");
        if (text.contains("deadline")) constraints.add("Deadline constraint detected");
        if (text.contains("remote")) constraints.add("Remote work required");

        return List.copyOf(constraints);
    }

    private String extractAfter(String text, String keyword) {
        int idx = text.toLowerCase(Locale.ROOT).indexOf(keyword);
        if (idx >= 0 && idx + keyword.length() < text.length()) {
            String after = text.substring(idx + keyword.length()).trim();
            int end = after.indexOf(' ');
            return end > 0 ? after.substring(0, end) : after;
        }
        return "";
    }

    private int countMatches(String text, List<Pattern> patterns) {
        int count = 0;
        for (Pattern p : patterns) {
            if (p.matcher(text).find()) count++;
        }
        return count;
    }
}
