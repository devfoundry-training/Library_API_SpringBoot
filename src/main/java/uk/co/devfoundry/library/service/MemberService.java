package uk.co.devfoundry.library.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.co.devfoundry.library.domain.Member;
import uk.co.devfoundry.library.repository.MemberRepository;

@Service
@Transactional
public class MemberService {

    private final MemberRepository members;

    public MemberService(MemberRepository members) {
        this.members = members;
    }

    @Transactional(readOnly = true)
    public List<Member> list() {
        return members.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Member get(int id) {
        return find(id);
    }

    /** A member created without a joined date joined today. */
    public Member create(MemberData data, LocalDate today) {
        checkEmailIsFree(data.email(), null);
        LocalDate joined = data.joined() != null ? data.joined() : today;
        return members.save(new Member(data.name(), data.email(), data.type(), joined));
    }

    /** Replaces every field. Keeping your own email is fine; another member's is not. */
    public Member update(int id, MemberData data) {
        Member member = find(id);
        checkEmailIsFree(data.email(), id);
        member.replace(data.name(), data.email(), data.type(), data.joined());
        return member;
    }

    private Member find(int id) {
        return members.findById(id).orElseThrow(() -> NotFoundException.member(id));
    }

    private void checkEmailIsFree(String email, Integer memberId) {
        members.findByEmail(email)
                .filter(owner -> !owner.getId().equals(memberId))
                .ifPresent(owner -> {
                    throw RuleViolationException.emailTaken(email, owner.getId());
                });
    }
}
