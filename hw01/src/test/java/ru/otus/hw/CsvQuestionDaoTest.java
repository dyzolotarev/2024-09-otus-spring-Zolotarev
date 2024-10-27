package ru.otus.hw;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.otus.hw.config.AppProperties;
import ru.otus.hw.dao.CsvQuestionDao;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CsvQuestionDaoTest {

    @DisplayName("Should skip first line in file")
    @Test
    void testSkipFirstLineInFile()  {

        AppProperties mockAppProperties = Mockito.mock(AppProperties.class);
        Mockito.when(mockAppProperties.getTestFileName()).thenReturn("questions_test.csv");

        CsvQuestionDao csvQuestionDao = new CsvQuestionDao(mockAppProperties);
        List<Question> questionList = csvQuestionDao.findAll();

        assertAll("Should skip first line in file\"",
                () -> assertEquals(5, questionList.size()),
                () -> assertEquals("Is there life on Mars?", questionList.get(0).text() ));

    }

}
