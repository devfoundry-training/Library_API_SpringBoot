package uk.co.devfoundry.library.service;

/** Every field of a book, already validated by the API layer. */
public record BookData(String title, String author, int year, String genre, int copies, String format, int length) {
}
