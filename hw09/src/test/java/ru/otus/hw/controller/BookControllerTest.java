package ru.otus.hw.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookForViewDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.NotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("Контроллер для работы с книгами ")
@WebMvcTest(BookController.class)
@Import({AuthorConverter.class, GenreConverter.class, BookConverter.class})
public class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private GenreService genreService;

    private final List<AuthorDto> authors = List.of(new AuthorDto(2L, "Author 2L"),
            new AuthorDto(4L, "Author 4L"));

    private final List<GenreDto> genres = List.of(new GenreDto(1L, "Genre 1L"),
            new GenreDto(2L, "Genre 2L"), new GenreDto(3L, "Genre 3L"),
            new GenreDto(4L, "Genre 4L"));

    private final List<BookDto> books = List.of(
            new BookDto(1L, "TestBook 1L", authors.get(0), List.of(genres.get(0))),
            new BookDto(3L, "TestBook 3L", authors.get(1),
                    List.of(genres.get(0), genres.get(2), genres.get(3))),
            new BookDto(10L, "TestBook 10L", authors.get(1),
                    List.of(genres.get(1), genres.get(2), genres.get(3))),
            new BookDto(11L, "TestBook 11L", authors.get(0),
                    List.of(genres.get(2), genres.get(3))));

    private final List<BookForViewDto> booksView = List.of(
            new BookForViewDto("1", "TestBook 1L", "2", Set.of("1")),
            new BookForViewDto("3", "TestBook 3L", "4", Set.of("1", "3", "4")),
            new BookForViewDto("10", "TestBook 10L", "4", Set.of("2", "3", "4")));

    @DisplayName("должен отображать спиcок книг на главной странице")
    @Test
    void shouldRenderListBookPageWithCorrectViewAndModelAttributes() throws Exception {
        when(bookService.findAll()).thenReturn(books);
        mvc.perform(get("/"))
                .andExpect(view().name("books"))
                .andExpect(model().attribute("books", books));
    }

    @DisplayName("должен отображать данные о книге на странице редактирования")
    @ParameterizedTest
    @ValueSource(ints = {0, 2})
    void shouldRenderEditBookPageWithCorrectViewAndModelAttributes(int id) throws Exception {
        BookDto expectedBook = books.get(id);
        BookForViewDto expectedBookView = booksView.get(id);
        when(bookService.findById(expectedBook.getId())).thenReturn(Optional.of(expectedBook));
        mvc.perform(get("/edit_book").param("id", String.valueOf(expectedBook.getId())))
                .andExpect(view().name("edit_book"))
                .andExpect(model().attribute("book", expectedBookView));
    }

    @DisplayName("должен отображать ошибку, если книга не найдена")
    @Test
    void shouldRenderErrorPageWhenBookNotFound() throws Exception {
        when(bookService.findById(1L)).thenThrow(new NotFoundException());
        mvc.perform(get("/edit_book").param("id", "1"))
                .andExpect(view().name("customError"));
    }

    @DisplayName("должен сохранять отредактированную книгу")
    @Test
    void shouldUpdateBookAndRedirectToContextPath() throws Exception {
        BookDto existingBook = books.get(1);
        BookForViewDto modifiedBook = new BookForViewDto(String.valueOf(existingBook.getId()),
                "Modified Title", "2", Set.of("1", "2", "3"));
        mvc.perform(post("/edit_book")
                        .param("id", String.valueOf(modifiedBook.getId()))
                        .param("title", modifiedBook.getTitle())
                        .param("authorId", modifiedBook.getAuthorId())
                        .param("genreIds", String.join(",", modifiedBook.getGenreIds())))
                .andExpect(view().name("redirect:/"));
        verify(bookService, times(1)).update(existingBook.getId(), modifiedBook.getTitle(),
                Long.parseLong(modifiedBook.getAuthorId()),
                modifiedBook.getGenreIds().stream().map(Long::parseLong).collect(Collectors.toSet()));
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldInsertBookAndRedirectToContextPath() throws Exception {
        BookForViewDto newBook = new BookForViewDto("", "Title of new book", "1", Set.of("1", "2"));
        mvc.perform(post("/edit_book")
                        .param("id", "")
                        .param("title", newBook.getTitle())
                        .param("authorId", newBook.getAuthorId())
                        .param("genreIds", String.join(",", newBook.getGenreIds())))
                .andExpect(view().name("redirect:/"));
        verify(bookService, times(1)).insert(newBook.getTitle(),
                Long.parseLong(newBook.getAuthorId()),
                newBook.getGenreIds().stream().map(Long::parseLong).collect(Collectors.toSet()));
    }

    @DisplayName("должен отображать страницу удаления книги")
    @Test
    void RenderDeleteBookPageWithCorrectViewAttributes() throws Exception {
        BookDto expectedBook = books.get(3);
        when(bookService.findById(expectedBook.getId())).thenReturn(Optional.of(expectedBook));
        mvc.perform(get("/delete_book").param("id", String.valueOf(expectedBook.getId())))
                .andExpect(view().name("delete_book"));
    }

    @DisplayName("должен удалять книгу")
    @Test
    void shouldDeleteBookAndRedirectToContextPath() throws Exception {
        BookDto expectedBook = books.get(3);
        mvc.perform(post("/delete_book")
                        .param("id", String.valueOf(expectedBook.getId())))
                .andExpect(view().name("redirect:/"));
        verify(bookService, times(1)).deleteById(expectedBook.getId());
    }
}
