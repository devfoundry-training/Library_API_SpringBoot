package uk.co.devfoundry.library.web.dto;

import java.time.LocalDate;

import uk.co.devfoundry.library.domain.Member;

public record MemberResponse(int id, String name, String email, String type, LocalDate joined) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getEmail(), member.getType(),
                member.getJoined());
    }
}
