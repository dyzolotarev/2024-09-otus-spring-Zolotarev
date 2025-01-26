package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;

@Component
public class AuthorConverter {
    public String authorToString(AuthorDto author) {
        return "Id: %s, FullName: %s".formatted(author.getId(), author.getFullName());
    }

    public AuthorDto authorToAuthorDto(Author author) {
        return new AuthorDto(author.getId(), author.getFullName());
    }

    public Author authorDtoToAuthor(AuthorDto authorDto) {
        return new Author(authorDto.getId(), authorDto.getFullName());
    }
}
