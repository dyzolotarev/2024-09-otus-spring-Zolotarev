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
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Репозиторий на основе Jpa для работы с книгами ")
@DataJpaTest
@Import({JpaBookRepository.class, JpaGenreRepository.class})
class JpaBookRepositoryTest {

    @Autowired
    private JpaBookRepository repositoryJpa;

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
        var expectedBooks = em.getEntityManager().createQuery("select b from Book b", Book.class).getResultList();
        assertThat(actualBooks).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBooks);
        // в случае большого количества книг вытаскивать все и сравнивать думаю долго и избыточно
        // и лучше ограничится count-ом ниже и выборочной сверкой, но пока так,
        // чтоб без фанатизма и не думать о выборочной сверке)
        //  var countBooks= em.getEntityManager()
        //          .createQuery("select count(b) from Book b", Long.class).getSingleResult();
        //  assertThat(countBooks).isEqualTo(actualBooks.size());
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldSaveNewBook() {
        var listAuthors = em.getEntityManager().createQuery("select a from Author a", Author.class).getResultList();
        var listGenres = em.getEntityManager().createQuery("select g from Genre g", Genre.class).getResultList();
        var expectedBook = new Book(0, "Book of all genres", listAuthors.get(0), listGenres);
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
        var currenBook = em.getEntityManager()
                .createQuery("select b from Book b", Book.class).getResultList().get(0);
        em.detach(currenBook);

        TypedQuery<Author> queryAuthor = em.getEntityManager()
                .createQuery("select a from Author a where a.id != :id", Author.class);
        queryAuthor.setParameter("id", currenBook.getAuthor().getId());
        var newAuthor = queryAuthor.getResultList().get(0);

        var newGenres = em.getEntityManager().createQuery("select g from Genre g", Genre.class).getResultList();

        var expectedBook = new Book(currenBook.getId(),
                "Modified " + currenBook.getTitle(), newAuthor, newGenres);

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
        var currenBook = em.getEntityManager()
                .createQuery("select b from Book b", Book.class).getResultList().get(0);
        assertThat(currenBook).isNotNull();
        em.detach(currenBook);
        repositoryJpa.deleteById(currenBook.getId());
        var deletedBook = em.find(Book.class, currenBook.getId());
        assertThat(deletedBook).isNull();
    }
}