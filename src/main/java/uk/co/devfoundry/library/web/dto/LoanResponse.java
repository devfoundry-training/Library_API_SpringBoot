package uk.co.devfoundry.library.web.dto;

import java.time.LocalDate;

import uk.co.devfoundry.library.service.LoanView;

/** returned is written as null while the book is out. */
public record LoanResponse(int id, int bookId, int memberId, LocalDate borrowed, LocalDate due, LocalDate returned,
                           boolean overdue, int daysLate, int fine) {

    public static LoanResponse from(LoanView loan) {
        return new LoanResponse(loan.id(), loan.bookId(), loan.memberId(), loan.borrowed(), loan.due(),
                loan.returned(), loan.overdue(), loan.daysLate(), loan.fine());
    }
}
