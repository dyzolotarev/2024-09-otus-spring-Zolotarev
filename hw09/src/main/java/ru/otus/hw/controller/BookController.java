package ru.otus.hw.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import java.util.stream.Collectors;

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

    @GetMapping("/edit_book")
    public String editBook(@RequestParam("id") String id, Model model) {
        BookDto book = bookService.findById(Long.parseLong(id)).orElseThrow(NotFoundException::new);
        model.addAttribute("book", bookConverter.bookDtoToBookForViewDto(book));

        List<Long> selectedGenres = book.getGenres().stream().map(GenreDto::getId).toList();
        model.addAttribute("selectedGenres", selectedGenres);

        List<AuthorDto> authors = authorService.findAll();
        model.addAttribute("authors", authors);

        List<GenreDto> genres = genreService.findAll();
        model.addAttribute("genres", genres);

        return "edit_book";
    }

    @PostMapping("/edit_book")
    public String saveBook(@ModelAttribute("book") BookForViewDto book) {
        if (book.getId().isEmpty()) {
            bookService.insert(book.getTitle(), Long.parseLong(book.getAuthorId()),
                    book.getGenreIds().stream().map(Long::parseLong).collect(Collectors.toSet()));
        } else {
            bookService.update(Long.parseLong(book.getId()), book.getTitle(), Long.parseLong(book.getAuthorId()),
                    book.getGenreIds().stream().map(Long::parseLong).collect(Collectors.toSet()));
        }
        return "redirect:/";
    }

    @GetMapping("/delete_book")
    public String deleteBook(@RequestParam("id") String id, Model model) {
        BookDto book = bookService.findById(Long.parseLong(id)).orElseThrow(NotFoundException::new);
        model.addAttribute("bookDescription", bookConverter.bookToString(book));
        model.addAttribute("id", id);
        return "delete_book";
    }

    @PostMapping("/delete_book")
    public String removeBook(@RequestParam("id") String id) {
        bookService.deleteById(Long.parseLong(id));
        return "redirect:/";
    }

    @GetMapping("/create_book")
    public String createBook(Model model) {
        BookForViewDto book = new BookForViewDto("", "", "", Set.of(""));
        model.addAttribute("book", book);

        List<AuthorDto> authors = authorService.findAll();
        model.addAttribute("authors", authors);

        List<GenreDto> genres = genreService.findAll();
        model.addAttribute("genres", genres);

        return "edit_book";
    }
}
