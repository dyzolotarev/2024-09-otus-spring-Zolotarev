package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.BookComment;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaBookCommentRepository implements BookCommentRepository {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public Optional<BookComment> findById(long id) {
        return Optional.ofNullable(em.find(BookComment.class, id));
    }

    @Override
    public List<BookComment> findForBook(long bookId) {
        TypedQuery<BookComment> query = em.createQuery("select c from BookComment c " +
                                                           "where c.book.id = :book_id", BookComment.class);
        query.setParameter("book_id", bookId);
        return query.getResultList();
    }

    @Override
    public BookComment save(BookComment bookComment) {
        if (bookComment.getId() == 0) {
            return insert(bookComment);
        }
        return update(bookComment);
    }

    private BookComment insert(BookComment bookComment) {
        em.persist(bookComment);
        return bookComment;
    }

    private BookComment update(BookComment bookComment) {
        em.merge(bookComment);
        return bookComment;
    }

    @Override
    public void deleteById(long id) {
        var bookComment = findById(id);
        bookComment.ifPresent(em::remove);
    }
}
