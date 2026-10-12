package uk.co.devfoundry.library.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false, length = 50)
    private String genre;

    @Column(nullable = false)
    private int copies;

    @Column(nullable = false, length = 10)
    private String format;

    @Column(nullable = false)
    private int length;

    protected Book() {
        // for JPA
    }

    public Book(String title, String author, int year, String genre, int copies, String format, int length) {
        replace(title, author, year, genre, copies, format, length);
    }

    public void replace(String title, String author, int year, String genre, int copies, String format, int length) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.genre = genre;
        this.copies = copies;
        this.format = format;
        this.length = length;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getYear() {
        return year;
    }

    public String getGenre() {
        return genre;
    }

    public int getCopies() {
        return copies;
    }

    public String getFormat() {
        return format;
    }

    public int getLength() {
        return length;
    }
}
