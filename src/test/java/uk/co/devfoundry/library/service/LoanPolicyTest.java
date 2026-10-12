package uk.co.devfoundry.library.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/** The policy table from task 06, with no database at all. */
class LoanPolicyTest {

    @Test
    void eachMemberTypeHasItsPolicy() {
        assertThat(LoanPolicy.forMemberType("standard")).isEqualTo(LoanPolicy.STANDARD);
        assertThat(LoanPolicy.forMemberType("student")).isEqualTo(LoanPolicy.STUDENT);
        assertThat(LoanPolicy.forMemberType("staff")).isEqualTo(LoanPolicy.STAFF);
    }

    @Test
    void loanDaysFinesAndLimits() {
        assertThat(LoanPolicy.STANDARD.dueDate(LocalDate.of(2026, 10, 12))).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(LoanPolicy.STANDARD.fine(1)).isEqualTo(25);
        assertThat(LoanPolicy.STUDENT.fine(10)).isEqualTo(100);
        assertThat(LoanPolicy.STAFF.fine(90)).isZero();
        assertThat(LoanPolicy.STANDARD.maxLoans()).isEqualTo(3);
        assertThat(LoanPolicy.STUDENT.maxLoans()).isEqualTo(5);
        assertThat(LoanPolicy.STAFF.maxLoans()).isEqualTo(10);
    }
}
