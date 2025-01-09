package ru.otus.hw.services;

import ru.otus.hw.dto.BookCommentDto;

import java.util.List;
import java.util.Optional;

public interface BookCommentService {
    Optional<BookCommentDto> findById(long id);

    List<BookCommentDto> findForBook(long bookId);

    BookCommentDto insert(long bookId, String comment);

    BookCommentDto update(long id, String comment);

    void deleteById(long id);
}
