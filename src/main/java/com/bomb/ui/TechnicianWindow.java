package com.bomb.ui;

import com.bomb.game.Bomb;
import com.bomb.game.ScoreFile;
import com.bomb.puzzles.ButtonPuzzle;
import com.bomb.puzzles.ColourCodePuzzle;
import com.bomb.puzzles.KeypadPuzzle;
import com.bomb.puzzles.MazePuzzle;
import com.bomb.puzzles.Puzzle;
import com.bomb.puzzles.ShapesPuzzle;
import com.bomb.puzzles.SwitchesPuzzle;
import com.bomb.puzzles.SymbolsPuzzle;
import com.bomb.puzzles.WiresPuzzle;
import com.bomb.server.GameService;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

// The bomb, as the technician sees it
public class TechnicianWindow extends JFrame {

    private static final int PUZZLES_PER_BOMB = 6;

    private final GameService game;
    private final String team;
    private final int minutes;
    private final Bomb bomb;
    private final List<Puzzle> puzzles = new ArrayList<>();
    private final BriefcasePanel briefcase = new BriefcasePanel();
    private final JLabel timer = Theme.label("00:00", Theme.mono(52), Theme.LED);
    private final JLabel[] strikeBoxes = new JLabel[Bomb.MAX_STRIKES];
    private final Timer clock;
    private boolean over;

