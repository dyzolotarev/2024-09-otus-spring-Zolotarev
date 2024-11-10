package ru.otus.hw;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.otus.hw.dao.CsvQuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.service.IOService;
import ru.otus.hw.service.TestServiceImpl;
import ru.otus.hw.domain.TestResult;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;

public class TestServiceImplTest {

    @DisplayName("Should print all questions and answers")
    @Test
    public void testPrintAllQuestionsAndAnswers() {

        IOService mockIOService = Mockito.mock(IOService.class);
        CsvQuestionDao mockCsvQuestionDao = Mockito.mock(CsvQuestionDao.class);
        Student mockStudent = Mockito.mock(Student.class);

        List<Answer> testListAnwser = new ArrayList<>();
        testListAnwser.add(new Answer("TestAnswer1", true));
        testListAnwser.add(new Answer("TestAnswer2", false));

        List<Question> testQuestionList = new ArrayList<>();
        testQuestionList.add(new Question("TestQuestion1", testListAnwser));

        Mockito.when(mockCsvQuestionDao.findAll()).thenReturn(testQuestionList);
        Mockito.when(mockIOService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString())).thenReturn(1);

        TestServiceImpl testService = new TestServiceImpl(mockIOService, mockCsvQuestionDao);
        TestResult testResult = testService.executeTestFor(mockStudent);
        Mockito.verify(mockIOService, times(4)).printLine(anyString()); // 1 welcome message + 1 question + 2 answer
    }
}
