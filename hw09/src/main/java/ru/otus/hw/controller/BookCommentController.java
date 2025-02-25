package ru.otus.hw.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.dto.BookCommentForViewDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.NotFoundException;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookCommentController {

    private final BookCommentService bookCommentService;

    private final BookService bookService;

    @GetMapping("/comments/{book_id}")
    public String listCommentsPage(@PathVariable("book_id") long bookId, Model model) {
        List<BookCommentDto> comments = bookCommentService.findForBook(bookId);
        model.addAttribute("comments", comments);
        BookDto book = bookService.findById(bookId).orElse(null);
        model.addAttribute("book", book);
        return "comments";
    }

    @GetMapping("/edit_comment/{book_id}/{id}")
    public String editComment(@PathVariable("book_id") long bookId, @PathVariable("id") long id, Model model) {
        BookCommentDto comment = bookCommentService.findById(id).orElseThrow(NotFoundException::new);
        model.addAttribute("comment", comment);
        model.addAttribute("bookId", bookId);
        return "edit_comment";
    }

    @PostMapping("/edit_comment/{book_id}/{id}")
    public String saveBookComment(@PathVariable("book_id") long bookId,  @PathVariable(value = "id") long id,
                                  @RequestParam("comment") String comment) {
        if (id == 0) {
            bookCommentService.insert(bookId, comment);
        } else {
            bookCommentService.update(id, comment);
        }
        return "redirect:/comments/" + bookId;
    }

    @GetMapping("/create_comment/{book_id}")
    public String createComment(@PathVariable("book_id") long bookId, Model model) {
        BookCommentForViewDto comment = new BookCommentForViewDto(0L, "");
        model.addAttribute("comment", comment);
        model.addAttribute("bookId", bookId);
        return "edit_comment";
    }

    @PostMapping("/delete_comment/{book_id}/{id}")
    public String deleteBookComment(@PathVariable("book_id") long bookId, @PathVariable("id") long id,
                                    Model model) {
        bookCommentService.deleteById(id);
        model.addAttribute("bookId", bookId);
        return "redirect:/comments/" + bookId;
    }

}
