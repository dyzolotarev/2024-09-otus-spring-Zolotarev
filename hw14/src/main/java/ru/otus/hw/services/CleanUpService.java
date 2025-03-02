package ru.otus.hw.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CleanUpService {

    private final JdbcTemplate jdbcTemplate;

    public CleanUpService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @SuppressWarnings("unused")
    public void cleanUp() throws Exception {
        log.info("Очистка таблицы мэппинга, хотя по-хорошему ее имеет смысл какое-то время подержать.");
        log.info("Порой помогает разобраться, что откуда взялось...");
        jdbcTemplate.update("delete from item_mapping");
        log.info("Завершающие мероприятия закончены");
    }
}
