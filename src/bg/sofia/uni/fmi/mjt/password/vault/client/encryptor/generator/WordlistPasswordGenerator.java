package bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WordlistPasswordGenerator implements PasswordGenerator {

    private static final int NUMBER_SYSTEM = 10;
    private static final int DICE_MAX_VALUE = 6;
    private static final int DICE_MIN_VALUE = 1;

    private final Map<Integer, WordEntry> wordEntries;
    private final int wordsCount;
    private final int dicesCount;

    @Override
    public char[] generatePassword() {
        StringBuffer resultBuffer = new StringBuffer();

        for (int i = 0; i < wordsCount; i++) {
            int random = getRandomWithDices(dicesCount);
            char[] word = wordEntries.get(random).getWord();
            resultBuffer.append(word);
            resultBuffer.append(' ');
        }

        int resultLength = Math.max(resultBuffer.length() - 1, 0);
        char[] result = new char[resultLength];
        resultBuffer.getChars(0, resultLength, result, 0);
        resultBuffer.delete(0, resultBuffer.length());

        return result;
    }

    public static WordlistPasswordGeneratorBuilder builder() {
        return new WordlistPasswordGeneratorBuilder();
    }

    private WordlistPasswordGenerator(WordlistPasswordGeneratorBuilder builder) {
        this.wordEntries = readWordEntries(builder.wordlistReader);
        this.wordsCount = builder.wordsCount;
        this.dicesCount = builder.dicesCount;
    }

    private Map<Integer, WordEntry> readWordEntries(Reader wordlistReader) {
        Map<Integer, WordEntry> wordEntries = new HashMap<>();
        BufferedReader bufferedReader = new BufferedReader(wordlistReader);

        try {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                WordEntry wordEntry = WordEntry.of(line);
                wordEntries.put(wordEntry.getId(), wordEntry);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot retrieve word entries data.", e);
        }

        return wordEntries;
    }

    private int getRandomWithDices(int dices) {
        Random random = new Random();
        int nextNumber = random.nextInt(DICE_MAX_VALUE - DICE_MIN_VALUE) + DICE_MIN_VALUE;

        if (dices == 1) {
            return nextNumber;
        }
        return NUMBER_SYSTEM * getRandomWithDices(dices - 1) + nextNumber;
    }

    public static class WordlistPasswordGeneratorBuilder {

        private static final int DEFAULT_WORDS_COUNT = 10;
        private static final int DEFAULT_DICES_COUNT = 5;

        //required
        private Reader wordlistReader;

        //optional
        private int wordsCount = DEFAULT_WORDS_COUNT;
        private int dicesCount = DEFAULT_DICES_COUNT;

        private WordlistPasswordGeneratorBuilder() {
        }

        public WordlistPasswordGeneratorBuilder setWordlistReader(Reader wordlistReader) {
            if (wordlistReader == null) {
                throw new IllegalArgumentException("WordlistReader should not be null.");
            }

            this.wordlistReader = wordlistReader;
            return this;
        }

        public WordlistPasswordGeneratorBuilder setWordsCount(int wordsCount) {
            if (wordsCount < 0) {
                throw new IllegalArgumentException("WordsCount should have a non-negative value.");
            }

            this.wordsCount = wordsCount;
            return this;
        }

        public WordlistPasswordGeneratorBuilder setDicesCount(int dicesCount) {
            if (dicesCount < 0) {
                throw new IllegalArgumentException("DicesCount should have a non-negative value.");
            }

            this.dicesCount = dicesCount;
            return this;
        }

        public WordlistPasswordGenerator build() {
            if (wordlistReader == null) {
                throw new IllegalStateException("Wordlist reader is a required parameter and cannot be null.");
            }

            return new WordlistPasswordGenerator(this);
        }

    }

}