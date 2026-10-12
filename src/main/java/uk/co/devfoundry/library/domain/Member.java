package uk.co.devfoundry.library.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    /** standard, student or staff: the service layer turns it into a LoanPolicy. */
    @Column(nullable = false, length = 10)
    private String type;

    @Column(nullable = false)
    private LocalDate joined;

    protected Member() {
        // for JPA
    }

    public Member(String name, String email, String type, LocalDate joined) {
        replace(name, email, type, joined);
    }

    public void replace(String name, String email, String type, LocalDate joined) {
        this.name = name;
        this.email = email;
        this.type = type;
        this.joined = joined;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getType() {
        return type;
    }

    public LocalDate getJoined() {
        return joined;
    }
}
