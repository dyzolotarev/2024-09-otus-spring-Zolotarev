package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.services.BookService;

import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
@RequiredArgsConstructor
@ShellComponent
public class BookCommands {

    private final BookService bookService;

    private final BookConverter bookConverter;

    @ShellMethod(value = "Find all books", key = "ab")
    public String findAllBooks() {
        return bookService.findAll().stream()
                .map(bookConverter::bookToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Find book by id", key = "bbid")
    public String findBookById(String id) {
        return bookService.findById(id)
                .map(bookConverter::bookToString)
                .orElse("Book with id %s not found".formatted(id));
    }

    // bbt BookTitle_1
    @ShellMethod(value = "Find book by title", key = "bbt")
    public String findBookByTitle(String title) {
        return bookService.findByTitle(title)
                .map(bookConverter::bookToString)
                .orElse("Book with id %s not found".formatted(title));
    }

    // bins newBook Author_1 Genre_1,Genre_2,Genre_6
    @ShellMethod(value = "Insert book", key = "bins")
    public String insertBook(String title, String authorFullName, Set<String> genreNames) {
        var savedBook = bookService.insert(title, authorFullName, genreNames);
        return bookConverter.bookToString(savedBook);
    }

    // bupd BookTitle_1 editedBook Author_2 Genre_4,Genre_5,Genre_6
    @ShellMethod(value = "Update book", key = "bupd")
    public String updateBook(String currentTitle, String title, String authorFullName, Set<String> genreNames) {
        var savedBook = bookService.update(currentTitle, title, authorFullName, genreNames);
        return bookConverter.bookToString(savedBook);
    }

    // bdel 678ec0975d13b21ea9c542e6
    @ShellMethod(value = "Delete book by id", key = "bdel")
    public void deleteBook(String id) {
        bookService.deleteById(id);
    }

    // bdelt BookTitle_2
    @ShellMethod(value = "Delete book by title", key = "bdelt")
    public void deleteBookbyTitle(String title) {
        bookService.deleteByTitle(title);
    }
}
