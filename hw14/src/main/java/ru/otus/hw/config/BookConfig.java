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
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookV2;

import javax.sql.DataSource;
import java.util.Collections;

import static ru.otus.hw.config.JobConfig.CHUNK_SIZE;

@Slf4j
@Configuration
public class BookConfig {

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private JobRepository jobRepository;

    private final Logger logger = LoggerFactory.getLogger("Batch");

    @Bean
    @StepScope
    public MongoPagingItemReader<Book> readerBook(MongoTemplate template) {
        return new MongoPagingItemReaderBuilder<Book>()
                .name("bookItemReader")
                .template(template)
                .jsonQuery("{}")
                .targetType(Book.class)
                .pageSize(100)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Book, BookV2> processorBook(BookConverter bookConverter) {
        return bookConverter::bookToBookV2;
    }

    @Bean
    public JdbcBatchItemWriter<BookV2> writerJdbcBook(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<BookV2>()
                .dataSource(dataSource)
                .sql("insert into books (id, title, author_id) " +
                     "values (nextval('books_seq'), :title, (select h2_id from item_mapping " +
                     "                                        where mongo_id = :author.mongoId " +
                     "                                          and item_type = 'A')); " +
                     "insert into books_genres (book_id, genre_id)  " +
                     "select currval('books_seq'), h2_id from item_mapping where mongo_id in (:genreMongoIds);" +
                     "insert into item_mapping (h2_id, mongo_id, item_type) " +
                     "values (currval('books_seq'), :mongoId, 'B');")
                .beanMapped()
                .build();
    }

    @Bean
    public Step transformBookStep(ItemReader<Book> reader, JdbcBatchItemWriter<BookV2> writer,
                                   ItemProcessor<Book, BookV2> itemProcessor) {
        return new StepBuilder("transformBookStep", jobRepository)
                .<Book, BookV2>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(reader)
                .processor(itemProcessor)
                .writer(writer)
                .listener(new ChunkListener() {
                    public void beforeChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Конвертация книг: начало пачки");
                    }

                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        logger.info("Конвертация книг: конец пачки");
                    }
                })
                .build();
    }
}
