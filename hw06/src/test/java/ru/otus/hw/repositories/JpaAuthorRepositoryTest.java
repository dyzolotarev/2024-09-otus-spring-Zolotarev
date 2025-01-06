package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Репозиторий на основе Jpa для работы с авторами ")
@DataJpaTest
@Import({JpaAuthorRepository.class})
public class JpaAuthorRepositoryTest {

    @Autowired
    private JpaAuthorRepository repositoryJpa;

    @Autowired
    private TestEntityManager em;

    @DisplayName("должен загружать автора по id")
    @ParameterizedTest
    @ValueSource(longs = {1, 3, 5, 100000000})
    void shouldReturnCorrectAuthorById(long id) {
        var actualAuthor = repositoryJpa.findById(id);
        var expectedAuthor = Optional.ofNullable(em.find(Author.class, id));
        assertTrue(actualAuthor.isPresent() && expectedAuthor.isPresent()
                || actualAuthor.isEmpty() && expectedAuthor.isEmpty());
        if (actualAuthor.isPresent()) {
            assertThat(actualAuthor).get().isEqualTo(expectedAuthor.get());
        };
    }

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        var actualAuthors = repositoryJpa.findAll();
        var expectedAuthors = em.getEntityManager()
                .createQuery("select a from Author a", Author.class).getResultList();
        assertThat(actualAuthors).isEqualTo(expectedAuthors);
    }
}
