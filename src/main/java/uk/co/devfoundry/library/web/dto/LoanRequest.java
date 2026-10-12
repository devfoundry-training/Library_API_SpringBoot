package uk.co.devfoundry.library.web.dto;

import jakarta.validation.constraints.NotNull;

/** The body of POST /loans. */
public record LoanRequest(
        @NotNull(message = "body must be a JSON object with whole-number member_id and book_id")
        Integer memberId,

        @NotNull(message = "body must be a JSON object with whole-number member_id and book_id")
        Integer bookId) {
}
