package docSharing.utils;

import java.util.regex.Pattern;

public class ParametersValidator {

    private final static String EMAIL_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
            + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    private final static String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";

    public static boolean isCorrectEmail(String email) {
        return patternMatches(email, EMAIL_PATTERN);
    }

    public static boolean isCorrectPassword(String password) {
        return patternMatches(password, PASSWORD_PATTERN);
    }

    private static boolean patternMatches(String str, String regexPattern) {
        return Pattern.compile(regexPattern)
                .matcher(str)
                .matches();
    }
}
