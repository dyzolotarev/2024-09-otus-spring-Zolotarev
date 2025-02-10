package ru.otus.hw.services;

import ru.otus.hw.dto.BookCommentDto;

import java.util.List;
import java.util.Optional;

public interface BookCommentService {
    Optional<BookCommentDto> findById(String id);

    List<BookCommentDto> findByBookId(String bookId);

    List<BookCommentDto> findByBookTitle(String bookTitle);

    BookCommentDto insert(String bookId, String comment);

    BookCommentDto update(String id, String comment);

    void deleteById(String id);

    void deleteByBookTitle(String bookTitle);
}
