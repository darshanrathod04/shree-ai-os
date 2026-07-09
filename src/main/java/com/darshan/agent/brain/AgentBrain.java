package com.darshan.agent.brain;

import com.darshan.agent.brain.perception.IdentityPerceptionEngine;
import com.darshan.agent.chief.ChiefOfStaffEngine;
import com.darshan.agent.cognition.*;
import com.darshan.agent.memory.UserProfile;
import com.darshan.agent.planning.AutonomousPlanningEngine;
import com.darshan.agent.planning.ExecutionPlan;
import com.darshan.agent.planning.ExecutionTask;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.context.LessonEngine;
import com.darshan.agent.context.LessonState;
import com.darshan.agent.debate.swarm.DebateSwarmEngine;
import com.darshan.agent.dto.AgentResponse;
import com.darshan.agent.graph.KnowledgeGraphEngine;
import com.darshan.agent.llm.OllamaClient;
import com.darshan.agent.memory.MemoryFacade;
import com.darshan.agent.project.ProjectIntelligenceEngine;
import com.darshan.agent.personality.PersonalityEngine;
import com.darshan.agent.router.SkillRouter;
import com.darshan.agent.skills.Skill;
import com.darshan.agent.learning.CourseState;
import com.darshan.agent.learning.LearningSessionEngine;
import com.darshan.agent.learning.TeachingEngine;
import com.darshan.agent.capability.CapabilityMatch;
import com.darshan.agent.capability.CapabilityRegistry;
import com.darshan.agent.resolver.CapabilityResolution;
import com.darshan.agent.resolver.CapabilityResolver;
import com.darshan.agent.cognition.uqc.ClassificationResult;
import com.darshan.agent.cognition.uqc.UniversalQueryClassifier;
import com.darshan.agent.learning.adaptive.AdaptiveLearningEngine;
import com.darshan.agent.learning.adaptive.StudentLearningProfile;
import com.darshan.agent.production.ConversationOptimizer;
import com.darshan.agent.production.ContextResolutionEngine;
import com.darshan.agent.production.FallbackEngine;
import com.darshan.agent.production.ResolvedContext;
import com.darshan.agent.production.ResponseRouter;
import com.darshan.agent.validation.DecisionValidator;
import com.darshan.agent.validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AgentBrain {

    private final CognitiveGovernorEngine governor;
    private final ConversationStateMachine stateMachine;
    private final IntentEngine intentEngine;
    private final SkillRouter router;
    private final DebateSwarmEngine swarm;
    private final PersonalityEngine personality;
    private final MemoryFacade memoryFacade;
    private final MetaCognitionEngine meta;
    private final IdentityPerceptionEngine identityPerceptionEngine;
    private final LessonEngine lessonEngine;
    private final PromptBuilder promptBuilder;
    private final OllamaClient ollamaClient;
    private final KnowledgeGraphEngine knowledgeGraph;
    private final ProjectIntelligenceEngine projectIntelligence;
    private final ChiefOfStaffEngine chiefOfStaff;
    private final AutonomousPlanningEngine planningEngine;
    private final LearningSessionEngine learningSessionEngine;
    private final TeachingEngine teachingEngine;
    private final AdaptiveLearningEngine adaptiveEngine;
    private final ResponseRouter responseRouter;
    private final ConversationOptimizer conversationOptimizer;
    private final ContextResolutionEngine contextResolver;
    private final FallbackEngine fallbackEngine;
    private final UniversalQueryClassifier universalQueryClassifier;
    private final CapabilityRegistry capabilityRegistry;
    private final CapabilityResolver capabilityResolver;
    private final DecisionValidator decisionValidator;

    public AgentBrain(
            CognitiveGovernorEngine governor,
            ConversationStateMachine stateMachine,
            IntentEngine intentEngine,
            SkillRouter router,
            DebateSwarmEngine swarm,
            PersonalityEngine personality,
            MemoryFacade memoryFacade,
            MetaCognitionEngine meta,
            IdentityPerceptionEngine identityPerceptionEngine,
            LessonEngine lessonEngine,
            PromptBuilder promptBuilder,
            OllamaClient ollamaClient,
            KnowledgeGraphEngine knowledgeGraph,
            ProjectIntelligenceEngine projectIntelligence,
            ChiefOfStaffEngine chiefOfStaff,
            AutonomousPlanningEngine planningEngine,
            LearningSessionEngine learningSessionEngine,
            TeachingEngine teachingEngine,
            AdaptiveLearningEngine adaptiveEngine,
            ResponseRouter responseRouter,
            ConversationOptimizer conversationOptimizer,
            ContextResolutionEngine contextResolver,
            FallbackEngine fallbackEngine,
            UniversalQueryClassifier universalQueryClassifier,
            CapabilityRegistry capabilityRegistry,
            CapabilityResolver capabilityResolver,
            DecisionValidator decisionValidator
    ) {
        this.governor = governor;
        this.stateMachine = stateMachine;
        this.intentEngine = intentEngine;
        this.router = router;
        this.swarm = swarm;
        this.personality = personality;
        this.memoryFacade = memoryFacade;
        this.meta = meta;
        this.identityPerceptionEngine = identityPerceptionEngine;
        this.lessonEngine = lessonEngine;
        this.promptBuilder = promptBuilder;
        this.ollamaClient = ollamaClient;
        this.knowledgeGraph = knowledgeGraph;
        this.projectIntelligence = projectIntelligence;
        this.chiefOfStaff = chiefOfStaff;
        this.planningEngine = planningEngine;
        this.learningSessionEngine = learningSessionEngine;
        this.teachingEngine = teachingEngine;
        this.adaptiveEngine = adaptiveEngine;
        this.responseRouter = responseRouter;
        this.conversationOptimizer = conversationOptimizer;
        this.contextResolver = contextResolver;
        this.fallbackEngine = fallbackEngine;
        this.universalQueryClassifier = universalQueryClassifier;
        this.capabilityRegistry = capabilityRegistry;
        this.capabilityResolver = capabilityResolver;
        this.decisionValidator = decisionValidator;
    }

    // =====================================================
    // MAIN COGNITIVE PIPELINE
    // =====================================================
    public AgentResponse process(
            String input,
            ConversationContext context,
            LessonState lessonState,
            CourseState courseState,
            StudentLearningProfile learningProfile
    ) throws Exception {

        if (context == null) {
            context = new ConversationContext();
        }

        // 1. GOVERNOR (Safety + Stability)
        CognitiveDecision decision =
                governor.evaluate(input, context);

        switch (decision.getAction()) {
            case PAUSE_AGENT:
                return new AgentResponse("I need a short recovery pause.", false);
            case REFUSE:
                return new AgentResponse("I cannot help with that request.", false);
            case ASK_CLARIFICATION:
                return new AgentResponse("Can you clarify what you mean?", false);
        }

        // 2. STATE MACHINE
        String stateReply = stateMachine.handle(input, context);
        if (stateReply != null) {
            return new AgentResponse(stateReply, false);
        }

        // 3. IDENTITY PERCEPTION (extract user name + interests) with per-session context
        identityPerceptionEngine.perceive(input, context);

        // 3b. KNOWLEDGE GRAPH EXTRACTION
        knowledgeGraph.extractFromInput(input);

        // 3c. PROJECT INTELLIGENCE EXTRACTION
        projectIntelligence.extractFromInput(input);

        // 4. MEMORY RECALL
        String recalledMemory = memoryFacade.recallAll(input);
        context.setWorkingMemory(recalledMemory);

        // ROADMAP SELECTION FLOW

        if ("ROADMAP_SELECTION".equals(context.getPendingAction())) {

            context.setPendingAction(null);

            System.out.println("[ROADMAP] User selected=" + input);

            ExecutionPlan plan =
                    planningEngine.generatePlan(input);

            String planSummary =
                    planningEngine.getPlanSummary();

            return new AgentResponse(
                    "📋 Roadmap Created\n\n"
                            + planSummary,
                    false
            );
        }

        // 5. INTENT DETECTION — Shadow UQC (observer only, never changes routing)
        ClassificationResult uqcResult = universalQueryClassifier.classify(input);
        String intent = intentEngine.detectIntent(input);
        logUqcComparison(input, intent, uqcResult);
        context.setLastIntent(intent);
        System.out.println("[AgentBrain] DETECTED INTENT: " + intent);

        // 5b. SHADOW CAPABILITY REGISTRY LOOKUP (observer only, never changes routing)
        CapabilityMatch capabilityMatch = capabilityRegistry.findBestCapability(intent);
        logCapabilityComparison(intent, capabilityMatch);

        // 5c. SHADOW CAPABILITY RESOLVER (observer only, never changes routing)
        // Uses deterministic scoring (intent 40%, priority 20%, context 20%, health 10%, availability 10%)
        CapabilityResolution resolverResult = capabilityResolver.resolve(intent);
        // Compare resolver prediction with actual handler (logs mismatches for analysis)
        capabilityResolver.compareWithProduction(intent, null, mapIntentToHandler(intent));

        // 5d. SHADOW DECISION VALIDATOR (observer only, never changes routing)
        // Validates the decision before execution - purely for analysis and monitoring
        Thought decisionThought = new Thought(input, intent, mapIntentToHandler(intent), "Decision from intent engine");
        ResolvedContext resolvedContext = resolveContext(context);
        ValidationResult validationResult = decisionValidator.validate(decisionThought, null, resolvedContext);
        // NOTE: Validation results are logged but NEVER affect production execution
        // The switch(intent) below continues unchanged

        // 5e. ROADMAP-AWARE REDIRECTION
        // If user says "next"/"continue" and an active roadmap exists,
        // redirect to NEXT_STEP instead of lesson follow-up
        if (("CONTINUE".equals(intent) || "FOLLOW_UP".equals(intent))
                && planningEngine.getActivePlan().isPresent()) {
            System.out.println("[AgentBrain] Redirecting " + intent + " -> NEXT_STEP (active roadmap found)");
            intent = "NEXT_STEP";
        }

        // 6. HANDLE INTENTS
        switch (intent) {
            case "ROADMAP_REQUEST": {

                context.setPendingAction(
                        "ROADMAP_SELECTION"
                );

                return new AgentResponse(
                        """
                        Which roadmap would you like?
            
                        1. Java Developer
                        2. Spring Boot
                        3. DSA
                        4. Interview Preparation
                        5. AI Engineer
                        """,
                        false
                );
            }
            case "WHO_AM_I": {
                System.out.println("[AgentBrain] EXECUTING WHO_AM_I BRANCH");
                String name = context.getUserName();
                System.out.println("[AgentBrain] WHO_AM_I: context.getUserName() = " + name);
                if (name != null && !name.isEmpty()) {
                    System.out.println("[AgentBrain] WHO_AM_I: Returning session name: " + name);
                    return new AgentResponse("Your name is " + name + ".", false);
                }
                // No global fallback — each session has isolated identity.
                // Session C starting fresh should NOT see Session A's name.
                System.out.println("[AgentBrain] WHO_AM_I: No name in context, returning default");
                return new AgentResponse("I don't know your name yet. Please tell me your name.", false);
            }
            case "PLAN": {
                System.out.println("[AgentBrain] EXECUTING PLAN BRANCH");
                String planDescription = input.replaceFirst("(?i)(i want to become a|become a|plan|roadmap|career path|learning path|steps to|how do i become|how to become)\\s*", "").trim();
                if (planDescription.isEmpty()) planDescription = input;
                ExecutionPlan plan = planningEngine.generatePlan(planDescription);
                String planSummary = planningEngine.getPlanSummary();
                String responseText = "📋 **Roadmap Created**\n\n" + planSummary + "\n\nI've broken this down into milestones and tasks. Check the Planning tab for full details, or ask me about your daily priorities!";
                System.out.println("[AgentBrain] PLAN: Returning roadmap directly (no LLM)");
                return new AgentResponse(responseText, false);
            }
            case "LEARN": {
                String topic = input.replaceFirst("(?i)learn\\s+", "").trim();
                if (topic.isEmpty()) topic = "general topics";
                String result = lessonEngine.startLesson(topic, lessonState);
                return new AgentResponse(result, false);
            }
            case "CONTINUE": {
                String result = lessonEngine.nextChapter(lessonState);
                return new AgentResponse(result, false);
            }
            case "PREVIOUS": {
                String result = lessonEngine.previousChapter(lessonState);
                return new AgentResponse(result, false);
            }
            case "SUMMARY": {
                String result = lessonEngine.getSummary(lessonState);
                return new AgentResponse(result, false);
            }
            case "QUIZ": {
                String result = lessonEngine.quizMode(lessonState);
                return new AgentResponse(result, false);
            }

        case "GOAL_QUERY": {
                String goalStatus = promptBuilder.buildGoalContext();
                if (goalStatus.isEmpty()) {
                    goalStatus = "No active goal. Say 'goal: <description>' to set one.";
                }
                return new AgentResponse(goalStatus, false);
            }
            case "NEXT_STEP": {
                System.out.println("[NEXT_STEP] Intent detected");
                
                Optional<ExecutionPlan> activePlanOpt = planningEngine.getActivePlan();
                if (activePlanOpt.isEmpty()) {
                    System.out.println("[NEXT_STEP] No active plan found");
                    return new AgentResponse("No active roadmap. Say 'plan: <your goal>' to create one.", false);
                }
                
                ExecutionPlan plan = activePlanOpt.get();
                System.out.println("[NEXT_STEP] goal=" + plan.getGoalName());
                
                List<ExecutionTask> allTasks = plan.getAllTasks();
                Optional<ExecutionTask> nextTaskOpt = allTasks.stream()
                        .filter(t -> !t.isCompleted() && !t.isBlocked())
                        .findFirst();
                
                if (nextTaskOpt.isEmpty()) {
                    System.out.println("[NEXT_STEP] No pending tasks found");
                    return new AgentResponse("All tasks completed! 🎉 Your roadmap is done.", false);
                }
                
                ExecutionTask nextTask = nextTaskOpt.get();
                System.out.println("[NEXT_STEP] task=" + nextTask.getTitle());
                
                StringBuilder response = new StringBuilder();
                response.append("📋 **Next Task**\n\n");
                response.append("**").append(nextTask.getTitle()).append("**\n");
                response.append(nextTask.getDescription()).append("\n\n");
                response.append("⏱️ Estimated: ").append((int) nextTask.getEstimatedHours()).append(" hours\n");
                response.append("📊 Priority: ").append(nextTask.getPriority()).append("\n");
                response.append("📈 Progress: ").append(String.format("%.0f%%", plan.getOverallProgress()));
                
                System.out.println("[NEXT_STEP] returned without LLM");
                return new AgentResponse(response.toString(), false);
            }
            case "COMPLETE_TASK": {
                System.out.println("[COMPLETE_TASK] Intent detected");
                
                Optional<ExecutionPlan> activePlanOpt = planningEngine.getActivePlan();
                if (activePlanOpt.isEmpty()) {
                    return new AgentResponse("No active roadmap. Say 'plan: <your goal>' to create one.", false);
                }
                
                ExecutionPlan plan = activePlanOpt.get();
                List<ExecutionTask> allTasks = plan.getAllTasks();
                Optional<ExecutionTask> nextTaskOpt = allTasks.stream()
                        .filter(t -> !t.isCompleted() && !t.isBlocked())
                        .findFirst();
                
                if (nextTaskOpt.isEmpty()) {
                    return new AgentResponse("All tasks completed! 🎉 Your roadmap is done.", false);
                }
                
                ExecutionTask taskToComplete = nextTaskOpt.get();
                boolean completed = planningEngine.completeTask(plan.getId(), taskToComplete.getId());
                
                if (!completed) {
                    return new AgentResponse("Failed to mark task as complete. Please try again.", false);
                }
                
                long completedCount = plan.getCompletedTasks();
                long totalCount = plan.getTotalTasks();
                double progressPercent = plan.getOverallProgress();
                
                StringBuilder response = new StringBuilder();
                response.append("✅ Task Completed\n\n");
                response.append("**").append(taskToComplete.getTitle()).append("**\n\n");
                response.append("Progress: ").append(completedCount).append(" / ").append(totalCount).append(" Tasks\n");
                response.append("Completion: ").append(String.format("%.0f%%", progressPercent)).append("\n\n");
                
                // Show next task if available
                Optional<ExecutionTask> newNextOpt = allTasks.stream()
                        .filter(t -> !t.isCompleted() && !t.isBlocked())
                        .findFirst();
                
                if (newNextOpt.isPresent()) {
                    ExecutionTask newNext = newNextOpt.get();
                    response.append("Next Task:\n");
                    response.append(newNext.getTitle());
                } else {
                    response.append("🎉 All tasks completed!");
                }
                
                System.out.println("[COMPLETE_TASK] Task marked complete: " + taskToComplete.getTitle());
                return new AgentResponse(response.toString(), false);
            }
            case "PROGRESS": {
                System.out.println("[PROGRESS] Intent detected");
                
                Optional<ExecutionPlan> activePlanOpt = planningEngine.getActivePlan();
                if (activePlanOpt.isEmpty()) {
                    return new AgentResponse("No active roadmap. Say 'plan: <your goal>' to create one.", false);
                }
                
                ExecutionPlan plan = activePlanOpt.get();
                long completedCount = plan.getCompletedTasks();
                long totalCount = plan.getTotalTasks();
                double progressPercent = plan.getOverallProgress();
                
                StringBuilder response = new StringBuilder();
                response.append("🎯 Goal: ").append(plan.getGoalName()).append("\n\n");
                response.append("Progress: ").append(completedCount).append(" / ").append(totalCount).append(" Tasks\n");
                response.append("Completion: ").append(String.format("%.0f%%", progressPercent)).append("\n");
                
                System.out.println("[PROGRESS] Returned progress: " + completedCount + "/" + totalCount);
                return new AgentResponse(response.toString(), false);
            }
            case "CURRENT_TASK": {
                System.out.println("[CURRENT_TASK] Intent detected");
                
                Optional<ExecutionPlan> activePlanOpt = planningEngine.getActivePlan();
                if (activePlanOpt.isEmpty()) {
                    return new AgentResponse("No active roadmap. Say 'plan: <your goal>' to create one.", false);
                }
                
                ExecutionPlan plan = activePlanOpt.get();
                List<ExecutionTask> allTasks = plan.getAllTasks();
                Optional<ExecutionTask> currentTaskOpt = allTasks.stream()
                        .filter(t -> !t.isCompleted() && !t.isBlocked())
                        .findFirst();
                
                if (currentTaskOpt.isEmpty()) {
                    return new AgentResponse("All tasks completed! 🎉 Your roadmap is done.", false);
                }
                
                ExecutionTask currentTask = currentTaskOpt.get();
                
                StringBuilder response = new StringBuilder();
                response.append("📌 Current Task\n\n");
                response.append("**").append(currentTask.getTitle()).append("**\n\n");
                response.append("Topics:\n");
                
                // Parse description into bullet points
                String desc = currentTask.getDescription();
                String[] parts = desc.split("[,;]");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        response.append("* ").append(trimmed).append("\n");
                    }
                }
                
                response.append("\n⏱️ Estimated: ").append((int) currentTask.getEstimatedHours()).append(" Hours\n");
                response.append("📊 Priority: ").append(currentTask.getPriority()).append("\n");
                response.append("📈 Progress: ").append(String.format("%.0f%%", plan.getOverallProgress()));
                
                System.out.println("[CURRENT_TASK] Returned task: " + currentTask.getTitle());
                return new AgentResponse(response.toString(), false);
            }

            // === COURSE LEARNING INTENTS ===
            // These MUST bypass ChatSkill and go directly through TeachingEngine

            case "START_COURSE": {
                System.out.println("[AgentBrain] EXECUTING START_COURSE BRANCH (via TeachingEngine)");
                String courseName = input.replaceFirst("(?i)(start course|begin course|start learning|enroll in course|today we learn|today we will learn|i want to learn|teach me|teach me about|start|begin|learn|teach)\\s+", "").trim();
                if (courseName.isEmpty() || isGenericCoursePhrase(courseName)) {
                    return new AgentResponse("Which course would you like to start? Available: java, spring-boot", false);
                }
                String result = learningSessionEngine.startCourse(courseName, courseState);
                return new AgentResponse(result, false);
            }

            case "CONTINUE_LESSON": {
                System.out.println("[AgentBrain] EXECUTING CONTINUE_LESSON BRANCH (via TeachingEngine)");
                if (!courseState.hasActiveCourse()) {
                    return new AgentResponse("No active course. Say 'start course <name>' to begin.", false);
                }
                String result = learningSessionEngine.continueLesson(courseState);
                return new AgentResponse(result, false);
            }

            case "COMPLETE_LESSON": {
                System.out.println("[AgentBrain] EXECUTING COMPLETE_LESSON BRANCH");
                if (!courseState.hasActiveCourse()) {
                    return new AgentResponse("No active course. Say 'start course <name>' to begin.", false);
                }
                String result = learningSessionEngine.completeLesson(courseState);
                return new AgentResponse(result, false);
            }

            case "CURRENT_LESSON": {
                System.out.println("[AgentBrain] EXECUTING CURRENT_LESSON BRANCH (via TeachingEngine)");
                if (!courseState.hasActiveCourse()) {
                    return new AgentResponse("No active course. Say 'start course <name>' to begin.", false);
                }
                String result = learningSessionEngine.currentLesson(courseState);
                return new AgentResponse(result, false);
            }

            case "TEACH_TOPIC": {
                System.out.println("[AgentBrain] EXECUTING TEACH_TOPIC BRANCH (via TeachingEngine)");
                if (!courseState.hasActiveCourse()) {
                    return new AgentResponse("No active course. Say 'start course <name>' to begin.", false);
                }
                // Extract the actual topic question from the input
                String topic = input.replaceFirst("(?i)(explain|what is|what's|what are|tell me about|describe|how does|how do|what does)\\s+", "").trim();
                if (topic.isEmpty()) {
                    topic = input;
                }
                String result = learningSessionEngine.teachTopic(topic, courseState);
                return new AgentResponse(result, false);
            }

            case "REPEAT_LESSON": {
                System.out.println("[AgentBrain] EXECUTING REPEAT_LESSON BRANCH (via TeachingEngine)");
                if (!courseState.hasActiveCourse()) {
                    return new AgentResponse("No active course. Say 'start course <name>' to begin.", false);
                }
                String result = learningSessionEngine.repeatLesson(courseState);
                return new AgentResponse(result, false);
            }

            case "LESSON_PROGRESS": {
                System.out.println("[AgentBrain] EXECUTING LESSON_PROGRESS BRANCH");
                String result = learningSessionEngine.progress(courseState);
                return new AgentResponse(result, false);
            }

            case "EXIT_COURSE": {
                System.out.println("[AgentBrain] EXECUTING EXIT_COURSE BRANCH");
                String result = learningSessionEngine.exitCourse(courseState);
                return new AgentResponse(result, false);
            }

            // === QUIZ INTENTS ===

            case "START_QUIZ": {
                System.out.println("[AgentBrain] EXECUTING START_QUIZ BRANCH");
                if (!courseState.hasActiveCourse()) {
                    return new AgentResponse("No active course. Say 'start course <name>' to begin.", false);
                }
                // QuizSession is accessed from the session — we pass it through
                // The caller (AgentService) will need to provide it. For now, we use
                // a placeholder that will be resolved at the service layer.
                String result = "Quiz functionality requires session integration. Use the session-aware API.";
                return new AgentResponse(result, false);
            }

        }

        // 7. SKILL ROUTING — learning intents should NOT reach this point
        Skill skill = router.route(intent);
        System.out.println("[AgentBrain] SKILL ROUTING | intent=" + intent + " | skill=" + (skill != null ? skill.getClass().getSimpleName() : "null"));
        String rawReply;

        if (skill != null) {
            System.out.println("[AgentBrain] EXECUTING SKILL: " + skill.getClass().getSimpleName());
            rawReply = skill.execute(input, context);
        } else {
            System.out.println("[AgentBrain] FALLING THROUGH TO LLM | intent=" + intent);
            boolean isLearningIntent = isLearningIntent(intent);
            String instruction = buildInstruction(intent, isLearningIntent, lessonState);
            List<String> graphFacts = knowledgeGraph.getContextFacts(input);
            List<String> projectFacts = projectIntelligence.getContextFacts(input);
            List<String> chiefInsights = chiefOfStaff.getContextInsights();
            List<String> planFacts = planningEngine.getActivePlan()
                    .map(p -> List.of(planningEngine.getPlanSummary()))
                    .orElse(List.of());
            String fullPrompt = promptBuilder.buildFullPrompt(input, instruction, context, isLearningIntent, graphFacts, projectFacts, chiefInsights, planFacts);
            System.out.println("========== TRACE ==========");
            System.out.println("SESSION USER = " + context.getUserName());

            System.out.println("===========================");
            System.out.println("[AgentBrain] LLM PROMPT (first 500 chars): " + fullPrompt.substring(0, Math.min(500, fullPrompt.length())));
            rawReply = ollamaClient.generateDirect(fullPrompt);
        }

        // 8. META COGNITION
        MetaThought reflection = meta.evaluate(input, rawReply);
        if (!reflection.isSuccessful()) {
            rawReply += "\n\n(Self-correction applied)";
        }

        // 9. POST-PROCESSING: Strip placeholders and fake content
        rawReply = stripPlaceholders(rawReply);

        // 10. PERSONALITY RENDER
        String finalReply = personality.applyPersonality(rawReply);

        return new AgentResponse(finalReply, false);
    }

    public AgentResponse process(
            String input,
            ConversationContext context,
            LessonState lessonState
    ) throws Exception {
        return process(input, context, lessonState, new CourseState(), new StudentLearningProfile());
    }

    public AgentResponse process(
            String input,
            ConversationContext context
    ) throws Exception {
        return process(input, context, new LessonState(), new CourseState(), new StudentLearningProfile());
    }

    /**
     * Check if the input is just a leftover generic phrase after course name extraction.
     */
    private boolean isGenericCoursePhrase(String text) {
        if (text == null || text.isBlank()) return true;
        String t = text.toLowerCase().trim();
        return t.contains("course") || t.contains("lesson") || t.contains("start")
                || t.contains("begin") || t.contains("learn") || t.contains("teach")
                || t.contains("today") || t.contains("we will") || t.equals("me");
    }

    private boolean isLearningIntent(String intent) {
        return "LEARN".equals(intent) || "CONTINUE".equals(intent)
                || "PREVIOUS".equals(intent) || "SUMMARY".equals(intent)
                || "QUIZ".equals(intent)
                || "START_COURSE".equals(intent) || "CONTINUE_LESSON".equals(intent)
                || "COMPLETE_LESSON".equals(intent) || "CURRENT_LESSON".equals(intent)
                || "LESSON_PROGRESS".equals(intent) || "EXIT_COURSE".equals(intent)
                || "TEACH_TOPIC".equals(intent) || "REPEAT_LESSON".equals(intent)
                || "START_QUIZ".equals(intent);
    }

    private String buildInstruction(String intent, boolean isLearningIntent, LessonState lessonState) {
        if (isLearningIntent && lessonState.hasActiveLesson()) {
            return "Teach the user about " + lessonState.getActiveTopic()
                    + ", chapter " + lessonState.getChapterNumber()
                    + ". Use a teaching tone.";
        }
        return "Respond naturally and helpfully.";
    }

    private void logUqcComparison(String input, String existingIntent, ClassificationResult uqcResult) {
        boolean same = existingIntent.equals(uqcResult.getPredictedIntent());
        String status = same ? "SAME" : "DIFFERENT";

        System.out.println("[UQC] ========================================================");
        System.out.println("[UQC] INPUT: " + input);
        System.out.println("[UQC] OLD INTENT: " + existingIntent);
        System.out.println("[UQC] PREDICTED: " + uqcResult.getPredictedIntent());
        System.out.println("[UQC] CATEGORY: " + uqcResult.getQueryCategory());
        System.out.println("[UQC] CONFIDENCE: " + String.format("%.0f%%", uqcResult.getConfidence() * 100));
        System.out.println("[UQC] ENTITIES: " + uqcResult.getEntities());
        System.out.println("[UQC] RULES: " + uqcResult.getMatchedRules());
        System.out.println("[UQC] RESULT: " + status);
        System.out.println("[UQC] PROCESSING: " + uqcResult.getProcessingTimeNanos() / 1_000_000 + "ms");
        System.out.println("[UQC] ========================================================");
    }

    private void logCapabilityComparison(String intent, CapabilityMatch capabilityMatch) {
        String currentHandler = mapIntentToHandler(intent);

        System.out.println("[CAPABILITY] ========================================================");
        System.out.println("[CAPABILITY] INTENT: " + intent);
        System.out.println("[CAPABILITY] REGISTRY: " + (capabilityMatch != null ? capabilityMatch.getCapability().getName() : "null"));
        System.out.println("[CAPABILITY] CURRENT: " + currentHandler);
        System.out.println("[CAPABILITY] CONFIDENCE: " + (capabilityMatch != null ? String.format("%.0f%%", capabilityMatch.getConfidence() * 100) : "N/A"));
        System.out.println("[CAPABILITY] RESULT: " + (capabilityMatch != null && capabilityMatch.getCapability().getName().equalsIgnoreCase(currentHandler) ? "SAME" : "DIFFERENT"));
        System.out.println("[CAPABILITY] PROCESSING: " + (capabilityMatch != null ? capabilityMatch.getProcessingTimeNanos() / 1_000_000 + "ms" : "N/A"));
        System.out.println("[CAPABILITY] ========================================================");
    }

    /**
     * Resolve context from conversation context.
     * Simplified version for shadow validation.
     */
    private ResolvedContext resolveContext(ConversationContext context) {
        // Simplified context resolution for shadow mode
        // In production, this would use ContextResolutionEngine
        return new ResolvedContext(
                ResolvedContext.Mode.CHAT,
                null, 0, 0,
                false, false,
                null,
                false
        );
    }

    private String mapIntentToHandler(String intent) {
        return switch (intent) {
            case "START_COURSE", "CONTINUE_LESSON", "COMPLETE_LESSON", "CURRENT_LESSON",
                 "TEACH_TOPIC", "REPEAT_LESSON", "EXIT_COURSE", "LESSON_PROGRESS", "LEARN" ->
                    "LearningSessionEngine";
            case "START_QUIZ", "CONTINUE_QUIZ", "SUBMIT_ANSWER", "FINISH_QUIZ", "QUIZ_RESULT" ->
                    "QuizEngine";
            case "PLAN", "ROADMAP_REQUEST", "NEXT_STEP", "COMPLETE_TASK", "PROGRESS", "CURRENT_TASK" ->
                    "PlanningEngine";
            case "GREETING" -> "GreetingSkill";
            case "WHO_AM_I" -> "AgentBrain";
            case "QUIZ" -> "LessonEngine";
            default -> "ChatSkill";
        };
    }

    private String stripPlaceholders(String reply) {
        if (reply == null) return reply;
        reply = reply.replaceAll("(?i)\\[insert[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[add[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[your[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[link[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[placeholder[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[TODO[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[TBD[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[fill[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)Click here to learn more\\.", "");
        reply = reply.replaceAll("(?i)Learn more at \\[.*?\\]\\.", "");
        reply = reply.replaceAll("(?i)\\(insert link\\)", "");
        reply = reply.replaceAll("\\n{3,}", "\n\n");
        return reply.trim();
    }
}
