package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityDuplicateException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final BookCommentRepository bookCommentRepository;

    private final BookConverter bookConverter;

    @Override
    public Optional<BookDto> findById(String id) {
        return bookRepository.findById(id).map(bookConverter::bookToBookDto);
    }

    @Override
    public Optional<BookDto> findByTitle(String title) {
        return bookRepository.findByTitle(title).map(bookConverter::bookToBookDto);
    }

    @Override
    public List<BookDto> findAll() {
        return bookRepository.findAll().stream().map(bookConverter::bookToBookDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public BookDto insert(String title, String authorFullName, Set<String> genreNames) {
        return save(null, title, authorFullName, genreNames);
    }

    @Transactional
    @Override
    public BookDto update(String currentTitle, String title, String authorFullName, Set<String> genreNames) {
        var book = bookRepository.findByTitle(currentTitle).orElseThrow(() ->
                new EntityNotFoundException("Book with title %s not found".formatted(currentTitle)));
        return save(book.getId(), title, authorFullName, genreNames);
    }

    @Transactional
    @Override
    public void deleteById(String id) {
        bookRepository.deleteById(id);
        bookCommentRepository.deleteByBookId(id);
    }

    @Transactional
    @Override
    public void deleteByTitle(String title) {
        var book = bookRepository.deleteByTitle(title);
        bookCommentRepository.deleteByBookId(book.getId());
    }

    private BookDto save(String id, String title, String authorFullName, Set<String> genreNames) {
        if (isEmpty(genreNames)) {
            throw new IllegalArgumentException("Genres must not be null");
        }

        var author = authorRepository.findByFullName(authorFullName).orElseThrow(() ->
                new EntityNotFoundException("Author with full name %s not found".formatted(authorFullName)));
        var genres = genreRepository.findAllByNameIn(genreNames);
        if (isEmpty(genres) || genreNames.size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with names %s not found".formatted(genreNames));
        }

        try {
            var book = bookRepository.save(new Book(id, title, author, genres));
            return bookConverter.bookToBookDto(book);
        } catch (DuplicateKeyException e) {
            throw new EntityDuplicateException("A book with that title already exists.");
        }
    }
}
