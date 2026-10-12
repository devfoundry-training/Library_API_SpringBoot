package uk.co.devfoundry.library.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class BookServiceTest extends ServiceTestBase {

    private static final BookData PIRANESI = new BookData("Piranesi", "Susanna Clarke", 2020, "Fantasy", 2, "print", 272);

    @Test
    void availableIsCopiesMinusOpenLoans() {
        assertThat(books.get(4).available()).isEqualTo(3);   // Dune: 4 copies, loan 3 out
        assertThat(books.get(1).available()).isEqualTo(2);   // loan 1 was returned, loan 11 is out
        assertThat(books.get(5).available()).isZero();
        assertThat(books.get(3).available()).isZero();       // The Hobbit has no copies
    }

    @Test
    void searchMatchesTextAnywhereAndGenreWhole() {
        assertThat(ids(books.list("the", null, null))).containsExactly(3, 6, 9, 10, 11, 14, 17, 20);
        assertThat(ids(books.list(null, "orwell", null))).containsExactly(2, 7);
        assertThat(ids(books.list(null, null, "science fiction"))).containsExactly(4, 6, 9, 14, 20);
        assertThat(books.list(null, null, "science")).isEmpty();
        assertThat(ids(books.list(null, "wells", "Science Fiction"))).containsExactly(6, 20);
    }

    @Test
    void createGetsTheNextId() {
        BookView created = books.create(PIRANESI);

        assertThat(created.book().getId()).isEqualTo(21);
        assertThat(created.available()).isEqualTo(2);
    }

    @Test
    void copiesCannotGoBelowCopiesOnLoan() {
        BookData dune = new BookData("Dune", "Frank Herbert", 1965, "Science Fiction", 0, "print", 412);

        assertThatThrownBy(() -> books.update(4, dune))
                .isInstanceOf(RuleViolationException.class)
                .hasMessage("copies cannot be less than the 1 on loan");

        BookView updated = books.update(4, new BookData("Dune", "Frank Herbert", 1965, "Science Fiction", 1, "print", 412));
        assertThat(updated.available()).isZero();
    }

    @Test
    void bookWithLoansCannotBeDeleted() {
        assertThatThrownBy(() -> books.delete(1))
                .isInstanceOf(RuleViolationException.class)
                .hasMessage("Pride and Prejudice has loans and cannot be deleted");
        // only a returned loan, but the history still counts
        assertThatThrownBy(() -> books.delete(13)).hasMessage("Brave New World has loans and cannot be deleted");
    }

    @Test
    void bookWithoutLoansIsDeleted() {
        books.delete(8);

        assertThatThrownBy(() -> books.get(8))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("no book with id 8");
    }

    private static List<Integer> ids(List<BookView> found) {
        return found.stream().map(view -> view.book().getId()).toList();
    }
}
