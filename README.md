# Bomb Defusal

A two-player bomb defusal game made with **Java Swing** and **Spring Boot**.

One player is the **Technician**: they can see the bomb but have no manual.
The other is the **Analyst**: they have the manual but cannot see the bomb.
The two players talk to each other to defuse the bomb before the timer runs out.

---

## How to play

- The bomb has a **5 minute timer** (3 and 7 minutes can also be picked).
- Each bomb has **6 puzzles**, picked at random from 8.
- Each puzzle has a light that turns **green** when it is solved.
- Every mistake is a **strike**. **3 strikes and the bomb explodes.**
- Solve all 6 puzzles before the timer reaches 00:00 to win.

| Puzzle | What the technician sees |
|---|---|
| Wires | 3 to 6 coloured wires. Cut the right one. |
| Symbols | 5 strange symbols that hide a word. Type the word. |
| Keypad | 8 arrows and a screen of 4 "lying" arrows. Press the real ones. |
| Button | A big button. Press it, tap it or hold it, depending on the rules. |
| Shapes | Flashing coloured shapes. Do the maths and type the answer. |
| Maze | A grid with 2 circles and invisible walls. Walk from one circle to the other. |
| Switches | 3 lights and 5 switches. Set the switches the right way. |
| Colour Code | 4 coloured lights. Turn them into a 4-digit code and type it. |

The rules for all of them are in the analyst's manual (inside the game).

---

## Running the game

You need **Java 17 or newer** on both laptops.

```
java -jar bomb-defusal.jar
```

(or double-click `run.bat` after building)

### Two laptops

1. Connect both laptops to the **same network** (the same Wi-Fi or a LAN cable).
   Internet is **not** needed.
2. **Laptop 1** (Technician): start the game and pick **TECHNICIAN**.
   The start screen shows this laptop's IP, for example `192.168.1.5`.
   When Windows asks about the firewall, click **Allow** (private networks).
3. **Laptop 2** (Analyst): start the game, pick **ANALYST**, type laptop 1's IP
   and press **START**.
4. The analyst's screen shows the manual and the bomb's time and strikes.

To check the connection, open `http://<technician-ip>:8080/ping` in a browser on laptop 2.
It should say "Bomb server is running".

### One laptop (for testing)

Start the game twice. Pick **TECHNICIAN** in the first window and **ANALYST**
in the second window (leave the IP as `localhost`).

---

## Building from the code

```
mvn package
```

This makes `target/bomb-defusal.jar`. You can also open the folder in Eclipse or
IntelliJ as a Maven project and run `com.bomb.BombApp`.

---

## How it is built

```
Technician laptop                              Analyst laptop
+-----------------------------+               +---------------------------+
| Swing bomb window           |               | Swing manual window       |
|   6 puzzles, timer, strikes |               |   manual from manual.txt  |
|            |                |               |   time and strikes  <-----+-- asks every second
| Bomb (game state)           |               +---------------------------+
|            |                |    HTTP                    |
| Spring Boot + Tomcat  <-----+----------------------------+
|   GET /api/status (REST)    |   GET /api/status
|   GET /ping (Servlet)       |
+-----------------------------+
```

- The **game data** lives in plain text files in `src/main/resources/data`
  (manual, symbols, mazes, switch table, colour digits, words).
  The bomb and the manual read the same files, so they always match.
- **Scores** are saved to `scores.csv` next to the jar, and the best ones are shown on the start screen.

See **[CODE_GUIDE.md](CODE_GUIDE.md)** for a file-by-file explanation of the code.

---

## Syllabus topics used (Unit V)

| Topic | Where |
|---|---|
| Event handling, listeners | Buttons (`ActionListener`), toggles (`ItemListener`), list (`ListSelectionListener`) |
| Adapter classes | `MouseAdapter` (wires, big button), `WindowAdapter` (closing windows) |
| Anonymous inner classes | Keypad buttons, switches, maze circle icon, launcher role choice |
| AWT / Swing components | `JButton`, `JLabel`, `JRadioButton` + `ButtonGroup` (checkbox group), `JTextField`, `JTextArea`, `JComboBox` (choice), `JList`, `JMenuBar` / `JMenu`, `JPanel`, `JScrollPane` (scrollbar), `JToggleButton`, `JTable` |
| Layout managers | `BorderLayout`, `GridLayout`, `GridBagLayout`, `FlowLayout`, `CardLayout` |
| Java Servlets | `PingServlet` (`@WebServlet("/ping")`) |
| Apache Tomcat | Embedded Tomcat inside Spring Boot |
| Spring Boot | `@SpringBootApplication`, `@Service`, `@RestController` |
| File handling | `BufferedReader` for the data files, `FileWriter` / `FileReader` for `scores.csv` |
