package uk.co.devfoundry.library.service;

import java.time.LocalDate;

/** A loan with everything the rules work out from today and the member's current policy. */
public record LoanView(int id, int bookId, int memberId, LocalDate borrowed, LocalDate due, LocalDate returned,
                       boolean overdue, int daysLate, int fine) {
}
