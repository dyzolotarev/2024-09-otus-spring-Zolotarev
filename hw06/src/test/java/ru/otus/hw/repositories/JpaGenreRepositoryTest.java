package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Репозиторий на основе Jpa для работы с жанрами ")
@DataJpaTest
@Import({JpaGenreRepository.class})
public class JpaGenreRepositoryTest {

    @Autowired
    private JpaGenreRepository repositoryJpa;

    @Autowired
    private TestEntityManager em;

    @DisplayName("должен загружать жанр по id")
    @ParameterizedTest
    @ValueSource(longs = {1, 3, 5, 100000000})
    void shouldReturnCorrectGenreById(long id) {
        var actualGenre = repositoryJpa.findById(id);
        var expectedGenre = Optional.ofNullable(em.find(Genre.class, id));
        assertTrue(actualGenre.isPresent() && expectedGenre.isPresent()
                || actualGenre.isEmpty() && expectedGenre.isEmpty());
        if (actualGenre.isPresent()) {
            assertThat(actualGenre).get().isEqualTo(expectedGenre.get());
        }
    }

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        var actualGenres = repositoryJpa.findAll();
        var expectedGenres= em.getEntityManager()
                .createQuery("select g from Genre g", Genre.class).getResultList();
        assertThat(actualGenres).isEqualTo(expectedGenres);
//        actualGenres.forEach(System.out::println);
    }
}
