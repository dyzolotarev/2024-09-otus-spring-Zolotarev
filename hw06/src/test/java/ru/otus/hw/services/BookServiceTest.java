package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Пока не хватило запала реализовать все операции, но остальные функции из сервисов
// просто вызывают соответствующие методы репозитория, которые уже покрыты тестами и конвертят в DTO.
// Непонятно насколько осмысленно повторять это в тестах сервиса, логичнее отдельно конвертацию в DTO тогда тестить
@DisplayName("Сервис для работы с книгами ")
@DataJpaTest
@ComponentScan(basePackages = "ru.otus.hw")
public class BookServiceTest {

    private static final long BOOK_ID = 2L;
    private static final long NEW_AUTHOR_ID = 2L;
    private static final Set<Long> NEW_GENRE_IDS = Set.of(1L, 3L, 4L);

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookService bookService;

    @DisplayName("должен сохранять новую книгу, обрабатывать пустые и неверные параметры")
    @Test
    void shouldSaveNewBook() {
        String newBookTitle = "New Book Title";

        var savedBook = bookService.insert(newBookTitle, NEW_AUTHOR_ID, NEW_GENRE_IDS);
        var dbBook = em.find(Book.class, savedBook.getId());
        assertThat(dbBook).isNotNull();

        // Руками в DTO, чтоб не использовать функцию, которую не тестируем
        var authorDto = new AuthorDto(dbBook.getAuthor().getId(), dbBook.getAuthor().getFullName());
        List<GenreDto> genresDto = dbBook.getGenres().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName())).toList();
        var bookDto = new BookDto(dbBook.getId(),dbBook. getTitle(), authorDto, genresDto);

        assertThat(bookDto).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(savedBook);

        IllegalArgumentException thrownGenre = assertThrows(IllegalArgumentException.class, () -> {
            bookService.insert(newBookTitle, NEW_AUTHOR_ID, null);
        });
        assertEquals("Genres ids must not be null", thrownGenre.getMessage());

        EntityNotFoundException thrownAuthor = assertThrows(EntityNotFoundException.class, () -> {
            bookService.insert(newBookTitle, 100000000L, NEW_GENRE_IDS);
        });
        assertEquals("Author with id 100000000 not found", thrownAuthor.getMessage());

        EntityNotFoundException thrownGenre2 = assertThrows(EntityNotFoundException.class, () -> {
            bookService.insert(newBookTitle, NEW_AUTHOR_ID, Set.of(100000000L));
        });
        assertEquals("One or all genres with ids [100000000] not found", thrownGenre2.getMessage());
    }

    @DisplayName("должен обновлять существующую книгу, обрабатывать пустые и неверные параметры")
    @Test
    void shouldUpdateBook() {
        var currenBook = em.find(Book.class, BOOK_ID);
        em.detach(currenBook);

        String modifiedBookTitle = "Modified " + currenBook.getTitle();
        var savedBook = bookService.update(BOOK_ID, modifiedBookTitle, NEW_AUTHOR_ID, NEW_GENRE_IDS);
        var dbBook = em.find(Book.class, BOOK_ID);
        assertThat(dbBook).isNotNull();

        // Руками в DTO, чтоб не использовать функцию, которую не тестируем
        var authorDto = new AuthorDto(dbBook.getAuthor().getId(), dbBook.getAuthor().getFullName());
        List<GenreDto> genresDto = dbBook.getGenres().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName())).toList();
        var bookDto = new BookDto(dbBook.getId(),dbBook. getTitle(), authorDto, genresDto);

        assertThat(bookDto).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(savedBook);

        IllegalArgumentException thrownGenre = assertThrows(IllegalArgumentException.class, () -> {
            bookService.update(BOOK_ID, modifiedBookTitle, NEW_AUTHOR_ID, null);
        });
        assertEquals("Genres ids must not be null", thrownGenre.getMessage());

        EntityNotFoundException thrownAuthor = assertThrows(EntityNotFoundException.class, () -> {
            bookService.update(BOOK_ID, modifiedBookTitle, 100000000L, NEW_GENRE_IDS);
        });
        assertEquals("Author with id 100000000 not found", thrownAuthor.getMessage());

        EntityNotFoundException thrownGenre2 = assertThrows(EntityNotFoundException.class, () -> {
            bookService.update(BOOK_ID, modifiedBookTitle, NEW_AUTHOR_ID, Set.of(100000000L));
        });
        assertEquals("One or all genres with ids [100000000] not found", thrownGenre2.getMessage());
    }
}
