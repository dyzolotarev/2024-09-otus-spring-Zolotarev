package ru.otus.hw.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "book_comments")
public class BookComment {
    @Id
    private String id;

    @DBRef
    private Book book;

    private String comment;

    public BookComment(Book book, String comment) {
        this.book = book;
        this.comment = comment;
    }
}
