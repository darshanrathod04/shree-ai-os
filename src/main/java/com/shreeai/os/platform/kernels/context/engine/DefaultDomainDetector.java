package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.DomainCandidate;
import com.shreeai.os.platform.kernels.context.model.DomainProfile;
import com.shreeai.os.platform.kernels.context.model.PrimaryDomain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DefaultDomainDetector</b>
 *
 * <p>Deterministic keyword-based implementation of {@link DomainDetector}.</p>
 *
 * <p><b>Determinism Guarantee:</b> Same input always produces identical output.</p>
 * <p><b>Thread Safety:</b> This class is thread-safe. It maintains no mutable state.</p>
 *
 * @see DomainDetector
 * @see DomainProfile
 */
public final class DefaultDomainDetector implements DomainDetector {

    private static final String DETECTION_METHOD = "keyword-based-deterministic";
    private static final double MIN_CONFIDENCE_THRESHOLD = 0.08;
    private static final Map<PrimaryDomain, Map<String, Double>> KEYWORD_MAPPINGS = createKeywordMappings();

    public DefaultDomainDetector() {
    }

    @Override
    public DomainProfile detect(String userInput) {
        Objects.requireNonNull(userInput, "userInput must not be null");

        String normalizedInput = userInput.toLowerCase().trim();

        if (normalizedInput.isEmpty()) {
            return DomainProfile.unknown(DETECTION_METHOD);
        }

        Map<PrimaryDomain, Double> scores = calculateDomainScores(normalizedInput);
        List<DomainCandidate> candidates = buildRankedCandidates(scores);

        if (candidates.isEmpty()) {
            return DomainProfile.general(DETECTION_METHOD);
        }

        DomainCandidate primary = candidates.get(0);
        List<DomainCandidate> detectedDomains = candidates;

        return new DomainProfile(
                primary.domain(),
                primary.confidence(),
                detectedDomains,
                Instant.now(),
                DETECTION_METHOD
        );
    }

    private Map<PrimaryDomain, Double> calculateDomainScores(String normalizedInput) {
        Map<PrimaryDomain, Double> scores = new LinkedHashMap<>();

        for (Map.Entry<PrimaryDomain, Map<String, Double>> entry : KEYWORD_MAPPINGS.entrySet()) {
            PrimaryDomain domain = entry.getKey();
            Map<String, Double> keywords = entry.getValue();

            double totalScore = 0.0;
            int matchCount = 0;

            for (Map.Entry<String, Double> keywordEntry : keywords.entrySet()) {
                String keyword = keywordEntry.getKey();
                double weight = keywordEntry.getValue();

                if (containsWholeWord(normalizedInput, keyword)) {
                    totalScore += weight;
                    matchCount++;
                }
            }

            double confidence = calculateConfidence(totalScore, matchCount, keywords.size());
            scores.put(domain, confidence);
        }

        return scores;
    }

    private double calculateConfidence(double totalScore, int matchCount, int totalKeywords) {
        if (matchCount == 0) {
            return 0.0;
        }

        double maxKeywordWeight = KEYWORD_MAPPINGS.values().stream()
                .flatMap(m -> m.values().stream())
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1.0);

        double normalizedScore = totalScore / maxKeywordWeight;
        double matchRatio = (double) matchCount / totalKeywords;
        double confidence = (normalizedScore * 0.6 + matchRatio * 0.4);

