package me.numenmc.pedrohack.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NamingUtils {
    public static String getDisplayName(String identifier) {
        return Arrays.stream(identifier.split("[-_]"))
                .filter(s -> !s.isEmpty())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
