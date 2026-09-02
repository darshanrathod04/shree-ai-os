package com.shreeai.os.platform.kernels.planning.engine.planners;

import com.shreeai.os.platform.kernels.planning.engine.MilestoneGenerator;
import com.shreeai.os.platform.kernels.planning.engine.TaskGraphBuilder;
import com.shreeai.os.platform.kernels.planning.model.Milestone;
import com.shreeai.os.platform.kernels.planning.model.Phase;
import com.shreeai.os.platform.kernels.planning.model.PlanBlueprint;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult;
import com.shreeai.os.platform.kernels.planning.model.PlanningAnalysisResult.Domain;

import java.util.List;
import java.util.Map;

/**
 * Domain planner for AI. Generates a structured AI engineering roadmap
 * covering ML fundamentals, deep learning, LLMs, agents, and deployment.
 */
public final class AIPlanner implements DomainPlanner {

    @Override
    public Domain domain() { return Domain.AI; }

    @Override
    public PlanBlueprint buildPlan(PlanningAnalysisResult analysis) {
        String goal = deriveGoal(analysis);

        List<Phase> phases = TaskGraphBuilder.buildChain(
                new String[]{
                        "Python & Math Foundations",
                        "Machine Learning Fundamentals",
                        "Deep Learning",
                        "Large Language Models",
                        "AI Agent Development",
                        "MLOps & Deployment",
                        "Capstone AI Project"
                },
                new String[]{
                        "Master Python for ML, NumPy, Pandas, and data preprocessing",
                        "Learn supervised/unsupervised learning, model evaluation, and feature engineering",
                        "Understand neural networks, CNNs, RNNs, and transformers",
                        "Master prompt engineering, RAG, fine-tuning, and LLM APIs",
                        "Build autonomous agents, tools, memory, and multi-agent systems",
                        "Containerize models, set up CI/CD, monitor, and scale ML systems",
                        "Design, build, and deploy a complete AI-powered application"
                },
                new int[]{3, 4, 4, 4, 3, 2, 4},
                new String[][]{
                        {"Python syntax", "NumPy arrays", "Pandas DataFrames", "Matplotlib"},
                        {"Linear/logistic regression", "Decision trees", "SVM", "K-means"},
                        {"Perceptron", "PyTorch basics", "CNN", "RNN/LSTM"},
                        {"Prompt engineering", "RAG architecture", "LangChain", "Fine-tuning"},
                        {"Agent architecture", "Tool use", "Memory systems", "Multi-agent"},
                        {"Docker for ML", "MLflow tracking", "FastAPI serving"},
                        {"End-to-end project", "Documentation", "Demo"}
                },
                new String[][]{
                        {"Build a data analysis notebook"},
                        {"Train a model on Kaggle dataset"},
                        {"Train a CNN on image data"},
                        {"Build a RAG chatbot"},
                        {"Deploy a working AI agent"},
                        {"Serve a model via REST API"},
                        {"Ship a portfolio AI project"}
                }
        );

        List<Milestone> milestones = MilestoneGenerator.generateSpaced(
                TaskGraphBuilder.totalWeeks(phases), 4,
                List.of("Python Ready", "ML Fundamentals", "Deep Learning", "LLM Expert", "Agent Builder", "Capstone Complete")
        );

        List<String> risks = List.of(
                "Math fundamentals gap (linear algebra, calculus, statistics)",
                "GPU/COMPUTE resources for training",
                "LLM API costs during development",
                "Scope creep in capstone project",
                "Rapidly evolving AI tooling landscape"
        );

        List<String> successMetrics = List.of(
                "Kaggle competition submission",
                "Deployed LLM application",
                "Functional AI agent with tool use",
                "Capstone project with public demo",
                "Portfolio with 3+ AI projects"
        );

        List<String> recommendations = List.of(
                "Start with Andrew Ng's ML course on Coursera",
                "Read 'Hands-On Machine Learning' by Géron",
                "Use Kaggle for practice and portfolio building",
                "Build a personal AI assistant as your first agent",
                "Join AI communities like r/MachineLearning and LocalLLaMA"
        );

        return new PlanBlueprint(
                "AI Engineering Roadmap",
                "Become an AI Engineer — " + goal,
                TaskGraphBuilder.totalWeeks(phases),
                phases,
                milestones,
                risks,
                successMetrics,
                recommendations,
                Map.of("domain", "AI", "version", "1.0")
        );
    }

    private String deriveGoal(PlanningAnalysisResult analysis) {
        if (analysis.goalText().toLowerCase().contains("assistant")) return "AI Assistant Developer";
        if (analysis.goalText().toLowerCase().contains("agent")) return "AI Agent Developer";
        if (analysis.goalText().toLowerCase().contains("llm")) return "LLM Engineer";
        return "AI Engineer";
    }
}
