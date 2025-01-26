package ru.otus.hw.services;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.converters.BookCommentConverter;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Сервис для работы с комментариями к книгам ")
@DataMongoTest
@Import({BookCommentConverter.class, BookConverter.class, AuthorConverter.class, GenreConverter.class,
        BookCommentServiceImpl.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional(propagation = Propagation.NEVER)
public class BookCommentServiceTest {

    private static final long EXPECTED_NUMBER_OF_COMMENTS = 2;
    private static final String TITLE_OF_BOOK_WITH_COMMENTS = "BookTitle_1";

    @Autowired
    private BookCommentService bookCommentService;

    @DisplayName("должен загружать список всех комментариев книги")
    @Order(1)
    @Test
    void shouldReturnCorrectBookCommentsList() {
        var actualBookComments = bookCommentService.findByBookTitle(TITLE_OF_BOOK_WITH_COMMENTS);
        assertThat(actualBookComments.size()).isEqualTo(EXPECTED_NUMBER_OF_COMMENTS);

        var expectedBookComments = LongStream.range(1, 3).boxed().map(id ->
                new BookCommentDto(null, actualBookComments.get(0).getBook(), "comment" + id)).toList();
        assertThat(actualBookComments).usingRecursiveComparison()
                .ignoringExpectedNullFields().isEqualTo(expectedBookComments);
    }

    @DisplayName("должен сохранять новый комментарий, обрабатывать неверные параметры")
    @Order(2)
    @Test
    void shouldSaveNewBookComment() {
        String newComment = "New Comment";

        var countCommentsBefore = bookCommentService.findByBookTitle(TITLE_OF_BOOK_WITH_COMMENTS).size();
        var savedBookComment = bookCommentService.insert(TITLE_OF_BOOK_WITH_COMMENTS, newComment);
        var countCommentsAfter = bookCommentService.findByBookTitle(TITLE_OF_BOOK_WITH_COMMENTS).size();
        assertThat(countCommentsAfter).isEqualTo(countCommentsBefore + 1);

        var dbBookComment = bookCommentService.findById(savedBookComment.getId());
        assertThat(dbBookComment).isPresent().get().usingRecursiveComparison().isEqualTo(savedBookComment);

        EntityNotFoundException thrownBook = assertThrows(EntityNotFoundException.class, () -> {
            bookCommentService.insert("0", newComment);
        });
        assertEquals("Book with title 0 not found", thrownBook.getMessage());
    }

    @DisplayName("должен обновлять комментарий, обрабатывать неверные параметры")
    @Order(2)
    @Test
    void shouldUpdateNewBookComment() {
        String modifiedComment = "Modified book comment";

        var commentsBefore = bookCommentService.findByBookTitle(TITLE_OF_BOOK_WITH_COMMENTS);
        var savedBookComment = bookCommentService.update(commentsBefore.get(0).getId(), modifiedComment);
        var commentsAfter = bookCommentService.findByBookTitle(TITLE_OF_BOOK_WITH_COMMENTS);
        assertThat(commentsAfter.size()).isEqualTo(commentsBefore.size());

        var dbBookComment = bookCommentService.findById(commentsBefore.get(0).getId());
        assertThat(dbBookComment).isPresent().get().usingRecursiveComparison().isEqualTo(savedBookComment);

        EntityNotFoundException thrownBook = assertThrows(EntityNotFoundException.class, () -> {
            bookCommentService.update("0", modifiedComment);
        });
        assertEquals("Book comment with id 0 not found", thrownBook.getMessage());
    }

    @DisplayName("должен удалять все комментарии книги")
    @Test
    void shouldDeleteBookComment() {
        var countCommentsBefore = bookCommentService.findByBookTitle(TITLE_OF_BOOK_WITH_COMMENTS).size();
        assertThat(countCommentsBefore).isGreaterThan(0);

        bookCommentService.deleteByBookTitle(TITLE_OF_BOOK_WITH_COMMENTS);
        var countCommentsAfter = bookCommentService.findByBookTitle(TITLE_OF_BOOK_WITH_COMMENTS).size();
        assertThat(countCommentsAfter).isEqualTo(0);

        var deletedBookComment = bookCommentService.findByBookTitle(TITLE_OF_BOOK_WITH_COMMENTS);
        assertThat(deletedBookComment).isEmpty();
    }
}
