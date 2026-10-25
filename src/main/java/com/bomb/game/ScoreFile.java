package com.bomb.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Saves every finished game to scores.csv (next to the jar) and reads the best ones back.
// Each line: date,team,result,secondsLeft,strikes
public final class ScoreFile {

    private static final File FILE = new File("scores.csv");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private ScoreFile() {
    }

    public static void save(String team, String result, int secondsLeft, int strikes) {
        String line = String.join(",", LocalDateTime.now().format(DATE), team.replace(",", " "),
                result, String.valueOf(secondsLeft), String.valueOf(strikes));
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE, true))) {
            out.println(line);
        } catch (IOException e) {
            System.err.println("Could not save the score: " + e.getMessage());
        }
    }

    // Defused games only, the ones with the most time left first
    public static List<String[]> best(int count) {
        List<String[]> rows = new ArrayList<>();
        if (!FILE.exists()) return rows;
        try (BufferedReader in = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5 && parts[2].equals("DEFUSED")) rows.add(parts);
            }
        } catch (IOException e) {
            System.err.println("Could not read scores: " + e.getMessage());
        }
        rows.sort((a, b) -> Integer.parseInt(b[3]) - Integer.parseInt(a[3]));
        return rows.subList(0, Math.min(count, rows.size()));
    }
}
