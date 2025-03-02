package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import ru.otus.hw.models.Author;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Репозиторий для работы с авторами ")
@DataMongoTest
public class AuthorRepositoryTest {

    private static final long EXPECTED_NUMBER_OF_AUTHORS = 3;

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private AuthorRepository repositoryMongo;

    @DisplayName("должен загружать автора по имени")
    @ParameterizedTest
    @ValueSource(strings = {"Author_1", "Author_2", "Author_3"})
    void shouldReturnCorrectAuthorByName(String name) {
        var actualAuthor = repositoryMongo.findByFullName(name);
        assertTrue(actualAuthor.isPresent());
        assertThat(actualAuthor.get().getFullName()).isEqualTo(name);

        var expectedAuthor = Optional.ofNullable(mongoOperations.findById(actualAuthor.get().getId(), Author.class));
        assertThat(actualAuthor).get().isEqualTo(expectedAuthor.get());

    }

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        var actualAuthors = repositoryMongo.findAll();
        assertThat(actualAuthors.size()).isEqualTo(EXPECTED_NUMBER_OF_AUTHORS);

        var expectedAuthors = mongoOperations.findAll(Author.class);
        assertThat(actualAuthors).isEqualTo(expectedAuthors);
    }
}
