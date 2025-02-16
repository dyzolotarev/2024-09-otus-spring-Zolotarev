package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Репозиторий для работы с книгами ")
@DataJpaTest
class BookRepositoryTest {

    private static final long EXPECTED_NUMBER_OF_BOOKS = 3;
    private static final long BOOK_ID = 1L;
    private static final long NEW_AUTHOR_ID = 3L;
    private static final List<Long> NEW_GENRE_IDS = List.of(2L, 3L, 4L);

    @Autowired
    private BookRepository repositoryJpa;

    @Autowired
    private TestEntityManager em;

    @DisplayName("должен загружать книгу по id")
    @ParameterizedTest
    @ValueSource(longs = {1, 3, 5, 100000000})
    void shouldReturnCorrectBookById(long id) {
        var actualBook = repositoryJpa.findById(id);
        var expectedBook = Optional.ofNullable(em.find(Book.class, id));
        assertTrue(actualBook.isPresent() && expectedBook.isPresent()
                || actualBook.isEmpty() && expectedBook.isEmpty());
        if (actualBook.isPresent()) {
            assertThat(actualBook).get().isEqualTo(expectedBook.get());
        }
    }

    @DisplayName("должен загружать список всех книг")
    @Test
    void shouldReturnCorrectBooksList() {
        var actualBooks = repositoryJpa.findAll();
        var expectedBooks = LongStream.range(1, EXPECTED_NUMBER_OF_BOOKS + 1).boxed()
                .map(id -> em.find(Book.class, id)).toList();
        assertThat(actualBooks).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBooks);
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldSaveNewBook() {
        var author = em.find(Author.class, NEW_AUTHOR_ID);
        var listGenres = NEW_GENRE_IDS.stream().map(i -> em.find(Genre.class,i)).toList();
        var expectedBook = new Book(0, "New test book 111222", author, listGenres);
        var returnedBook = repositoryJpa.save(expectedBook);
        assertThat(returnedBook).isNotNull()
                .matches(book -> book.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBook);
        var foundBook = Optional.ofNullable(em.find(Book.class, returnedBook.getId()));
        assertThat(foundBook).isPresent().get()
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(returnedBook);
    }

    @DisplayName("должен сохранять измененную книгу")
    @Test
    void shouldSaveUpdatedBook() {
        var currenBook = em.find(Book.class, BOOK_ID);
        em.detach(currenBook);
        var newAuthor = em.find(Author.class, NEW_AUTHOR_ID);
        var newListGenres = NEW_GENRE_IDS.stream().map(i -> em.find(Genre.class,i)).toList();

        var expectedBook = new Book(currenBook.getId(),
                "Modified " + currenBook.getTitle(), newAuthor, newListGenres);
        var returnedBook = repositoryJpa.save(expectedBook);

        assertThat(returnedBook).isNotNull()
                .matches(book -> book.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBook);

        var modifiedBook = Optional.ofNullable(em.find(Book.class, returnedBook.getId()));
        assertThat(modifiedBook).isPresent().get()
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(returnedBook);
    }

    @DisplayName("должен удалять книгу по id ")
    @Test
    void shouldDeleteBook() {
        var currenBook = em.find(Book.class, BOOK_ID);
        assertThat(currenBook).isNotNull();
        em.detach(currenBook);

        repositoryJpa.deleteById(BOOK_ID);
        var deletedBook = em.find(Book.class, BOOK_ID);
        assertThat(deletedBook).isNull();
    }
}