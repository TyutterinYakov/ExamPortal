package ru.pet.portal.api.service.impl;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pet.portal.api.service.QuizService;
import ru.pet.portal.api.service.model.QuizSpecification;
import ru.pet.portal.store.entity.Category;
import ru.pet.portal.store.entity.Quiz;
import ru.pet.portal.store.repository.CategoryRepository;
import ru.pet.portal.store.repository.QuestionRepository;
import ru.pet.portal.store.repository.QuizRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    @Override
    public void create(UUID categoryId, Quiz quiz) {
        Category category = categoryRepository.findByIdWithThrow(categoryId);
        quiz.setCategory(category);
        quizRepository.save(quiz);
    }

    @Override
    public void deleteById(UUID id) {
        quizRepository.deleteById(id);
    }

    @Override
    public List<Quiz> getAllByCategoryId(UUID categoryId, int from, int size) {
        final List<Quiz> quizzes = quizRepository.findAllByCategoryId(categoryId, PageRequest.of(from, size, Sort.by("title")));
        setQuizSpecification(quizzes);
        return quizzes;
    }

    @Override
    public Quiz getById(UUID quizId) {
        return quizRepository.findByIdWithThrow(quizId);
    }

    @Override
    @Transactional
    public void update(UUID categoryId, Quiz quiz, UUID quizId) {
        Quiz havingQuiz = quizRepository.findByIdWithThrow(quizId);
        if (categoryId != null) {
            havingQuiz.setCategory(categoryRepository.findByIdWithThrow(categoryId));
        }
        final String title = quiz.getTitle();
        if (!StringUtils.isBlank(title)) {
            havingQuiz.setTitle(title);
        }
        final String description = quiz.getDescription();
        if (!StringUtils.isBlank(description)) {
            havingQuiz.setDescription(description);
        }
        final Boolean active = quiz.getActive();
        if (active != null) {
            havingQuiz.setActive(active);
        }
    }

    @Override
    public List<Quiz> getAll(int from, int size) {
        final List<Quiz> quizzes = quizRepository.findAll(PageRequest.of(from, size, Sort.by("title")))
                .getContent();
        setQuizSpecification(quizzes);
        return quizzes;
    }


    private void setQuizSpecification(List<Quiz> quizzes) {
        final Map<UUID, QuizSpecification> quizSpecificationByQuizId =
                questionRepository.getQuizSpecificationByQuizId(quizzes);
        quizzes.forEach(quiz -> Optional.ofNullable(quizSpecificationByQuizId.get(quiz.getId()))
                .ifPresent(s ->
                        quiz.setCountOfQuestion(s.getCountOfQuestion())
                                .setMaxMarks(s.getMaxMarks())
                                .setTime(s.getTime()))
        );
    }
}
