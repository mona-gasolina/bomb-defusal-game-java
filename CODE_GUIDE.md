# Code Guide

A simple walk through the code: what each file does and how the parts fit together.

---

## The big picture

```
BombApp  ->  Launcher (start screen)
               |
               +-- TECHNICIAN -> starts Spring Boot -> TechnicianWindow (the bomb)
               |                                         |-- Bomb (timer, strikes)
               |                                         |-- 6 random Puzzles
               |
               +-- ANALYST    -> AnalystWindow (the manual)
                                  |-- asks the technician's laptop for
                                      GET /api/status every second
```

1. `BombApp.main` opens the **start screen**.
2. If you pick **Technician**, Spring Boot starts (with Tomcat on port 8080),
   a new `Bomb` is made and the bomb window opens.
3. If you pick **Analyst**, the manual window opens and asks the technician's
   laptop for the time and strikes every second.

---

## Folders

```
src/main/java/com/bomb/
    BombApp.java          main method, starts Spring Boot when needed
    server/               Spring Boot part (runs on the technician's laptop)
    game/                 game rules and file reading (no screens)
    ui/                   windows and screens
    puzzles/              the 8 puzzles
src/main/resources/
    application.properties   server port (8080)
    data/                     the text files the game reads
```

---

## server/ : Spring Boot

| File | What it does |
|---|---|
| `GameService` | A Spring `@Service` that holds the bomb being played right now. |
| `StatusController` | A `@RestController`. `GET /api/status` returns the time left, strikes, how many puzzles are solved and the state (RUNNING / DEFUSED / EXPLODED) as JSON. The analyst's window reads this. |
| `PingServlet` | A plain Java **servlet** (`@WebServlet("/ping")`). It says "Bomb server is running". Used to test the connection. |

Spring Boot runs these on its built-in **Apache Tomcat**.

---

## game/ : rules and files

| File | What it does |
|---|---|
| `Bomb` | The bomb: random serial number, batteries, timer, strikes and solved count. `strike()` adds a strike (3 = explode). `solved()` counts a solved puzzle (all solved = defused). `secondsLeft()` works out the time from the end time. |
| `GameFiles` | Reads the text files in `resources/data` with a `BufferedReader`. Turns them into lists and maps (the manual sections, symbol table, switch table, colour digits, words). |
| `Maze` | Loads the 6 mazes from `mazes.txt` and answers "is there a wall between these two cells?". |
| `ScoreFile` | Writes every finished game to `scores.csv` with a `FileWriter`, and reads the best ones back with a `FileReader`. |

**Why files?** The bomb and the manual read the **same** files, so the rules
can never disagree. To change a rule table, you just edit a text file.

---

## data/ : the text files

| File | What is inside |
|---|---|
| `manual.txt` | The analyst's manual. Each `## TITLE` line starts a new page. |
| `symbols.txt` | Which symbol stands for which letter (A Ψ, B Ж, ...). |
| `words.txt` | The 5-letter words for the Symbols puzzle. |
| `mazes.txt` | 6 mazes drawn with `#` for walls and `O` for circles. |
| `switches.txt` | Light pattern to switch positions. |
| `colours.txt` | The digit each light colour stands for (RED 3, BLUE 7, GREEN 1, YELLOW 9). |

---

## puzzles/ : the 8 puzzles

Every puzzle **extends `Puzzle`**. The parent class:

- draws the metal panel, the title and the status light,
- gives each puzzle `solve()` (light goes green, tells the bomb) and
  `strike()` (beep, red flash, tells the bomb),
- has a `body` panel where the puzzle puts its buttons, centred by `GridBagLayout`.

| Puzzle | How it works in code |
|---|---|
| `WiresPuzzle` | Random wire colours. `wireToCut()` follows the 5 manual rules in order. A `MouseAdapter` finds which wire was clicked. |
| `SymbolsPuzzle` | Picks a word, shows its symbols. If batteries > 2, the answer is the word backwards. |
| `KeypadPuzzle` | Directions are numbers 0 to 7 going clockwise. The real arrow is the shown arrow turned 2 steps (even serial) or 4 steps (odd serial). Arrows are drawn by `ArrowIcon`. |
| `ButtonPuzzle` | `decide()` follows the 6 manual rules. A `MouseAdapter` measures how long the button is held. Short clicks are counted as taps by a `Timer`. |
| `ShapesPuzzle` | 4 random shapes. A `Timer` flashes them one by one. `work()` does the maths (star +, circle -, triangle x). |
| `MazePuzzle` | A 6 x 6 grid of buttons. Clicking a cell next to you moves you, unless `Maze.blocked()` says there is a wall (strike). |
| `SwitchesPuzzle` | Random lights. Looks up the right switch positions in `switches.txt`. `JToggleButton`s with an `ItemListener`. |
| `ColourCodePuzzle` | 4 random coloured lights. The answer is their digits from `colours.txt`, typed backwards if the serial has a vowel. |

`TechnicianWindow` puts all 8 puzzle makers in a list, **shuffles** it and
uses the first 6. That is how each bomb is different.

---

## ui/ : the windows

| File | What it does |
|---|---|
| `Launcher` | Start screen: role (radio buttons in a `ButtonGroup`), team name, timer (`JComboBox`), technician IP, best scores (`JTable`). Starts Spring Boot in a `SwingWorker` so the screen does not freeze. |
| `TechnicianWindow` | The bomb. Top bar (timer, strikes, warning, serial sticker) plus a 2 x 3 `GridLayout` of puzzles. A `Timer` updates the clock 5 times a second and shows the end screen. |
| `AnalystWindow` | The manual. A `JList` of pages on the left, `CardLayout` on the right to switch pages. Asks `/api/status` every second with Java's `HttpClient`. |
| `MazeView` | Draws all 6 mazes, with walls, for the manual. |
| `BriefcasePanel` | Draws the briefcase background (handle, brackets, hazard stripes). |
| `SerialSticker` | Draws the yellow sticker with the serial number and batteries. |
| `Theme` | All colours and fonts in one place, plus helpers to make buttons and labels. |

---

## Syllabus topics: where to point

| Topic | Look at |
|---|---|
| Event listeners | `ActionListener` in every button, `ItemListener` in `SwitchesPuzzle` and `Launcher`, `ListSelectionListener` in `AnalystWindow` |
| Adapter classes | `MouseAdapter` in `WiresPuzzle` and `ButtonPuzzle`; `WindowAdapter` in both windows |
| Anonymous inner classes | `new ActionListener() {...}` in `KeypadPuzzle`, `new ItemListener() {...}` in `SwitchesPuzzle`, `new Icon() {...}` in `MazePuzzle` |
| Layout managers | `BorderLayout`, `GridLayout`, `GridBagLayout`, `FlowLayout`, `CardLayout` |
| Menus | `JMenuBar` in `TechnicianWindow` and `AnalystWindow` |
| Servlet + Tomcat | `PingServlet` running on Spring Boot's embedded Tomcat |
| Spring Boot | `BombApp`, `GameService`, `StatusController` |
| Files | `GameFiles` (reading), `ScoreFile` (writing and reading) |
