package ru.otus.hw;

import com.github.cloudyrock.spring.v5.EnableMongock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

// Запуск миграции sm-jl
// Перезапуск миграции rm-jl (еще раз закидывает теже данные, старые не чистит, наверное это и подразумевалось)
// Запуск консоли h2
@EnableMongock
@EnableMongoRepositories(basePackages = {"ru.otus.hw.repositories"})
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
