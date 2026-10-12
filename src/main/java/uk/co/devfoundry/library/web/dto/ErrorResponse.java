package uk.co.devfoundry.library.web.dto;

/** The one error format: {"error": {"code": "...", "message": "..."}}. */
public record ErrorResponse(Detail error) {

    public record Detail(String code, String message) {
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(new Detail(code, message));
    }
}
