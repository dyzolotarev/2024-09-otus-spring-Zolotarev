package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BookForViewDto {
    private long id;

    private String title;

    private long authorId;

    private Set<Long> genreIds;
}
