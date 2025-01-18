package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;

import java.util.Optional;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Репозиторий на основе Jpa для работы с комментариями ")
@DataJpaTest
@Import({JpaBookCommentRepository.class, JpaBookRepository.class, JpaGenreRepository.class})
public class JpaBookCommentRepositoryTest {

    private static final long COMMENTED_BOOK_ID = 1L;
    private static final long COMMENT_ID = 1L;
    private static final long LAST_COMMENT_ID_FOR_COMMENTED_BOOK = 2;

    @Autowired
    private JpaBookCommentRepository repositoryJpa;

    @Autowired
    private TestEntityManager em;

    @DisplayName("должен загружать комментарий по id")
    @ParameterizedTest
    @ValueSource(longs = {1, 3, 5, 100000000})
    void shouldReturnCorrectCommentById(long id) {
        var actualBookComment = repositoryJpa.findById(id);
        var expectedBookComment = Optional.ofNullable(em.find(BookComment.class, id));
        assertTrue(actualBookComment.isPresent() && expectedBookComment.isPresent()
                || actualBookComment.isEmpty() && expectedBookComment.isEmpty());
        if (actualBookComment.isPresent()) {
            assertThat(actualBookComment).get().isEqualTo(expectedBookComment.get());
        }
    }

    @DisplayName("должен загружать список всех комментариев книги")
    @Test
    void shouldReturnCorrectBookCommentsList() {
        var actualBookComments = repositoryJpa.findForBook(COMMENTED_BOOK_ID);
        var expectedBookComments = LongStream.range(1, LAST_COMMENT_ID_FOR_COMMENTED_BOOK + 1).boxed()
                .map(id -> em.find(BookComment.class, id)).toList();
        assertThat(actualBookComments).usingRecursiveComparison()
                .ignoringExpectedNullFields().isEqualTo(expectedBookComments);
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewBookComment() {
        var commentedBook = em.find(Book.class, COMMENTED_BOOK_ID);
        var expectedBookComment = new BookComment(0, commentedBook, "NewComment");
        var returnedBookComment = repositoryJpa.save(expectedBookComment);
        assertThat(returnedBookComment).isNotNull()
                .matches(book -> book.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBookComment);
        var foundBookComment = Optional.ofNullable(em.find(BookComment.class, returnedBookComment.getId()));
        assertThat(foundBookComment).isPresent().get()
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(returnedBookComment);
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedBookComment() {
        var currenBookComment = em.find(BookComment.class, COMMENT_ID);
        em.detach(currenBookComment);

        var expectedBookComment = new BookComment(currenBookComment.getId(), currenBookComment.getBook()
                , "Modified " + currenBookComment.getComment());
        var returnedBookComment = repositoryJpa.save(expectedBookComment);
        assertThat(returnedBookComment).isNotNull()
                .matches(bookComment -> bookComment.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBookComment);

        var modifiedBookComment = Optional.ofNullable(em.find(BookComment.class, returnedBookComment.getId()));
        assertThat(modifiedBookComment).isPresent().get()
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(returnedBookComment);
    }

    @DisplayName("должен удалять комментарий по id ")
    @Test
    void shouldDeleteBookComment() {
        var currenBookComment = em.find(BookComment.class, COMMENT_ID);;
        assertThat(currenBookComment).isNotNull();
        em.detach(currenBookComment);

        repositoryJpa.deleteById(currenBookComment.getId());
        var deletedBookComment = em.find(BookComment.class, currenBookComment.getId());
        assertThat(deletedBookComment).isNull();
    }

}
