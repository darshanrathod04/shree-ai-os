package platform.learning.quiz;

import platform.learning.curriculum.CurriculumRepository;
import platform.learning.curriculum.QuizResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository layer for quiz resources.
 * Loads quiz definitions from existing curriculum quiz.json files.
 * Converts curriculum QuizResource to domain QuizQuestion objects.
 *
 * Repository → Service → Engine architecture.
 */
@Repository
public class QuizRepository {

    private static final Logger log = LoggerFactory.getLogger(QuizRepository.class);

    private final CurriculumRepository curriculumRepository;

    public QuizRepository(CurriculumRepository curriculumRepository) {
        this.curriculumRepository = curriculumRepository;
        log.info("[QUIZ] QuizRepository initialized");
    }

    /**
     * Load quiz questions for a given course chapter.
     *
     * @param courseName    the course name (e.g., "java")
     * @param chapterNumber the 1-based chapter number
     * @return list of QuizQuestion domain objects, or empty if no quiz found
     */
    public List<QuizQuestion> loadQuizQuestions(String courseName, int chapterNumber) {
        Optional<QuizResource> quizOpt = curriculumRepository.loadQuiz(courseName, chapterNumber);
        if (quizOpt.isEmpty()) {
            log.warn("[QUIZ] No quiz found for {}/chapter{}", courseName, chapterNumber);
            return List.of();
        }

        QuizResource quizResource = quizOpt.get();
        List<QuizQuestion> questions = new ArrayList<>();

        for (int i = 0; i < quizResource.getQuestions().size(); i++) {
            QuizResource.QuizQuestion src = quizResource.getQuestions().get(i);
            QuizQuestion.QuestionType type = mapType(src.getType());

            String id = UUID.nameUUIDFromBytes(
                    (courseName + "/ch" + chapterNumber + "/q" + i).getBytes()
            ).toString().substring(0, 8);

            QuizQuestion question = new QuizQuestion(
                    id,
                    type,
                    src.getQuestion(),
                    src.getOptions(),
                    src.getCorrectAnswer(),
                    src.getExplanation()
            );
            questions.add(question);
        }

        log.info("[QUIZ] Loaded {} questions for {}/chapter{}", questions.size(), courseName, chapterNumber);
        return questions;
    }

    /**
     * Check if a quiz exists for a given course chapter.
     */
    public boolean hasQuiz(String courseName, int chapterNumber) {
        return curriculumRepository.loadQuiz(courseName, chapterNumber).isPresent();
    }

    /**
     * Get the quiz title for a given course chapter.
     */
    public Optional<String> loadQuizTitle(String courseName, int chapterNumber) {
        return curriculumRepository.loadQuiz(courseName, chapterNumber)
                .map(QuizResource::getTitle);
    }

    private QuizQuestion.QuestionType mapType(String type) {
        if (type == null) return QuizQuestion.QuestionType.MCQ;
        return switch (type.toUpperCase()) {
            case "MCQ" -> QuizQuestion.QuestionType.MCQ;
            case "TRUE_FALSE", "TRUE/FALSE" -> QuizQuestion.QuestionType.TRUE_FALSE;
            case "FILL_BLANK", "FILL_IN_THE_BLANK" -> QuizQuestion.QuestionType.FILL_BLANK;
            case "CODING" -> QuizQuestion.QuestionType.CODING;
            default -> {
                log.warn("[QUIZ] Unknown question type '{}', defaulting to MCQ", type);
                yield QuizQuestion.QuestionType.MCQ;
            }
        };
    }
}