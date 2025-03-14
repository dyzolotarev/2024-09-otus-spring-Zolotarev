package ru.otus.hw.services;

import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreService {
    Optional<Genre> findById(long id);

    List<Genre> findAll();
}
