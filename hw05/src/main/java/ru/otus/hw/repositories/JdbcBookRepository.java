package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final GenreRepository genreRepository;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Optional<Book> findById(long id) {
        Map<String, Object> params = Collections.singletonMap("id", id);
        String sql = "select b.id, b.title, b.author_id, a.full_name, g.id genre_id, g.name genre_name " +
                     "  from books b " +
                     "  left join authors a on a.id = b.author_id " +
                     "  join books_genres bg on bg.book_id = b.id " +
                     "  join genres g on g.id = bg.genre_id " +
                     " where b.id = :id " +
                     " order by g.name";

        return Optional.ofNullable(namedParameterJdbcTemplate.query(sql, params, new BookResultSetExtractor()));
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var relations = getAllGenreRelations();
        var books = getAllBooksWithoutGenres();
        mergeBooksInfo(books, genres, relations);
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
        Map<String, Object> params = Collections.singletonMap("id", id);
        String sql = "delete from books where id = :id";
        namedParameterJdbcTemplate.update(sql, params);
    }

    private List<Book> getAllBooksWithoutGenres() {
        String sql = "select b.id, b.title, b.author_id, a.full_name" +
                     "  from books b left join authors a on b.author_id = a.id" +
                     " order by b.title, a.full_name";
        return namedParameterJdbcTemplate.query(sql, new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        String sql = "select book_id, genre_id from books_genres";
        return namedParameterJdbcTemplate.query(sql, new BookGenreRowMapper());
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        Map<Long, Book> bookMap =
                booksWithoutGenres.stream().collect(Collectors.toMap(Book::getId, Function.identity()));
        Map<Long, Genre> genreMap =
                genres.stream().collect(Collectors.toMap(Genre::getId, Function.identity()));

        relations.forEach(relation -> {
            if (bookMap.containsKey(relation.bookId) && genreMap.containsKey(relation.genreId)) {
                bookMap.get(relation.bookId).getGenres().add(genreMap.get(relation.genreId));
            }
        });
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();
        String sql = "insert into books (title, author_id) values (:title, :author_id)";
        MapSqlParameterSource params = new MapSqlParameterSource(
                Map.of("title", book.getTitle(), "author_id", book.getAuthor().getId()));

        namedParameterJdbcTemplate.update(sql, params, keyHolder);

        //noinspection DataFlowIssue
        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        String sql = "update books set title = :title, author_id = :author_id where id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource(
                Map.of("title", book.getTitle(),
                        "author_id", book.getAuthor().getId(),
                        "id", book.getId()));

        int rowUpdated = namedParameterJdbcTemplate.update(sql, params);
        if (rowUpdated == 0) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        String sql = "insert into books_genres (book_id, genre_id) values (:book_id, :genre_id)";
        SqlParameterSource[] params = book.getGenres().stream().map(
                genre -> {
                    var param = new MapSqlParameterSource();
                    param.addValue("book_id", book.getId());
                    param.addValue("genre_id", genre.getId());
                    return param;
                }).toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(sql, params);
    }

    private void removeGenresRelationsFor(Book book) {
        Map<String, Object> params = Collections.singletonMap("book_id", book.getId());
        String sql = "delete from books_genres where book_id = :book_id";
        namedParameterJdbcTemplate.update(sql, params);
    }

    private static class BookGenreRowMapper implements RowMapper<BookGenreRelation> {

        @Override
        public BookGenreRelation mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new BookGenreRelation(rs.getLong("book_id"), rs.getLong("genre_id"));
        }
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String title = rs.getString("title");
            Author author = new Author(rs.getLong("author_id"), rs.getString("full_name"));
            List<Genre> genres = new ArrayList<>();
            return new Book(id, title, author, genres);
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<Genre> genres = new ArrayList<>();
            while (rs.next()) {
                genres.add(new Genre(rs.getLong("genre_id"), rs.getString("genre_name")));
                if (rs.isLast()) {
                    long id = rs.getLong("id");
                    String title = rs.getString("title");
                    Author author = new Author(rs.getLong("author_id"), rs.getString("full_name"));
                    return new Book(id, title, author, genres);
                }
            }
            return null;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
