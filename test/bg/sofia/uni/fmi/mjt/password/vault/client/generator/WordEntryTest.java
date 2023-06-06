package bg.sofia.uni.fmi.mjt.password.vault.client.generator;

import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.WordEntry;
import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.exceptions.InvalidWordEntryDataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WordEntryTest {
//    @Test
//    void testOfThrowsIllegalArgumentExceptionWhenLineIsNullOrBlank() {
//        assertThrows(IllegalArgumentException.class,
//                () -> WordEntry.of(null),
//                "Method does not throw IllegalArgumentException when line is null.");
//
//        assertThrows(IllegalArgumentException.class,
//                () -> WordEntry.of("  "),
//                "Method does not throw IllegalArgumentException when line is blank.");
//    }
//
//    @Test
//    void testOfThrowsInvalidWordEntryDataExceptionWhenLineHasThreeArguments() {
//        assertThrows(InvalidWordEntryDataException.class,
//                () -> WordEntry.of("1 2 3"),
//                "Method does not throw InvalidWordEntryDataException when line has 3 arguments.");
//    }
//
//    private void assertThrows(Class<InvalidWordEntryDataException> invalidWordEntryDataExceptionClass, Object o, String s) {
//
//    }
//
//    @Test
//    void testOfThrowsInvalidWordEntryDataExceptionWhenLineHasOneArguments() {
//        assertThrows(InvalidWordEntryDataException.class,
//                () -> WordEntry.of("1 2 3"),
//                "Method does not throw InvalidWordEntryDataException when line has 1 argument.");
//    }
    @Test
    void testOfCreatesCorrectWordEntryWhenArgumentsAreCorrect() {
        WordEntry wordEntry = WordEntry.of("1 word1");

        assertEquals(1, wordEntry.getId(), "WordEntry does not have a correct id.");
        assertArrayEquals("word1".toCharArray(), wordEntry.getWord(), "WordEntry does not have a correct word value.");
    }

}