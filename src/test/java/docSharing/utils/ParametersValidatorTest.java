package docSharing.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParametersValidatorTest {

    @Test
    void isCorrectEmailPositive() {
        String correctEmail = "abcdefg@hig.kl";
        boolean isCorrect = ParametersValidator.isCorrectEmail(correctEmail);
        assertTrue(isCorrect);
    }

    @Test
    void isCorrectEmailNegative() {
        String incorrectEmail = "abcdefghig.kl";
        boolean isCorrect = ParametersValidator.isCorrectEmail(incorrectEmail);
        assertFalse(isCorrect);
    }

    @Test
    void isCorrectPasswordPositive() {
        String correctPassword = "12aBt456";
        boolean isCorrect = ParametersValidator.isCorrectPassword(correctPassword);
        assertTrue(isCorrect);
    }

    @Test
    void isCorrectPasswordNegative() {
        String incorrectPassword = "123456";
        boolean isCorrect = ParametersValidator.isCorrectPassword(incorrectPassword);
        assertFalse(isCorrect);
    }
}
