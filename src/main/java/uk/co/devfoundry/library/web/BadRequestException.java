package uk.co.devfoundry.library.web;

/** A query value or header the API layer refuses: 400 validation_error with this message. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
