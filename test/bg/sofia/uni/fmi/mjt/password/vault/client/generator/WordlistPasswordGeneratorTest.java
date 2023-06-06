package bg.sofia.uni.fmi.mjt.password.vault.client.generator;

import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.PasswordGenerator;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.WordlistPasswordGenerator;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WordlistPasswordGeneratorTest {

    private static final String WORDLIST =
            "1  word1" + System.lineSeparator() +
                    "2  word2" + System.lineSeparator() +
                    "3  word3" + System.lineSeparator() +
                    "4  word4" + System.lineSeparator() +
                    "5  word5" + System.lineSeparator() +
                    "6  word6";

    @Test
    public void testGeneratePasswordGeneratesEmptyArrayWhenWordsCountIsSetToZero() {
        PasswordGenerator passwordGenerator = WordlistPasswordGenerator.builder()
                .setWordlistReader(new StringReader(WORDLIST))
                .setWordsCount(0)
                .setDicesCount(1)
                .build();

        assertArrayEquals(new char[]{}, passwordGenerator.generatePassword(),
                "Password generator should generate an empty char array when words count is set to 0.");
    }

    @Test
    public void testGeneratePasswordGeneratesPasswordWithCorrectWordLengthWhenWordsCountIsNotZero() {
        PasswordGenerator passwordGenerator = WordlistPasswordGenerator.builder()
                .setWordlistReader(new StringReader(WORDLIST))
                .setWordsCount(10)
                .setDicesCount(1)
                .build();

        int expectedLength = 5 * 10 + 9; // length of word is 5 and we have nine spaces
        assertEquals(expectedLength, passwordGenerator.generatePassword().length,
                "Generated paasword does not have correct length when words count is set to 10.");
    }

    @Test
    public void testBuildThrowsIllegalStateExceptionWhenWordlistReaderIsNotSet() {
        assertThrows(IllegalStateException.class,
                () -> WordlistPasswordGenerator.builder()
                        .build(),
                "setWordListReader should throw an IllegalStateException when argument is null.");
    }

    @Test
    public void testSetWordlistReaderThrowsIllegalArgumentExceptionWhenWordlistReaderIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> WordlistPasswordGenerator.builder()
                        .setWordlistReader(null)
                        .build(),
                "setWordListReader should throw an IllegalArgumentException when argument is null.");
    }

    @Test
    public void testSetWordsCountThrowsIllegalArgumentExceptionWhenWordsCountIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> WordlistPasswordGenerator.builder()
                        .setWordsCount(-1)
                        .build(),
                "setWordsCount should throw an IllegalArgumentException when argument is negative.");
    }

    @Test
    public void testSetDicesCountThrowsIllegalArgumentExceptionWhenDicesCountIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> WordlistPasswordGenerator.builder()
                        .setDicesCount(-1)
                        .build(),
                "setDicesCount should throw an IllegalArgumentException when argument is negative.");
    }

}