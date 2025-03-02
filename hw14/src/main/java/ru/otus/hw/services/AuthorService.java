package ru.otus.hw.services;

import ru.otus.hw.dto.AuthorDto;

import java.util.List;
import java.util.Optional;

public interface AuthorService {
    Optional<AuthorDto> findById(String id);

    Optional<AuthorDto> findByFullName(String name);

    List<AuthorDto> findAll();
}
