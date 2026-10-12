package uk.co.devfoundry.library.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.co.devfoundry.library.domain.Book;
import uk.co.devfoundry.library.repository.BookRepository;
import uk.co.devfoundry.library.repository.LoanRepository;

@Service
@Transactional
public class BookService {

    private final BookRepository books;
    private final LoanRepository loans;

    public BookService(BookRepository books, LoanRepository loans) {
        this.books = books;
        this.loans = loans;
    }

    /** Null filters are left out. Title and author: text anywhere. Genre: the whole genre. Ignoring case. */
    @Transactional(readOnly = true)
    public List<BookView> list(String title, String author, String genre) {
        Map<Integer, Long> onLoan = loans.openLoansPerBook();
        return books.search(title, author, genre).stream()
                .map(book -> view(book, onLoan.getOrDefault(book.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public BookView get(int id) {
        Book book = find(id);
        return view(book, onLoan(book));
    }

    public BookView create(BookData data) {
        Book book = books.save(new Book(data.title(), data.author(), data.year(), data.genre(), data.copies(),
                data.format(), data.length()));
        return view(book, 0);
    }

    /** Replaces every field. copies cannot go below the copies on loan. */
    public BookView update(int id, BookData data) {
        Book book = find(id);
        long onLoan = onLoan(book);
        if (data.copies() < onLoan) {
            throw RuleViolationException.copiesBelowOnLoan(onLoan);
        }
        book.replace(data.title(), data.author(), data.year(), data.genre(), data.copies(), data.format(),
                data.length());
        return view(book, onLoan);
    }

    /** A book that has ever been lent stays, so the loan history is complete. */
    public void delete(int id) {
        Book book = find(id);
        if (loans.existsByBook_Id(id)) {
            throw RuleViolationException.bookHasLoans(book.getTitle());
        }
        books.delete(book);
    }

    private Book find(int id) {
        return books.findById(id).orElseThrow(() -> NotFoundException.book(id));
    }

    private long onLoan(Book book) {
        return loans.countByBook_IdAndReturnedIsNull(book.getId());
    }

    private static BookView view(Book book, long onLoan) {
        return new BookView(book, (int) (book.getCopies() - onLoan));
    }
}
