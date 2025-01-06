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

    // abc 1
    @ShellMethod(value = "Find all book comments", key = "abc")
    public String findAllBookCommentsForBook(long bookId) {
        return bookCommentService.findForBook(bookId).stream()
                .map(bookCommentConverter::bookCommentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    // bcbi 1
    @ShellMethod(value = "Find book comment by id", key = "bcbi")
    public String findBookCommentById(long id) {
        return bookCommentService.findById(id)
                .map(bookCommentConverter::bookCommentToString)
                .orElse("Comment with id %d not found".formatted(id));
    }

    // cins 1 newComment
    @ShellMethod(value = "Insert book comment", key = "cins")
    public String insertBookComment(long bookId, String comment) {
        var savedBookComment = bookCommentService.insert(bookId, comment);
        return bookCommentConverter.bookCommentToString(savedBookComment);
    }

    // cupd 1 updComment
    @ShellMethod(value = "Update book comment", key = "cupd")
    public String updateBookComment(long id, String comment) {
        var savedBookComment = bookCommentService.update(id, comment);
        return bookCommentConverter.bookCommentToString(savedBookComment);
    }

    // cdel 1
    @ShellMethod(value = "Delete book comment", key = "cdel")
    public void deleteBookComment(long id) {
        bookCommentService.deleteById(id);
    }
}
