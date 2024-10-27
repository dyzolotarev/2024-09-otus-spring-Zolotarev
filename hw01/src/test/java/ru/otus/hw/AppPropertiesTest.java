package ru.otus.hw;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.config.AppProperties;

import static org.assertj.core.api.Assertions.assertThat;

public class AppPropertiesTest {

    @DisplayName("Should return expected file name")
    @Test
    void testFileNameProviderGetFileName(){
        String testFileName = "questions_test.csv";
        AppProperties appProperties = new AppProperties(testFileName);
        assertThat(appProperties.getTestFileName()).isEqualTo(testFileName);
    }

}