    public TechnicianWindow(GameService game, String team, int minutes) {
        super("Bomb Defusal - Technician (" + team + ")");
        this.game = game;
        this.team = team;
        this.minutes = minutes;
        this.bomb = game.newBomb(minutes, PUZZLES_PER_BOMB);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                leave();
            }
        });
        setJMenuBar(menu());

        briefcase.setLayout(new BorderLayout(0, 16));
        briefcase.add(topBar(), BorderLayout.NORTH);
        briefcase.add(puzzleGrid(), BorderLayout.CENTER);
        setContentPane(briefcase);

        setMinimumSize(new Dimension(1180, 700));
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);

        clock = new Timer(200, e -> tick());
        clock.start();
        tick();
    }

    // 6 puzzles picked at random from the 8
    private JPanel puzzleGrid() {
        List<Function<Bomb, Puzzle>> all = new ArrayList<>(List.of(
                WiresPuzzle::new, SymbolsPuzzle::new, KeypadPuzzle::new, ButtonPuzzle::new,
                ShapesPuzzle::new, MazePuzzle::new, SwitchesPuzzle::new, ColourCodePuzzle::new));
        Collections.shuffle(all);
        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setOpaque(false);
        for (int i = 0; i < PUZZLES_PER_BOMB; i++) {
            Puzzle p = all.get(i).apply(bomb);
            puzzles.add(p);
            grid.add(p);
        }
        return grid;
    }

    // Timer, strikes, warning text and the serial sticker
    private JPanel topBar() {
        timer.setOpaque(true);
        timer.setBackground(Theme.SCREEN);
        timer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Theme.METAL, 5), new EmptyBorder(0, 18, 0, 18)));
        timer.setPreferredSize(new Dimension(240, 78));

        JPanel strikes = new JPanel(new BorderLayout(0, 4));
        strikes.setOpaque(false);
        strikes.add(Theme.label("STRIKES", Theme.stencil(18), Theme.MUTED), BorderLayout.NORTH);
        JPanel boxes = new JPanel(new GridLayout(1, 3, 8, 0));
        boxes.setOpaque(false);
        for (int i = 0; i < strikeBoxes.length; i++) {
            strikeBoxes[i] = Theme.label("", Theme.plain(28), Theme.LED);
            strikeBoxes[i].setOpaque(true);
            strikeBoxes[i].setBackground(Theme.SCREEN);
            strikeBoxes[i].setBorder(new LineBorder(Theme.METAL, 3));
            strikeBoxes[i].setPreferredSize(new Dimension(42, 44));
            boxes.add(strikeBoxes[i]);
        }
        strikes.add(boxes, BorderLayout.CENTER);

        JPanel warning = new JPanel(new BorderLayout());
        warning.setOpaque(false);
        warning.add(Theme.label("DANGER", Theme.stencil(42), Theme.KHAKI), BorderLayout.CENTER);
        warning.add(Theme.label("HANDLE WITH CARE", Theme.stencil(17), Theme.KHAKI), BorderLayout.SOUTH);

        JPanel bar = new JPanel(new GridLayout(1, 4, 16, 0));
        bar.setOpaque(false);
        bar.add(centred(timer));
        bar.add(centred(strikes));
        bar.add(centred(warning));
        bar.add(centred(new SerialSticker(bomb.getSerial(), bomb.getBatteries())));
        return bar;
    }

    private static JPanel centred(Component c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    private JMenuBar menu() {
        JMenuBar bar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem restart = new JMenuItem("New bomb");
        restart.addActionListener(e -> {
            if (confirm("Throw this bomb away and start a new one?")) {
                close();
                new TechnicianWindow(game, team, minutes).setVisible(true);
            }
        });
        JMenuItem quit = new JMenuItem("Back to main menu");
        quit.addActionListener(e -> leave());
        gameMenu.add(restart);
        gameMenu.add(quit);

        JMenu help = new JMenu("Help");
        JMenuItem how = new JMenuItem("How to play");
        String howToPlay = """
                Describe the bomb to your analyst. They have the manual and tell you what to do.
                Solve all 6 puzzles before the timer runs out. 3 strikes and it explodes.

                The analyst connects to this laptop's IP (shown on the start screen).""";
        how.addActionListener(e -> JOptionPane.showMessageDialog(this, howToPlay, "How to play",
                JOptionPane.INFORMATION_MESSAGE));
        help.add(how);

        bar.add(gameMenu);
        bar.add(help);
        return bar;
    }

    private void tick() {
        int left = bomb.secondsLeft();
        timer.setText(String.format("%02d:%02d", left / 60, left % 60));
        // the timer blinks in the last 30 seconds
        boolean blink = left <= 30 && (System.currentTimeMillis() / 400) % 2 == 0;
        timer.setForeground(blink ? Theme.SCREEN : Theme.LED);
        for (int i = 0; i < strikeBoxes.length; i++) {
            strikeBoxes[i].setText(i < bomb.getStrikes() ? "X" : "");
        }
        if (bomb.getState() != Bomb.State.RUNNING && !over) {
            over = true;
            timer.setForeground(Theme.LED);
            endGame();
        }
    }

    private void endGame() {
        stopPuzzles();
        boolean won = bomb.getState() == Bomb.State.DEFUSED;
        int left = bomb.secondsLeft();
        ScoreFile.save(team, won ? "DEFUSED" : "EXPLODED", left, bomb.getStrikes());
        if (!won) briefcase.setExploded(true);

        JPanel message = new JPanel(new BorderLayout(0, 8));
        message.add(Theme.label(won ? "BOMB DEFUSED" : "BOOM!", Theme.stencil(54), won ? Theme.GREEN : Theme.RED),
                BorderLayout.CENTER);
        message.add(Theme.label(String.format("Time left %02d:%02d   |   Strikes %d", left / 60, left % 60,
                bomb.getStrikes()), Theme.mono(16), Theme.TEXT), BorderLayout.SOUTH);

        String[] options = {"PLAY AGAIN", "MAIN MENU"};
        int choice = JOptionPane.showOptionDialog(this, message, won ? "Defused" : "Exploded",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        close();
        if (choice == 0) {
            new TechnicianWindow(game, team, minutes).setVisible(true);
        } else {
            game.clear();
            new Launcher().setVisible(true);
        }
    }

    private void leave() {
        if (over || confirm("Leave this bomb and go back to the main menu?")) {
            close();
            game.clear();
            new Launcher().setVisible(true);
        }
    }

    private boolean confirm(String question) {
        return JOptionPane.showConfirmDialog(this, question, "Are you sure?", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    private void stopPuzzles() {
        for (Puzzle p : puzzles) p.stop();
    }

    private void close() {
        over = true;
        clock.stop();
        stopPuzzles();
        dispose();
    }
}
