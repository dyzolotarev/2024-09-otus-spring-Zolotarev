package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Genre;
import ru.otus.hw.models.GenreV2;

@Component
public class GenreConverter {
    public String genreToString(GenreDto genre) {
        return "Id: %s, Name: %s".formatted(genre.getId(), genre.getName());
    }

    public GenreDto genreToGenreDto(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName());
    }

    public Genre genreDtoToGenre(GenreDto genreDto) {
        return new Genre(genreDto.getId(), genreDto.getName());
    }

    public GenreV2 genreToGenreV2(Genre genre) {
        return new GenreV2(0, genre.getName(), genre.getId());
    }
}
