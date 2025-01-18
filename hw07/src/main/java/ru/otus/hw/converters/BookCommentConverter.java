package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.models.BookComment;

@RequiredArgsConstructor
@Component
public class BookCommentConverter {

    private final BookConverter bookConverter;

    public String bookCommentToString(BookCommentDto bookComment) {
        return "Id: %d, Comment: %s".formatted(bookComment.getId(), bookComment.getComment());
    }

    public BookCommentDto bookCommentToBookCommentDto(BookComment bookComment) {
        BookCommentDto bookCommentDto = new BookCommentDto();
        bookCommentDto.setId(bookComment.getId());
        bookCommentDto.setBook(bookConverter.bookToBookDto(bookComment.getBook()));
        bookCommentDto.setComment(bookComment.getComment());
        return bookCommentDto;
    }

    public BookComment bookCommentDtoToBookComment(BookCommentDto bookCommentDto) {
        BookComment bookComment = new BookComment();
        bookComment.setId(bookCommentDto.getId());
        bookComment.setBook(bookConverter.bookDtoToBook(bookCommentDto.getBook()));
        bookComment.setComment(bookCommentDto.getComment());
        return bookComment;
    }
}
