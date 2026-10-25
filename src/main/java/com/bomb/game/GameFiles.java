package com.bomb.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Reads the text files in src/main/resources/data.
// The bomb and the analyst's manual read the SAME files, so they always agree.
public final class GameFiles {

    private GameFiles() {
    }

    public static List<String> lines(String fileName) {
        InputStream in = GameFiles.class.getResourceAsStream("/data/" + fileName);
        if (in == null) {
            throw new IllegalStateException("Missing data file: " + fileName);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // manual.txt is split into sections. A line starting with "## " starts a new section.
    public static Map<String, String> manual() {
        Map<String, String> sections = new LinkedHashMap<>();
        String title = null;
        StringBuilder text = new StringBuilder();
        for (String line : lines("manual.txt")) {
            if (line.startsWith("## ")) {
                if (title != null) sections.put(title, text.toString().trim());
                title = line.substring(3).trim();
                text.setLength(0);
            } else {
                text.append(line).append('\n');
            }
        }
        if (title != null) sections.put(title, text.toString().trim());
        return sections;
    }

    public static List<String> words() {
        return lines("words.txt").stream().map(String::trim).filter(w -> !w.isEmpty()).toList();
    }

    // symbols.txt lines look like "A Ψ"
    public static Map<Character, String> symbols() {
        Map<Character, String> map = new LinkedHashMap<>();
        for (String line : lines("symbols.txt")) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 2 && !line.startsWith("#")) {
                map.put(parts[0].charAt(0), parts[1]);
            }
        }
        return map;
    }

    // switches.txt lines look like "ON OFF ON | UP DOWN UP UP DOWN"
    public static Map<String, String> switchTable() {
        return table("switches.txt");
    }

    // colours.txt lines look like "RED | 3"
    public static Map<String, String> colourDigits() {
        return table("colours.txt");
    }

    // Reads "left | right" lines into a map. Lines starting with # are comments.
    private static Map<String, String> table(String fileName) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String line : lines(fileName)) {
            if (line.startsWith("#") || !line.contains("|")) continue;
            String[] parts = line.split("\\|");
            map.put(clean(parts[0]), clean(parts[1]));
        }
        return map;
    }

    private static String clean(String s) {
        return s.trim().replaceAll("\\s+", " ");
    }
}
