package ru.otus.hw.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookForViewDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.NotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final BookConverter bookConverter;

    @GetMapping({"/", "/books"})
    public String listBookPage(Model model) {
        List<BookDto> books = bookService.findAll();
        model.addAttribute("books", books);
        return "books";
    }

    @GetMapping("/edit_book/{id}")
    public String editBook(@PathVariable("id") long id, Model model) {
        BookDto book = bookService.findById(id).orElseThrow(NotFoundException::new);
        model.addAttribute("book", bookConverter.bookDtoToBookForViewDto(book));

        List<Long> selectedGenres = book.getGenres().stream().map(GenreDto::getId).toList();
        model.addAttribute("selectedGenres", selectedGenres);

        List<AuthorDto> authors = authorService.findAll();
        model.addAttribute("authors", authors);

        List<GenreDto> genres = genreService.findAll();
        model.addAttribute("genres", genres);

        return "edit_book";
    }

    @PostMapping("/edit_book/{id}")
    public String saveBook(@ModelAttribute("book") BookForViewDto book) {
        if (book.getId() == 0) {
            bookService.insert(book.getTitle(), book.getAuthorId(), book.getGenreIds());
        } else {
            bookService.update(book.getId(), book.getTitle(), book.getAuthorId(), book.getGenreIds());
        }
        return "redirect:/";
    }

    @GetMapping("/delete_book/{id}")
    public String deleteBook(@PathVariable("id") long id, Model model) {
        BookDto book = bookService.findById(id).orElseThrow(NotFoundException::new);
        model.addAttribute("bookDescription", bookConverter.bookToString(book));
        model.addAttribute("id", id);
        return "delete_book";
    }

    @PostMapping("/delete_book/{id}")
    public String removeBook(@PathVariable("id") long id) {
        bookService.deleteById(id);
        return "redirect:/";
    }

    @GetMapping("/create_book")
    public String createBook(Model model) {
        BookForViewDto book = new BookForViewDto(0L, "", 0L, Set.of(0L));
        model.addAttribute("book", book);

        List<AuthorDto> authors = authorService.findAll();
        model.addAttribute("authors", authors);

        List<GenreDto> genres = genreService.findAll();
        model.addAttribute("genres", genres);

        return "edit_book";
    }
}
