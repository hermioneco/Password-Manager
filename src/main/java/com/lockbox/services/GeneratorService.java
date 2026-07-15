/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.lockbox.services;

import com.nulabinc.zxcvbn.Zxcvbn;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author ashie
 */
public class GeneratorService {

    private static final String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String lowerCase = "abcdefghijklmnopqrstuvwxyz";
    private static final String numbers = "1234567890";
    private static final String symbols = "!@#$%^&*{}[]|/<>()";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();


    public String generate(boolean useSymbols, int length) {
        if (length < 8 || length > 64) {
            throw new IllegalArgumentException(
                    "La longueur doit être entre 8 et 64 caractères");
        }
        StringBuilder passwordBuilder = new StringBuilder();
        passwordBuilder.append(randomCharFrom(upperCase.toCharArray()));
        passwordBuilder.append(randomCharFrom(lowerCase.toCharArray()));
        passwordBuilder.append(randomCharFrom(numbers.toCharArray()));
        if (useSymbols) {
            passwordBuilder.append(randomCharFrom(symbols.toCharArray()));
        }
        String pool = upperCase + lowerCase + numbers + symbols;
        int guaranteed = useSymbols ? 4 : 3;
        int remaining = length - guaranteed;
        for (int i = 0; i < remaining; i++) {
            passwordBuilder.append(randomCharFrom(pool.toCharArray()));
        }
        List<Character> characters = new ArrayList<>();
        for (char c : passwordBuilder.toString().toCharArray()) {
            characters.add(c);
        }
        Collections.shuffle(characters);

        StringBuilder shuffledString = new StringBuilder();
        for (char c : characters) {
            shuffledString.append(c);
        }

        return shuffledString.toString();

    }

    public char randomCharFrom(char[] pool) {
        return pool[SECURE_RANDOM.nextInt(pool.length)];
    }

}
