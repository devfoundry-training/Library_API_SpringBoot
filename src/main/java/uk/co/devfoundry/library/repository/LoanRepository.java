package uk.co.devfoundry.library.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import uk.co.devfoundry.library.domain.Loan;

public interface LoanRepository extends JpaRepository<Loan, Integer> {

    // The member comes with each loan: its type decides the due date and the fine.
    @EntityGraph(attributePaths = "member")
    List<Loan> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "member")
    List<Loan> findByMember_IdOrderByIdAsc(Integer memberId);

    long countByMember_IdAndReturnedIsNull(Integer memberId);

    long countByBook_IdAndReturnedIsNull(Integer bookId);

    boolean existsByBook_Id(Integer bookId);

    interface OpenLoanCount {
        Integer getBookId();

        long getOpenLoans();
    }

    @Query("select l.book.id as bookId, count(l) as openLoans from Loan l where l.returned is null group by l.book.id")
    List<OpenLoanCount> countOpenLoansPerBook();

    /** Book id to the number of its loans that have not been returned (books with none are missing). */
    default Map<Integer, Long> openLoansPerBook() {
        return countOpenLoansPerBook().stream()
                .collect(Collectors.toMap(OpenLoanCount::getBookId, OpenLoanCount::getOpenLoans));
    }
}
