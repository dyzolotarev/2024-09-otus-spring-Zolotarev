package ru.otus.hw.services;

import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Optional;

public interface BookCommentService {
    Optional<BookComment> findById(long id);

    List<BookComment> findForBook(long bookId);

    BookComment insert(long bookId, String comment);

    BookComment update(long id, String comment);

    void deleteById(long id);
}
