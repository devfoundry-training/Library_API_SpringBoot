package uk.co.devfoundry.library.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import uk.co.devfoundry.library.domain.Book;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    /**
     * Books in id order. A null filter is left out. Title and author match text anywhere in the
     * field, genre matches the whole field, all ignoring case.
     */
    default List<Book> search(String titleContains, String authorContains, String genreEquals) {
        List<Specification<Book>> filters = new ArrayList<>();
        if (titleContains != null) {
            filters.add(contains("title", titleContains));
        }
        if (authorContains != null) {
            filters.add(contains("author", authorContains));
        }
        if (genreEquals != null) {
            String genre = genreEquals.toLowerCase(Locale.ROOT);
            filters.add((book, query, cb) -> cb.equal(cb.lower(book.get("genre")), genre));
        }
        return findAll(Specification.allOf(filters), Sort.by("id"));
    }

    private static Specification<Book> contains(String field, String text) {
        // Escape LIKE's own wildcards so a search for "50%" means the text "50%".
        String escaped = text.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return (book, query, cb) -> cb.like(cb.lower(book.get(field)), "%" + escaped + "%", '\\');
    }
}
