package uk.co.devfoundry.library.service;

import java.time.LocalDate;

/** The loan rules for each member type (task 06). */
public enum LoanPolicy {

    STANDARD("standard", 14, 25, 3),
    STUDENT("student", 21, 10, 5),
    STAFF("staff", 28, 0, 10);

    private final String memberType;
    private final int loanDays;
    private final int finePerDay;
    private final int maxLoans;

    LoanPolicy(String memberType, int loanDays, int finePerDay, int maxLoans) {
        this.memberType = memberType;
        this.loanDays = loanDays;
        this.finePerDay = finePerDay;
        this.maxLoans = maxLoans;
    }

    public static LoanPolicy forMemberType(String memberType) {
        for (LoanPolicy policy : values()) {
            if (policy.memberType.equals(memberType)) {
                return policy;
            }
        }
        throw new IllegalArgumentException("unknown member type " + memberType);
    }

    public LocalDate dueDate(LocalDate borrowed) {
        return borrowed.plusDays(loanDays);
    }

    /** Whole pence. Staff are never fined. */
    public int fine(int daysLate) {
        return daysLate * finePerDay;
    }

    public int maxLoans() {
        return maxLoans;
    }

    public int loanDays() {
        return loanDays;
    }

    public int finePerDay() {
        return finePerDay;
    }
}
