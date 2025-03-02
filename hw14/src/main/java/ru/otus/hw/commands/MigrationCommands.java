package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.h2.tools.Console;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.sql.SQLException;
import java.util.Date;

import static ru.otus.hw.config.JobConfig.IMPORT_JOB_NAME;

@RequiredArgsConstructor
@ShellComponent
public class MigrationCommands {

    private final Job importJob;

    private final JobLauncher jobLauncher;

    private final JobExplorer jobExplorer;

    @ShellMethod(value = "Run h2 console", key = "h2")
    public void runConsole() {
        try {
            String[] args = new String[0];
            Console.main(args);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    @ShellMethod(value = "startMigrationJobWithJobLauncher", key = "sm-jl")
    public void startMigrationJobWithJobLauncher() throws Exception {
        JobExecution execution = jobLauncher.run(importJob,  new JobParametersBuilder()
                .toJobParameters());
        System.out.println(execution);
    }

    @SuppressWarnings("unused")
    @ShellMethod(value = "restartMigrationJobWithJobLauncher", key = "rm-jl")
    public void restartMigrationJobWithJobLauncher() throws Exception {
        JobExecution execution = jobLauncher.run(importJob,
                new JobParametersBuilder().addDate("start date",new Date())
                .toJobParameters());
        System.out.println(execution);
    }

    @SuppressWarnings("unused")
    @ShellMethod(value = "showInfo", key = "i")
    public void showInfo() {
        System.out.println(jobExplorer.getJobNames());
        System.out.println(jobExplorer.getLastJobInstance(IMPORT_JOB_NAME));
    }

}
