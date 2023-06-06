package bg.sofia.uni.fmi.mjt.password.vault.client.encryptor.generator;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DefaultPasswordGenerator implements PasswordGenerator {

    private static final int SYMBOLS_FROM = 33;
    private static final int SYMBOLS_TO = 45;

    private final int passwordLength;
    private final boolean shouldContainNonCapitalLetters;
    private final boolean shouldContainCapitalLetters;
    private final boolean shouldContainDigits;
    private final boolean shouldContainSymbols;

    @Override
    public char[] generatePassword() {
        int digitsCount =
                shouldContainDigits && (passwordLength - 1 > 0) ?
                        getRandomInRange(0, passwordLength - 1) : 0;
        int symbolsCount =
                shouldContainSymbols && (passwordLength - digitsCount - 1 > 0) ?
                        getRandomInRange(0, passwordLength - digitsCount - 1) : 0;
        int nonCapitalLettersCount =
                shouldContainNonCapitalLetters && (passwordLength - digitsCount - symbolsCount - 1 > 0) ?
                        getRandomInRange(0, passwordLength - digitsCount - symbolsCount - 1) : 0;
        int capitalLettersCount =
                shouldContainCapitalLetters &&
                        (passwordLength - digitsCount - symbolsCount - nonCapitalLettersCount - 1) > 0 ?
                        getRandomInRange(
                                0, passwordLength - digitsCount - symbolsCount - nonCapitalLettersCount - 1) : 0;

        int remainder = (passwordLength - digitsCount - symbolsCount - nonCapitalLettersCount - capitalLettersCount);
        if (remainder > 0) {
            if (shouldContainDigits) {
                digitsCount += remainder;
            } else if (shouldContainSymbols) {
                symbolsCount += remainder;
            } else if (shouldContainNonCapitalLetters) {
                nonCapitalLettersCount += remainder;
            } else if (shouldContainCapitalLetters) {
                capitalLettersCount += remainder;
            }
        }

        Stream<Character> pwdStream = Stream.concat(getRandomChars(digitsCount, '0', '9'),
                Stream.concat(getRandomChars(symbolsCount, SYMBOLS_FROM, SYMBOLS_TO),
                        Stream.concat(
                                getRandomChars(nonCapitalLettersCount, 'a', 'z'),
                                getRandomChars(capitalLettersCount, 'A', 'Z'))));
        List<Character> charList = pwdStream.collect(Collectors.toList());
        Collections.shuffle(charList);

        char[] password = charList.stream()
                .map(ch -> ch.toString())
                .collect(Collectors.joining())
                .toCharArray();

        return password;
    }

    public static DefaultPasswordGeneratorBuilder builder() {
        return new DefaultPasswordGeneratorBuilder();
    }

    private DefaultPasswordGenerator(DefaultPasswordGeneratorBuilder builder) {
        this.passwordLength = builder.passwordLength;
        this.shouldContainCapitalLetters = builder.shouldContainCapitalLetters;
        this.shouldContainNonCapitalLetters = builder.shouldContainNonCapitalLetters;
        this.shouldContainDigits = builder.shouldContainDigits;
        this.shouldContainSymbols = builder.shouldContainSymbols;
    }

    private Stream<Character> getRandomChars(int count, int from, int to) {
        Random random = new SecureRandom();
        IntStream specialChars = random.ints(count, from, to);
        return specialChars.mapToObj(data -> (char) data);
    }

    private int getRandomInRange(int from, int to) {
        Random random = new Random();
        return random.nextInt(to - from) + from;
    }

    public static class DefaultPasswordGeneratorBuilder {


        private static final int DEFAULT_PASSWORD_LENGTH = 32;
        private static final boolean DEFAULT_SHOULD_CONTAIN_NON_CAPITAL_LETTERS = true;
        private static final boolean DEFAULT_SHOULD_CONTAIN_CAPITAL_LETTERS = true;
        private static final boolean DEFAULT_SHOULD_CONTAIN_DIGITS = true;
        private static final boolean DEFAULT_SHOULD_CONTAIN_SYMBOLS = true;

        //optional
        private int passwordLength = DEFAULT_PASSWORD_LENGTH;
        private boolean shouldContainNonCapitalLetters = DEFAULT_SHOULD_CONTAIN_NON_CAPITAL_LETTERS;
        private boolean shouldContainCapitalLetters = DEFAULT_SHOULD_CONTAIN_CAPITAL_LETTERS;
        private boolean shouldContainDigits = DEFAULT_SHOULD_CONTAIN_DIGITS;
        private boolean shouldContainSymbols = DEFAULT_SHOULD_CONTAIN_SYMBOLS;

        private DefaultPasswordGeneratorBuilder() {
        }

        public DefaultPasswordGeneratorBuilder setPasswordLength(int passwordLength) {
            if (passwordLength < 0) {
                throw new IllegalArgumentException("Password length should be a positive number.");
            }

            this.passwordLength = passwordLength;
            return this;
        }

        public DefaultPasswordGeneratorBuilder setShouldContainNonCapitalLetters(
                boolean shouldContainNonCapitalLetters) {
            this.shouldContainNonCapitalLetters = shouldContainNonCapitalLetters;
            return this;
        }

        public DefaultPasswordGeneratorBuilder setShouldContainCapitalLetters(boolean shouldContainCapitalLetters) {
            this.shouldContainCapitalLetters = shouldContainCapitalLetters;
            return this;
        }

        public DefaultPasswordGeneratorBuilder setShouldContainDigits(boolean shouldContainDigits) {
            this.shouldContainDigits = shouldContainDigits;
            return this;
        }

        public DefaultPasswordGeneratorBuilder setShouldContainSymbols(boolean shouldContainSymbols) {
            this.shouldContainSymbols = shouldContainSymbols;
            return this;
        }

        public DefaultPasswordGenerator build() {
            return new DefaultPasswordGenerator(this);
        }

    }

}