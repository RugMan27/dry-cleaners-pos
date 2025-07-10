package xyz.rugman27.drycleanerspos.utilites;

import java.util.regex.*;

public class PhoneNumberUtil {

    // Simple country patterns (expandable)
    private static final Pattern US_10_DIGIT = Pattern.compile("^(\\d{10})$");
    private static final Pattern US_11_DIGIT = Pattern.compile("^1(\\d{10})$");
    private static final Pattern E164 = Pattern.compile("^\\+(\\d{1,3})(\\d+)$");
    private static final Pattern US_7_DIGIT = Pattern.compile("^\\d{7}$");

    /**
     * Normalize a phone number to E.164 format.
     * - Assumes US if 10 digits
     * - Retains other country codes if prefixed with +
     */
    public static String normalizeForStorage(String input) {
        if (input == null) return null;

        String digits = input.replaceAll("\\D", "");


        // If 7 digits, assume local (513 area code)
        if (US_7_DIGIT.matcher(digits).matches()) {
            digits = "513" + digits;
        }

        // US number, 10 digits
        if (US_10_DIGIT.matcher(digits).matches()) {
            return "+1" + digits;
        }

        // US number, 11 digits starting with 1
        if (US_11_DIGIT.matcher(digits).matches()) {
            return "+" + digits;
        }

        // Already in +[country][number] format
        if (input.startsWith("+") && E164.matcher(input).matches()) {
            return input;
        }

        // If unknown, just store as-is
        return "+" + digits;
    }

    /**
     * Converts E.164 format number into a readable format.
     * For US numbers, returns (XXX) XXX-XXXX
     * For others, splits into country code and local number.
     */
    public static String formatReadable(String normalized) {
        if (normalized == null || !normalized.startsWith("+")){
            return normalized;
        }

        Matcher matcher = E164.matcher(normalized);
        if (matcher.matches()) {
            String countryCode = matcher.group(1);
            String local = matcher.group(2);

            if (countryCode.equals("1") && local.length() == 10) {
                return String.format("(%s) %s-%s",
                        local.substring(0, 3),
                        local.substring(3, 6),
                        local.substring(6));
            } else {
                // Break international number into 3-4 digit blocks
                return "+" + countryCode + " " + splitInternational(local);
            }
        }
        return normalized;
    }

    // Splits international number into groups of 3 or 4 digits
    private static String splitInternational(String number) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < number.length(); ) {
            int next = Math.min(3 + (i % 2), number.length() - i);
            result.append(number, i, i + next).append(" ");
            i += next;
        }
        return result.toString().trim();
    }
}
