package uk.co.devfoundry.library.web.dto;

import uk.co.devfoundry.library.domain.Book;
import uk.co.devfoundry.library.service.BookView;

public record BookResponse(int id, String title, String author, int year, String genre, int copies, String format,
                           int length, int available) {

    public static BookResponse from(BookView view) {
        Book book = view.book();
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getYear(), book.getGenre(),
                book.getCopies(), book.getFormat(), book.getLength(), view.available());
    }
}