        return Math.min(1.0, Math.max(0.0, confidence));
    }

    private List<DomainCandidate> buildRankedCandidates(Map<PrimaryDomain, Double> scores) {
        List<DomainCandidate> candidates = new ArrayList<>();

        for (Map.Entry<PrimaryDomain, Double> entry : scores.entrySet()) {
            double confidence = entry.getValue();
            if (confidence >= MIN_CONFIDENCE_THRESHOLD) {
                candidates.add(new DomainCandidate(entry.getKey(), roundConfidence(confidence)));
            }
        }

        candidates.sort((a, b) -> {
            int cmp = Double.compare(b.confidence(), a.confidence());
            if (cmp != 0) return cmp;
            return a.domain().compareTo(b.domain());
        });

        return candidates;
    }

    private double roundConfidence(double confidence) {
        return Math.round(confidence * 1000.0) / 1000.0;
    }

    private boolean containsWholeWord(String input, String keyword) {
        String regex = "\\b" + keyword + "\\b";
        return input.matches(".*" + regex + ".*");
    }

    private static Map<PrimaryDomain, Map<String, Double>> createKeywordMappings() {
        Map<PrimaryDomain, Map<String, Double>> mappings = new LinkedHashMap<>();
        addJavaKeywords(mappings);
        addSpringKeywords(mappings);
        addDatabaseKeywords(mappings);
        addAIKeywords(mappings);
        addWebKeywords(mappings);
        addMobileKeywords(mappings);
        addDevOpsKeywords(mappings);
        addCloudKeywords(mappings);
        addSecurityKeywords(mappings);
        addFinanceKeywords(mappings);
        addMedicalKeywords(mappings);
        addEducationKeywords(mappings);
        return Map.copyOf(mappings);
    }

    private static void addJavaKeywords(Map<PrimaryDomain, Map<String, Double>> mappings) {
        mappings.put(PrimaryDomain.JAVA, Map.ofEntries(
                Map.entry("java", 1.0), Map.entry("jvm", 0.9), Map.entry("jdk", 0.9),
                Map.entry("jre", 0.8), Map.entry("maven", 0.8), Map.entry("gradle", 0.8),
                Map.entry("servlet", 0.7), Map.entry("jsp", 0.7), Map.entry("jpa", 0.7),
                Map.entry("hibernate", 0.7), Map.entry("jdbc", 0.7), Map.entry("javafx", 0.7),
                Map.entry("multithreading", 0.6), Map.entry("concurrency", 0.6), Map.entry("stream", 0.4)
        ));
    }

    private static void addSpringKeywords(Map<PrimaryDomain, Map<String, Double>> mappings) {
        mappings.put(PrimaryDomain.SPRING, Map.ofEntries(
                Map.entry("spring", 1.0), Map.entry("spring boot", 1.0), Map.entry("springboot", 1.0),
                Map.entry("spring mvc", 0.9), Map.entry("spring security", 0.9), Map.entry("spring data", 0.9),
                Map.entry("spring cloud", 0.9), Map.entry("spring batch", 0.8), Map.entry("thymeleaf", 0.7),
                Map.entry("bean", 0.4), Map.entry("autowired", 0.6), Map.entry("controller", 0.4)
        ));
    }

    private static void addDatabaseKeywords(Map<PrimaryDomain, Map<String, Double>> mappings) {
        mappings.put(PrimaryDomain.DATABASE, Map.ofEntries(
                Map.entry("database", 1.0), Map.entry("db", 0.8), Map.entry("sql", 1.0),
                Map.entry("mysql", 0.9), Map.entry("postgresql", 0.9), Map.entry("postgres", 0.9),
                Map.entry("oracle", 0.9), Map.entry("mongodb", 0.9), Map.entry("nosql", 0.8),
                Map.entry("redis", 0.8), Map.entry("elasticsearch", 0.8), Map.entry("cassandra", 0.8),
                Map.entry("table", 0.4), Map.entry("query", 0.5), Map.entry("schema", 0.5)
        ));
    }

    private static void addAIKeywords(Map<PrimaryDomain, Map<String, Double>> mappings) {
        mappings.put(PrimaryDomain.AI, Map.ofEntries(
                Map.entry("ai", 1.0), Map.entry("artificial intelligence", 1.0), Map.entry("machine learning", 1.0),
                Map.entry("ml", 0.9), Map.entry("deep learning", 0.9), Map.entry("neural network", 0.9),
                Map.entry("nlp", 0.8), Map.entry("natural language processing", 0.9), Map.entry("computer vision", 0.9),
                Map.entry("tensorflow", 0.9), Map.entry("pytorch", 0.9), Map.entry("model", 0.4),
                Map.entry("training", 0.5), Map.entry("dataset", 0.6), Map.entry("algorithm", 0.4)
        ));
    }

    private static void addWebKeywords(Map<PrimaryDomain, Map<String, Double>> mappings) {
        mappings.put(PrimaryDomain.WEB, Map.ofEntries(
                Map.entry("web", 0.8), Map.entry("html", 1.0), Map.entry("css", 1.0),
                Map.entry("javascript", 1.0), Map.entry("typescript", 0.9),
                Map.entry("react", 0.9), Map.entry("angular", 0.9), Map.entry("vue", 0.9),
                Map.entry("nodejs", 0.8), Map.entry("express", 0.7),
                Map.entry("rest", 0.6), Map.entry("api", 0.5), Map.entry("http", 0.6)
        ));
    }

    private static void addMobileKeywords(Map<PrimaryDomain, Map<String, Double>> mappings) {
        mappings.put(PrimaryDomain.MOBILE, Map.ofEntries(
                Map.entry("mobile", 1.0), Map.entry("android", 1.0), Map.entry("ios", 1.0),
                Map.entry("swift", 0.9), Map.entry("kotlin", 0.8), Map.entry("flutter", 0.9),
                Map.entry("react native", 0.9), Map.entry("app", 0.5),
                Map.entry("smartphone", 0.8), Map.entry("responsive", 0.6)
        ));
    }

    private static void addDevOpsKeywords(Map<PrimaryDomain, Map<String, Double>> mappings) {
        mappings.put(PrimaryDomain.DEVOPS, Map.ofEntries(
                Map.entry("devops", 1.0), Map.entry("ci/cd", 1.0),
                Map.entry("docker", 0.9), Map.entry("kubernetes", 0.9), Map.entry("k8s", 0.9),
                Map.entry("jenkins", 0.8), Map.entry("terraform", 0.8), Map.entry("ansible", 0.8),
                Map.entry("pipeline", 0.5), Map.entry("deploy", 0.5), Map.entry("infrastructure", 0.5)
        ));
    }

    private static void addCloudKeywords(Map<PrimaryDomain, Map<String, Double>> mappings) {
        mappings.put(PrimaryDomain.CLOUD, Map.ofEntries(
                Map.entry("cloud", 1.0), Map.entry("aws", 1.0), Map.entry("amazon web services", 1.0),
                Map.entry("azure", 1.0), Map.entry("gcp", 1.0), Map.entry("google cloud", 1.0),
                Map.entry("serverless", 0.8), Map.entry("lambda", 0.7),
                Map.entry("scaling", 0.5), Map.entry("load balancer", 0.6), Map.entry("microservice", 0.5)
        ));
    }

    private static void addSecurityKeywords(Map<PrimaryDomain, Map<String, Double>> mappings) {
        mappings.put(PrimaryDomain.SECURITY, Map.ofEntries(
                Map.entry("security", 1.0), Map.entry("cybersecurity", 1.0), Map.entry("encryption", 0.9),
                Map.entry("authentication", 0.9), Map.entry("authorization", 0.9), Map.entry("oauth", 0.9),
                Map.entry("jwt", 0.8), Map.entry("ssl", 0.8), Map.entry("tls", 0.8),
                Map.entry("vulnerability", 0.7), Map.entry("xss", 0.8),
                Map.entry("sql injection", 0.8), Map.entry("csrf", 0.8), Map.entry("firewall", 0.6)
        ));
    }

    private static void addFinanceKeywords(Map<PrimaryDomain, Map<String, Double>> mappings) {
        mappings.put(PrimaryDomain.FINANCE, Map.ofEntries(
                Map.entry("finance", 1.0), Map.entry("fintech", 1.0), Map.entry("banking", 1.0),
                Map.entry("payment", 0.9), Map.entry("transaction", 0.8), Map.entry("trading", 0.9),
                Map.entry("stock", 0.8), Map.entry("investment", 0.8), Map.entry("cryptocurrency", 0.9),
                Map.entry("bitcoin", 0.8), Map.entry("blockchain", 0.8),
                Map.entry("credit", 0.6), Map.entry("loan", 0.7)
        ));
    }

    private static void addMedicalKeywords(Map<PrimaryDomain, Map<String, Double>> mappings) {
        mappings.put(PrimaryDomain.MEDICAL, Map.ofEntries(
                Map.entry("medical", 1.0), Map.entry("healthcare", 1.0), Map.entry("health", 0.8),
                Map.entry("patient", 0.8), Map.entry("diagnosis", 0.9), Map.entry("treatment", 0.8),
                Map.entry("clinical", 0.8), Map.entry("hospital", 0.8),
                Map.entry("pharmacy", 0.8), Map.entry("electronic health record", 0.9),
                Map.entry("ehr", 0.8), Map.entry("telemedicine", 0.9)
        ));
    }

    private static void addEducationKeywords(Map<PrimaryDomain, Map<String, Double>> mappings) {
        mappings.put(PrimaryDomain.EDUCATION, Map.ofEntries(
                Map.entry("education", 1.0), Map.entry("learning", 0.9), Map.entry("teaching", 0.9),
                Map.entry("course", 0.8), Map.entry("curriculum", 0.9), Map.entry("student", 0.8),
                Map.entry("teacher", 0.8), Map.entry("school", 0.8), Map.entry("university", 0.8),
                Map.entry("lecture", 0.7), Map.entry("assignment", 0.7),
                Map.entry("exam", 0.7), Map.entry("mooc", 0.8)
        ));
    }
}