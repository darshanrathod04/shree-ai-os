package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.ConstraintEvidence;
import com.shreeai.os.platform.kernels.context.model.ExperienceLevel;
import com.shreeai.os.platform.kernels.context.model.OutputPreference;
import com.shreeai.os.platform.kernels.context.model.PlatformType;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>DefaultConstraintExtractionEngine</b>
 *
 * <p>Deterministic, rule-based implementation of {@link ConstraintExtractionEngine}.</p>
 *
 * <p><b>Extraction Pipeline:</b></p>
 * <ol>
 *   <li>Duration - Extract explicit durations</li>
 *   <li>Budget - Extract explicit monetary values</li>
 *   <li>Experience - Extract explicit experience levels</li>
 *   <li>Platform - Extract explicit platform targets</li>
 *   <li>Language - Extract language preference</li>
 *   <li>Output Preference - Extract explicit output format requests</li>
 * </ol>
 *
 * <p><b>Determinism Guarantee:</b> Same input always produces identical output.</p>
 *
 * @see ConstraintExtractionEngine
 * @see UserConstraints
 */
public final class DefaultConstraintExtractionEngine implements ConstraintExtractionEngine {

    public DefaultConstraintExtractionEngine() {
    }

    @Override
    public UserConstraints extract(String userInput) {
        Objects.requireNonNull(userInput, "userInput must not be null");

        String normalizedInput = userInput.trim();

        if (normalizedInput.isEmpty()) {
            return UserConstraints.empty();
        }

        List<ConstraintEvidence> evidence = new ArrayList<>();

        String duration = extractDuration(normalizedInput, evidence);
        String budget = extractBudget(normalizedInput, evidence);
        ExperienceLevel experience = extractExperience(normalizedInput, evidence);
        PlatformType platform = extractPlatform(normalizedInput, evidence);
        String language = extractLanguage(normalizedInput, evidence);
        OutputPreference outputPreference = extractOutputPreference(normalizedInput, evidence);

        return new UserConstraints(duration, budget, experience, platform, language, outputPreference, evidence);
    }

    /**
     * Stage 1: Extract explicit duration constraints.
     */
    private String extractDuration(String input, List<ConstraintEvidence> evidence) {
        // Pattern: number + time unit
        Pattern pattern = Pattern.compile(
                "(\\d+\\s*(?:days?|weeks?|months?|years?))",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String matched = matcher.group(1);
            evidence.add(new ConstraintEvidence("duration", matched, matcher.start(), matcher.end()));
            return matched;
        }

        // Pattern: "in X days/weeks/months"
        pattern = Pattern.compile(
                "in\\s+(\\d+\\s*(?:days?|weeks?|months?|years?))",
                Pattern.CASE_INSENSITIVE
        );
        matcher = pattern.matcher(input);
        if (matcher.find()) {
            String matched = matcher.group(1);
            evidence.add(new ConstraintEvidence("duration", matched, matcher.start(), matcher.end()));
            return matched;
        }

        // Pattern: "within X days/weeks/months"
        pattern = Pattern.compile(
                "within\\s+(\\d+\\s*(?:days?|weeks?|months?|years?))",
                Pattern.CASE_INSENSITIVE
        );
        matcher = pattern.matcher(input);
        if (matcher.find()) {
            String matched = matcher.group(1);
            evidence.add(new ConstraintEvidence("duration", matched, matcher.start(), matcher.end()));
            return matched;
        }

        return null;
    }

    /**
     * Stage 2: Extract explicit budget constraints.
     */
    private String extractBudget(String input, List<ConstraintEvidence> evidence) {
        // Pattern: Currency symbol + number
        Pattern pattern = Pattern.compile(
                "([₹$€£]\\s*[\\d,]+(?:\\.\\d{1,2})?)",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String matched = matcher.group(1);
            evidence.add(new ConstraintEvidence("budget", matched, matcher.start(), matcher.end()));
            return matched;
        }

        // Pattern: Number + currency code
        pattern = Pattern.compile(
                "([\\d,]+(?:\\.\\d{1,2})?\\s*(?:INR|USD|EUR|GBP))",
                Pattern.CASE_INSENSITIVE
        );
        matcher = pattern.matcher(input);
        if (matcher.find()) {
            String matched = matcher.group(1);
            evidence.add(new ConstraintEvidence("budget", matched, matcher.start(), matcher.end()));
            return matched;
        }

        return null;
    }
    private ExperienceLevel extractExperience(String input, List<ConstraintEvidence> evidence) {
        String lowerInput = input.toLowerCase();
        if (containsWholeWord(lowerInput, "beginner")) {
            int idx = lowerInput.indexOf("beginner");
            evidence.add(new ConstraintEvidence("experience", "beginner", idx, idx + 8));
            return ExperienceLevel.BEGINNER;
        }
        if (containsWholeWord(lowerInput, "intermediate")) {
            int idx = lowerInput.indexOf("intermediate");
            evidence.add(new ConstraintEvidence("experience", "intermediate", idx, idx + 12));
            return ExperienceLevel.INTERMEDIATE;
        }
        if (containsWholeWord(lowerInput, "advanced")) {
            int idx = lowerInput.indexOf("advanced");
            evidence.add(new ConstraintEvidence("experience", "advanced", idx, idx + 8));
            return ExperienceLevel.ADVANCED;
        }
        if (containsWholeWord(lowerInput, "expert")) {
            int idx = lowerInput.indexOf("expert");
            evidence.add(new ConstraintEvidence("experience", "expert", idx, idx + 6));
            return ExperienceLevel.EXPERT;
        }
        return null;
    }

