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
import ru.otus.hw.models.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Репозиторий для работы с жанрами ")
@DataMongoTest
public class GenreRepositoryTest {

    private static final long EXPECTED_NUMBER_OF_GENRES = 6;

    private static final Set<String> GENRE_NAMES = Set.of("Genre_1", "Genre_3", "Genre_4");

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private GenreRepository repositoryMongo;

    @DisplayName("должен загружать жанры по имени")
    @ParameterizedTest
    @ValueSource(strings = {"Genre_1", "Genre_3", "Genre_100000000"})
    void shouldReturnCorrectGenreById(String name) {
        var actualGenre = repositoryMongo.findByName(name);
        Query query = new Query(Criteria.where("name").is(name));
        var expectedGenre = Optional.ofNullable(mongoOperations.findOne(query, Genre.class));

        assertTrue(actualGenre.isPresent() && expectedGenre.isPresent()
                || actualGenre.isEmpty() && expectedGenre.isEmpty());
        if (actualGenre.isPresent()) {
            assertThat(actualGenre).get().isEqualTo(expectedGenre.get());
        }
    }

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        var actualGenres = repositoryMongo.findAll();
        assertThat(actualGenres.size()).isEqualTo(EXPECTED_NUMBER_OF_GENRES);
        var expectedGenres = mongoOperations.findAll(Genre.class);
        assertThat(actualGenres).isEqualTo(expectedGenres);
    }

    @DisplayName("должен загружать список жанров по набору имен")
    @Test
    void shouldReturnCorrectGenreByNames() {
        var actualGenres = repositoryMongo.findAllByNameIn(GENRE_NAMES);
        List<Genre> expectedGenres = new ArrayList<>();
        for (String name : GENRE_NAMES) {
            Query query = new Query(Criteria.where("name").is(name));
            var expectedGenre = mongoOperations.findOne(query, Genre.class);
            assertThat(expectedGenre).isNotNull();
            expectedGenres.add(expectedGenre);
        }
        assertThat(actualGenres).hasSameElementsAs(expectedGenres);
    }
}
