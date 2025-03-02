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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "authors")
public class AuthorV2 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authors_seq")
    @SequenceGenerator(name = "authors_seq", sequenceName = "authors_seq", allocationSize = 1)
    private long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Transient
    private String mongoId;
}
