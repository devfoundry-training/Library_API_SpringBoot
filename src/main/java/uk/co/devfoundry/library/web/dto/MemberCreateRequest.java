package uk.co.devfoundry.library.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import uk.co.devfoundry.library.service.MemberData;

/** The body of POST /members. joined is optional: the service makes it today. */
public record MemberCreateRequest(
        @NotNull(message = "name is required")
        @NotBlank(message = "name must not be blank")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        @NotNull(message = "email is required")
        @Size(max = 100, message = "email must be at most 100 characters")
        @Pattern(regexp = MemberUpdateRequest.EMAIL, message = "email must look like name@example.com")
        String email,

        @NotNull(message = "type is required")
        @Pattern(regexp = MemberUpdateRequest.TYPES, message = "member type must be standard, student or staff")
        String type,

        LocalDate joined) {

    public MemberData toData() {
        return new MemberData(name, email, type, joined);
    }
}
