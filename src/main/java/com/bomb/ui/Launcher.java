package com.bomb.ui;

import com.bomb.BombApp;
import com.bomb.game.ScoreFile;
import com.bomb.server.GameService;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// The start screen: pick your role, team name and timer, see the best scores
public class Launcher extends JFrame {

    private final JRadioButton technician = radio("TECHNICIAN  -  I defuse the bomb", true);
    private final JRadioButton analyst = radio("ANALYST  -  I read the manual", false);
    private final JTextField team = field("TEAM ALPHA");
    private final JTextField hostIp = field("localhost");
    private final JComboBox<String> minutes = new JComboBox<>(new String[]{"5 minutes", "3 minutes", "7 minutes"});
    private final JButton start = Theme.button("START");
    private final JLabel info = Theme.label(" ", Theme.mono(14), Theme.KHAKI);

    public Launcher() {
        super("Bomb Defusal");
        Theme.init();
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        ButtonGroup roles = new ButtonGroup();
        roles.add(technician);
        roles.add(analyst);
        hostIp.setEnabled(false);
        // only the analyst needs the technician's IP; only the technician picks the timer
        analyst.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean isAnalyst = analyst.isSelected();
                hostIp.setEnabled(isAnalyst);
                minutes.setEnabled(!isAnalyst);
            }
        });
        minutes.setFont(Theme.mono(15));
        minutes.setBackground(Theme.PANEL_DARK);
        minutes.setForeground(Theme.TEXT);
        start.setFont(Theme.stencil(24));
        start.setPreferredSize(new Dimension(220, 46));
        start.addActionListener(e -> begin());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CASE);
        form.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Theme.METAL, 6), new EmptyBorder(20, 40, 20, 40)));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(4, 6, 4, 6);

        c.gridy = 0;
        form.add(Theme.label("BOMB DEFUSAL", Theme.stencil(52), Theme.KHAKI), c);
        c.gridy = 1;
        form.add(Theme.label("Keep talking. Nobody explodes.", Theme.mono(15), Theme.MUTED), c);
        c.gridy = 2;
        c.insets = new Insets(18, 6, 2, 6);
        form.add(technician, c);
        c.gridy = 3;
        c.insets = new Insets(2, 6, 12, 6);
        form.add(analyst, c);

        c.gridwidth = 1;
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.EAST;
        addField(form, c, 4, "TEAM NAME", team);
        addField(form, c, 5, "TIMER", minutes);
        addField(form, c, 6, "TECHNICIAN IP", hostIp);

        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.gridy = 7;
        form.add(Theme.label("This laptop's IP: " + myIp(), Theme.mono(13), Theme.MUTED), c);
        c.gridy = 8;
        c.insets = new Insets(14, 6, 6, 6);
        form.add(start, c);
        c.gridy = 9;
        c.insets = new Insets(2, 6, 10, 6);
        form.add(info, c);
        c.gridy = 10;
        form.add(Theme.label("BEST DEFUSALS", Theme.stencil(18), Theme.KHAKI), c);
        c.gridy = 11;
        form.add(scoreTable(), c);

        // centre the form in the window
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(Theme.BG);
        page.add(form);
        setContentPane(page);
        setSize(760, 760);
        setMinimumSize(new Dimension(680, 700));
        setLocationRelativeTo(null);
    }

    private void begin() {
        String name = team.getText().trim().isEmpty() ? "TEAM" : team.getText().trim();
        if (analyst.isSelected()) {
            new AnalystWindow(hostIp.getText().trim()).setVisible(true);
            dispose();
            return;
        }
        int mins = Integer.parseInt(((String) minutes.getSelectedItem()).split(" ")[0]);
        start.setEnabled(false);
        info.setText("Arming the bomb... (starting the server)");

        // Spring Boot takes a few seconds to start, so do it off the screen thread
        new SwingWorker<GameService, Void>() {
            @Override
            protected GameService doInBackground() {
                return BombApp.startServer();
            }

            @Override
            protected void done() {
                try {
                    new TechnicianWindow(get(), name, mins).setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    info.setText("Could not start the server. Is port 8080 already in use?");
                    start.setEnabled(true);
                }
            }
        }.execute();
    }

    private void addField(JPanel form, GridBagConstraints c, int row, String text, Component input) {
        c.gridy = row;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        form.add(Theme.label(text, Theme.stencil(16), Theme.TEXT), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        input.setPreferredSize(new Dimension(220, 30));
        form.add(input, c);
    }

    private JScrollPane scoreTable() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"TEAM", "TIME LEFT", "STRIKES", "DATE"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        for (String[] s : ScoreFile.best(5)) {
            int left = Integer.parseInt(s[3]);
            model.addRow(new Object[]{s[1], String.format("%02d:%02d", left / 60, left % 60), s[4], s[0]});
        }
        JTable table = new JTable(model);
        table.setFont(Theme.mono(13));
        table.setRowHeight(22);
        table.setBackground(Theme.PANEL_DARK);
        table.setForeground(Theme.TEXT);
        table.setGridColor(Theme.BORDER);
        table.getTableHeader().setFont(Theme.mono(13));
        table.getTableHeader().setBackground(Theme.PANEL);
        table.getTableHeader().setForeground(Theme.KHAKI);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Theme.PANEL_DARK);
        scroll.setBorder(new LineBorder(Theme.BORDER, 2));
        scroll.setPreferredSize(new Dimension(520, 136));
        return scroll;
    }

    private static JRadioButton radio(String text, boolean selected) {
        JRadioButton r = new JRadioButton(text, selected);
        r.setFont(Theme.stencil(18));
        r.setForeground(Theme.TEXT);
        r.setOpaque(false);
        r.setFocusPainted(false);
        return r;
    }

    private static JTextField field(String text) {
        JTextField f = new JTextField(text);
        f.setFont(Theme.mono(15));
        f.setBackground(Theme.PANEL_DARK);
        f.setForeground(Theme.TEXT);
        f.setCaretColor(Theme.TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Theme.BORDER, 2), new EmptyBorder(2, 6, 2, 6)));
        return f;
    }

    // All the local network addresses of this laptop (what the other laptop types in)
    private static String myIp() {
        List<String> ips = new ArrayList<>();
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                for (InetAddress a : Collections.list(ni.getInetAddresses())) {
                    if (a instanceof Inet4Address && a.isSiteLocalAddress()) ips.add(a.getHostAddress());
                }
            }
        } catch (SocketException ignored) {
            // no network: show the message below
        }
        return ips.isEmpty() ? "no network found" : String.join(" / ", ips);
    }
}
