package uk.co.devfoundry.library.service;

/** The book, member or loan does not exist. */
public class NotFoundException extends LibraryException {

    private NotFoundException(String code, String message) {
        super(code, message);
    }

    public static NotFoundException book(int id) {
        return new NotFoundException("book_not_found", "no book with id " + id);
    }

    public static NotFoundException member(int id) {
        return new NotFoundException("member_not_found", "no member with id " + id);
    }

    public static NotFoundException loan(int id) {
        return new NotFoundException("loan_not_found", "no loan with id " + id);
    }
}
