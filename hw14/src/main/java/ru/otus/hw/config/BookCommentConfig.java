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
import ru.otus.hw.converters.BookCommentConverter;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.BookCommentV2;

import javax.sql.DataSource;
import java.util.Collections;

import static ru.otus.hw.config.JobConfig.CHUNK_SIZE;

@Slf4j
@Configuration
public class BookCommentConfig {

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private JobRepository jobRepository;

    private final Logger logger = LoggerFactory.getLogger("Batch");

    @Bean
    @StepScope
    public MongoPagingItemReader<BookComment> readerBookComment(MongoTemplate template) {
        return new MongoPagingItemReaderBuilder<BookComment>()
                .name("bookCommentItemReader")
                .template(template)
                .jsonQuery("{}")
                .targetType(BookComment.class)
                .pageSize(100)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<BookComment, BookCommentV2> processorBookComment(BookCommentConverter bookCommentConverter) {
        return bookCommentConverter::bookCommentToBookCommentV2;
    }

    @Bean
    public JdbcBatchItemWriter<BookCommentV2> writerJdbcBookComment(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<BookCommentV2>()
                .dataSource(dataSource)
                .sql("insert into book_comments (id, comment, book_id) " +
                      "values (nextval('book_comments_seq'), :comment, (select h2_id from item_mapping " +
                      "                                                  where mongo_id = :book.mongoId " +
                      "                                                    and item_type = 'B')); ")
                .beanMapped()
                .build();
    }

    @Bean
    public Step transformBookCommentStep(ItemReader<BookComment> reader, JdbcBatchItemWriter<BookCommentV2> writer,
                                         ItemProcessor<BookComment, BookCommentV2> itemProcessor) {
        return new StepBuilder("transformBookCommentStep", jobRepository)
                .<BookComment, BookCommentV2>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(reader)
                .processor(itemProcessor)
                .writer(writer)
                .listener(new ChunkListener() {
                    public void beforeChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Конвертация комментариев: начало пачки");
                    }

                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Конвертация комментариев: конец пачки");
                    }
                })
                .build();
    }

}
