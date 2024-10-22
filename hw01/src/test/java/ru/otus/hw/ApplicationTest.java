package ru.otus.hw;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.config.AppProperties;
import ru.otus.hw.dao.CsvQuestionDao;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTest {

    private AppProperties appProperties;
    private final String testFileName = "questions_test.csv";
    @BeforeEach
     void setup() {
        appProperties = new AppProperties(testFileName);
    }

    @DisplayName("Should return expected file name")
    @Test
    void testFileNameProviderGetFileName(){
        assertThat(appProperties.getTestFileName()).isEqualTo(testFileName);
    }

    @DisplayName("Should skip first line in file")
    @Test
    void testSkipFirstLineInFile()  {

        AppProperties appProperties = new AppProperties(testFileName);
        CsvQuestionDao csvQuestionDao = new CsvQuestionDao(appProperties);
        List<Question> questionList = csvQuestionDao.findAll();

        assertAll("Should skip first line in file\"",
                () -> assertEquals(5, questionList.size()),
                () -> assertEquals("Is there life on Mars?", questionList.get(0).text() ));

    }

}
