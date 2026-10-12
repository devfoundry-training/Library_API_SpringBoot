package uk.co.devfoundry.library.web;

import java.util.Comparator;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import uk.co.devfoundry.library.service.LibraryException;
import uk.co.devfoundry.library.service.NotFoundException;
import uk.co.devfoundry.library.service.RuleViolationException;
import uk.co.devfoundry.library.web.dto.ErrorResponse;

/** The one place that turns exceptions into {"error": {"code", "message"}}. No framework error body gets out. */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String BODY_MESSAGE = "body must be a JSON object";

    // -- the service layer's rules --------------------------------------

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(RuleViolationException.class)
    ResponseEntity<ErrorResponse> ruleViolation(RuleViolationException e) {
        return error(HttpStatus.CONFLICT, e);
    }

    // -- 400 validation_error -------------------------------------------

    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ErrorResponse> badRequest(BadRequestException e) {
        return validation(e.getMessage());
    }

    /** Bean Validation on a request record. The first problem by field name, "is required" first. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> invalidBody(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .min(Comparator.comparing(FieldError::getField)
                        .thenComparing(error -> !"NotNull".equals(error.getCode())))
                .map(FieldError::getDefaultMessage)
                .orElse(BODY_MESSAGE);
        return validation(message);
    }

    /** Not JSON, not an object, or a value of the wrong JSON type (Jackson is strict, see JacksonConfig). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> unreadableBody(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof MismatchedInputException mismatch && !mismatch.getPath().isEmpty()) {
            JsonMappingException.Reference field = mismatch.getPath().get(mismatch.getPath().size() - 1);
            if (field.getFieldName() != null) {
                return validation(field.getFieldName() + " " + expected(mismatch.getTargetType()));
            }
        }
        return validation(BODY_MESSAGE);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ErrorResponse> notJson(HttpMediaTypeNotSupportedException e) {
        return validation(BODY_MESSAGE);
    }

    // -- routing ---------------------------------------------------------

    /** A path id that is not a whole number (/books/abc) is an unknown route. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> typeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of("not_found", "not found"));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    ResponseEntity<ErrorResponse> noRoute(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of("not_found", "not found"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> methodNotAllowed(HttpRequestMethodNotSupportedException e) {
        HttpHeaders headers = new HttpHeaders();
        Set<?> allowed = e.getSupportedHttpMethods();
        if (allowed != null && !allowed.isEmpty()) {
            headers.setAllow(e.getSupportedHttpMethods());
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers)
                .body(ErrorResponse.of("method_not_allowed", "method not allowed"));
    }

    // -- anything else ---------------------------------------------------

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected(Exception e) {
        log.error("Unexpected problem", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("internal_error", "unexpected problem"));
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, LibraryException e) {
        return ResponseEntity.status(status).body(ErrorResponse.of(e.getCode(), e.getMessage()));
    }

    private static ResponseEntity<ErrorResponse> validation(String message) {
        return ResponseEntity.badRequest().body(ErrorResponse.of("validation_error", message));
    }

    private static String expected(Class<?> type) {
        if (type == Integer.class || type == int.class) {
            return "must be a whole number";
        }
        if (type == String.class) {
            return "must be text";
        }
        if (type == java.time.LocalDate.class) {
            return "must be a date in the form YYYY-MM-DD";
        }
        return "has the wrong type";
    }
}
