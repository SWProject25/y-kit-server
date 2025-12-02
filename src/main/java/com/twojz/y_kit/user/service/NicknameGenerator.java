package com.twojz.y_kit.user.service;

import java.util.List;
import java.util.Random;

public class NicknameGenerator {
    private static final List<String> PREFIX = List.of(
            "청년이", "알뜰이", "절약이", "지킴이", "새싹이", "활동이"
    );

    private static final Random RANDOM = new Random();

    public static String generate() {
        String prefix = PREFIX.get(RANDOM.nextInt(PREFIX.size()));
        int number = 1000 + RANDOM.nextInt(9000); // 1000~9999
        return prefix + number;
    }
}
