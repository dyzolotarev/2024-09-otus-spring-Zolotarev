package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BookForViewDto {
    private String id;

    private String title;

    private String authorId;

    private Set<String> genreIds;
}
