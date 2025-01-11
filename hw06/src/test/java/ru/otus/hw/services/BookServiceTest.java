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
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.JpaAuthorRepository;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaGenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Сервис для работы с книгами ")
@DataJpaTest
@Import({AuthorConverter.class, GenreConverter.class, BookConverter.class, BookServiceImpl.class,
        JpaAuthorRepository.class, JpaBookRepository.class, JpaGenreRepository.class})
public class BookServiceTest {

    private static final long BOOK_ID = 2L;
    private static final long NEW_AUTHOR_ID = 2L;
    private static final Set<Long> NEW_GENRE_IDS = Set.of(1L, 3L, 4L);
    private static final long EXPECTED_NUMBER_OF_BOOKS = 3;

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
        var bookDto = bookToBookDto(dbBook);

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
        var bookDto = bookToBookDto(dbBook);

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

    @DisplayName("должен загружать книгу по id")
    @ParameterizedTest
    @ValueSource(longs = {1, 3, 5, 100000000})
    void shouldReturnCorrectBookById(long id) {
        var actualBook = bookService.findById(id);
        var expectedBook = Optional.ofNullable(em.find(Book.class, id));
        assertTrue(actualBook.isPresent() && expectedBook.isPresent()
                || actualBook.isEmpty() && expectedBook.isEmpty());
        if (actualBook.isPresent()) {
            var expectedBookDto = bookToBookDto(expectedBook.get());
            assertThat(actualBook).get().usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBookDto);
        }
    }

    @DisplayName("должен загружать список всех книг")
    @Test
    void shouldReturnCorrectBooksList() {
        var actualBooks = bookService.findAll();
        var expectedBooksDto = LongStream.range(1, EXPECTED_NUMBER_OF_BOOKS + 1).boxed()
                .map(id -> bookToBookDto(em.find(Book.class, id))).toList();

        assertThat(actualBooks).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBooksDto);
    }

    @DisplayName("должен удалять книгу по id ")
    @Test
    void shouldDeleteBook() {
        var currenBook = em.find(Book.class, BOOK_ID);
        assertThat(currenBook).isNotNull();
        em.detach(currenBook);

        bookService.deleteById(BOOK_ID);
        var deletedBook = em.find(Book.class, BOOK_ID);
        assertThat(deletedBook).isNull();
    }

    // Чтоб не использовать функцию, которую не тестируем
    private BookDto bookToBookDto (Book book) {
        var authorDto = new AuthorDto(book.getAuthor().getId(), book.getAuthor().getFullName());
        List<GenreDto> genresDto = book.getGenres().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName())).toList();
        return new BookDto(book.getId(),book. getTitle(), authorDto, genresDto);
    }

}
