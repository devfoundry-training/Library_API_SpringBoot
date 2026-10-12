package uk.co.devfoundry.library.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import uk.co.devfoundry.library.domain.Member;

class MemberServiceTest extends ServiceTestBase {

    @Test
    void createWithoutJoinedJoinsToday() {
        Member created = members.create(new MemberData("Iris Murdoch", "iris.murdoch@example.com", "student", null),
                date("2026-10-20"));

        assertThat(created.getId()).isEqualTo(110);
        assertThat(created.getJoined()).isEqualTo(date("2026-10-20"));
    }

    @Test
    void duplicateEmailOnCreate() {
        MemberData data = new MemberData("Someone", "amira.khan@example.com", "standard", null);

        assertThatThrownBy(() -> members.create(data, TODAY))
                .isInstanceOf(RuleViolationException.class)
                .hasMessage("email amira.khan@example.com is already used by member 101");
    }

    @Test
    void duplicateEmailOnUpdate() {
        MemberData data = new MemberData("Ben Taylor", "ella.brown@example.com", "student", date("2024-09-02"));

        assertThatThrownBy(() -> members.update(102, data))
                .hasMessage("email ella.brown@example.com is already used by member 105");
    }

    @Test
    void keepingYourOwnEmailIsFine() {
        Member updated = members.update(102,
                new MemberData("Ben Taylor-Smith", "ben.taylor@example.com", "staff", date("2024-09-03")));

        assertThat(updated.getName()).isEqualTo("Ben Taylor-Smith");
        assertThat(updated.getType()).isEqualTo("staff");
    }

    @Test
    void unknownMember() {
        MemberData data = new MemberData("Nobody", "nobody@example.com", "standard", TODAY);

        assertThatThrownBy(() -> members.update(999, data))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("no member with id 999");
    }
}
