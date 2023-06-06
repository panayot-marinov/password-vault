package bg.sofia.uni.fmi.mjt.password.vault.client.generator;

import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.DefaultPasswordGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.PasswordGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultPasswordGeneratorTest {

    private static final int SYMBOLS_FROM = 33;
    private static final int SYMBOLS_TO = 45;

    @Test
    public void testGeneratePasswordGeneratesEmptyCharArrayWhenLengthIsSetToZero() {
        PasswordGenerator passwordGenerator = DefaultPasswordGenerator.builder()
                .setPasswordLength(0)
                .build();

        assertArrayEquals(new char[]{}, passwordGenerator.generatePassword(),
                "Password generator should generate an empty char array when length is set to zero.");
    }

    @Test
    public void testGeneratePasswordGeneratesOnlyCapitalLettersWhenAllOtherParametersAreSetToFalse() {
        PasswordGenerator passwordGenerator = DefaultPasswordGenerator.builder()
                .setPasswordLength(8)
                .setShouldContainCapitalLetters(true)
                .setShouldContainNonCapitalLetters(false)
                .setShouldContainDigits(false)
                .setShouldContainSymbols(false)
                .build();

        char[] generatedPassword = passwordGenerator.generatePassword();
        assertEquals(8, generatedPassword.length, "Generated password length is not correct.");
        for (int i = 0; i < generatedPassword.length; i++) {
            assertTrue(generatedPassword[i] >= 'A' && generatedPassword[i] <= 'Z',
                    "Generated password should contain only capital letters.");
        }
    }

    @Test
    public void testGeneratePasswordGeneratesOnlyNonCapitalLettersWhenAllOtherParametersAreSetToFalse() {
        PasswordGenerator passwordGenerator = DefaultPasswordGenerator.builder()
                .setPasswordLength(1)
                .setShouldContainCapitalLetters(false)
                .setShouldContainNonCapitalLetters(true)
                .setShouldContainDigits(false)
                .setShouldContainSymbols(false)
                .build();

        char[] generatedPassword = passwordGenerator.generatePassword();
        assertEquals(1, generatedPassword.length, "Generated password length is not correct.");
        for (int i = 0; i < generatedPassword.length; i++) {
            assertTrue(generatedPassword[i] >= 'a' && generatedPassword[i] <= 'z',
                    "Generated password should contain only non-capital letters.");
        }
    }

    @Test
    public void testGeneratePasswordGeneratesOnlyDigitsWhenAllOtherParametersAreSetToFalse() {
        PasswordGenerator passwordGenerator = DefaultPasswordGenerator.builder()
                .setPasswordLength(128)
                .setShouldContainCapitalLetters(false)
                .setShouldContainNonCapitalLetters(false)
                .setShouldContainDigits(true)
                .setShouldContainSymbols(false)
                .build();

        char[] generatedPassword = passwordGenerator.generatePassword();
        assertEquals(128, generatedPassword.length, "Generated password length is not correct.");
        for (int i = 0; i < generatedPassword.length; i++) {
            assertTrue(generatedPassword[i] >= '0' && generatedPassword[i] <= '9',
                    "Generated password should contain only digit symbols.");
        }
    }

    @Test
    public void testGeneratePasswordGeneratesOnlySymbolsWhenAllOtherParametersAreSetToFalse() {
        PasswordGenerator passwordGenerator = DefaultPasswordGenerator.builder()
                .setPasswordLength(11)
                .setShouldContainCapitalLetters(false)
                .setShouldContainNonCapitalLetters(false)
                .setShouldContainDigits(false)
                .setShouldContainSymbols(true)
                .build();

        char[] generatedPassword = passwordGenerator.generatePassword();
        assertEquals(11, generatedPassword.length, "Generated password length is not correct.");
        for (int i = 0; i < generatedPassword.length; i++) {
            assertTrue(generatedPassword[i] >= SYMBOLS_FROM && generatedPassword[i] <= SYMBOLS_TO,
                    "Generated password should contain only symbols.");
        }
    }

    @Test
    public void testSetPasswordLengthThrowsIllegalArgumentExceptionWhenPasswordLengthIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> DefaultPasswordGenerator.builder()
                        .setPasswordLength(-1)
                        .build(), "SetPasswordLength should throw an exception when argument is negative.");
    }

}