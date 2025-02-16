package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Author;

import java.util.Optional;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Репозиторий для работы с авторами ")
@DataJpaTest
public class AuthorRepositoryTest {

    private static final long EXPECTED_NUMBER_OF_AUTHORS = 3;

    @Autowired
    private AuthorRepository repositoryJpa;

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
        var expectedAuthors = LongStream.range(1, EXPECTED_NUMBER_OF_AUTHORS + 1).boxed()
                .map(id -> em.find(Author.class, id)).toList();
        assertThat(actualAuthors).isEqualTo(expectedAuthors);
    }
}
