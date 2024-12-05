package ru.otus.hw.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.otus.hw.config.AppProperties;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class CsvQuestionDaoTest {

    @MockBean
    private AppProperties mockAppProperties;

    @Autowired
    private CsvQuestionDao csvQuestionDao;

    @DisplayName("Should skip first line in file")
    @Test
    void testSkipFirstLineInFile() {

        Mockito.when(mockAppProperties.getTestFileName()).thenReturn("questions_test.csv");

        List<Question> questionList = csvQuestionDao.findAll();

        assertAll("Should skip first line in file\"",
                () -> assertEquals(5, questionList.size()),
                () -> assertEquals("Is there life on Mars?", questionList.get(0).text()));

    }

}
