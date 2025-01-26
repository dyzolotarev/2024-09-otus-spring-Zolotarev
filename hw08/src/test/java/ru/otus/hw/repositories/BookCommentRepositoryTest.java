package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.AFTER_METHOD;

@DisplayName("Репозиторий для работы с комментариями ")
@DataMongoTest
public class BookCommentRepositoryTest {

    private static final String EXISTING_BOOK_TITLE = "BookTitle_1";

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private BookCommentRepository repositoryMongo;

    @Autowired
    private BookRepository bookRepository;

    private Book existingBook;

    @BeforeEach
    void setUp() {
        var bookQuery = Query.query(Criteria.where("title").is(EXISTING_BOOK_TITLE));
        existingBook = mongoOperations.findOne(bookQuery, Book.class);
    }

    @DisplayName("должен загружать список всех комментариев книги")
    @Test
    void shouldReturnCorrectBookCommentsList() {
        var actualBookComments = repositoryMongo.findByBookId(existingBook.getId());

        var expectedBookCommentQuery = Query.query(Criteria.where("book._id").is(existingBook.getId()));
        var expectedBookComments = mongoOperations.find(expectedBookCommentQuery, BookComment.class);

        assertThat(actualBookComments).usingRecursiveComparison().isEqualTo(expectedBookComments);
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewBookComment() {
        var expectedBookComment = new BookComment(existingBook, "NewComment");
        var returnedBookComment = repositoryMongo.save(expectedBookComment);
        assertThat(returnedBookComment).isNotNull()
                .matches(book -> !book.getId().isEmpty())
                .usingRecursiveComparison().isEqualTo(expectedBookComment);

        var foundBookCommentQuery = Query.query(Criteria.where("id").is(returnedBookComment.getId()));
        var foundBookComment = Optional.ofNullable(mongoOperations.findOne(foundBookCommentQuery, BookComment.class));
        assertThat(foundBookComment).isPresent().get().usingRecursiveComparison().isEqualTo(returnedBookComment);
    }

    @DirtiesContext(methodMode = AFTER_METHOD)
    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedBookComment() {
        var currentBookCommentQuery = Query.query(Criteria.where("book._id").is(existingBook.getId()));
        var currenBookComment = mongoOperations.findOne(currentBookCommentQuery, BookComment.class);

        var expectedBookComment = new BookComment(currenBookComment.getId(), currenBookComment.getBook()
                , "Modified " + currenBookComment.getComment());
        var returnedBookComment = repositoryMongo.save(expectedBookComment);
        assertThat(returnedBookComment).isNotNull()
                .matches(bookComment -> !bookComment.getId().isEmpty())
                .usingRecursiveComparison().isEqualTo(expectedBookComment);

        var modifiedBookCommentQuery = Query.query(Criteria.where("id").is(returnedBookComment.getId()));
        var modifiedBookComment = Optional.ofNullable(
                mongoOperations.findOne(modifiedBookCommentQuery, BookComment.class));
        assertThat(modifiedBookComment).isPresent().get().usingRecursiveComparison().isEqualTo(returnedBookComment);
    }

    @DirtiesContext(methodMode = AFTER_METHOD)
    @DisplayName("должен удалять комментарии книги")
    @Test
    void shouldDeleteBookComment() {
        var currentBookCommentQuery = Query.query(Criteria.where("book._id").is(existingBook.getId()));
        var currentBookComment = mongoOperations.find(currentBookCommentQuery, BookComment.class);
        assertThat(currentBookComment).isNotEmpty();

        repositoryMongo.deleteByBookId(existingBook.getId());
        var deletedBookComment = mongoOperations.find(currentBookCommentQuery, BookComment.class);
        assertThat(deletedBookComment).isEmpty();
    }

}
