package uk.co.devfoundry.library.service;

/**
 * A request the rules refuse. The code and message are part of the contract (RULES.md); the API layer
 * decides the HTTP status from the subclass.
 */
public abstract class LibraryException extends RuntimeException {

    private final String code;

    protected LibraryException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
