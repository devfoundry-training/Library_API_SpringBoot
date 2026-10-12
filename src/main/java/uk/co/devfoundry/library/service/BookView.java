package uk.co.devfoundry.library.service;

import uk.co.devfoundry.library.domain.Book;

/** A book with the copies available today: copies minus its open loans. */
public record BookView(Book book, int available) {
}
