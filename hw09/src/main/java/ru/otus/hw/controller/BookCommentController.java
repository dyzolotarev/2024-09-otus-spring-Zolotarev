package ru.otus.hw.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/comments")
    public String listCommentsPage(@RequestParam("id") String id, Model model) {
        List<BookCommentDto> comments = bookCommentService.findForBook(Long.parseLong(id));
        model.addAttribute("comments", comments);
        BookDto book = bookService.findById(Long.parseLong(id)).orElse(null);
        model.addAttribute("book", book);
        return "comments";
    }

    @GetMapping("/edit_comment")
    public String editComment(@RequestParam("book_id") String bookId, @RequestParam("id") String id, Model model) {
        BookCommentDto comment = bookCommentService.findById(Long.parseLong(id)).orElseThrow(NotFoundException::new);
        model.addAttribute("comment", comment);
        model.addAttribute("bookId", bookId);
        return "edit_comment";
    }

    @PostMapping("/edit_comment")
    public String saveBookComment(@RequestParam("id") String id, @RequestParam("bookId") String bookId,
                                  @RequestParam("comment") String comment) {
        if (id.isEmpty()) {
            bookCommentService.insert(Long.parseLong(bookId), comment);
        } else {
            bookCommentService.update(Long.parseLong(id), comment);
        }
        return "redirect:/comments?id=" + bookId;
    }

    @GetMapping("/create_comment")
    public String createComment(@RequestParam("book_id") String bookId, Model model) {
        BookCommentForViewDto comment = new BookCommentForViewDto("", "");
        model.addAttribute("comment", comment);
        model.addAttribute("bookId", bookId);
        return "edit_comment";
    }

    @PostMapping("/delete_comment")
    public String deleteBookComment(@RequestParam("id") String id, @RequestParam("book_id") String bookId,
                                    Model model) {
        bookCommentService.deleteById(Long.parseLong(id));
        model.addAttribute("bookId", bookId);
        return "redirect:/comments?id=" + bookId;
    }

}
