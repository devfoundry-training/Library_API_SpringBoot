package uk.co.devfoundry.library.web;

import java.time.LocalDate;

/** Today for one request. A controller method asks for it as a parameter; TodayResolver supplies it. */
public record Today(LocalDate date) {
}
