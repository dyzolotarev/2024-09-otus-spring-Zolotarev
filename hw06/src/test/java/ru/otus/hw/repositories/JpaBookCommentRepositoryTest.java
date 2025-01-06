package ru.otus.hw.repositories;

import jakarta.persistence.TypedQuery;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Репозиторий на основе Jpa для работы с комментариями ")
@DataJpaTest
@Import({JpaBookCommentRepository.class, JpaBookRepository.class, JpaGenreRepository.class})
public class JpaBookCommentRepositoryTest {

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
        // произвольная книга у которой есть комментарии
        var commentedBooks = em.getEntityManager()
                .createQuery("select bc.book.id from BookComment bc", Long.class).getResultList();
        var actualBookComments = repositoryJpa.findForBook(commentedBooks.get(0));

        TypedQuery<BookComment> expectedBookCommentsQuery = em.getEntityManager()
                .createQuery("select bc from BookComment bc where bc.book.id = :book_id", BookComment.class);
        expectedBookCommentsQuery.setParameter("book_id", commentedBooks.get(0)).getResultList();
        var expectedBookComments = expectedBookCommentsQuery.getResultList();

        assertThat(actualBookComments).usingRecursiveComparison()
                .ignoringExpectedNullFields().isEqualTo(expectedBookComments);

    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewBookComment() {
        // произвольная книга
        var listBooks = em.getEntityManager().createQuery("select b from Book b", Book.class).getResultList();
        var expectedBookComment = new BookComment(0, listBooks.get(0), "NewComment");
        var returnedBookComment = repositoryJpa.save(expectedBookComment);
        assertThat(returnedBookComment).isNotNull()
                .matches(book -> book.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBookComment);
        var foundBook = Optional.ofNullable(em.find(Book.class, returnedBookComment.getId()));
        assertThat(foundBook).isPresent().get()
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(returnedBookComment);
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedBookComment() {
        var currenBookComment = em.getEntityManager()
                .createQuery("select bc from BookComment bc", BookComment.class).getResultList().get(0);
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
        var currenBookComment = em.getEntityManager()
                .createQuery("select bc from BookComment bc", BookComment.class).getResultList().get(0);
        assertThat(currenBookComment).isNotNull();
        em.detach(currenBookComment);
        repositoryJpa.deleteById(currenBookComment.getId());
        var deletedBookComment = em.find(BookComment.class, currenBookComment.getId());
        assertThat(deletedBookComment).isNull();
    }

}
