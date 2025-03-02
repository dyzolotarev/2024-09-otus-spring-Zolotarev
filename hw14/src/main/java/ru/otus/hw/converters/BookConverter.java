package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookV2;
import ru.otus.hw.models.Genre;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BookConverter {
    private final AuthorConverter authorConverter;

    private final GenreConverter genreConverter;

    public String bookToString(BookDto book) {
        var genresString = book.getGenres().stream()
                .map(genreConverter::genreToString)
                .map("{%s}"::formatted)
                .collect(Collectors.joining(", "));
        return "Id: %s, title: %s, author: {%s}, genres: [%s]".formatted(
                book.getId(),
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

    public BookV2 bookToBookV2(Book book) {
        var bookV2 = new BookV2();
        bookV2.setId(0);
        bookV2.setTitle(book.getTitle());
        bookV2.setAuthor(authorConverter.authorToAuthorV2(book.getAuthor()));
        bookV2.setGenres(book.getGenres().stream().map(genreConverter::genreToGenreV2).collect(Collectors.toList()));
        bookV2.setMongoId(book.getId());
        bookV2.setGenreMongoIds(book.getGenres().stream().map(Genre::getId).toList());
        return bookV2;
    }
}
