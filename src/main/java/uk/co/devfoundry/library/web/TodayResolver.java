package uk.co.devfoundry.library.web;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * The one place that answers "what is today", in this order:
 * 1. the X-Today header, only when TEST_MODE=1 (a badly formed value is 400),
 * 2. the LIBRARY_TODAY environment variable,
 * 3. the current date in Europe/London.
 * As an interceptor it runs before every endpoint, so a bad X-Today is a 400 everywhere. As an argument
 * resolver it hands the result to any controller method with a Today parameter.
 */
@Component
public class TodayResolver implements HandlerInterceptor, HandlerMethodArgumentResolver {

    public static final String HEADER = "X-Today";
    private static final String ATTRIBUTE = TodayResolver.class.getName();
    private static final Pattern DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    private final boolean testMode;
    private final LocalDate libraryToday;
    private final Clock clock;

    public TodayResolver(@Value("${library.test-mode:false}") boolean testMode,
                         @Value("${LIBRARY_TODAY:}") String libraryToday,
                         Clock clock) {
        this.testMode = testMode;
        this.clock = clock;
        if (libraryToday.isBlank()) {
            this.libraryToday = null;
        } else {
            this.libraryToday = parse(libraryToday);
            if (this.libraryToday == null) {
                throw new IllegalStateException("LIBRARY_TODAY must be a date in the form YYYY-MM-DD, got " + libraryToday);
            }
        }
    }

    public Today resolve(HttpServletRequest request) {
        if (testMode) {
            String header = request.getHeader(HEADER);
            if (header != null) {
                LocalDate date = parse(header);
                if (date == null) {
                    throw new BadRequestException("X-Today must be a date in the form YYYY-MM-DD");
                }
                return new Today(date);
            }
        }
        return new Today(libraryToday != null ? libraryToday : LocalDate.now(clock));
    }

    /** A real date written YYYY-MM-DD, or null. */
    static LocalDate parse(String text) {
        if (!DATE.matcher(text).matches()) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod) {
            request.setAttribute(ATTRIBUTE, resolve(request));
        }
        return true;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Today.class);
    }

    @Override
    public Today resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                 NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Object today = request.getAttribute(ATTRIBUTE);
        return today instanceof Today known ? known : resolve(request);
    }
}
