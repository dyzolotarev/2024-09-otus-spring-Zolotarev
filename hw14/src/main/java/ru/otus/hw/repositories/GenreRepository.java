package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreRepository extends MongoRepository<Genre, String> {
    List<Genre> findAll();

    List<Genre> findAllByIdIn(Set<String> ids);

    List<Genre> findAllByNameIn(Set<String> names);

    Optional<Genre> findById(String id);

    Optional<Genre> findByName(String name);
}
