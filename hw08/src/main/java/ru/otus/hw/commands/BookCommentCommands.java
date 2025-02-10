package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.BookCommentConverter;
import ru.otus.hw.services.BookCommentService;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@ShellComponent
public class BookCommentCommands {
    private final BookCommentService bookCommentService;

    private final BookCommentConverter bookCommentConverter;

    // abc 678c1c6b57e7db04b2c04d5f
    @ShellMethod(value = "Find all book comments by book id", key = "abc")
    public String findAllBookCommentsByBookId(String bookId) {
        return bookCommentService.findByBookId(bookId).stream()
                .map(bookCommentConverter::bookCommentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    // abct BookTitle_1
    @ShellMethod(value = "Find all book comments by book title", key = "abct")
    public String findAllBookCommentsByBookTitle(String bookTitle) {
        return bookCommentService.findByBookTitle(bookTitle).stream()
                .map(bookCommentConverter::bookCommentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    // bcbi 678c1c6b57e7db04b2c04d63
    @ShellMethod(value = "Find book comment by id", key = "bcbi")
    public String findBookCommentById(String id) {
        return bookCommentService.findById(id)
                .map(bookCommentConverter::bookCommentToString)
                .orElse("Comment with id %s not found".formatted(id));
    }

    // cins BookTitle_1 newComment
    @ShellMethod(value = "Insert book comment", key = "cins")
    public String insertBookComment(String bookTitle, String comment) {
        var savedBookComment = bookCommentService.insert(bookTitle, comment);
        return bookCommentConverter.bookCommentToString(savedBookComment);
    }

    // cupd 678d654b09522d3a5ebfbca0 updComment
    @ShellMethod(value = "Update book comment", key = "cupd")
    public String updateBookComment(String id, String comment) {
        var savedBookComment = bookCommentService.update(id, comment);
        return bookCommentConverter.bookCommentToString(savedBookComment);
    }

    // cdel 678d69d91adf6702a651b47e
    @ShellMethod(value = "Delete book comment", key = "cdel")
    public void deleteBookComment(String id) {
        bookCommentService.deleteById(id);
    }

    // cdelb BookTitle_2
    @ShellMethod(value = "Delete comments by book title", key = "cdelb")
    public void deleteBookCommentByBookTitle(String bookTitle) {
        bookCommentService.deleteByBookTitle(bookTitle);
    }
}
