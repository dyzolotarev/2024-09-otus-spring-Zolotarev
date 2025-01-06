package ru.otus.hw.repositories;

import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Optional;

public interface BookCommentRepository {
    Optional<BookComment> findById(long id);

    List<BookComment> findForBook(long bookId);

    BookComment save(BookComment bookComment);

    void deleteById(long id);
}
