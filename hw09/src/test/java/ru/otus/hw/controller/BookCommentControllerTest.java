package ru.otus.hw.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.dto.BookCommentForViewDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.NotFoundException;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("Контроллер для работы с комментариями ")
@WebMvcTest(BookCommentController.class)
public class BookCommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private BookCommentService bookCommentService;

    private final AuthorDto author = new AuthorDto(2L, "Author 2L");

    private final List<GenreDto> genres = List.of(new GenreDto(1L, "Genre 1L"),
            new GenreDto(2L, "Genre 2L"));

    private final BookDto book = new BookDto(2L, "TestBook 1L", author, genres);

    private final List<BookCommentDto> bookComments = List.of(new BookCommentDto(1, "Test comment 1"),
            new BookCommentDto(2, "Test comment 2"), new BookCommentDto(3, "Test comment 3"));

    @DisplayName("должен отображать спиcок комментариев книги")
    @Test
    void shouldRenderListPageCommentWithCorrectViewAndModelAttributes() throws Exception {
        when(bookCommentService.findForBook(book.getId())).thenReturn(bookComments);
        when(bookService.findById(book.getId())).thenReturn(Optional.of(book));
        mvc.perform(get("/comments/{book_id}", book.getId()))
                .andExpect(view().name("comments"))
                .andExpect(model().attribute("book", book))
                .andExpect(model().attribute("comments", bookComments));
    }

    @DisplayName("должен отображать данные о комментарии на странице редактирования")
    @ParameterizedTest
    @ValueSource(ints = {0, 2})
    void shouldRenderEditCommentPageWithCorrectViewAndModelAttributes(int id) throws Exception {
        BookCommentDto expectedComment = bookComments.get(id);
        when(bookCommentService.findById(expectedComment.getId())).thenReturn(Optional.of(expectedComment));
        mvc.perform(get("/edit_comment/{book_id}/{id}", book.getId(), expectedComment.getId()))
                .andExpect(view().name("edit_comment"))
                .andExpect(model().attribute("comment", expectedComment))
                .andExpect(model().attribute("bookId", book.getId()));
    }

    @DisplayName("должен отображать ошибку, если комментарий не найден")
    @Test
    void shouldRenderErrorPageWhenBookCommentNotFound() throws Exception {
        when(bookCommentService.findById(1L)).thenThrow(new NotFoundException());
        mvc.perform(get("/edit_comment/{book_id}/{id}", book.getId(), 1L))
                .andExpect(view().name("customError"));
    }

    @DisplayName("должен сохранять отредактированный комментарий")
    @Test
    void shouldUpdateCommentPageAndRedirectToBookPage() throws Exception {
        BookCommentDto existingComment = bookComments.get(0);
        BookCommentForViewDto modifiedComment = new BookCommentForViewDto(existingComment.getId()
                , "Modified comment");
        when(bookCommentService.findById(existingComment.getId())).thenReturn(Optional.of(existingComment));
        mvc.perform(post("/edit_comment/{book_id}/{id}", book.getId(), existingComment.getId())
                        .param("id", String.valueOf(existingComment.getId()))
                        .param("bookId", String.valueOf(book.getId()))
                        .param("comment", modifiedComment.getComment()))
                .andExpect(view().name("redirect:/comments/" + book.getId()));
        verify(bookCommentService, times(1))
                .update(existingComment.getId(), modifiedComment.getComment());
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldInsertCommentPageAndRedirectToBookPage() throws Exception {
        BookCommentForViewDto newComment = new BookCommentForViewDto(0, "New comment");
        mvc.perform(post("/edit_comment/{book_id}/{id}", book.getId(), 0)
                        .param("bookId", String.valueOf(book.getId()))
                        .param("id", "0")
                        .param("comment", newComment.getComment()))
                .andExpect(view().name("redirect:/comments/" + book.getId()));
        verify(bookCommentService, times(1))
                .insert(book.getId(), newComment.getComment());
    }

    @DisplayName("должен удалять комментарий")
    @Test
    void shouldDeleteCommentPageAndRedirectToBookPage() throws Exception {
        BookCommentDto existingComment = bookComments.get(1);
        mvc.perform(post("/delete_comment/{book_id}/{id}", book.getId(), existingComment.getId()))
                .andExpect(view().name("redirect:/comments/" + book.getId()));
        verify(bookCommentService, times(1))
                .deleteById(existingComment.getId());
    }
}
