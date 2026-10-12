package uk.co.devfoundry.library.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    /** The real clock, in the library's time zone. Only the web layer's TodayResolver reads it. */
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Europe/London"));
    }
}
