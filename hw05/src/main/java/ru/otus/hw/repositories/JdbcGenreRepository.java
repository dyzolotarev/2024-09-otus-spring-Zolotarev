package ru.otus.hw.repositories;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;
import java.util.Map;

@Repository
public class JdbcGenreRepository implements GenreRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcGenreRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<Genre> findAll() {
        return namedParameterJdbcTemplate.query("select id, name from genres", new GnreRowMapper());
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        List<Genre> genres = new ArrayList<>();
        // Для отдельной книги много жанров не ожидаю, думаю допустимо их выдернуть поштучно
        for (Long id : ids) {
            findById(id).ifPresent(genres::add);
        }
        return genres;
    }

    @Override
    public Optional<Genre> findById(long id) {
        Map<String, Object> params = Collections.singletonMap("id", id);
        String sql = "select id, name from genres where id = :id";
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(sql, params, new GnreRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private static class GnreRowMapper implements RowMapper<Genre> {

        @Override
        public Genre mapRow(ResultSet rs, int i) throws SQLException {
            long id = rs.getLong("id");
            String name = rs.getString("name");
            return new Genre(id, name);
        }
    }
}
