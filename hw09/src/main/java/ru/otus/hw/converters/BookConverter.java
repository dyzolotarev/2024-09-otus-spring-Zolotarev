package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookForViewDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Book;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BookConverter {
    private final AuthorConverter authorConverter;

    private final GenreConverter genreConverter;

    public String bookToString(BookDto book) {
        var genresString = book.getGenres().stream()
                .map(genreConverter::genreToString)
                .map("%s"::formatted)
                .collect(Collectors.joining(", "));
        return "Title: %s, author: %s, genres: %s".formatted(
                book.getTitle(),
                authorConverter.authorToString(book.getAuthor()),
                genresString);
    }

    public BookDto bookToBookDto(Book book) {
        var bookDto = new BookDto();
        bookDto.setId(book.getId());
        bookDto.setTitle(book.getTitle());
        bookDto.setAuthor(authorConverter.authorToAuthorDto(book.getAuthor()));
        bookDto.setGenres(book.getGenres().stream().map(genreConverter::genreToGenreDto).collect(Collectors.toList()));
        return bookDto;
    }

    public Book bookDtoToBook(BookDto bookDto) {
        var book = new Book();
        book.setId(bookDto.getId());
        book.setTitle(bookDto.getTitle());
        book.setAuthor(authorConverter.authorDtoToAuthor(bookDto.getAuthor()));
        book.setGenres(bookDto.getGenres().stream().map(genreConverter::genreDtoToGenre).collect(Collectors.toList()));
        return book;
    }

    public BookForViewDto bookDtoToBookForViewDto(BookDto bookDto) {
        var bookFlatDto = new BookForViewDto();
        bookFlatDto.setId(String.valueOf(bookDto.getId()));
        bookFlatDto.setTitle(bookDto.getTitle());
        bookFlatDto.setAuthorId(String.valueOf(bookDto.getAuthor().getId()));
        bookFlatDto.setGenreIds(bookDto.getGenres().stream().map(GenreDto::getId).map(String::valueOf)
                .collect(Collectors.toSet()));
        return bookFlatDto;
    }
}
