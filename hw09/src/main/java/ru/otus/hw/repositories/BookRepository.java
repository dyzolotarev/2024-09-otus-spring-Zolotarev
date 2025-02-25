package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @EntityGraph(value = "books-authors-entity-graph")
    Optional<Book> findById(long id);

    @EntityGraph(value = "books-authors-entity-graph")
    List<Book> findAll();

    Book save(Book book);

    void deleteById(long id);
}
