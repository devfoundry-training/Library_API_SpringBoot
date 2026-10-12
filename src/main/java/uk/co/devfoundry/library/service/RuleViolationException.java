package uk.co.devfoundry.library.service;

import java.time.LocalDate;

/** The request is well formed but a business rule refuses it. The messages are exactly as in RULES.md. */
public class RuleViolationException extends LibraryException {

    private RuleViolationException(String code, String message) {
        super(code, message);
    }

    public static RuleViolationException loanLimitReached(String memberName, long openLoans, int limit) {
        return new RuleViolationException("loan_limit_reached",
                memberName + " already has " + openLoans + " loans (limit " + limit + ")");
    }

    public static RuleViolationException noCopiesAvailable(String title) {
        return new RuleViolationException("no_copies_available", title + " has no copies available");
    }

    public static RuleViolationException alreadyReturned(int loanId) {
        return new RuleViolationException("already_returned", "loan " + loanId + " has already been returned");
    }

    public static RuleViolationException returnBeforeBorrowed(int loanId, LocalDate borrowed, LocalDate today) {
        return new RuleViolationException("return_before_borrowed",
                "loan " + loanId + " was borrowed on " + borrowed + " and cannot be returned on " + today);
    }

    public static RuleViolationException copiesBelowOnLoan(long onLoan) {
        return new RuleViolationException("copies_below_on_loan",
                "copies cannot be less than the " + onLoan + " on loan");
    }

    public static RuleViolationException bookHasLoans(String title) {
        return new RuleViolationException("book_has_loans", title + " has loans and cannot be deleted");
    }

    public static RuleViolationException emailTaken(String email, int memberId) {
        return new RuleViolationException("email_taken",
                "email " + email + " is already used by member " + memberId);
    }
}
