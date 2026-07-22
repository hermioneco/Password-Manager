package com.lockbox.services;

import com.nulabinc.zxcvbn.Zxcvbn;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GeneratorService {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "1234567890";
    private static final String SYMBOLS = "!@#$%^&*{}[]|/<>()";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Zxcvbn ZXCVBN = new Zxcvbn();

    public String generate(boolean useSymbols, int length) {
        if (length < 8 || length > 64) {
            throw new IllegalArgumentException("La longueur doit être entre 8 et 64 caractères");
        }
        StringBuilder passwordBuilder = new StringBuilder();
        passwordBuilder.append(randomCharFrom(UPPER.toCharArray()));
        passwordBuilder.append(randomCharFrom(LOWER.toCharArray()));
        passwordBuilder.append(randomCharFrom(NUMBERS.toCharArray()));
        if (useSymbols) {
            passwordBuilder.append(randomCharFrom(SYMBOLS.toCharArray()));
        }

        String pool = UPPER + LOWER + NUMBERS + (useSymbols ? SYMBOLS : "");
        int guaranteed = useSymbols ? 4 : 3;
        int remaining = length - guaranteed;
        for (int i = 0; i < remaining; i++) {
            passwordBuilder.append(randomCharFrom(pool.toCharArray()));
        }

        List<Character> characters = new ArrayList<>();
        for (char c : passwordBuilder.toString().toCharArray()) {
            characters.add(c);
        }
        Collections.shuffle(characters, SECURE_RANDOM);

        StringBuilder shuffledString = new StringBuilder();
        for (char c : characters) {
            shuffledString.append(c);
        }

        return shuffledString.toString();
    }

    public char randomCharFrom(char[] pool) {
        return pool[SECURE_RANDOM.nextInt(pool.length)];
    }

    public int getScore(String password) {
        if (password == null || password.isEmpty()) return 0;
        return ZXCVBN.measure(password).getScore();
    }
}
