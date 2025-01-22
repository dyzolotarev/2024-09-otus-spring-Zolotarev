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
    public Optional<BookCommentDto> findById(long id) {
        return bookCommentRepository.findById(id).map(bookCommentConverter::bookCommentToBookCommentDto);
    }

    @Override
    public List<BookCommentDto> findForBook(long bookId) {
        return bookCommentRepository.findByBookId(bookId).stream()
                .map(bookCommentConverter::bookCommentToBookCommentDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookCommentDto insert(long bookId, String comment) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(bookId)));
        return save(0, book, comment);
    }

    @Override
    @Transactional
    public BookCommentDto update(long id, String comment) {
        var bookComment = bookCommentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book comment with id %d not found".formatted(id)));
        return save(id, bookComment.getBook(), comment);
    }

    private BookCommentDto save (long id, Book book, String comment)  {
        var bookComment = bookCommentRepository.save(new BookComment(id, book, comment));
        return bookCommentConverter.bookCommentToBookCommentDto(bookComment);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        bookCommentRepository.deleteById(id);
    }
}
