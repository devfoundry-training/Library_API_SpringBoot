package uk.co.devfoundry.library.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.co.devfoundry.library.domain.Book;
import uk.co.devfoundry.library.domain.Loan;
import uk.co.devfoundry.library.domain.Member;
import uk.co.devfoundry.library.repository.BookRepository;
import uk.co.devfoundry.library.repository.LoanRepository;
import uk.co.devfoundry.library.repository.MemberRepository;

/**
 * Borrowing, returning, due dates, overdue and fines. Today is always passed in: the service never reads
 * the clock. Due date and fine use the member's current type, so they are worked out every time.
 */
@Service
@Transactional
public class LoanService {

    private final LoanRepository loans;
    private final MemberRepository members;
    private final BookRepository books;

    public LoanService(LoanRepository loans, MemberRepository members, BookRepository books) {
        this.loans = loans;
        this.members = members;
        this.books = books;
    }

    /**
     * Every loan in id order. Each filter is optional (null means any): memberId, active (true for open,
     * false for returned) and overdue (true for open loans past their due date, false for the rest).
     * An unknown memberId is just a filter that matches nothing.
     */
    @Transactional(readOnly = true)
    public List<LoanView> list(Integer memberId, Boolean active, Boolean overdue, LocalDate today) {
        List<Loan> found = memberId == null ? loans.findAllByOrderByIdAsc() : loans.findByMember_IdOrderByIdAsc(memberId);
        return filter(found, active, overdue, today);
    }

    /** Like list, but for a member that must exist. */
    @Transactional(readOnly = true)
    public List<LoanView> listForMember(int memberId, Boolean active, Boolean overdue, LocalDate today) {
        if (!members.existsById(memberId)) {
            throw NotFoundException.member(memberId);
        }
        return filter(loans.findByMember_IdOrderByIdAsc(memberId), active, overdue, today);
    }

    @Transactional(readOnly = true)
    public LoanView get(int id, LocalDate today) {
        return view(find(id), today);
    }

    /**
     * The checks run in this order and the first failure decides the answer (task 07): the member exists,
     * the book exists, the member is under the policy limit, a copy is available.
     */
    public LoanView borrow(int memberId, int bookId, LocalDate today) {
        Member member = members.findById(memberId).orElseThrow(() -> NotFoundException.member(memberId));
        Book book = books.findById(bookId).orElseThrow(() -> NotFoundException.book(bookId));

        LoanPolicy policy = LoanPolicy.forMemberType(member.getType());
        long openLoans = loans.countByMember_IdAndReturnedIsNull(memberId);
        if (openLoans >= policy.maxLoans()) {
            throw RuleViolationException.loanLimitReached(member.getName(), openLoans, policy.maxLoans());
        }
        if (book.getCopies() - loans.countByBook_IdAndReturnedIsNull(bookId) <= 0) {
            throw RuleViolationException.noCopiesAvailable(book.getTitle());
        }
        return view(loans.save(new Loan(book, member, today)), today);
    }

    public LoanView returnLoan(int id, LocalDate today) {
        Loan loan = find(id);
        if (!loan.isOpen()) {
            throw RuleViolationException.alreadyReturned(id);
        }
        if (today.isBefore(loan.getBorrowed())) {
            throw RuleViolationException.returnBeforeBorrowed(id, loan.getBorrowed(), today);
        }
        loan.markReturned(today);
        return view(loan, today);
    }

    /**
     * due = borrowed + loan days. Days late: returned minus due for a returned loan, today minus due for an
     * open one, never below 0. Overdue: open and today is after the due date (due today is not overdue).
     */
    private static LoanView view(Loan loan, LocalDate today) {
        LoanPolicy policy = LoanPolicy.forMemberType(loan.getMember().getType());
        LocalDate due = policy.dueDate(loan.getBorrowed());
        LocalDate end = loan.isOpen() ? today : loan.getReturned();
        int daysLate = (int) Math.max(0, ChronoUnit.DAYS.between(due, end));
        boolean overdue = loan.isOpen() && today.isAfter(due);
        return new LoanView(loan.getId(), loan.getBook().getId(), loan.getMember().getId(), loan.getBorrowed(), due,
                loan.getReturned(), overdue, daysLate, policy.fine(daysLate));
    }

    private Loan find(int id) {
        return loans.findById(id).orElseThrow(() -> NotFoundException.loan(id));
    }

    private static List<LoanView> filter(List<Loan> found, Boolean active, Boolean overdue, LocalDate today) {
        return found.stream()
                .map(loan -> view(loan, today))
                .filter(loan -> active == null || active == (loan.returned() == null))
                .filter(loan -> overdue == null || overdue == loan.overdue())
                .toList();
    }
}
