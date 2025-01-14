package ru.otus.hw.services;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.converters.BookCommentConverter;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.repositories.JpaBookCommentRepository;
import ru.otus.hw.repositories.JpaBookRepository;

import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Сервис для работы с комментариями к книгам ")
@DataJpaTest
@Import({BookCommentConverter.class, BookConverter.class, AuthorConverter.class, GenreConverter.class,
        BookCommentServiceImpl.class, JpaBookCommentRepository.class, JpaBookRepository.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional(propagation = Propagation.NEVER)
public class BookCommentServiceTest {

    private static final long COMMENTED_BOOK_ID = 2L;
    private static final long COMMENT_ID = 3L;
    private static final long EXPECTED_NUMBER_OF_COMMENTS = 2;

    @Autowired
    private BookCommentService bookCommentService;

    @DisplayName("должен загружать комментарий по id")
    @Order(1)
    @Test
    void shouldReturnCorrectCommentById() {
        var actualBookComment = bookCommentService.findById(COMMENT_ID);
        var book = new BookDto(COMMENTED_BOOK_ID, "BookTitle_2", new AuthorDto(2, "Author_2"),
                List.of(new GenreDto(3, "Genre_3"), new GenreDto(4, "Genre_4")));
        var expectedBookComment = new BookCommentDto(COMMENT_ID, book, "comment3");
        assertThat(actualBookComment).get().usingRecursiveComparison()
                    .ignoringExpectedNullFields().isEqualTo(expectedBookComment);
    }

    @DisplayName("должен загружать список всех комментариев книги")
    @Order(1)
    @Test
    void shouldReturnCorrectBookCommentsList() {
        var actualBookComments = bookCommentService.findForBook(COMMENTED_BOOK_ID);
        assertThat(actualBookComments.size()).isEqualTo(EXPECTED_NUMBER_OF_COMMENTS);

        var expectedBookComments = LongStream.range(3, 5).boxed()
                .map(id -> bookCommentService.findById(id).get()).toList();
        assertThat(actualBookComments).usingRecursiveComparison()
                .ignoringExpectedNullFields().isEqualTo(expectedBookComments);
    }

    @DisplayName("должен сохранять новый комментарий, обрабатывать неверные параметры")
    @Order(2)
    @Test
    void shouldSaveNewBookComment() {
        String newComment = "New Comment";

        var countCommentsBefore = bookCommentService.findForBook(COMMENTED_BOOK_ID).size();
        var savedBookComment = bookCommentService.insert(COMMENTED_BOOK_ID, newComment);
        var countCommentsAfter = bookCommentService.findForBook(COMMENTED_BOOK_ID).size();
        assertThat(countCommentsAfter).isEqualTo(countCommentsBefore + 1);

        var dbBookComment = bookCommentService.findById(savedBookComment.getId());
        assertThat(dbBookComment).isPresent().get()
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(savedBookComment);

        EntityNotFoundException thrownBook = assertThrows(EntityNotFoundException.class, () -> {
            bookCommentService.insert(10000000000L, newComment);
        });
        assertEquals("Book with id 10000000000 not found", thrownBook.getMessage());
    }

    @DisplayName("должен обновлять комментарий, обрабатывать неверные параметры")
    @Order(2)
    @Test
    void shouldUpdateNewBookComment() {
        String modifiedComment = "Modified book comment";

        var countCommentsBefore = bookCommentService.findForBook(COMMENTED_BOOK_ID).size();
        var savedBookComment = bookCommentService.update(COMMENT_ID, modifiedComment);
        var countCommentsAfter = bookCommentService.findForBook(COMMENTED_BOOK_ID).size();
        assertThat(countCommentsAfter).isEqualTo(countCommentsBefore);

        var dbBookComment = bookCommentService.findById(COMMENT_ID);
        assertThat(dbBookComment).isPresent().get()
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(savedBookComment);

        EntityNotFoundException thrownBook = assertThrows(EntityNotFoundException.class, () -> {
            bookCommentService.update(2000000000000L, modifiedComment);
        });
        assertEquals("Book comment with id 2000000000000 not found", thrownBook.getMessage());
    }

    @DisplayName("должен удалять комментарий по id ")
    @Test
    void shouldDeleteBookComment() {
        var countCommentsBefore = bookCommentService.findForBook(COMMENTED_BOOK_ID).size();
        bookCommentService.deleteById(COMMENT_ID);
        var countCommentsAfter = bookCommentService.findForBook(COMMENTED_BOOK_ID).size();
        assertThat(countCommentsAfter).isEqualTo(countCommentsBefore - 1);
        var deletedBookComment = bookCommentService.findById(COMMENT_ID);
        assertThat(deletedBookComment).isEmpty();
    }
}
