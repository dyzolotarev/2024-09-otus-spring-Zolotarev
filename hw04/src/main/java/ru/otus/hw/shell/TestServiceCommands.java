package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.service.LocalizedIOService;
import ru.otus.hw.service.TestRunnerService;

@ShellComponent
@ShellCommandGroup("Application commands")
@RequiredArgsConstructor
public class TestServiceCommands {

    private final TestRunnerService testRunner;

    private final QuestionDao questionDao;

    private final LocalizedIOService ioService;

    private boolean isTestPassing = false;

    @ShellMethod(value = "Start test", key = {"start-test", "start"})
    public void startTest() {
        testRunner.run();
        isTestPassing = true;
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
