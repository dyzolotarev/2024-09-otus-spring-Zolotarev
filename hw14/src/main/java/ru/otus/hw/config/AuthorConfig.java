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
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.AuthorV2;

import javax.sql.DataSource;
import java.util.Collections;

import static ru.otus.hw.config.JobConfig.CHUNK_SIZE;

@Slf4j
@Configuration
public class AuthorConfig {

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private JobRepository jobRepository;

    private final Logger logger = LoggerFactory.getLogger("Batch");

    @Bean
    @StepScope
    public MongoPagingItemReader<Author> readerAuthor(MongoTemplate template) {
        return new MongoPagingItemReaderBuilder<Author>()
                .name("authorItemReader")
                .template(template)
                .jsonQuery("{}")
                .targetType(Author.class)
                .pageSize(100)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Author, AuthorV2> processorAuthor(AuthorConverter authorConverter) {
        return authorConverter::authorToAuthorV2;
    }

    @Bean
    public JdbcBatchItemWriter<AuthorV2> writerJdbcAuthor(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<AuthorV2>()
                .dataSource(dataSource)
                .sql("insert into authors (id, full_name) " +
                     "values (nextval('authors_seq'), :fullName); " +
                     "insert into item_mapping (h2_id, mongo_id, item_type) " +
                     "values (currval('authors_seq'), :mongoId, 'A');")
                .beanMapped()
                .build();
    }

    @Bean
    public Step transformAuthorStep(ItemReader<Author> reader, JdbcBatchItemWriter<AuthorV2> writer,
                                    ItemProcessor<Author, AuthorV2> itemProcessor) {
        return new StepBuilder("transformAuthorStep", jobRepository)
                .<Author, AuthorV2>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(reader)
                .processor(itemProcessor)
                .writer(writer)
                .listener(new ChunkListener() {
                    public void beforeChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Конвертация авторов: начало пачки");
                    }

                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Конвертация авторов: конец пачки");
                    }
                })
                .build();
    }
}
