package com.darshan.agent.brain.perception;

import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.memory.EpisodicMemoryEngine;
import com.darshan.agent.memory.UserProfile;
import com.darshan.agent.memory.episodic.Episode;
import com.darshan.agent.memory.episodic.EpisodeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class IdentityPerceptionEngine {

    private final UserProfile userProfile;
    private final EpisodicMemoryEngine episodicMemory;

    /**
     * Known role/profession/title keywords that should NEVER be treated as names.
     * This prevents "I am Java Developer" or "I am backend developer" from
     * being extracted as user names.
     */
    private static final List<String> KNOWN_ROLES = Arrays.asList(
            "java developer", "backend developer", "frontend developer", "full stack developer",
            "software engineer", "software developer", "devops engineer", "data scientist",
            "data engineer", "machine learning engineer", "ai engineer", "cloud engineer",
            "senior developer", "junior developer", "web developer", "mobile developer",
            "system administrator", "database administrator", "security engineer",
            "tech lead", "team lead", "engineering manager", "product manager",
            "project manager", "scrum master", "business analyst", "qa engineer",
            "test engineer", "automation engineer", "architect", "solution architect",
            "technical lead", "intern", "student", "developer", "programmer",
            "coder", "back-end developer", "front-end developer", "full-stack developer",
            "java programmer", "spring boot developer", "python developer",
            "javascript developer", "react developer", "angular developer",
            "node.js developer", "node developer", "typescript developer",
            "cloud architect", "network engineer", "site reliability engineer",
            "principal engineer", "staff engineer", "consultant",
            "technical architect", "application developer", "software architect"
    );

    /**
     * Extract and store user identity from input.
     * Stores name in both the per-session context and global profile for persistence.
     * Only extracts actual names from patterns like:
     *   "I am Darshan"
     *   "I'm Darshan"
     *   "My name is Darshan"
     *
     * Does NOT extract role/profession descriptions as names:
     *   "I am Java Developer" -> ignored
     *   "I am backend developer" -> ignored
     *   "As I am Java Developer" -> ignored
     *
     * @param input The user's message
     * @param context The per-session conversation context
     */
    public void perceive(String input, ConversationContext context) {

        String normalized = normalize(input);

        String name = extractName(normalized, input);

        if (name == null) return;

        // Store in per-session context for session-isolated identity
        context.setUserName(name);

        // Store as episodic memory (long-term cognition), tagged with session context info
        Episode episode = new Episode(
                EpisodeType.CONVERSATION,
                "Learned user's name: " + name,
                input,
                "Stored identity: " + name,
                1.0
        );

        episodicMemory.store(episode);

        System.out.println("🧠 Identity Learned → " + name + " (session: " + (context.getUserName() != null ? context.getUserName() : "unknown") + ")");
    }

    /**
     * Get the currently stored user name (from global profile).
     * Used as fallback when session context has no name.
     */
    public String getGlobalUserName() {
        return userProfile.getName();
    }

    /**
     * Backward-compatible perceive without context (uses global UserProfile).
     * @deprecated Use perceive(input, context) for session isolation
     */
    @Deprecated
    public void perceive(String input) {
        perceive(input, new ConversationContext());
    }

    /**
     * Extract a real user name from input text.
     * Only matches explicit name-introduction patterns:
     *   "I am X", "I'm X", "My name is X"
     *
     * Filters out role/profession descriptions to prevent:
     *   "I am Java Developer" from being treated as name="Java Developer"
     */
    private String extractName(String normalizedInput, String originalInput) {
        // Pattern 1: "I am X" or "I'm X"
        Pattern p1 = Pattern.compile(
                "(?:i am|i'm)\\s+([a-zA-Z ]+)"
        );
        Matcher m1 = p1.matcher(normalizedInput);

        // Pattern 2: "My name is X"
        Pattern p2 = Pattern.compile(
                "my name is\\s+([a-zA-Z ]+)"
        );
        Matcher m2 = p2.matcher(normalizedInput);

        // Try "I am" / "I'm" first
        if (m1.find()) {
            String candidate = m1.group(1).trim().toLowerCase();
            // Check if the candidate or original input contains a known role
            if (isRoleOrProfession(candidate, originalInput.toLowerCase())) {
                System.out.println("[IdentityPerception] Rejecting name extraction: '" + candidate + "' appears to be a role/profession");
                return null;
            }
            // Must be a simple name: 1-3 words, each starting with letter, no role keywords
            if (isValidName(candidate)) {
                return capitalize(candidate);
            }
        }

        // Try "My name is" second
        if (m2.find()) {
            String candidate = m2.group(1).trim().toLowerCase();
            if (isRoleOrProfession(candidate, originalInput.toLowerCase())) {
                System.out.println("[IdentityPerception] Rejecting name extraction: '" + candidate + "' appears to be a role/profession");
                return null;
            }
            if (isValidName(candidate)) {
                return capitalize(candidate);
            }
        }

        return null;
    }

    /**
     * Check if the extracted candidate or its context contains a known role/profession.
     */
    private boolean isRoleOrProfession(String candidate, String fullInputLower) {
        // Direct check: candidate is a known role
        if (KNOWN_ROLES.contains(candidate)) {
            return true;
        }

        // Check if any known role is a substring of the candidate
        for (String role : KNOWN_ROLES) {
            if (candidate.contains(role)) {
                return true;
            }
        }

        // Check the full input for role patterns near the name extraction point
        // e.g., "I am Java Developer" - "Java Developer" is captured but should be rejected
        // because it matches a known role or contains role keywords
        for (String role : KNOWN_ROLES) {
            if (fullInputLower.contains(role)) {
                // If the role is within the extraction context, flag it
                String[] roleWords = role.split("\\s+");
                String[] candidateWords = candidate.split("\\s+");

                // Check if any segment of the candidate matches a role
                for (int i = 0; i < candidateWords.length; i++) {
                    StringBuilder segment = new StringBuilder();
                    for (int j = i; j < candidateWords.length; j++) {
                        if (segment.length() > 0) segment.append(" ");
                        segment.append(candidateWords[j]);
                        if (KNOWN_ROLES.contains(segment.toString())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Validate that a name candidate looks like a real person's name:
     * - 1 to 3 words
     * - Each word starts with a letter
     * - No known role keywords
     * - Not just a single common word that could be a role
     */
    private boolean isValidName(String name) {
        if (name == null || name.isBlank()) return false;

        String[] words = name.split("\\s+");

        // Names should be 1 to 3 words max
        if (words.length > 3) return false;

        // Each word should be at least 2 characters (single letters like "I" are excluded)
        for (String word : words) {
            if (word.length() < 2) return false;
            // Must start with a letter
            if (!Character.isLetter(word.charAt(0))) return false;
        }

        // Reject common non-name patterns
        List<String> nonNameWords = Arrays.asList(
                "developer", "engineer", "programmer", "architect", "lead",
                "manager", "analyst", "consultant", "tester", "designer",
                "intern", "student", "beginner", "expert", "professional",
                "backend", "frontend", "fullstack", "full-stack", "back-end", "front-end",
                "senior", "junior", "principal", "staff", "tech",
                "java", "python", "javascript", "typescript", "react", "angular",
                "spring", "springboot", "node", "docker", "kubernetes",
                "aws", "azure", "gcp", "cloud", "devops", "sql",
                "nosql", "database", "api", "rest", "microservices",
                "system", "software", "web", "mobile", "data", "ai",
                "machine", "learning", "deep", "security", "network",
                "test", "automation", "site", "reliability", "solution",
                "technical", "engineering", "product", "project", "scrum"
        );

        for (String word : words) {
            if (nonNameWords.contains(word.toLowerCase())) {
                return false;
            }
        }

        return true;
    }

    private String normalize(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String capitalize(String name){
        return Arrays.stream(name.split(" "))
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }
}