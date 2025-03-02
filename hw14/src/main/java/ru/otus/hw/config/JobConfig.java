package ru.otus.hw.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.services.CleanUpService;

@SuppressWarnings("unused")
@Configuration
public class JobConfig {

    public static final String IMPORT_JOB_NAME = "importJob";

    public static final int CHUNK_SIZE = 100;

    private final Logger logger = LoggerFactory.getLogger("Batch");

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private Step transformBookStep;

    @Autowired
    private Step transformGenreStep;

    @Autowired
    private Step transformAuthorStep;

    @Autowired
    private Step transformBookCommentStep;

    @Autowired
    private CleanUpService cleanUpService;

    @Bean
    public Job importJob(Flow flowAuthorsAndGenres, Step cleanUpStep) {
        return new JobBuilder(IMPORT_JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(flowAuthorsAndGenres)
                .next(transformBookStep)
                .next(transformBookCommentStep)
                .next(cleanUpStep)
                .end()
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(@NonNull JobExecution jobExecution) {
                        logger.info("Начало job");
                    }

                    @Override
                    public void afterJob(@NonNull JobExecution jobExecution) {
                        logger.info("Конец job");
                    }
                })
                .build();
    }

    @Bean
    public MethodInvokingTaskletAdapter cleanUpTasklet() {
        MethodInvokingTaskletAdapter adapter = new MethodInvokingTaskletAdapter();

        adapter.setTargetObject(cleanUpService);
        adapter.setTargetMethod("cleanUp");

        return adapter;
    }

    @Bean
    public Step cleanUpStep() {
        return new StepBuilder("cleanUpStep", jobRepository)
                .tasklet(cleanUpTasklet(), platformTransactionManager)
                .build();
    }

    @Bean
    public Flow flowAuthor(Step transformAuthorStep) {
        return new FlowBuilder<SimpleFlow>("flowAuthor")
                .start(transformAuthorStep)
                .build();
    }

    @Bean
    public Flow flowGenre(Step transformGenreStep) {
        return new FlowBuilder<SimpleFlow>("flowGenre")
                .start(transformGenreStep)
                .build();
    }

    @Bean
    public Flow flowAuthorsAndGenres(Step transformAuthorStep, Step transformGenreStep) {
        return new FlowBuilder<SimpleFlow>("flowAuthorsAndGenres")
                .split(new SimpleAsyncTaskExecutor())
                .add(flowAuthor(transformAuthorStep), flowGenre(transformGenreStep))
                .build();
    }

}
