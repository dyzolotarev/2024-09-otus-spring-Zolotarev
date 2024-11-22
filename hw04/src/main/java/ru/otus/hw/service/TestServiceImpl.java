package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

@Service
@ShellComponent
@ShellCommandGroup("Application commands")
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    private boolean isTestPassing = false;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printLineLocalized("TestService.answer.the.questions");
        ioService.printLine("");

        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question : questions) {
            printQuestion(question);
            var chosenAnswer = ioService.readIntForRangeWithPromptLocalized(1, question.answers().size(),
                    "TestService.answer.choose", "TestService.answer.choose.error");
            var isAnswerValid = question.answers().get(chosenAnswer - 1).isCorrect();
            testResult.applyAnswer(question, isAnswerValid);
        }
        isTestPassing = true;
        return testResult;
    }

    private void printQuestion(Question question) {
        ioService.printLine(question.text());
        byte numAnswer = 1;
        for (Answer answer : question.answers()) {
            ioService.printLine(numAnswer++ + ". " + answer.text());
        }
    }

    @ShellMethod(value = "Show questions", key = {"show-questions", "show"})
    @ShellMethodAvailability(value = "isShowQuestionsAvailable")
    private void printAllQuestions() {

        var questions = questionDao.findAll();

        for (var question : questions) {
            ioService.printLine(question.text());
        }
    }

    private Availability isShowQuestionsAvailable() {
        return isTestPassing
                ? Availability.available()
                : Availability.unavailable(ioService.getMessage("TestService.show.message.not.availability"));
    }

}
