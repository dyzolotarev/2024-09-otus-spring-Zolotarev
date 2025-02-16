package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Optional;

public interface BookCommentRepository extends JpaRepository<BookComment, Long> {
    Optional<BookComment> findById(long id);

    List<BookComment> findByBookId(long bookId);

    BookComment save(BookComment bookComment);

    void deleteById(long id);
}
