package uk.co.devfoundry.library.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LoanServiceTest extends ServiceTestBase {

    @Nested
    class Borrowing {

        @Test
        void borrowReducesAvailable() {
            LoanView loan = loans.borrow(101, 4, TODAY);

            assertThat(loan.id()).isEqualTo(13);
            assertThat(loan.borrowed()).isEqualTo(TODAY);
            assertThat(loan.returned()).isNull();
            assertThat(books.get(4).available()).isEqualTo(2);
        }

        @Test
        void returnRestoresAvailable() {
            loans.returnLoan(4, TODAY);

            assertThat(books.get(5).available()).isEqualTo(1);
        }

        @Test
        void unknownMember() {
            assertThatThrownBy(() -> loans.borrow(999, 1, TODAY))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("no member with id 999");
        }

        @Test
        void unknownBook() {
            assertThatThrownBy(() -> loans.borrow(101, 99, TODAY))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("no book with id 99");
        }

        @Test
        void noCopiesAvailable() {
            assertThatThrownBy(() -> loans.borrow(101, 3, TODAY))
                    .isInstanceOf(RuleViolationException.class)
                    .hasMessage("The Hobbit has no copies available");
        }

        @Test
        void memberIsCheckedBeforeBook() {
            assertThatThrownBy(() -> loans.borrow(999, 99, TODAY)).hasMessage("no member with id 999");
        }

        @Test
        void standardLimitIs3() {
            loans.borrow(105, 2, TODAY);

            assertThatThrownBy(() -> loans.borrow(105, 7, TODAY))
                    .isInstanceOf(RuleViolationException.class)
                    .hasMessage("Ella Brown already has 3 loans (limit 3)");
        }

        @Test
        void studentLimitIs5() {
            for (int bookId : new int[] {1, 2, 4, 6}) {
                loans.borrow(102, bookId, TODAY);
            }

            assertThatThrownBy(() -> loans.borrow(102, 7, TODAY))
                    .hasMessage("Ben Taylor already has 5 loans (limit 5)");
        }

        @Test
        void limitIsCheckedBeforeCopies() {
            loans.borrow(105, 2, TODAY);

            assertThatThrownBy(() -> loans.borrow(105, 3, TODAY))
                    .hasMessage("Ella Brown already has 3 loans (limit 3)");
        }

        @Test
        void refusedBorrowChangesNothing() {
            assertThatThrownBy(() -> loans.borrow(101, 3, TODAY)).isInstanceOf(RuleViolationException.class);

            assertThat(loans.list(null, null, null, TODAY)).hasSize(12);
        }
    }

    @Nested
    class DatesAndFines {

        @Test
        void dueDatesFollowTheMemberPolicy() {
            assertThat(loans.borrow(101, 7, TODAY).due()).isEqualTo(date("2026-10-26"));
            assertThat(loans.borrow(102, 7, TODAY).due()).isEqualTo(date("2026-11-02"));
            assertThat(loans.borrow(103, 7, TODAY).due()).isEqualTo(date("2026-11-09"));
        }

        @Test
        void dueDateCrossesTheYearEnd() {
            assertThat(loans.borrow(102, 4, date("2026-12-20")).due()).isEqualTo(date("2027-01-10"));
        }

        @Test
        void dueDateInALeapYear() {
            assertThat(loans.borrow(101, 4, date("2028-02-20")).due()).isEqualTo(date("2028-03-05"));
        }

        @Test
        void notOverdueOnTheDueDateButOverdueTheDayAfter() {
            LoanView dueToday = loans.get(10, TODAY);
            assertThat(dueToday.overdue()).isFalse();
            assertThat(dueToday.daysLate()).isZero();

            LoanView nextDay = loans.get(10, date("2026-10-13"));
            assertThat(nextDay.overdue()).isTrue();
            assertThat(nextDay.daysLate()).isEqualTo(1);
            assertThat(nextDay.fine()).isEqualTo(10);
        }

        @Test
        void oneDayLateStandardIs25() {
            LoanView loan = loans.get(11, TODAY);

            assertThat(loan.daysLate()).isEqualTo(1);
            assertThat(loan.fine()).isEqualTo(25);
        }

        @Test
        void tenDaysLateStudentIs100() {
            // loan 8: Ben Taylor (student), due 2026-10-06
            LoanView loan = loans.get(8, date("2026-10-16"));

            assertThat(loan.daysLate()).isEqualTo(10);
            assertThat(loan.fine()).isEqualTo(100);
        }

        @Test
        void staffAreNeverFined() {
            LoanView loan = loans.get(3, date("2027-01-01"));

            assertThat(loan.overdue()).isTrue();
            assertThat(loan.daysLate()).isEqualTo(90);
            assertThat(loan.fine()).isZero();
        }

        @Test
        void returnedLoanKeepsTheFineFromItsReturnDate() {
            LoanView loan = loans.get(2, date("2027-01-01"));

            assertThat(loan.overdue()).isFalse();
            assertThat(loan.daysLate()).isEqualTo(4);
            assertThat(loan.fine()).isEqualTo(40);
        }

        @Test
        void sampleLoansMatchTheTask09Totals() {
            List<LoanView> all = loans.list(null, null, null, TODAY);

            int openFines = all.stream().filter(l -> l.returned() == null).mapToInt(LoanView::fine).sum();
            int returnedFines = all.stream().filter(l -> l.returned() != null).mapToInt(LoanView::fine).sum();
            assertThat(openFines).isEqualTo(525);
            assertThat(returnedFines).isEqualTo(90);
        }

        @Test
        void typeChangeMovesTheDueDate() {
            members.update(101, new MemberData("Amira Khan", "amira.khan@example.com", "staff", date("2023-01-15")));

            assertThat(loans.get(5, TODAY).due()).isEqualTo(date("2026-10-29"));
        }
    }

    @Nested
    class Returning {

        @Test
        void returnSetsTheDateAndTheFinalFine() {
            LoanView loan = loans.returnLoan(4, TODAY);

            assertThat(loan.returned()).isEqualTo(TODAY);
            assertThat(loan.overdue()).isFalse();
            assertThat(loan.daysLate()).isEqualTo(8);
            assertThat(loan.fine()).isEqualTo(200);
        }

        @Test
        void alreadyReturned() {
            assertThatThrownBy(() -> loans.returnLoan(1, TODAY))
                    .isInstanceOf(RuleViolationException.class)
                    .hasMessage("loan 1 has already been returned");
        }

        @Test
        void returnBeforeBorrowed() {
            assertThatThrownBy(() -> loans.returnLoan(7, date("2026-10-01")))
                    .isInstanceOf(RuleViolationException.class)
                    .hasMessage("loan 7 was borrowed on 2026-10-10 and cannot be returned on 2026-10-01");
        }

        @Test
        void unknownLoan() {
            assertThatThrownBy(() -> loans.returnLoan(99, TODAY))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("no loan with id 99");
        }

        @Test
        void returningFreesAPlaceUnderTheLimit() {
            loans.borrow(105, 2, TODAY);
            assertThatThrownBy(() -> loans.borrow(105, 7, TODAY)).isInstanceOf(RuleViolationException.class);

            loans.returnLoan(4, TODAY);

            assertThat(loans.borrow(105, 7, TODAY).memberId()).isEqualTo(105);
        }
    }

    @Nested
    class Filters {

        @Test
        void activeAndOverdue() {
            assertThat(ids(loans.list(null, true, null, TODAY))).containsExactly(3, 4, 5, 6, 7, 8, 10, 11);
            assertThat(ids(loans.list(null, false, null, TODAY))).containsExactly(1, 2, 9, 12);
            assertThat(ids(loans.list(null, null, true, TODAY))).containsExactly(3, 4, 6, 8, 11);
            assertThat(ids(loans.list(null, true, false, TODAY))).containsExactly(5, 7, 10);
        }

        @Test
        void unknownMemberIdIsAFilterButTheMemberRouteIs404() {
            assertThat(loans.list(999, null, null, TODAY)).isEmpty();
            assertThatThrownBy(() -> loans.listForMember(999, null, null, TODAY))
                    .hasMessage("no member with id 999");
        }

        private List<Integer> ids(List<LoanView> found) {
            return found.stream().map(LoanView::id).toList();
        }
    }
}
