package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Genre;

@Component
public class GenreConverter {
    public String genreToString(GenreDto genre) {
        return "%s".formatted(genre.getName());
    }

    public GenreDto genreToGenreDto(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName());
    }

    public Genre genreDtoToGenre(GenreDto genreDto) {
        return new Genre(genreDto.getId(), genreDto.getName());
    }
}
