package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.InputMismatchException;
import java.util.Scanner;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao csvQuestionDao;

    private void checkAnswer(Question question, int answer) {
        if (question.answers().get(answer).isCorrect()) {
            ioService.printLine("it's true");
        } else {
            ioService.printLine("it's false");
        }
    }

    private void getAnswer(Question question) {
        ioService.printLine("Choose the right option: ");
        Scanner inputAnswer = new Scanner(System.in);
        while (true) {
            try {
                byte chosenAnswerNum = inputAnswer.nextByte();
                if (chosenAnswerNum >= 0 && chosenAnswerNum <= question.answers().size()) {
                    checkAnswer(question, chosenAnswerNum - 1);
                    break;
                } else {
                    throw new InputMismatchException();
                }
            } catch (InputMismatchException err) {
                ioService.printLine("There is no such answer, repeat again");
                inputAnswer.next();
            }
        }
    }

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        // Получить вопросы из дао и вывести их с вариантами ответов
        for (Question question : csvQuestionDao.findAll()) {
            ioService.printLine(question.text());
            byte numAnswer = 1;
            for (Answer answer : question.answers()) {
                ioService.printLine(numAnswer++ + ". " + answer.text());
            }
            getAnswer(question);
        }
    }
}
