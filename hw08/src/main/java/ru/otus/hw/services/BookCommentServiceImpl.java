package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.BookCommentConverter;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.repositories.BookCommentRepository;
import ru.otus.hw.repositories.BookRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BookCommentServiceImpl implements BookCommentService {

    private final BookCommentRepository bookCommentRepository;

    private final BookRepository bookRepository;

    private final BookCommentConverter bookCommentConverter;

    @Override
    @Transactional(readOnly = true)
    public Optional<BookCommentDto> findById(String id) {
        return bookCommentRepository.findById(id).map(bookCommentConverter::bookCommentToBookCommentDto);
    }

    @Transactional(readOnly = true)
    public List<BookCommentDto> findByBookId(String bookId) {
        return bookCommentRepository.findByBookId(bookId).stream()
                .map(bookCommentConverter::bookCommentToBookCommentDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookCommentDto> findByBookTitle(String bookTitle) {
        var book = bookRepository.findByTitle(bookTitle)
                .orElseThrow(() -> new EntityNotFoundException("Book with title %s not found".formatted(bookTitle)));
        return bookCommentRepository.findByBookId(book.getId()).stream()
                .map(bookCommentConverter::bookCommentToBookCommentDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookCommentDto insert(String bookTitle, String comment) {
        var book = bookRepository.findByTitle(bookTitle)
                .orElseThrow(() -> new EntityNotFoundException("Book with title %s not found".formatted(bookTitle)));
        return save(null, book, comment);
    }

    @Override
    @Transactional
    public BookCommentDto update(String id, String comment) {
        var bookComment = bookCommentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book comment with id %s not found".formatted(id)));
        return save(id, bookComment.getBook(), comment);
    }

    private BookCommentDto save (String id, Book book, String comment)  {
        var bookComment = bookCommentRepository.save(new BookComment(id, book, comment));
        return bookCommentConverter.bookCommentToBookCommentDto(bookComment);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        bookCommentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByBookTitle(String bookTitle) {
        var book = bookRepository.findByTitle(bookTitle)
                .orElseThrow(() -> new EntityNotFoundException("Book with title %s not found".formatted(bookTitle)));
        bookCommentRepository.deleteByBookId(book.getId());
    }
}
