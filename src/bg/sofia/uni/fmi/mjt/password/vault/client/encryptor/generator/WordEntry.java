package bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator;

import bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator.exceptions.InvalidWordEntryDataException;

import java.util.Arrays;

public class WordEntry {

    private static final String ONE_OR_MORE_SPACES_REGEX = "\\s+";
    private static final int WORD_ENTRY_ID_INDEX = 0;
    private static final int WORD_ENTRY_VALUE_INDEX = 1;
    private static final int WORD_ENTRY_PARAMETERS_COUNT = 2;

    private final int id;
    private final char[] word;

    private WordEntry(int id, char[] word) {
        this.id = id;
        this.word = word;
    }

    public static WordEntry of(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Line cannot be neither null nor blank.");
        }

        String[] tokens = line.split(ONE_OR_MORE_SPACES_REGEX);

        if (tokens.length != WORD_ENTRY_PARAMETERS_COUNT) {
            throw new InvalidWordEntryDataException("Invalid parameters count in data.");
        }

        int id = Integer.valueOf(tokens[WORD_ENTRY_ID_INDEX]);
        char[] word = tokens[WORD_ENTRY_VALUE_INDEX].toCharArray();

        return new WordEntry(id, word);
    }

    public int getId() {
        return id;
    }

    public char[] getWord() {
        return Arrays.copyOf(word, word.length);
    }

}