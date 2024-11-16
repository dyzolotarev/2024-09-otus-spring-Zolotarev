package ru.otus.hw.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@PropertySource("classpath:application.test.properties")
public class AppPropertiesTest {

    @Value("${test.rightAnswersCountToPass}")
    int rightAnswersCountToPass;

    @Value("${test.fileName}")
    String testFileName;

    @DisplayName("Should return expected file name")
    @Test
    void testFileNameProviderGetFileName(){

        AppProperties appProperties = new AppProperties(rightAnswersCountToPass, testFileName);
        assertThat(appProperties.getTestFileName()).isEqualTo(testFileName);
    }

}
