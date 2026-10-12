package uk.co.devfoundry.library.web;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.co.devfoundry.library.service.BookService;
import uk.co.devfoundry.library.web.dto.BookRequest;
import uk.co.devfoundry.library.web.dto.BookResponse;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService books;

    public BookController(BookService books) {
        this.books = books;
    }

    @GetMapping
    public List<BookResponse> list(@RequestParam(required = false) String title,
                                   @RequestParam(required = false) String author,
                                   @RequestParam(required = false) String genre) {
        return books.list(title, author, genre).stream().map(BookResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        BookResponse created = BookResponse.from(books.create(request.toData()));
        return ResponseEntity.created(URI.create("/books/" + created.id())).body(created);
    }

    @GetMapping("/{bookId}")
    public BookResponse get(@PathVariable int bookId) {
        return BookResponse.from(books.get(bookId));
    }

    @PutMapping("/{bookId}")
    public BookResponse update(@PathVariable int bookId, @Valid @RequestBody BookRequest request) {
        return BookResponse.from(books.update(bookId, request.toData()));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> delete(@PathVariable int bookId) {
        books.delete(bookId);
        return ResponseEntity.noContent().build();
    }
}
