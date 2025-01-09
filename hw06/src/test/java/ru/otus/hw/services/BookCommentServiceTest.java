package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Пока не хватило запала реализовать все операции, но остальные функции из сервисов
// просто вызывают соответствующие методы репозитория, которые уже покрыты тестами и конвертят в DTO.
// Непонятно насколько осмысленно повторять это в тестах сервиса, логичнее отдельно конвертацию в DTO тогда тестить
@DisplayName("Сервис для работы с комментариями к книгам ")
@DataJpaTest
@ComponentScan(basePackages = "ru.otus.hw")
public class BookCommentServiceTest {

    private static final long COMMENTED_BOOK_ID = 2L;
    private static final long COMMENT_ID = 3L;

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

        // Руками в DTO, чтоб не использовать функцию, которую не тестируем
        var authorDto = new AuthorDto(
                dbBookComment.getBook().getAuthor().getId(),
                dbBookComment.getBook().getAuthor().getFullName());

        List<GenreDto> genresDto = dbBookComment.getBook().getGenres().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName())).toList();

        var bookDto = new BookDto(dbBookComment.getBook().getId()
                , dbBookComment.getBook().getTitle(), authorDto, genresDto);

        var bookCommentDto = new BookCommentDto(dbBookComment.getId(), bookDto, dbBookComment.getComment());

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

        // Руками в DTO, чтоб не использовать функцию, которую не тестируем
        var authorDto = new AuthorDto(
                dbBookComment.getBook().getAuthor().getId(),
                dbBookComment.getBook().getAuthor().getFullName());

        List<GenreDto> genresDto = dbBookComment.getBook().getGenres().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName())).toList();

        var bookDto = new BookDto(dbBookComment.getBook().getId()
                , dbBookComment.getBook().getTitle(), authorDto, genresDto);

        var bookCommentDto = new BookCommentDto(dbBookComment.getId(), bookDto, dbBookComment.getComment());

        assertThat(bookCommentDto).usingRecursiveComparison()
                .ignoringExpectedNullFields().isEqualTo(savedBookComment);

        EntityNotFoundException thrownBook = assertThrows(EntityNotFoundException.class, () -> {
            bookCommentService.update(2000000000000L, modifiedComment);
        });
        assertEquals("Book comment with id 2000000000000 not found", thrownBook.getMessage());
    }

}
