package ru.otus.hw.services;

import ru.otus.hw.dto.GenreDto;

import java.util.List;
import java.util.Optional;

public interface GenreService {
    Optional<GenreDto> findById(String id);

    Optional<GenreDto> findByName(String name);

    List<GenreDto> findAll();
}
