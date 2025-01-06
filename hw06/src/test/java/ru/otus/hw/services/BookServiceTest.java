package ru.otus.hw.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// пока не хватило запала реализовать все операции, но остальные функции из сервисов
// просто вызывают соответствующие методы репозитория, которые уже покрыты тестами
// и непонятно насколько осмысленно повторять это в тестах сервиса
@DisplayName("Сервис для работы с книгами ")
@DataJpaTest
@ComponentScan(basePackages = "ru.otus.hw")
public class BookServiceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookService bookService;

    private Author dbAuthor;

    private List<Genre> dbGenres;

    @BeforeEach
    void setUp() {
        dbAuthor = em.getEntityManager().createQuery("select a from Author a", Author.class).getResultList().get(0);
        dbGenres = em.getEntityManager().createQuery("select g from Genre g", Genre.class).getResultList();
    }

    @DisplayName("должен сохранять новую книгу, обрабатывать пустые и неверные параметры")
    @Test
    void shouldSaveNewBook() {
        String newBookTitle = "New Book Title";
        Set<Long> genreIds = dbGenres.stream().map(Genre::getId).collect(Collectors.toSet());

        var savedBook = bookService.insert(newBookTitle, dbAuthor.getId(), genreIds);
        var dbBook = Optional.ofNullable(em.find(Book.class, savedBook.getId()));
        assertThat(dbBook).isPresent().get()
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(savedBook);

        IllegalArgumentException thrownGenre = assertThrows(IllegalArgumentException.class, () -> {
            bookService.insert(newBookTitle, dbAuthor.getId(), null);
        });
        assertEquals("Genres ids must not be null", thrownGenre.getMessage());

        EntityNotFoundException thrownAuthor = assertThrows(EntityNotFoundException.class, () -> {
            bookService.insert(newBookTitle, 100000000L, genreIds);
        });
        assertEquals("Author with id 100000000 not found", thrownAuthor.getMessage());

        EntityNotFoundException thrownGenre2 = assertThrows(EntityNotFoundException.class, () -> {
            bookService.insert(newBookTitle, dbAuthor.getId(), Set.of(100000000L));
        });
        assertEquals("One or all genres with ids [100000000] not found", thrownGenre2.getMessage());
    }

    @DisplayName("должен обновлять существующую книгу, обрабатывать пустые и неверные параметры")
    @Test
    void shouldUpdateBook() {
        Set<Long> genreIds = dbGenres.stream().map(Genre::getId).collect(Collectors.toSet());

        var currenBook = em.getEntityManager()
                .createQuery("select b from Book b", Book.class).getResultList().get(0);
        em.detach(currenBook);

        String modifiedBookTitle = "Modified " + currenBook.getTitle();
        var savedBook = bookService.update(currenBook.getId(), modifiedBookTitle, dbAuthor.getId(), genreIds);
        var dbBook = Optional.ofNullable(em.find(Book.class, currenBook.getId()));
        assertThat(dbBook).isPresent().get()
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(savedBook);

        IllegalArgumentException thrownGenre = assertThrows(IllegalArgumentException.class, () -> {
            bookService.update(currenBook.getId(), modifiedBookTitle, dbAuthor.getId(), null);
        });
        assertEquals("Genres ids must not be null", thrownGenre.getMessage());

        EntityNotFoundException thrownAuthor = assertThrows(EntityNotFoundException.class, () -> {
            bookService.update(currenBook.getId(), modifiedBookTitle, 100000000L, genreIds);
        });
        assertEquals("Author with id 100000000 not found", thrownAuthor.getMessage());

        EntityNotFoundException thrownGenre2 = assertThrows(EntityNotFoundException.class, () -> {
            bookService.update(currenBook.getId(), modifiedBookTitle, dbAuthor.getId(), Set.of(100000000L));
        });
        assertEquals("One or all genres with ids [100000000] not found", thrownGenre2.getMessage());
    }

    @AfterEach
    void tearDown() {
        em.clear();
    }
}
