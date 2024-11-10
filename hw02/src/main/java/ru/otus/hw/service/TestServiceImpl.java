package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

@RequiredArgsConstructor
@Service
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    private void printQuestion(Question question) {
        ioService.printLine(question.text());
        byte numAnswer = 1;
        for (Answer answer : question.answers()) {
            ioService.printLine(numAnswer++ + ". " + answer.text());
        }
    }

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question : questions) {
            printQuestion(question);
            var chosenAnswer = ioService.readIntForRangeWithPrompt(1, question.answers().size(),
                    "Choose the right option: ", "There is no such answer, repeat again");
            var isAnswerValid = question.answers().get(chosenAnswer - 1).isCorrect();
            testResult.applyAnswer(question, isAnswerValid);
        }
        return testResult;
    }
}
