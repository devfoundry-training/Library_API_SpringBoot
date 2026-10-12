package uk.co.devfoundry.library.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Only the facts are stored. Due date, days late, fine and overdue are worked out by the service layer. */
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate borrowed;

    /** null while the book is still out. */
    @Column
    private LocalDate returned;

    protected Loan() {
        // for JPA
    }

    public Loan(Book book, Member member, LocalDate borrowed) {
        this.book = book;
        this.member = member;
        this.borrowed = borrowed;
    }

    public boolean isOpen() {
        return returned == null;
    }

    public void markReturned(LocalDate date) {
        this.returned = date;
    }

    public Integer getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getBorrowed() {
        return borrowed;
    }

    public LocalDate getReturned() {
        return returned;
    }
}
