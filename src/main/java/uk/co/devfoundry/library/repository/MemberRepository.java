package uk.co.devfoundry.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.co.devfoundry.library.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    List<Member> findAllByOrderByIdAsc();

    Optional<Member> findByEmail(String email);
}
