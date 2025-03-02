package ru.otus.hw.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.MongoPagingItemReader;
import org.springframework.batch.item.data.builder.MongoPagingItemReaderBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.models.GenreV2;
import ru.otus.hw.models.Genre;

import javax.sql.DataSource;
import java.util.Collections;

import static ru.otus.hw.config.JobConfig.CHUNK_SIZE;

@Slf4j
@Configuration
public class GenreConfig {

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private JobRepository jobRepository;

    private final Logger logger = LoggerFactory.getLogger("Batch");

    @Bean
    @StepScope
    public MongoPagingItemReader<Genre> readerGenre(MongoTemplate template) {
        return new MongoPagingItemReaderBuilder<Genre>()
                .name("genreItemReader")
                .template(template)
                .jsonQuery("{}")
                .targetType(Genre.class)
                .pageSize(100)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Genre, GenreV2> processorGenre(GenreConverter genreConverter) {
        return genreConverter::genreToGenreV2;
    }

    @Bean
    public JdbcBatchItemWriter<GenreV2> writerJdbcGenre(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<GenreV2>()
                .dataSource(dataSource)
                .sql("insert into genres (id, name) " +
                     "values (nextval('genres_seq'), :name); " +
                     "insert into item_mapping (h2_id, mongo_id, item_type) " +
                     "values (currval('genres_seq'), :mongoId, 'G');")
                .beanMapped()
                .build();
    }

    @Bean
    public Step transformGenreStep(ItemReader<Genre> reader, JdbcBatchItemWriter<GenreV2> writer,
                                    ItemProcessor<Genre, GenreV2> itemProcessor) {
        return new StepBuilder("transformGenreStep", jobRepository)
                .<Genre, GenreV2>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(reader)
                .processor(itemProcessor)
                .writer(writer)
                .listener(new ChunkListener() {
                    public void beforeChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Конвертация жанров: начало пачки");
                    }

                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Конвертация жанров: конец пачки");
                    }
                })
                .build();
    }
}