    private PlatformType extractPlatform(String input, List<ConstraintEvidence> evidence) {
        String lowerInput = input.toLowerCase();
        if (containsWholeWord(lowerInput, "macbook") || containsWholeWord(lowerInput, "macos")) {
            int idx = findFirstIndex(lowerInput, "macbook", "macos");
            String matched = lowerInput.contains("macbook") ? "macbook" : "macos";
            evidence.add(new ConstraintEvidence("platform", matched, idx, idx + matched.length()));
            return PlatformType.MAC;
        }
        if (containsWholeWord(lowerInput, "windows")) {
            int idx = lowerInput.indexOf("windows");
            evidence.add(new ConstraintEvidence("platform", "windows", idx, idx + 7));
            return PlatformType.WINDOWS;
        }
        if (containsWholeWord(lowerInput, "linux") || containsWholeWord(lowerInput, "ubuntu")) {
            int idx = findFirstIndex(lowerInput, "linux", "ubuntu");
            evidence.add(new ConstraintEvidence("platform", lowerInput.substring(idx, idx + 5), idx, idx + 5));
            return PlatformType.LINUX;
        }
        if (containsWholeWord(lowerInput, "android")) {
            int idx = lowerInput.indexOf("android");
            evidence.add(new ConstraintEvidence("platform", "android", idx, idx + 7));
            return PlatformType.ANDROID;
        }
        if (containsWholeWord(lowerInput, "iphone") || containsWholeWord(lowerInput, "ios")) {
            int idx = findFirstIndex(lowerInput, "iphone", "ios");
            String matched = lowerInput.contains("iphone") ? "iphone" : "ios";
            evidence.add(new ConstraintEvidence("platform", matched, idx, idx + matched.length()));
            return PlatformType.IOS;
        }
        if (containsWholeWord(lowerInput, "web")) {
            int idx = lowerInput.indexOf("web");
            evidence.add(new ConstraintEvidence("platform", "web", idx, idx + 3));
            return PlatformType.WEB;
        }
        return null;
    }

    private String extractLanguage(String input, List<ConstraintEvidence> evidence) {
        String lowerInput = input.toLowerCase();
        if (containsWholeWord(lowerInput, "english")) {
            int idx = lowerInput.indexOf("english");
            evidence.add(new ConstraintEvidence("language", "English", idx, idx + 7));
            return "English";
        }
        if (containsWholeWord(lowerInput, "hindi")) {
            int idx = lowerInput.indexOf("hindi");
            evidence.add(new ConstraintEvidence("language", "Hindi", idx, idx + 5));
            return "Hindi";
        }
        return null;
    }

    private OutputPreference extractOutputPreference(String input, List<ConstraintEvidence> evidence) {
        String lowerInput = input.toLowerCase();
        if (containsWholeWord(lowerInput, "roadmap")) {
            int idx = lowerInput.indexOf("roadmap");
            evidence.add(new ConstraintEvidence("outputPreference", "roadmap", idx, idx + 7));
            return OutputPreference.ROADMAP;
        }
        if (containsWholeWord(lowerInput, "complete code")) {
            int idx = lowerInput.indexOf("complete code");
            evidence.add(new ConstraintEvidence("outputPreference", "complete code", idx, idx + 13));
            return OutputPreference.CODE;
        }
        if (containsWholeWord(lowerInput, "code")) {
            int idx = lowerInput.indexOf("code");
            evidence.add(new ConstraintEvidence("outputPreference", "code", idx, idx + 4));
            return OutputPreference.CODE;
        }
        if (containsWholeWord(lowerInput, "explain")) {
            int idx = lowerInput.indexOf("explain");
            evidence.add(new ConstraintEvidence("outputPreference", "explain", idx, idx + 7));
            return OutputPreference.EXPLANATION;
        }
        if (containsWholeWord(lowerInput, "explanation")) {
            int idx = lowerInput.indexOf("explanation");
            evidence.add(new ConstraintEvidence("outputPreference", "explanation", idx, idx + 11));
            return OutputPreference.EXPLANATION;
        }
        if (containsWholeWord(lowerInput, "checklist")) {
            int idx = lowerInput.indexOf("checklist");
            evidence.add(new ConstraintEvidence("outputPreference", "checklist", idx, idx + 9));
            return OutputPreference.CHECKLIST;
        }
        if (containsWholeWord(lowerInput, "architecture")) {
            int idx = lowerInput.indexOf("architecture");
            evidence.add(new ConstraintEvidence("outputPreference", "architecture", idx, idx + 12));
            return OutputPreference.ARCHITECTURE;
        }
        return null;
    }

    private boolean containsWholeWord(String input, String keyword) {
        String regex = "\\b" + Pattern.quote(keyword) + "\\b";
        return Pattern.compile(regex).matcher(input).find();
    }

    private int findFirstIndex(String input, String... keywords) {
        int firstIndex = -1;
        for (String keyword : keywords) {
            int idx = input.indexOf(keyword);
            if (idx != -1 && (firstIndex == -1 || idx < firstIndex)) {
                firstIndex = idx;
            }
        }
        return firstIndex;
    }
}
