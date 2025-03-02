package ru.otus.hw.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToOne;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "book_comments")
public class BookCommentV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_comments_seq")
    @SequenceGenerator(name = "book_comments_seq", sequenceName = "book_comments_seq", allocationSize = 1)
    private long id;

    @ManyToOne(targetEntity = BookV2.class, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "book_id", nullable = false, referencedColumnName = "id")
    private BookV2 book;

    @Column(name = "comment", nullable = false)
    private String comment;

    @Transient
    private String mongoId;
}
