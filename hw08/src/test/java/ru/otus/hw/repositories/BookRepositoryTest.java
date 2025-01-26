package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.AFTER_METHOD;

@DisplayName("Репозиторий для работы с книгами ")
@DataMongoTest
class BookRepositoryTest {

    private static final long EXPECTED_NUMBER_OF_BOOKS = 3;
    private static final String EXISTING_BOOK_TITLE = "BookTitle_3";
    private static final String EXISTING_AUTHOR_NAME = "Author_2";
    private static final List<String> EXISTING_GENRE_NAMES = List.of("Genre_4", "Genre_5", "Genre_6");

    @Autowired
    private BookRepository repositoryMongo;

    @Autowired
    private MongoOperations mongoOperations;

    @DisplayName("должен загружать книгу по названию")
    @ParameterizedTest
    @ValueSource(strings = {"BookTitle_1", "BookTitle_3", "BookTitle_100000000"})
    void shouldReturnCorrectBookById(String title) {
        var actualBook = repositoryMongo.findByTitle(title);
        Query query = new Query(Criteria.where("title").is(title));
        var expectedBook = Optional.ofNullable(mongoOperations.findOne(query, Book.class));

        assertTrue(actualBook.isPresent() && expectedBook.isPresent()
                || actualBook.isEmpty() && expectedBook.isEmpty());
        if (actualBook.isPresent()) {
            assertThat(actualBook).get().usingRecursiveComparison().isEqualTo(expectedBook.get());
        }
    }

    @DisplayName("должен загружать список всех книг")
    @Test
    void shouldReturnCorrectBooksList() {
        var actualBooks = repositoryMongo.findAll();
        assertThat(actualBooks.size()).isEqualTo(EXPECTED_NUMBER_OF_BOOKS);
        var expectedBooks = mongoOperations.findAll(Book.class);
        assertThat(actualBooks).usingRecursiveComparison().isEqualTo(expectedBooks);
    }

    @DirtiesContext(methodMode = AFTER_METHOD)
    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldSaveNewBook() {
        var authorQuery = Query.query(Criteria.where("fullName").is(EXISTING_AUTHOR_NAME));
        var author = mongoOperations.findOne(authorQuery, Author.class);
        var genresQuery = Query.query(Criteria.where("name").in(EXISTING_GENRE_NAMES));
        var genres = mongoOperations.find(genresQuery, Genre.class);

        var expectedBook = new Book("New test book 111222", author, genres);
        var returnedBook = repositoryMongo.save(expectedBook);
        assertThat(returnedBook).isNotNull()
                .matches(book -> !book.getId().isEmpty())
                .usingRecursiveComparison().isEqualTo(expectedBook);

        Query query = new Query(Criteria.where("id").is(returnedBook.getId()));
        var foundBook = Optional.ofNullable(mongoOperations.findOne(query, Book.class));
        assertThat(foundBook).isPresent().get()
                .usingRecursiveComparison().isEqualTo(returnedBook);
    }

    @DirtiesContext(methodMode = AFTER_METHOD)
    @DisplayName("должен сохранять измененную книгу")
    @Test
    void shouldSaveUpdatedBook() {
        var currentBookQuery = Query.query(Criteria.where("title").is(EXISTING_BOOK_TITLE));
        var currenBook = mongoOperations.findOne(currentBookQuery, Book.class);

        var newAuthorQuery = Query.query(Criteria.where("fullName").is(EXISTING_AUTHOR_NAME));
        var newAuthor = mongoOperations.findOne(newAuthorQuery, Author.class);

        var newGenresQuery = Query.query(Criteria.where("name").in(EXISTING_GENRE_NAMES));
        var newListGenres = mongoOperations.find(newGenresQuery, Genre.class);

        var expectedBook = new Book(currenBook.getId(),
                "Modified " + currenBook.getTitle(), newAuthor, newListGenres);
        var returnedBook = repositoryMongo.save(expectedBook);

        assertThat(returnedBook).isNotNull()
                .matches(book -> !book.getId().isEmpty())
                .usingRecursiveComparison().isEqualTo(expectedBook);

        var modifiedBookQuery = Query.query(Criteria.where("id").is(returnedBook.getId()));
        var modifiedBook = Optional.ofNullable(mongoOperations.findOne(modifiedBookQuery, Book.class));
        assertThat(modifiedBook).isPresent().get()
                .usingRecursiveComparison().isEqualTo(returnedBook);
    }

    @DirtiesContext(methodMode = AFTER_METHOD)
    @DisplayName("должен удалять книгу по названию")
    @Test
    void shouldDeleteBook() {
        Query query = new Query(Criteria.where("title").is(EXISTING_BOOK_TITLE));
        var currenBook = Optional.ofNullable(mongoOperations.findOne(query, Book.class));
        assertThat(currenBook).isNotNull();

        repositoryMongo.deleteByTitle(EXISTING_BOOK_TITLE);
        var deletedBook = mongoOperations.findOne(query, Book.class);
        assertThat(deletedBook).isNull();
    }
}