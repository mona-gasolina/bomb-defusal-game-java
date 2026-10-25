package com.bomb.ui;

import com.bomb.game.GameFiles;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

// The analyst's screen: the manual, plus the bomb's time and strikes from the technician's laptop
public class AnalystWindow extends JFrame {

    private final String statusUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final ObjectMapper json = new ObjectMapper();
    private final JLabel status = Theme.label("CONNECTING...", Theme.stencil(18), Theme.MUTED);
    private final JLabel timer = Theme.label("--:--", Theme.mono(44), Theme.LED);
    private final JLabel strikes = Theme.label("STRIKES  -", Theme.stencil(22), Theme.TEXT);
    private final CardLayout cards = new CardLayout();
    private final JPanel pages = new JPanel(cards);
    private final JList<String> sections;
    private final Timer poller;
    private boolean waiting;

    public AnalystWindow(String host) {
        super("Bomb Defusal - Analyst manual");
        // accept "192.168.1.5", "192.168.1.5:8080" or even "http://192.168.1.5/"
        String address = host.replace("http://", "").replaceAll("[/\\s]+", "");
        if (address.isEmpty()) address = "localhost";
        statusUrl = "http://" + (address.contains(":") ? address : address + ":8080") + "/api/status";

        Map<String, String> manual = GameFiles.manual();
        sections = new JList<>(manual.keySet().toArray(new String[0]));
        sections.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sections.setFont(Theme.stencil(18));
        sections.setBackground(Theme.PANEL_DARK);
        sections.setForeground(Theme.TEXT);
        sections.setSelectionBackground(Theme.KHAKI);
        sections.setSelectionForeground(Theme.SCREEN);
        sections.setFixedCellHeight(38);
        sections.setBorder(new EmptyBorder(8, 12, 8, 12));
        sections.addListSelectionListener(e -> {
            if (sections.getSelectedValue() != null) cards.show(pages, sections.getSelectedValue());
        });

        pages.setOpaque(false);
        manual.forEach((title, text) -> pages.add(page(title, text), title));
        sections.setSelectedIndex(0);

        JScrollPane listScroll = new JScrollPane(sections);
        listScroll.setBorder(new LineBorder(Theme.BORDER, 2));
        listScroll.setPreferredSize(new Dimension(220, 0));

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBackground(Theme.CASE);
        content.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Theme.METAL, 6), new EmptyBorder(16, 16, 16, 16)));
        content.add(topBar(), BorderLayout.NORTH);
        content.add(listScroll, BorderLayout.WEST);
        content.add(pages, BorderLayout.CENTER);
        setContentPane(content);
        setJMenuBar(menu(manual));

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                backToMenu();
            }
        });
        setMinimumSize(new Dimension(1000, 640));
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);
        setVisible(true);

        poller = new Timer(1000, e -> poll());
        poller.start();
        poll();
    }

    private JPanel topBar() {
        timer.setOpaque(true);
        timer.setBackground(Theme.SCREEN);
        timer.setBorder(new LineBorder(Theme.METAL, 4));
        timer.setPreferredSize(new Dimension(200, 62));
        JPanel bar = new JPanel(new GridLayout(1, 3, 16, 0));
        bar.setOpaque(false);
        bar.add(status);
        JPanel middle = new JPanel();
        middle.setOpaque(false);
        middle.add(timer);
        bar.add(middle);
        bar.add(strikes);
        return bar;
    }

    // One page of the manual. Some pages also show a table or the mazes from the data files.
    private JScrollPane page(String title, String text) {
        JTextArea area = new JTextArea(text + extra(title));
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.BOLD, 17));
        area.setBackground(Theme.PANEL_DARK);
        area.setForeground(Theme.TEXT);
        area.setBorder(new EmptyBorder(16, 22, 16, 22));

        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(Theme.PANEL_DARK);
        holder.add(area, BorderLayout.NORTH);
        if (title.equals("MAZE")) {
            MazeView mazes = new MazeView();
            mazes.setBackground(Theme.PANEL_DARK);
            holder.add(mazes, BorderLayout.CENTER);
        }
        JScrollPane scroll = new JScrollPane(holder);
        scroll.setBorder(new LineBorder(Theme.BORDER, 2));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private String extra(String title) {
        StringBuilder sb = new StringBuilder("\n\n");
        switch (title) {
            case "SYMBOLS" -> {
                sb.append("  SYMBOL = LETTER\n\n");
                int i = 0;
                for (Map.Entry<Character, String> e : GameFiles.symbols().entrySet()) {
                    sb.append(String.format("    %s = %c     ", e.getValue(), e.getKey()));
                    if (++i % 6 == 0) sb.append("\n\n");
                }
            }
            case "SWITCHES" -> {
                sb.append("  LIGHTS 1 2 3        SWITCHES 1 TO 5\n\n");
                GameFiles.switchTable().forEach((lights, switches) ->
                        sb.append(String.format("  %-18s  %s%n", lights, switches)));
            }
            case "COLOURS" -> {
                sb.append("  VOWEL?  STRIKES   RED      BLUE     GREEN    YELLOW\n\n");
                GameFiles.colourTable().forEach((key, row) -> {
                    String[] k = key.split(" ");
                    sb.append(String.format("  %-6s  %-8s  %-8s %-8s %-8s %-8s%n", k[0], k[1],
                            row.get("RED"), row.get("BLUE"), row.get("GREEN"), row.get("YELLOW")));
                });
                sb.append("\n  Example: vowel YES, 0 strikes, RED flashes  ->  press BLUE");
            }
            default -> {
                return "";
            }
        }
        return sb.toString();
    }

    private JMenuBar menu(Map<String, String> manual) {
        JMenuBar bar = new JMenuBar();
        JMenu pagesMenu = new JMenu("Manual");
        for (String title : manual.keySet()) {
            JMenuItem item = new JMenuItem(title);
            item.addActionListener(e -> sections.setSelectedValue(title, true));
            pagesMenu.add(item);
        }
        JMenu gameMenu = new JMenu("Game");
        JMenuItem back = new JMenuItem("Back to main menu");
        back.addActionListener(e -> backToMenu());
        gameMenu.add(back);
        bar.add(gameMenu);
        bar.add(pagesMenu);
        return bar;
    }

    // Asks the technician's laptop for the time and strikes (without freezing the screen)
    private void poll() {
        if (waiting) return;
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder(URI.create(statusUrl)).timeout(Duration.ofSeconds(2)).build();
        } catch (IllegalArgumentException badAddress) {
            setStatus("BAD IP ADDRESS", Theme.RED);
            return;
        }
        waiting = true;
        http.sendAsync(request, HttpResponse.BodyHandlers.ofString()).whenComplete((response, error) ->
                SwingUtilities.invokeLater(() -> {
                    waiting = false;
                    if (error != null || response.statusCode() != 200) offline();
                    else show(response.body());
                }));
    }

    private void show(String body) {
        try {
            JsonNode bomb = json.readTree(body);
            String state = bomb.get("state").asText();
            if (state.equals("WAITING")) {
                status.setText("CONNECTED - NO BOMB YET");
                return;
            }
            int left = bomb.get("timeLeft").asInt();
            timer.setText(String.format("%02d:%02d", left / 60, left % 60));
            strikes.setText("STRIKES  " + bomb.get("strikes").asInt() + " / 3");
            switch (state) {
                case "DEFUSED" -> setStatus("BOMB DEFUSED!", Theme.GREEN);
                case "EXPLODED" -> setStatus("BOOM! BOMB EXPLODED", Theme.RED);
                default -> setStatus("SOLVED " + bomb.get("solved").asInt() + " / " + bomb.get("total").asInt(), Theme.KHAKI);
            }
        } catch (Exception e) {
            offline();
        }
    }

    private void offline() {
        setStatus("NOT CONNECTED", Theme.RED);
        timer.setText("--:--");
    }

    private void setStatus(String text, Color colour) {
        status.setText(text);
        status.setForeground(colour);
    }

    private void backToMenu() {
        poller.stop();
        dispose();
        new Launcher();
    }
}
