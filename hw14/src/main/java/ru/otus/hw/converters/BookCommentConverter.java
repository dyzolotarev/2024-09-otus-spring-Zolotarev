package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.BookCommentDto;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.BookCommentV2;

@RequiredArgsConstructor
@Component
public class BookCommentConverter {

    private final BookConverter bookConverter;

    public String bookCommentToString(BookCommentDto bookComment) {
        return "Id: %s, Comment: %s".formatted(bookComment.getId(), bookComment.getComment());
    }

    public BookCommentDto bookCommentToBookCommentDto(BookComment bookComment) {
        BookCommentDto bookCommentDto = new BookCommentDto();
        bookCommentDto.setId(bookComment.getId());
        bookCommentDto.setComment(bookComment.getComment());
        return bookCommentDto;
    }

    public BookCommentV2 bookCommentToBookCommentV2(BookComment bookComment) {
        BookCommentV2 bookCommentV2 = new BookCommentV2();
        bookCommentV2.setId(0);
        bookCommentV2.setComment(bookComment.getComment());
        bookCommentV2.setBook(bookConverter.bookToBookV2(bookComment.getBook()));
        bookCommentV2.setMongoId(bookComment.getId());
        return bookCommentV2;
    }
}
