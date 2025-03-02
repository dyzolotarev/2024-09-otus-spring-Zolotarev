package ru.otus.hw.mongock.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.BookCommentRepository;

import java.util.List;
import java.util.stream.IntStream;

@ChangeLog
public class DatabaseChangelog {

    private static final int COUNT_ENTITIES = 3;

    @ChangeSet(order = "001", id = "dropDb", author = "dzolotarev", runAlways = true)
    public void dropDb(MongoDatabase db) {
        db.drop();
        MongoCollection<Document> books = db.getCollection("books");
        Document index = new Document("title", 1);
        books.createIndex(index, new IndexOptions().unique(true));
    }

    @ChangeSet(order = "002", id = "insertAuthors", author = "dzolotarev")
    public void insertAuthors(AuthorRepository authorRepository) {
        var authors = IntStream.range(1, COUNT_ENTITIES + 1).boxed()
                .map(id -> new Author("Author_" + id)).toList();
        authorRepository.saveAll(authors);
    }

    @ChangeSet(order = "003", id = "insertGenres", author = "dzolotarev")
    public void insertGenres(GenreRepository genreRepository) {
        var genres = IntStream.range(1, COUNT_ENTITIES * 2 + 1).boxed()
                .map(id -> new Genre("Genre_" + id)).toList();
        genreRepository.saveAll(genres);
    }

    @ChangeSet(order = "004", id = "insertBooks", author = "dzolotarev")
    public void insertBooks(BookRepository bookRepository,
                            AuthorRepository authorRepository,
                            GenreRepository genreRepository) {
        var authors = authorRepository.findAll();
        var genres = genreRepository.findAll();
        var books = IntStream.range(1, COUNT_ENTITIES + 1).boxed()
                .map(id -> new Book("BookTitle_" + id, authors.get(id - 1)
                        , List.of(genres.get((id - 1) * 2), genres.get((id - 1) * 2 + 1)))).toList();
        bookRepository.saveAll(books);
    }

    @ChangeSet(order = "005", id = "insertBookComments", author = "dzolotarev")
    public void insertBookComments(BookRepository bookRepository, BookCommentRepository bookCommentRepository) {
        var books = bookRepository.findAll();
        var bookComments = IntStream.range(1, COUNT_ENTITIES * 2 + 1).boxed()
                .map(id -> new BookComment(books.get((id - 1) / 2), "comment" + id)).toList();
        bookCommentRepository.saveAll(bookComments);
    }
}

