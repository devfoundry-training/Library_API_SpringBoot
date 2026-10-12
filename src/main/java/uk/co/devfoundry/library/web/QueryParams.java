package uk.co.devfoundry.library.web;

/** Strict parsing of query values: Spring would accept "TRUE", "1" or "yes" as true. */
final class QueryParams {

    private QueryParams() {
    }

    /** null when absent, otherwise exactly "true" or "false". */
    static Boolean trueOrFalse(String name, String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new BadRequestException(name + " must be true or false");
        };
    }

    /** null when absent, otherwise a whole number that fits in 32 bits. */
    static Integer wholeNumber(String name, String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException(name + " must be a whole number");
        }
    }
}
