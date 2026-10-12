package uk.co.devfoundry.library.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import uk.co.devfoundry.library.service.BookData;

/** The body of POST /books and PUT /books/{id}. Wrapper types, so a missing field is null and reported. */
public record BookRequest(
        @NotNull(message = "title is required")
        @NotBlank(message = "title must not be blank")
        @Size(max = 100, message = "title must be at most 100 characters")
        String title,

        @NotNull(message = "author is required")
        @NotBlank(message = "author must not be blank")
        @Size(max = 100, message = "author must be at most 100 characters")
        String author,

        @NotNull(message = "year is required")
        @Min(value = 1450, message = "year must be between 1450 and 2100")
        @Max(value = 2100, message = "year must be between 1450 and 2100")
        Integer year,

        @NotNull(message = "genre is required")
        @NotBlank(message = "genre must not be blank")
        @Size(max = 50, message = "genre must be at most 50 characters")
        String genre,

        @NotNull(message = "copies is required")
        @Min(value = 0, message = "copies must not be negative")
        Integer copies,

        @NotNull(message = "format is required")
        @Pattern(regexp = "print|ebook|audio", message = "format must be print, ebook or audio, got '${validatedValue}'")
        String format,

        @NotNull(message = "length is required")
        @Min(value = 1, message = "length must be positive")
        Integer length) {

    public BookData toData() {
        return new BookData(title, author, year, genre, copies, format, length);
    }
}
