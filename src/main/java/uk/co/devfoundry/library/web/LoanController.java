package uk.co.devfoundry.library.web;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.co.devfoundry.library.service.LoanService;
import uk.co.devfoundry.library.web.dto.LoanRequest;
import uk.co.devfoundry.library.web.dto.LoanResponse;

/** Loans cannot be changed or deleted, only returned: PUT and DELETE are 405. */
@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loans;

    public LoanController(LoanService loans) {
        this.loans = loans;
    }

    @GetMapping
    public List<LoanResponse> list(@RequestParam(name = "member_id", required = false) String memberId,
                                   @RequestParam(required = false) String active,
                                   @RequestParam(required = false) String overdue,
                                   Today today) {
        return loans.list(QueryParams.wholeNumber("member_id", memberId), QueryParams.trueOrFalse("active", active),
                        QueryParams.trueOrFalse("overdue", overdue), today.date())
                .stream().map(LoanResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<LoanResponse> borrow(@Valid @RequestBody LoanRequest request, Today today) {
        LoanResponse created = LoanResponse.from(loans.borrow(request.memberId(), request.bookId(), today.date()));
        return ResponseEntity.created(URI.create("/loans/" + created.id())).body(created);
    }

    @GetMapping("/{loanId}")
    public LoanResponse get(@PathVariable int loanId, Today today) {
        return LoanResponse.from(loans.get(loanId, today.date()));
    }

    @PostMapping("/{loanId}/return")
    public LoanResponse returnLoan(@PathVariable int loanId, Today today) {
        return LoanResponse.from(loans.returnLoan(loanId, today.date()));
    }
}
