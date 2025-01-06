package ru.otus.hw.repositories;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.FETCH;
import static org.springframework.util.CollectionUtils.isEmpty;

@Repository
@RequiredArgsConstructor
public class JpaBookRepository implements BookRepository {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public Optional<Book> getBook(long id) {
        return Optional.ofNullable(em.find(Book.class, id));
    }

    @Override
    public Optional<Book> findById(long id) {

        EntityGraph<?> entityGraph = em.getEntityGraph("books-authors-entity-graph");
        Map<String, Object> hints = new HashMap<>();
        hints.put(FETCH.getKey(), entityGraph);

        var book = Optional.ofNullable(em.find(Book.class, id, hints));
        book.ifPresent(value -> Hibernate.initialize(value.getGenres()));
        return book;
    }

    @Override
    public List<Book> findAll() {

        EntityGraph<?> entityGraph = em.getEntityGraph("books-authors-entity-graph");
        TypedQuery<Book> query = em.createQuery("select b from Book b", Book.class);
        query.setHint(FETCH.getKey(), entityGraph);

        var books = query.getResultList();
        if (!isEmpty(books)) {
            Hibernate.initialize(books.get(0).getGenres());
        }
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        var book = getBook(id);
        book.ifPresent(em::remove);
    }

    private Book insert(Book book) {
        em.persist(book);
        return book;
    }

    private Book update(Book book) {
        em.merge(book);
        return book;
    }
}
