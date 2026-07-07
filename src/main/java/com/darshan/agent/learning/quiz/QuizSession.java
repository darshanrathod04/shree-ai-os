package com.darshan.agent.learning.quiz;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Per-session quiz state. Belongs to ConversationSession.
 * NOT a singleton — each session owns its own QuizSession.
 *
 * Thread-safe via ReentrantReadWriteLock.
 * Tracks current question index, answers submitted, and quiz lifecycle.
 */
public class QuizSession {

    public enum QuizStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    private String quizTitle;
    private String courseName;
    private int chapterNumber;
    private List<QuizQuestion> questions;
    private int currentQuestionIndex;
    private final List<QuizAttempt> attempts;
    private QuizStatus status;
    private Instant startedAt;
    private Instant completedAt;

    public QuizSession() {
        this.questions = new ArrayList<>();
        this.attempts = new ArrayList<>();
        this.currentQuestionIndex = 0;
        this.status = QuizStatus.NOT_STARTED;
    }

    // --- Lifecycle ---

    /**
     * Start a quiz with the given questions.
     */
    public void startQuiz(String quizTitle, String courseName, int chapterNumber, List<QuizQuestion> questions) {
        writeLock.lock();
        try {
            this.quizTitle = quizTitle;
            this.courseName = courseName;
            this.chapterNumber = chapterNumber;
            this.questions = new ArrayList<>(questions);
            this.currentQuestionIndex = 0;
            this.attempts.clear();
            this.status = QuizStatus.IN_PROGRESS;
            this.startedAt = Instant.now();
            this.completedAt = null;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Submit an answer for the current question.
     *
     * @param submittedAnswer the user's answer
     * @param correct         whether the answer is correct
     * @return true if there are more questions, false if quiz is complete
     */
    public boolean submitAnswer(Object submittedAnswer, boolean correct) {
        writeLock.lock();
        try {
            if (status != QuizStatus.IN_PROGRESS) {
                return false;
            }

            if (currentQuestionIndex < questions.size()) {
                QuizQuestion question = questions.get(currentQuestionIndex);
                QuizAttempt attempt = new QuizAttempt(question.getId(), submittedAnswer, correct);
                attempts.add(attempt);
                currentQuestionIndex++;

                if (currentQuestionIndex >= questions.size()) {
                    status = QuizStatus.COMPLETED;
                    completedAt = Instant.now();
                    return false;
                }
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Manually finish the quiz (mark as completed).
     */
    public void finish() {
        writeLock.lock();
        try {
            if (status == QuizStatus.IN_PROGRESS) {
                status = QuizStatus.COMPLETED;
                completedAt = Instant.now();
            }
        } finally {
            writeLock.unlock();
        }
    }

    // --- Accessors ---

    public String getQuizTitle() {
        readLock.lock();
        try { return quizTitle; } finally { readLock.unlock(); }
    }

    public String getCourseName() {
        readLock.lock();
        try { return courseName; } finally { readLock.unlock(); }
    }

    public int getChapterNumber() {
        readLock.lock();
        try { return chapterNumber; } finally { readLock.unlock(); }
    }

    public QuizQuestion getCurrentQuestion() {
        readLock.lock();
        try {
            if (status != QuizStatus.IN_PROGRESS || currentQuestionIndex >= questions.size()) {
                return null;
            }
            return questions.get(currentQuestionIndex);
        } finally {
            readLock.unlock();
        }
    }

    public int getCurrentQuestionIndex() {
        readLock.lock();
        try { return currentQuestionIndex; } finally { readLock.unlock(); }
    }

    public int getTotalQuestions() {
        readLock.lock();
        try { return questions.size(); } finally { readLock.unlock(); }
    }

    public int getRemainingQuestions() {
        readLock.lock();
        try { return questions.size() - currentQuestionIndex; } finally { readLock.unlock(); }
    }

    public List<QuizAttempt> getAttempts() {
        readLock.lock();
        try { return Collections.unmodifiableList(new ArrayList<>(attempts)); } finally { readLock.unlock(); }
    }

    public QuizStatus getStatus() {
        readLock.lock();
        try { return status; } finally { readLock.unlock(); }
    }

    public boolean isInProgress() {
        readLock.lock();
        try { return status == QuizStatus.IN_PROGRESS; } finally { readLock.unlock(); }
    }

    public boolean isCompleted() {
        readLock.lock();
        try { return status == QuizStatus.COMPLETED; } finally { readLock.unlock(); }
    }

    public boolean hasActiveQuiz() {
        readLock.lock();
        try { return status == QuizStatus.IN_PROGRESS && questions != null && !questions.isEmpty(); } finally { readLock.unlock(); }
    }

    public Instant getStartedAt() {
        readLock.lock();
        try { return startedAt; } finally { readLock.unlock(); }
    }

    public Instant getCompletedAt() {
        readLock.lock();
        try { return completedAt; } finally { readLock.unlock(); }
    }

    /**
     * Reset the quiz session state.
     */
    public void reset() {
        writeLock.lock();
        try {
            quizTitle = null;
            courseName = null;
            chapterNumber = 0;
            questions = new ArrayList<>();
            currentQuestionIndex = 0;
            attempts.clear();
            status = QuizStatus.NOT_STARTED;
            startedAt = null;
            completedAt = null;
        } finally {
            writeLock.unlock();
        }
    }
}