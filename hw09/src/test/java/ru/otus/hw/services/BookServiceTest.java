package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Сервис для работы с книгами ")
@DataJpaTest
@Import({AuthorConverter.class, GenreConverter.class, BookConverter.class, BookServiceImpl.class})
@TestMethodOrder(OrderAnnotation.class)
@Transactional(propagation = Propagation.NEVER)
public class BookServiceTest {

    private static final long BOOK_ID = 2L;
    private static final long NEW_AUTHOR_ID = 2L;
    private static final Set<Long> NEW_GENRE_IDS = Set.of(1L, 3L, 4L);
    private static final long EXPECTED_NUMBER_OF_BOOKS = 3;

    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать книгу по id")
    @Order(1)
    @Test
    void shouldReturnCorrectBookById() {
        var actualBook = bookService.findById(BOOK_ID);
        assertFalse(actualBook.isEmpty());
        var expectedBook = new BookDto(BOOK_ID, "BookTitle_2", new AuthorDto(2, "Author_2")
                , List.of(new GenreDto(3, "Genre_3"), new GenreDto(4, "Genre_4")));
        assertThat(actualBook).get().usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBook);
    }

    @DisplayName("должен загружать список всех книг")
    @Order(1)
    @Test
    void shouldReturnCorrectBooksList() {
        var actualBooks = bookService.findAll();
        assertThat(actualBooks.size()).isEqualTo(EXPECTED_NUMBER_OF_BOOKS);

        var expectedBooks = LongStream.range(1, EXPECTED_NUMBER_OF_BOOKS + 1).boxed()
                .map(id -> bookService.findById(id).get()).toList();
        assertThat(actualBooks).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBooks);
    }

    @DisplayName("должен сохранять новую книгу, обрабатывать пустые и неверные параметры")
    @Order(2)
    @Test
    void shouldSaveNewBook() {
        String newBookTitle = "New Book Title";

        var countBooksBefore = bookService.findAll().size();
        var savedBook = bookService.insert(newBookTitle, NEW_AUTHOR_ID, NEW_GENRE_IDS);
        var countBooksAfter = bookService.findAll().size();
        assertThat(countBooksAfter).isEqualTo(countBooksBefore + 1);

        var dbBook =  bookService.findById(savedBook.getId());
        assertThat(dbBook).isPresent().get()
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(savedBook);

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
    @Order(2)
    @Test
    void shouldUpdateBook() {
        String modifiedBookTitle = "Modified book title";
        var countBooksBefore = bookService.findAll().size();
        var savedBook = bookService.update(BOOK_ID, modifiedBookTitle, NEW_AUTHOR_ID, NEW_GENRE_IDS);
        var countBooksAfter = bookService.findAll().size();
        assertThat(countBooksAfter).isEqualTo(countBooksBefore);

        var dbBook = bookService.findById(BOOK_ID);
        assertThat(dbBook).isPresent().get()
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(savedBook);

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

    @DisplayName("должен удалять книгу по id ")
    @Order(2)
    @Test
    void shouldDeleteBook() {
        var countBooksBefore = bookService.findAll().size();
        bookService.deleteById(BOOK_ID);
        var countBooksAfter = bookService.findAll().size();
        assertThat(countBooksAfter).isEqualTo(countBooksBefore - 1);
        var dbBook = bookService.findById(BOOK_ID);
        assertThat(dbBook).isEmpty();
    }
}
