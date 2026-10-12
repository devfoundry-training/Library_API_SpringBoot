package uk.co.devfoundry.library.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.co.devfoundry.library.web.dto.ErrorResponse;

/**
 * Replaces Spring Boot's /error page for the few errors that never reach a controller, for example a
 * request Tomcat itself refuses. They get the same error format as everything else.
 */
@RestController
public class FallbackErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ErrorResponse> error(HttpServletRequest request) {
        Object code = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = code instanceof Integer number ? number : HttpStatus.NOT_FOUND.value();
        ErrorResponse body = switch (status) {
            case 400 -> ErrorResponse.of("validation_error", "bad request");
            case 404 -> ErrorResponse.of("not_found", "not found");
            case 405 -> ErrorResponse.of("method_not_allowed", "method not allowed");
            default -> {
                status = HttpStatus.INTERNAL_SERVER_ERROR.value();
                yield ErrorResponse.of("internal_error", "unexpected problem");
            }
        };
        return ResponseEntity.status(status).body(body);
    }
}
