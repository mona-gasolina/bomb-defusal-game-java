package com.bomb.game;

import java.util.Random;

// The bomb itself: serial number, batteries, timer, strikes and how many puzzles are solved
public class Bomb {

    public enum State { RUNNING, DEFUSED, EXPLODED }

    public static final int MAX_STRIKES = 3;
    private static final Random random = new Random();

    private final String serial;
    private final int batteries;
    private final int total;
    private final long endTime;

    private int strikes;
    private int solved;
    private int timeAtEnd;
    private State state = State.RUNNING;

    public Bomb(int minutes, int total) {
        this.serial = makeSerial();
        this.batteries = random.nextInt(5);
        this.total = total;
        this.endTime = System.currentTimeMillis() + minutes * 60_000L;
    }

    // 5 random letters/digits, and the last one is always a digit.
    // There is no letter O, so it can't be mixed up with zero.
    private static String makeSerial() {
        String chars = "ABCDEFGHIJKLMNPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        sb.append(random.nextInt(10));
        return sb.toString();
    }

    public synchronized int secondsLeft() {
        if (state != State.RUNNING) {
            return timeAtEnd;
        }
        int left = (int) Math.max(0, (endTime - System.currentTimeMillis() + 999) / 1000);
        if (left == 0) {
            finish(State.EXPLODED, 0);
        }
        return left;
    }

    public synchronized void strike() {
        if (state != State.RUNNING) return;
        strikes++;
        if (strikes >= MAX_STRIKES) {
            finish(State.EXPLODED, secondsLeft());
        }
    }

    public synchronized void solved() {
        if (state != State.RUNNING) return;
        solved++;
        if (solved == total) {
            int left = secondsLeft();
            if (state == State.RUNNING) finish(State.DEFUSED, left);
        }
    }

    private void finish(State end, int left) {
        state = end;
        timeAtEnd = left;
    }

    public int lastDigit() {
        return serial.charAt(serial.length() - 1) - '0';
    }

    public boolean hasVowel() {
        return serial.matches(".*[AEIOU].*");
    }

    public String getSerial() { return serial; }
    public int getBatteries() { return batteries; }
    public int getTotal() { return total; }
    public synchronized int getStrikes() { return strikes; }
    public synchronized int getSolved() { return solved; }
    public synchronized State getState() { return state; }
}
