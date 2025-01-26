package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends MongoRepository<Author, String> {
    List<Author> findAll();

    Optional<Author> findById(String id);

    Optional<Author> findByFullName(String name);
}
