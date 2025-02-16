package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Репозиторий для работы с жанрами ")
@DataJpaTest
public class GenreRepositoryTest {

    private static final long EXPECTED_NUMBER_OF_GENRES = 6;

    private static final Set<Long> GENRE_IDS = Set.of(1L, 3L, 4L);

    @Autowired
    private GenreRepository repositoryJpa;

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
        var expectedGenres = LongStream.range(1, EXPECTED_NUMBER_OF_GENRES + 1).boxed()
                .map(id -> em.find(Genre.class, id)).toList();
        assertThat(actualGenres).isEqualTo(expectedGenres);
    }

    @DisplayName("должен загружать список жанров по набору Id")
    @Test
    void shouldReturnCorrectGenreByIds() {
        var actualGenres = repositoryJpa.findAllByIdIn(GENRE_IDS);
        List<Genre> expectedGenres = new ArrayList<>();
        for (long id : GENRE_IDS) {
            var genre = em.find(Genre.class, id);
            assertThat(genre).isNotNull();
            expectedGenres.add(genre);
        }
        assertThat(actualGenres).hasSameElementsAs(expectedGenres);
    }
}
