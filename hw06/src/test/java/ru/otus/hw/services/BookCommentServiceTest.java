package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// пока не хватило запала реализовать все операции, но остальные функции из сервисов
// просто вызывают соответствующие методы репозитория, которые уже покрыты тестами
// и непонятно насколько осмысленно повторять это в тестах сервиса
@DisplayName("Сервис для работы с комментариями к книгам ")
@DataJpaTest
@ComponentScan(basePackages = "ru.otus.hw")
public class BookCommentServiceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookCommentService bookCommentService;

    private BookComment bookComment;

    private Book book;

    @BeforeEach
    void setUp() {
        bookComment = em.getEntityManager()
                .createQuery("select bc from BookComment bc", BookComment.class).getResultList().get(0);
        book = bookComment.getBook();
        em.detach(bookComment);
    }

    @DisplayName("должен сохранять новый комментарий, обрабатывать неверные параметры")
    @Test
    void shouldSaveNewBookComment() {
        String newComment = "New Comment";
        var savedBookComment = bookCommentService.insert(book.getId(), newComment); // коммент к произвольной книге
        var dbBookComment = Optional.ofNullable(em.find(BookComment.class, savedBookComment.getId()));

        assertThat(dbBookComment).isPresent().get()
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(savedBookComment);

        EntityNotFoundException thrownBook = assertThrows(EntityNotFoundException.class, () -> {
            bookCommentService.insert(10000000000L, newComment);
        });
        assertEquals("Book with id 10000000000 not found", thrownBook.getMessage());
    }

    @DisplayName("должен обновлять комментарий, обрабатывать неверные параметры")
    @Test
    void shouldUpdateNewBookComment() {

        String modifiedComment = "Modified " + bookComment.getComment();

        var savedBookComment = bookCommentService.update(bookComment.getId(), modifiedComment);
        var dbBookComment = Optional.ofNullable(em.find(BookComment.class, bookComment.getId()));

        assertThat(dbBookComment).isPresent().get()
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(savedBookComment);

        EntityNotFoundException thrownBook = assertThrows(EntityNotFoundException.class, () -> {
            bookCommentService.update(2000000000000L, modifiedComment);
        });
        assertEquals("Book comment with id 2000000000000 not found", thrownBook.getMessage());
    }

}
