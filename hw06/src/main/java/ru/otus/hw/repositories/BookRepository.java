package ru.otus.hw.repositories;

import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    // lazy поиск книги без "обвеса" в виде авторов и жанров, когда он не нужен
    Optional<Book> getBook(long id);

    Optional<Book> findById(long id);

    List<Book> findAll();

    Book save(Book book);

    void deleteById(long id);
}
