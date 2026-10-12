package uk.co.devfoundry.library.service;

import java.time.LocalDate;

/** Every field of a member, already validated by the API layer. joined may be null on create. */
public record MemberData(String name, String email, String type, LocalDate joined) {
}
