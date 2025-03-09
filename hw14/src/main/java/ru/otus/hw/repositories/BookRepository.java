package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends MongoRepository<Book, String> {

    Optional<Book> findById(String id);

    Optional<Book> findByTitle(String title);

    List<Book> findAll();

    Book save(Book book);

    void deleteById(String id);

    Book deleteByTitle(String title);
}
