package uk.co.devfoundry.library.web;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.co.devfoundry.library.service.LoanService;
import uk.co.devfoundry.library.service.MemberService;
import uk.co.devfoundry.library.web.dto.LoanResponse;
import uk.co.devfoundry.library.web.dto.MemberCreateRequest;
import uk.co.devfoundry.library.web.dto.MemberResponse;
import uk.co.devfoundry.library.web.dto.MemberUpdateRequest;

/** There is no DELETE: members cannot be deleted in the basic API, so it is 405. */
@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService members;
    private final LoanService loans;

    public MemberController(MemberService members, LoanService loans) {
        this.members = members;
        this.loans = loans;
    }

    @GetMapping
    public List<MemberResponse> list() {
        return members.list().stream().map(MemberResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberCreateRequest request, Today today) {
        MemberResponse created = MemberResponse.from(members.create(request.toData(), today.date()));
        return ResponseEntity.created(URI.create("/members/" + created.id())).body(created);
    }

    @GetMapping("/{memberId}")
    public MemberResponse get(@PathVariable int memberId) {
        return MemberResponse.from(members.get(memberId));
    }

    @PutMapping("/{memberId}")
    public MemberResponse update(@PathVariable int memberId, @Valid @RequestBody MemberUpdateRequest request) {
        return MemberResponse.from(members.update(memberId, request.toData()));
    }

    @GetMapping("/{memberId}/loans")
    public List<LoanResponse> loans(@PathVariable int memberId,
                                    @RequestParam(required = false) String active,
                                    @RequestParam(required = false) String overdue,
                                    Today today) {
        return loans.listForMember(memberId, QueryParams.trueOrFalse("active", active),
                        QueryParams.trueOrFalse("overdue", overdue), today.date())
                .stream().map(LoanResponse::from).toList();
    }
}
