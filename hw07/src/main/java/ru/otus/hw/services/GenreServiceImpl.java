package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    private final GenreConverter genreConverter;

    @Override
    public Optional<GenreDto> findById(long id) {
        return genreRepository.findById(id).map(genreConverter::genreToGenreDto);
    }

    @Override
    public List<GenreDto> findAll() {
        return genreRepository.findAll().stream().map(genreConverter::genreToGenreDto).collect(Collectors.toList());
    }
}
