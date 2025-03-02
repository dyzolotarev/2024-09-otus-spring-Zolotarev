package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Optional;

public interface BookCommentRepository extends MongoRepository<BookComment, String> {
    Optional<BookComment> findById(String id);

    List<BookComment> findByBookId(String bookId);

    BookComment save(BookComment bookComment);

    void deleteById(String id);

    void deleteByBookId(String bookId);
}
