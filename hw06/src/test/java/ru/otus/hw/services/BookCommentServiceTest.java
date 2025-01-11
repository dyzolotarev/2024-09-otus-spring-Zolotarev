package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.converters.BookCommentConverter;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.JpaBookCommentRepository;
import ru.otus.hw.repositories.JpaBookRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Сервис для работы с комментариями к книгам ")
@DataJpaTest
@Import({BookCommentConverter.class, BookConverter.class, AuthorConverter.class, GenreConverter.class,
        BookCommentServiceImpl.class, JpaBookCommentRepository.class, JpaBookRepository.class})
public class BookCommentServiceTest {

    private static final long COMMENTED_BOOK_ID = 2L;
    private static final long COMMENT_ID = 3L;
    private static final long LAST_COMMENT_ID_FOR_COMMENTED_BOOK = 4;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookCommentService bookCommentService;

    @DisplayName("должен сохранять новый комментарий, обрабатывать неверные параметры")
    @Test
    void shouldSaveNewBookComment() {
        String newComment = "New Comment";

        var savedBookCommentDto = bookCommentService.insert(COMMENTED_BOOK_ID, newComment);

        var dbBookComment = em.find(BookComment.class, savedBookCommentDto.getId());
        assertThat(dbBookComment).isNotNull();

        var bookCommentDto = bookCommentToDto(dbBookComment);
        assertThat(bookCommentDto).usingRecursiveComparison()
                .ignoringExpectedNullFields().isEqualTo(savedBookCommentDto);

        EntityNotFoundException thrownBook = assertThrows(EntityNotFoundException.class, () -> {
            bookCommentService.insert(10000000000L, newComment);
        });
        assertEquals("Book with id 10000000000 not found", thrownBook.getMessage());
    }

    @DisplayName("должен обновлять комментарий, обрабатывать неверные параметры")
    @Test
    void shouldUpdateNewBookComment() {

        String modifiedComment = "Modified book comment";

        var savedBookComment = bookCommentService.update(COMMENT_ID, modifiedComment);

        var dbBookComment = em.find(BookComment.class, COMMENT_ID);
        assertThat(dbBookComment).isNotNull();

        var bookCommentDto = bookCommentToDto(dbBookComment);
        assertThat(bookCommentDto).usingRecursiveComparison()
                .ignoringExpectedNullFields().isEqualTo(savedBookComment);

        EntityNotFoundException thrownBook = assertThrows(EntityNotFoundException.class, () -> {
            bookCommentService.update(2000000000000L, modifiedComment);
        });
        assertEquals("Book comment with id 2000000000000 not found", thrownBook.getMessage());
    }

    @DisplayName("должен загружать комментарий по id")
    @ParameterizedTest
    @ValueSource(longs = {1, 3, 5, 100000000})
    void shouldReturnCorrectCommentById(long id) {
        var actualBookComment = bookCommentService.findById(id);
        var expectedBookComment = Optional.ofNullable(em.find(BookComment.class, id));
        assertTrue(actualBookComment.isPresent() && expectedBookComment.isPresent()
                || actualBookComment.isEmpty() && expectedBookComment.isEmpty());

        if (actualBookComment.isPresent()) {
            var expectedBookCommentDto = bookCommentToDto(expectedBookComment.get());
            assertThat(actualBookComment).get().usingRecursiveComparison()
                    .ignoringExpectedNullFields().isEqualTo(expectedBookCommentDto);
        }
    }

    @DisplayName("должен загружать список всех комментариев книги")
    @Test
    void shouldReturnCorrectBookCommentsList() {
        var actualBookComments = bookCommentService.findForBook(COMMENTED_BOOK_ID);
        var expectedBookComments = LongStream.range(3, LAST_COMMENT_ID_FOR_COMMENTED_BOOK + 1).boxed()
                .map(id -> bookCommentToDto(em.find(BookComment.class, id))).toList();
        assertThat(actualBookComments).usingRecursiveComparison()
                .ignoringExpectedNullFields().isEqualTo(expectedBookComments);
    }

    @DisplayName("должен удалять комментарий по id ")
    @Test
    void shouldDeleteBookComment() {
        var currenBookComment = em.find(BookComment.class, COMMENT_ID);;
        assertThat(currenBookComment).isNotNull();
        em.detach(currenBookComment);

        bookCommentService.deleteById(currenBookComment.getId());
        var deletedBookComment = em.find(BookComment.class, currenBookComment.getId());
        assertThat(deletedBookComment).isNull();
    }

    // Чтоб не использовать функцию, которую не тестируем
    private BookCommentDto bookCommentToDto(BookComment bookComment) {
        var authorDto = new AuthorDto(
                bookComment.getBook().getAuthor().getId(),
                bookComment.getBook().getAuthor().getFullName());
        List<GenreDto> genresDto = bookComment.getBook().getGenres().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName())).toList();
        var bookDto = new BookDto(bookComment.getBook().getId()
                , bookComment.getBook().getTitle(), authorDto, genresDto);

        return new BookCommentDto(bookComment.getId(), bookDto, bookComment.getComment());
    }

}
