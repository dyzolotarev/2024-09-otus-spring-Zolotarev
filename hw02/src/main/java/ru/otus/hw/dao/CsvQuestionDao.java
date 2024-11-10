package ru.otus.hw.dao;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {

        List<Question> questionList;
        try (Reader reader = new InputStreamReader(getFileFromResourceAsStream(fileNameProvider.getTestFileName()))) {

            ColumnPositionMappingStrategy<QuestionDto> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(QuestionDto.class);
            CsvToBean<QuestionDto> csvToBean = new CsvToBeanBuilder<QuestionDto>(reader)
                    .withSeparator(';')
                    .withSkipLines(1)
                    .withMappingStrategy(strategy)
                    .build();

            questionList = csvToBean.parse().stream().map(QuestionDto::toDomainObject).toList();

        } catch (RuntimeException | IOException ex) {
            throw new QuestionReadException("Error processing question list : ", ex);
        }

        return questionList;
    }

    private InputStream getFileFromResourceAsStream(String fileName) {

        // for static access
        //ClassLoader classLoader1 = FileResourcesUtils.class.getClassLoader();

        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }
}
