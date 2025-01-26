package ru.otus.hw.services;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Сервис для работы с книгами ")
@DataMongoTest
@Import({AuthorConverter.class, GenreConverter.class, BookConverter.class, BookServiceImpl.class,})
@TestMethodOrder(OrderAnnotation.class)
@Transactional(propagation = Propagation.NEVER)
public class BookServiceTest {

    private static final long EXPECTED_NUMBER_OF_BOOKS = 3;
    private static final List<String> EXISTING_BOOK_LIST = List.of ("BookTitle_1", "BookTitle_2", "BookTitle_3");
    private static final String EXISTING_AUTHOR_NAME = "Author_2";
    private static final Set<String> EXISTING_GENRE_NAMES = Set.of("Genre_4", "Genre_5", "Genre_6");

    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать книгу по названию")
    @Order(1)
    @Test
    void shouldReturnCorrectBookById() {
        String existingBookTitle = EXISTING_BOOK_LIST.get(1);
        var actualBook = bookService.findByTitle(existingBookTitle);
        assertFalse(actualBook.isEmpty());
        var expectedBook = new BookDto(null, "BookTitle_2", new AuthorDto(null, "Author_2")
                , List.of(new GenreDto(null, "Genre_3"), new GenreDto(null, "Genre_4")));
        assertThat(actualBook).get().usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBook);
    }

    @DisplayName("должен загружать список всех книг")
    @Order(1)
    @Test
    void shouldReturnCorrectBooksList() {
        var actualBooks = bookService.findAll();
        assertThat(actualBooks.size()).isEqualTo(EXPECTED_NUMBER_OF_BOOKS);

        var expectedBooks = EXISTING_BOOK_LIST.stream()
                .map(title -> bookService.findByTitle(title).get()).toList();
        assertThat(expectedBooks).usingRecursiveComparison().isEqualTo(actualBooks);
    }

    @DisplayName("должен сохранять новую книгу, обрабатывать пустые и неверные параметры")
    @Order(2)
    @Test
    void shouldSaveNewBook() {
        String newBookTitle = "New Book Title " + ObjectId.get(); // for unique;

        var countBooksBefore = bookService.findAll().size();
        var savedBook = bookService.insert(newBookTitle, EXISTING_AUTHOR_NAME, EXISTING_GENRE_NAMES);

        var countBooksAfter = bookService.findAll().size();
        assertThat(countBooksAfter).isEqualTo(countBooksBefore + 1);

        var dbBook =  bookService.findByTitle(savedBook.getTitle());
        assertThat(dbBook).isPresent().get().usingRecursiveComparison().isEqualTo(savedBook);

        IllegalArgumentException thrownGenre = assertThrows(IllegalArgumentException.class, () -> {
            bookService.insert(newBookTitle, EXISTING_AUTHOR_NAME, null);
        });
        assertEquals("Genres must not be null", thrownGenre.getMessage());

        EntityNotFoundException thrownAuthor = assertThrows(EntityNotFoundException.class, () -> {
            bookService.insert(newBookTitle, "0", EXISTING_GENRE_NAMES);
        });
        assertEquals("Author with full name 0 not found", thrownAuthor.getMessage());

        EntityNotFoundException thrownGenre2 = assertThrows(EntityNotFoundException.class, () -> {
            bookService.insert(newBookTitle, EXISTING_AUTHOR_NAME, Set.of("0"));
        });
        assertEquals("One or all genres with names [0] not found", thrownGenre2.getMessage());
    }

    @DisplayName("должен обновлять существующую книгу, обрабатывать пустые и неверные параметры")
    @Order(2)
    @Test
    void shouldUpdateBook() {
        String existingBookTitle = EXISTING_BOOK_LIST.get(0);
        String modifiedBookTitle = "Modified book title " + ObjectId.get(); // for unique
        var countBooksBefore = bookService.findAll().size();
        var savedBook = bookService.update(existingBookTitle, modifiedBookTitle, EXISTING_AUTHOR_NAME
                , EXISTING_GENRE_NAMES);
        var countBooksAfter = bookService.findAll().size();
        assertThat(countBooksAfter).isEqualTo(countBooksBefore);

        var dbBook = bookService.findByTitle(modifiedBookTitle);
        assertThat(dbBook).isPresent().get().usingRecursiveComparison().isEqualTo(savedBook);

        IllegalArgumentException thrownGenre = assertThrows(IllegalArgumentException.class, () -> {
            bookService.update(modifiedBookTitle, existingBookTitle, EXISTING_AUTHOR_NAME, null);
        });
        assertEquals("Genres must not be null", thrownGenre.getMessage());

        EntityNotFoundException thrownAuthor = assertThrows(EntityNotFoundException.class, () -> {
            bookService.update(modifiedBookTitle, existingBookTitle, "0", EXISTING_GENRE_NAMES);
        });
        assertEquals("Author with full name 0 not found", thrownAuthor.getMessage());

        EntityNotFoundException thrownGenre2 = assertThrows(EntityNotFoundException.class, () -> {
            bookService.update(modifiedBookTitle, existingBookTitle, EXISTING_AUTHOR_NAME, Set.of("0"));
        });
        assertEquals("One or all genres with names [0] not found", thrownGenre2.getMessage());
    }

    @DisplayName("должен удалять книгу по названию")
    @Order(2)
    @Test
    void shouldDeleteBook() {
        String existingBookTitle = EXISTING_BOOK_LIST.get(1);
        var countBooksBefore = bookService.findAll().size();
        bookService.deleteByTitle(existingBookTitle);
        var countBooksAfter = bookService.findAll().size();
        assertThat(countBooksAfter).isEqualTo(countBooksBefore - 1);
        var dbBook = bookService.findByTitle(existingBookTitle);
        assertThat(dbBook).isEmpty();
    }
}
