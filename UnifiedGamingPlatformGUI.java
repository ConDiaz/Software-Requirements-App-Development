import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class UnifiedGamingPlatformGUI {

    // ─── Data Models ────────────────────────────────────────────────────────

    static class Player {
        String playerID, username, platform, password;
        List<String> ownedGameIDs = new ArrayList<>();
        Map<String, String> gameVersions = new HashMap<>();

        Player(String id, String username, String platform, String password) {
            this.playerID = id; this.username = username;
            this.platform = platform; this.password = password;
        }
    }

    static class Game {
        String gameID, title, genre, requiredVersion;
        Game(String id, String title, String genre, String version) {
            this.gameID = id; this.title = title;
            this.genre = genre; this.requiredVersion = version;
        }
    }

    static class Lobby {
        String lobbyID, gameID, gameTitle, hostID, hostName;
        int maxPlayers;
        List<String> playerIDs = new ArrayList<>();
        List<String> playerNames = new ArrayList<>();
        String status = "WAITING"; // WAITING, ACTIVE, FINISHED
        List<String[]> chatMessages = new ArrayList<>(); // [time, sender, platform, message]
    }

    // ─── Platform Data ──────────────────────────────────────────────────────

    static final Map<String, Player> PLAYERS = new LinkedHashMap<>();
    static final Map<String, Game>   GAMES   = new LinkedHashMap<>();
    static final Map<String, Lobby>  LOBBIES = new LinkedHashMap<>();

    static {
        Game g1 = new Game("g001", "CrossFire Arena",  "Action",   "2.4.1");
        Game g2 = new Game("g002", "Quest Realms",     "RPG",      "1.8.0");
        Game g3 = new Game("g003", "TacticsX",         "Strategy", "3.1.2");
        GAMES.put(g1.gameID, g1); GAMES.put(g2.gameID, g2); GAMES.put(g3.gameID, g3);

        Player p1 = new Player("p001", "Alex_PC",    "PC",          "pass");
        p1.ownedGameIDs.addAll(List.of("g001","g002"));
        p1.gameVersions.put("g001","2.4.1"); p1.gameVersions.put("g002","1.8.0");

        Player p2 = new Player("p002", "Maria_PS5",  "PlayStation", "pass");
        p2.ownedGameIDs.addAll(List.of("g001","g003"));
        p2.gameVersions.put("g001","2.4.1"); p2.gameVersions.put("g003","3.1.2");

        Player p3 = new Player("p003", "David_Xbox", "Xbox",        "pass");
        p3.ownedGameIDs.addAll(List.of("g001","g002"));
        p3.gameVersions.put("g001","2.3.0"); // OUTDATED — triggers alternate flow
        p3.gameVersions.put("g002","1.8.0");

        Player p4 = new Player("p004", "Sam_PC",     "PC",          "pass");
        p4.ownedGameIDs.addAll(List.of("g002","g003"));
        p4.gameVersions.put("g002","1.8.0"); p4.gameVersions.put("g003","3.1.2");

        PLAYERS.put(p1.playerID,p1); PLAYERS.put(p2.playerID,p2);
        PLAYERS.put(p3.playerID,p3); PLAYERS.put(p4.playerID,p4);
    }

    // ─── Colors & Fonts ─────────────────────────────────────────────────────

    static final Color BG_DARK   = new Color(20, 20, 30);
    static final Color BG_MID    = new Color(30, 30, 44);
    static final Color BG_CARD   = new Color(48, 48, 68);
    static final Color ACCENT    = new Color(99, 102, 241);
    static final Color TEXT_WHT  = Color.WHITE;
    static final Color TEXT_GRAY = new Color(170, 170, 190);
    static final Color GREEN     = new Color(74, 222, 128);
    static final Color ORANGE    = new Color(251, 146, 60);
    static final Color RED       = new Color(248, 113, 113);
    static final Color YELLOW    = new Color(250, 204, 21);

    static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD, 22);
    static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 15);
    static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 12);

    // ─── State ──────────────────────────────────────────────────────────────

    private JFrame mainFrame;
    private Player currentPlayer;

    public UnifiedGamingPlatformGUI() {
        mainFrame = new JFrame("Unified Gaming Platform");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1050, 720);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.getContentPane().setBackground(BG_DARK);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private void setPanel(JPanel p) {
        mainFrame.setContentPane(p);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private JPanel darkPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG_MID);
        return p;
    }

    private JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font); l.setForeground(color);
        return l;
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return b;
    }

    private JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        if (title != null) {
            JLabel t = label(title, FONT_HEADING, TEXT_WHT);
            t.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
            p.add(t, BorderLayout.NORTH);
        }
        return p;
    }

    private JPanel topBar(String subtitle) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = label("⬡ UNIFIED GAMING PLATFORM", FONT_HEADING, ACCENT);
        bar.add(title, BorderLayout.WEST);

        if (subtitle != null) {
            JLabel sub = label(subtitle, FONT_SMALL, TEXT_GRAY);
            bar.add(sub, BorderLayout.EAST);
        }
        return bar;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SCREEN 1 — LOGIN
    // ════════════════════════════════════════════════════════════════════════

    public void showLoginScreen() {
        JPanel root = darkPanel(new BorderLayout());
        root.add(topBar(null), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG_MID);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);

        JLabel h = label("Sign In", FONT_TITLE, TEXT_WHT);
        g.gridx=0; g.gridy=0; g.gridwidth=2; g.anchor=GridBagConstraints.CENTER;
        center.add(h, g);

        JLabel hint = label("Demo password for all accounts: pass", FONT_SMALL, TEXT_GRAY);
        g.gridy=1; center.add(hint, g);

        g.gridwidth=1; g.anchor=GridBagConstraints.EAST;
        g.gridx=0; g.gridy=2; center.add(label("Account:", FONT_BODY, TEXT_WHT), g);

        String[] options = PLAYERS.values().stream()
            .map(p -> p.username + " [" + p.platform + "]")
            .toArray(String[]::new);
        JComboBox<String> accountBox = new JComboBox<>(options);
        accountBox.setFont(FONT_BODY); accountBox.setPreferredSize(new Dimension(220, 32));
        g.gridx=1; g.anchor=GridBagConstraints.WEST; center.add(accountBox, g);

        g.gridx=0; g.gridy=3; g.anchor=GridBagConstraints.EAST;
        center.add(label("Password:", FONT_BODY, TEXT_WHT), g);
        JPasswordField pwField = new JPasswordField(15);
        pwField.setFont(FONT_BODY); pwField.setPreferredSize(new Dimension(220, 32));
        g.gridx=1; g.anchor=GridBagConstraints.WEST; center.add(pwField, g);

        JLabel errLabel = label("", FONT_SMALL, RED);
        g.gridx=0; g.gridy=4; g.gridwidth=2; g.anchor=GridBagConstraints.CENTER;
        center.add(errLabel, g);

        JButton loginBtn = styledButton("LOGIN →", ACCENT, Color.WHITE);
        g.gridy=5; center.add(loginBtn, g);

        // Action — validate password then proceed
        ActionListener doLogin = e -> {
            int idx = accountBox.getSelectedIndex();
            Player[] arr = PLAYERS.values().toArray(new Player[0]);
            Player selected = arr[idx];
            String pw = new String(pwField.getPassword());
            if (pw.equals(selected.password)) {
                currentPlayer = selected;
                showMainDashboard();
            } else {
                errLabel.setText("⚠  Incorrect password.");
            }
        };
        loginBtn.addActionListener(doLogin);
        pwField.addActionListener(doLogin);

        root.add(center, BorderLayout.CENTER);
        mainFrame.setContentPane(root);
        mainFrame.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SCREEN 2 — MAIN DASHBOARD
    // ════════════════════════════════════════════════════════════════════════

    public void showMainDashboard() {
        JPanel root = darkPanel(new BorderLayout());

        // Header
        JPanel header = topBar("Logged in as: " + currentPlayer.username +
                "  |  Platform: " + currentPlayer.platform);
        JButton logoutBtn = styledButton("Logout", BG_CARD, TEXT_GRAY);
        logoutBtn.addActionListener(e -> { currentPlayer = null; showLoginScreen(); });
        header.add(logoutBtn, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // 2x2 card grid
        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setBackground(BG_MID);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        grid.add(dashCard("① Verify Ownership",
            "Check game ownership & version compatibility across players",
            "Open →", e -> showVerifyOwnership()));

        grid.add(dashCard("② Shared Lobby",
            "Create or join a cross-platform multiplayer session",
            "Open →", e -> showLobbyManager()));

        grid.add(dashCard("③ Chat Channel",
            "Cross-platform text communication for active lobbies",
            "Open →", e -> showChatLobbyPicker()));

        grid.add(dashCard("④ Server Status",
            "Monitor latency, server health, and communication servers",
            "Open →", e -> showServerStatus()));

        root.add(grid, BorderLayout.CENTER);
        setPanel(root);
    }

    private JPanel dashCard(String title, String desc, String btnText, ActionListener action) {
        JPanel p = card(title);
        JLabel d = label("<html>" + desc + "</html>", FONT_BODY, TEXT_GRAY);
        p.add(d, BorderLayout.CENTER);
        JButton b = styledButton(btnText, ACCENT, Color.WHITE);
        b.addActionListener(action);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        bp.setBackground(BG_CARD);
        bp.add(b);
        p.add(bp, BorderLayout.SOUTH);
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SCREEN 3 — VERIFY GAME OWNERSHIP & COMPATIBILITY
    // ════════════════════════════════════════════════════════════════════════

    private void showVerifyOwnership() {
        JPanel root = darkPanel(new BorderLayout());
        root.add(topBar("① Verify Game Ownership & Compatibility"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBackground(BG_MID);
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Controls
        JPanel controls = card("Select Game & Players to Check");
        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(BG_CARD);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6); g.anchor = GridBagConstraints.WEST;

        g.gridx=0; g.gridy=0; inner.add(label("Game:", FONT_BODY, TEXT_WHT), g);
        String[] gameTitles = GAMES.values().stream()
            .map(gm -> gm.title + " (v" + gm.requiredVersion + ")")
            .toArray(String[]::new);
        JComboBox<String> gameBox = new JComboBox<>(gameTitles);
        gameBox.setFont(FONT_BODY);
        g.gridx=1; inner.add(gameBox, g);

        g.gridx=0; g.gridy=1; inner.add(label("Players:", FONT_BODY, TEXT_WHT), g);
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        checkboxPanel.setBackground(BG_CARD);
        List<JCheckBox> boxes = new ArrayList<>();
        for (Player p : PLAYERS.values()) {
            JCheckBox cb = new JCheckBox(p.username + " [" + p.platform + "]");
            cb.setFont(FONT_BODY); cb.setForeground(TEXT_WHT);
            cb.setBackground(BG_CARD); cb.setSelected(p == currentPlayer);
            boxes.add(cb); checkboxPanel.add(cb);
        }
        g.gridx=1; inner.add(checkboxPanel, g);

        JButton verifyBtn = styledButton("Run Verification →", GREEN, BG_DARK);
        g.gridx=0; g.gridy=2; g.gridwidth=2; inner.add(verifyBtn, g);
        controls.add(inner, BorderLayout.CENTER);

        // Results table
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Player", "Platform", "Game Version", "Required", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(FONT_BODY); table.setBackground(BG_CARD);
        table.setForeground(TEXT_WHT); table.setGridColor(BG_MID);
        table.setRowHeight(26); table.getTableHeader().setBackground(BG_DARK);
        table.getTableHeader().setForeground(TEXT_WHT);
        table.getTableHeader().setFont(FONT_BODY);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(BG_CARD);

        JLabel summaryLabel = label("", FONT_BODY, TEXT_WHT);
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        verifyBtn.addActionListener(e -> {
            model.setRowCount(0);
            List<String> gameIDs = new ArrayList<>(GAMES.keySet());
            String selGame = gameIDs.get(gameBox.getSelectedIndex());
            Game game = GAMES.get(selGame);

            List<Player> selected = new ArrayList<>();
            Player[] arr = PLAYERS.values().toArray(new Player[0]);
            for (int i = 0; i < boxes.size(); i++) {
                if (boxes.get(i).isSelected()) selected.add(arr[i]);
            }
            if (selected.isEmpty()) {
                summaryLabel.setForeground(RED);
                summaryLabel.setText("⚠  Select at least one player.");
                return;
            }

            boolean allOk = true;
            for (Player p : selected) {
                String status; String ver;
                if (!p.ownedGameIDs.contains(game.gameID)) {
                    status = "✗ NOT OWNED"; ver = "—"; allOk = false;
                } else {
                    ver = p.gameVersions.getOrDefault(game.gameID, "?");
                    if (ver.equals(game.requiredVersion)) status = "✓ VERIFIED";
                    else { status = "⚠ OUTDATED (update needed)"; allOk = false; }
                }
                model.addRow(new Object[]{p.username, p.platform, ver, game.requiredVersion, status});
            }
            if (allOk) {
                summaryLabel.setForeground(GREEN);
                summaryLabel.setText("✓ All selected players verified — ready to play!");
            } else {
                summaryLabel.setForeground(ORANGE);
                summaryLabel.setText("⚠  Some players must resolve issues before the session can begin.");
            }
        });

        JPanel resultsCard = card("Verification Results");
        resultsCard.add(scroll, BorderLayout.CENTER);
        resultsCard.add(summaryLabel, BorderLayout.SOUTH);

        content.add(controls, BorderLayout.NORTH);
        content.add(resultsCard, BorderLayout.CENTER);

        JButton back = styledButton("← Back", BG_CARD, TEXT_GRAY);
        back.addActionListener(e -> showMainDashboard());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.setBackground(BG_MID); south.add(back);
        content.add(south, BorderLayout.SOUTH);

        root.add(content, BorderLayout.CENTER);
        setPanel(root);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SCREEN 4 — LOBBY MANAGER (Create + Join)
    // ════════════════════════════════════════════════════════════════════════

    private void showLobbyManager() {
        JPanel root = darkPanel(new BorderLayout());
        root.add(topBar("② Shared Lobby Manager"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBackground(BG_MID);
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // ── Create Lobby ──
        JPanel createCard = card("Create a New Lobby");
        JPanel cf = new JPanel(new GridBagLayout());
        cf.setBackground(BG_CARD);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6); g.anchor = GridBagConstraints.WEST;

        g.gridx=0; g.gridy=0; cf.add(label("Game:", FONT_BODY, TEXT_WHT), g);
        // Only games the player owns
        List<String> ownedIds = new ArrayList<>();
        List<String> ownedTitles = new ArrayList<>();
        for (String gid : currentPlayer.ownedGameIDs) {
            ownedIds.add(gid); ownedTitles.add(GAMES.get(gid).title);
        }
        JComboBox<String> createGameBox = new JComboBox<>(ownedTitles.toArray(new String[0]));
        createGameBox.setFont(FONT_BODY);
        g.gridx=1; cf.add(createGameBox, g);

        g.gridx=0; g.gridy=1; cf.add(label("Max Players:", FONT_BODY, TEXT_WHT), g);
        JSpinner maxSpin = new JSpinner(new SpinnerNumberModel(4, 2, 8, 1));
        ((JSpinner.DefaultEditor) maxSpin.getEditor()).getTextField().setColumns(4);
        g.gridx=1; cf.add(maxSpin, g);

        JLabel createResult = label("", FONT_SMALL, GREEN);
        g.gridx=0; g.gridy=2; g.gridwidth=2; cf.add(createResult, g);

        JButton createBtn = styledButton("Create Lobby →", ACCENT, Color.WHITE);
        g.gridy=3; cf.add(createBtn, g);
        createCard.add(cf, BorderLayout.CENTER);

        // ── Open Lobbies List ──
        JPanel joinCard = card("Open Lobbies — Join by ID or Select");
        JPanel topJoin = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topJoin.setBackground(BG_CARD);
        topJoin.add(label("Lobby ID:", FONT_BODY, TEXT_WHT));
        JTextField lobbyIdField = new JTextField(10); lobbyIdField.setFont(FONT_MONO);
        topJoin.add(lobbyIdField);
        JButton joinByIdBtn = styledButton("Join by ID →", GREEN, BG_DARK);
        topJoin.add(joinByIdBtn);

        DefaultTableModel lobbyModel = new DefaultTableModel(
            new String[]{"Lobby ID", "Game", "Host", "Players", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable lobbyTable = new JTable(lobbyModel);
        lobbyTable.setFont(FONT_BODY); lobbyTable.setBackground(BG_CARD);
        lobbyTable.setForeground(TEXT_WHT); lobbyTable.setGridColor(BG_MID);
        lobbyTable.setRowHeight(26); lobbyTable.getTableHeader().setBackground(BG_DARK);
        lobbyTable.getTableHeader().setForeground(TEXT_WHT);
        lobbyTable.getTableHeader().setFont(FONT_BODY);
        JScrollPane lobbyScroll = new JScrollPane(lobbyTable);
        lobbyScroll.getViewport().setBackground(BG_CARD);

        JLabel joinResult = label("", FONT_SMALL, TEXT_WHT);

        JButton joinSelectedBtn = styledButton("Join Selected →", ACCENT, Color.WHITE);
        JButton enterLobbyBtn = styledButton("Open Lobby Chat →", GREEN, BG_DARK);
        enterLobbyBtn.setEnabled(false);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(BG_CARD);
        btnRow.add(joinSelectedBtn); btnRow.add(enterLobbyBtn); btnRow.add(joinResult);

        JPanel joinInner = new JPanel(new BorderLayout(4, 8));
        joinInner.setBackground(BG_CARD);
        joinInner.add(topJoin, BorderLayout.NORTH);
        joinInner.add(lobbyScroll, BorderLayout.CENTER);
        joinInner.add(btnRow, BorderLayout.SOUTH);
        joinCard.add(joinInner, BorderLayout.CENTER);

        // ── Refresh lobby table ──
        Runnable refreshTable = () -> {
            lobbyModel.setRowCount(0);
            for (Lobby lb : LOBBIES.values()) {
                if (lb.status.equals("WAITING")) {
                    lobbyModel.addRow(new Object[]{
                        lb.lobbyID, lb.gameTitle, lb.hostName,
                        lb.playerIDs.size() + "/" + lb.maxPlayers, lb.status
                    });
                }
            }
        };
        refreshTable.run();

        // ── Create logic ──
        createBtn.addActionListener(e -> {
            if (ownedIds.isEmpty()) {
                createResult.setForeground(RED);
                createResult.setText("✗ You do not own any games.");
                return;
            }
            String gid = ownedIds.get(createGameBox.getSelectedIndex());
            Game game = GAMES.get(gid);
            String lid = UUID.randomUUID().toString().substring(0,6).toUpperCase();
            Lobby lb = new Lobby();
            lb.lobbyID = lid; lb.gameID = gid; lb.gameTitle = game.title;
            lb.hostID = currentPlayer.playerID; lb.hostName = currentPlayer.username;
            lb.maxPlayers = (int) maxSpin.getValue();
            lb.playerIDs.add(currentPlayer.playerID);
            lb.playerNames.add(currentPlayer.username);
            lb.chatMessages.add(new String[]{now(), "System", "", "Lobby created. Waiting for players..."});
            LOBBIES.put(lid, lb);
            createResult.setForeground(GREEN);
            createResult.setText("✓ Lobby created! ID: " + lid);
            refreshTable.run();
        });

        // ── Join by ID logic ──
        ActionListener doJoinById = e -> {
            String lid = lobbyIdField.getText().trim().toUpperCase();
            String err = tryJoin(lid);
            if (err == null) {
                joinResult.setForeground(GREEN);
                joinResult.setText("✓ Joined lobby " + lid + "!");
                enterLobbyBtn.setEnabled(true);
                enterLobbyBtn.putClientProperty("lid", lid);
                refreshTable.run();
            } else {
                joinResult.setForeground(RED);
                joinResult.setText("✗ " + err);
                enterLobbyBtn.setEnabled(false);
            }
        };
        joinByIdBtn.addActionListener(doJoinById);
        lobbyIdField.addActionListener(doJoinById);

        // ── Join selected row ──
        joinSelectedBtn.addActionListener(e -> {
            int row = lobbyTable.getSelectedRow();
            if (row < 0) { joinResult.setForeground(RED); joinResult.setText("✗ Select a lobby first."); return; }
            String lid = (String) lobbyModel.getValueAt(row, 0);
            String err = tryJoin(lid);
            if (err == null) {
                joinResult.setForeground(GREEN);
                joinResult.setText("✓ Joined lobby " + lid + "!");
                enterLobbyBtn.setEnabled(true);
                enterLobbyBtn.putClientProperty("lid", lid);
                refreshTable.run();
            } else {
                joinResult.setForeground(RED);
                joinResult.setText("✗ " + err);
                enterLobbyBtn.setEnabled(false);
            }
        });

        enterLobbyBtn.addActionListener(e -> {
            String lid = (String) enterLobbyBtn.getClientProperty("lid");
            if (lid != null) showLobbyChat(lid);
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createCard, joinCard);
        split.setDividerLocation(320); split.setBackground(BG_MID); split.setBorder(null);
        content.add(split, BorderLayout.CENTER);

        JButton back = styledButton("← Back", BG_CARD, TEXT_GRAY);
        back.addActionListener(e -> showMainDashboard());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.setBackground(BG_MID); south.add(back);
        content.add(south, BorderLayout.SOUTH);

        root.add(content, BorderLayout.CENTER);
        setPanel(root);
    }

    // Returns null on success, error string on failure
    private String tryJoin(String lid) {
        if (lid.isEmpty()) return "Enter a Lobby ID.";
        if (!LOBBIES.containsKey(lid)) return "Lobby '" + lid + "' not found.";
        Lobby lb = LOBBIES.get(lid);
        if (!lb.status.equals("WAITING")) return "Lobby is no longer accepting players.";
        if (lb.playerIDs.contains(currentPlayer.playerID)) return "You are already in this lobby.";
        if (lb.playerIDs.size() >= lb.maxPlayers) return "Lobby is full (" + lb.maxPlayers + "/" + lb.maxPlayers + ").";
        if (!currentPlayer.ownedGameIDs.contains(lb.gameID))
            return "You do not own '" + lb.gameTitle + "'.";
        lb.playerIDs.add(currentPlayer.playerID);
        lb.playerNames.add(currentPlayer.username);
        lb.chatMessages.add(new String[]{now(), "System", "", currentPlayer.username + " joined the lobby."});
        return null;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SCREEN 5 — LOBBY CHAT (Use Case 3: Cross-Platform Communication)
    // ════════════════════════════════════════════════════════════════════════

    private void showChatLobbyPicker() {
        List<Lobby> myLobbies = new ArrayList<>();
        for (Lobby lb : LOBBIES.values()) {
            if (lb.playerIDs.contains(currentPlayer.playerID)) myLobbies.add(lb);
        }
        if (myLobbies.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame,
                "You are not in any lobbies yet.\nCreate or join a lobby first.",
                "No Lobbies", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (myLobbies.size() == 1) { showLobbyChat(myLobbies.get(0).lobbyID); return; }
        String[] choices = myLobbies.stream()
            .map(lb -> lb.lobbyID + " — " + lb.gameTitle + " [" + lb.status + "]")
            .toArray(String[]::new);
        String pick = (String) JOptionPane.showInputDialog(mainFrame,
            "Select a lobby to open:", "Open Chat",
            JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
        if (pick != null) showLobbyChat(myLobbies.get(Arrays.asList(choices).indexOf(pick)).lobbyID);
    }

    private void showLobbyChat(String lobbyID) {
        Lobby lb = LOBBIES.get(lobbyID);
        if (lb == null) return;

        JPanel root = darkPanel(new BorderLayout());
        root.add(topBar("③ Cross-Platform Chat — Lobby " + lobbyID), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBackground(BG_MID);
        content.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        // ── Left: Lobby info + players ──
        JPanel infoCard = card("Lobby Info");
        JPanel infoInner = new JPanel();
        infoInner.setLayout(new BoxLayout(infoInner, BoxLayout.Y_AXIS));
        infoInner.setBackground(BG_CARD);

        Runnable refreshInfo = () -> {
            infoInner.removeAll();
            infoInner.add(infoRow("Lobby ID:", lobbyID));
            infoInner.add(infoRow("Game:", lb.gameTitle));
            infoInner.add(infoRow("Host:", lb.hostName));
            infoInner.add(infoRow("Status:", lb.status));
            infoInner.add(infoRow("Players:", lb.playerIDs.size() + "/" + lb.maxPlayers));
            infoInner.add(Box.createVerticalStrut(10));
            JLabel ph = label("Players:", FONT_BODY, TEXT_GRAY); ph.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoInner.add(ph);
            for (int i = 0; i < lb.playerIDs.size(); i++) {
                String pid = lb.playerIDs.get(i);
                Player p2 = PLAYERS.get(pid);
                String tag = (pid.equals(lb.hostID) ? " [HOST]" : "") +
                             (pid.equals(currentPlayer.playerID) ? " (you)" : "");
                JLabel pl = label("  • " + lb.playerNames.get(i) + " [" + (p2 != null ? p2.platform : "?") + "]" + tag,
                                  FONT_BODY, TEXT_WHT);
                pl.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoInner.add(pl);
            }
            infoInner.revalidate(); infoInner.repaint();
        };
        refreshInfo.run();
        infoCard.add(new JScrollPane(infoInner), BorderLayout.CENTER);
        infoCard.setPreferredSize(new Dimension(240, 0));

        // Start Game button (host only)
        JPanel gameCtrl = new JPanel(new BorderLayout());
        gameCtrl.setBackground(BG_CARD);
        if (lb.hostID.equals(currentPlayer.playerID)) {
            JButton startBtn = styledButton(lb.status.equals("ACTIVE") ? "▶ Game Running" : "▶ Start Game", GREEN, BG_DARK);
            startBtn.setEnabled(!lb.status.equals("ACTIVE") && lb.playerIDs.size() >= 2);
            startBtn.addActionListener(e -> {
                if (lb.playerIDs.size() < 2) {
                    JOptionPane.showMessageDialog(mainFrame, "Need at least 2 players to start.", "Cannot Start", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                lb.status = "ACTIVE";
                lb.chatMessages.add(new String[]{now(), "System", "", "🎮 Game started! Good luck everyone!"});
                startBtn.setText("▶ Game Running"); startBtn.setEnabled(false);
                refreshInfo.run();
            });
            gameCtrl.add(startBtn, BorderLayout.CENTER);
        } else {
            JLabel waitLbl = label("Waiting for host to start...", FONT_SMALL, TEXT_GRAY);
            waitLbl.setHorizontalAlignment(SwingConstants.CENTER);
            gameCtrl.add(waitLbl, BorderLayout.CENTER);
        }
        infoCard.add(gameCtrl, BorderLayout.SOUTH);

        // ── Right: Chat ──
        JPanel chatCard = card("Cross-Platform Chat Channel  [LIVE]");
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false); chatArea.setFont(FONT_MONO);
        chatArea.setBackground(new Color(18, 18, 28)); chatArea.setForeground(TEXT_WHT);
        chatArea.setLineWrap(true); chatArea.setWrapStyleWord(true);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        Runnable refreshChat = () -> {
            StringBuilder sb = new StringBuilder();
            for (String[] m : lb.chatMessages) {
                if (m[1].equals("System")) {
                    sb.append("[").append(m[0]).append("] ⚙ ").append(m[3]).append("\n");
                } else {
                    Player sp = PLAYERS.get(
                        PLAYERS.values().stream().filter(p->p.username.equals(m[1])).findFirst().map(p->p.playerID).orElse(""));
                    String plat = sp != null ? sp.platform : m[2];
                    sb.append("[").append(m[0]).append("] ").append(m[1])
                      .append(" [").append(plat).append("]: ").append(m[3]).append("\n");
                }
            }
            chatArea.setText(sb.toString());
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        };
        refreshChat.run();

        JTextField msgField = new JTextField();
        msgField.setFont(FONT_MONO); msgField.setBackground(BG_CARD);
        msgField.setForeground(TEXT_WHT); msgField.setCaretColor(Color.WHITE);
        msgField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)));

        JLabel charCount = label("0/200", FONT_SMALL, TEXT_GRAY);
        JLabel chatErr   = label("", FONT_SMALL, RED);

        JButton sendBtn = styledButton("Send", ACCENT, Color.WHITE);

        msgField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            void update() { charCount.setText(msgField.getText().length() + "/200"); }
        });

        ActionListener doSend = e -> {
            String msg = msgField.getText().trim();
            chatErr.setText("");
            if (msg.isEmpty()) { chatErr.setText("⚠  Message cannot be empty."); return; }
            if (msg.length() > 200) { chatErr.setText("⚠  Max 200 characters."); return; }
            if (!lb.playerIDs.contains(currentPlayer.playerID)) {
                chatErr.setText("⚠  You are not in this lobby."); return;
            }
            lb.chatMessages.add(new String[]{now(), currentPlayer.username, currentPlayer.platform, msg});
            msgField.setText("");
            refreshChat.run();
        };
        sendBtn.addActionListener(doSend);
        msgField.addActionListener(doSend);

        JPanel inputRow = new JPanel(new BorderLayout(6, 0));
        inputRow.setBackground(BG_CARD);
        inputRow.add(msgField, BorderLayout.CENTER);
        inputRow.add(sendBtn, BorderLayout.EAST);

        JPanel inputArea = new JPanel(new BorderLayout(4, 4));
        inputArea.setBackground(BG_CARD);
        inputArea.add(inputRow, BorderLayout.CENTER);
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        metaRow.setBackground(BG_CARD); metaRow.add(chatErr); metaRow.add(charCount);
        inputArea.add(metaRow, BorderLayout.SOUTH);

        JLabel fallbackNote = label("  ⚙ Fallback: If voice server fails, this encrypted text channel activates automatically.", FONT_SMALL, TEXT_GRAY);

        chatCard.add(chatScroll, BorderLayout.CENTER);
        JPanel chatSouth = new JPanel(new BorderLayout());
        chatSouth.setBackground(BG_CARD);
        chatSouth.add(inputArea, BorderLayout.NORTH);
        chatSouth.add(fallbackNote, BorderLayout.SOUTH);
        chatCard.add(chatSouth, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, infoCard, chatCard);
        split.setDividerLocation(260); split.setBackground(BG_MID); split.setBorder(null);
        content.add(split, BorderLayout.CENTER);

        JButton back = styledButton("← Back", BG_CARD, TEXT_GRAY);
        back.addActionListener(e -> showMainDashboard());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.setBackground(BG_MID); south.add(back);
        content.add(south, BorderLayout.SOUTH);

        root.add(content, BorderLayout.CENTER);
        setPanel(root);
    }

    private JPanel infoRow(String key, String val) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        row.setBackground(BG_CARD);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.add(label(key, FONT_SMALL, TEXT_GRAY));
        row.add(label(val, FONT_BODY, TEXT_WHT));
        return row;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SCREEN 6 — SERVER STATUS (Latency Simulation)
    // ════════════════════════════════════════════════════════════════════════

    private void showServerStatus() {
        JPanel root = darkPanel(new BorderLayout());
        root.add(topBar("④ Server & Communication Status"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBackground(BG_MID);
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Server rows
        String[][] servers = {
            {"Game Server — US Central", "RUNNING"},
            {"Game Server — EU West",    "RUNNING"},
            {"Communication Server",     "RECOVERING"},
            {"Voice Server",             "RUNNING"},
        };
        int[] basePings = {42, 68, 150, 78};
        Random rng = new Random();

        JPanel serverList = new JPanel(new GridLayout(4, 1, 0, 10));
        serverList.setBackground(BG_MID);

        List<JLabel> pingLabels  = new ArrayList<>();
        List<JLabel> statLabels  = new ArrayList<>();
        List<JLabel> warnLabels  = new ArrayList<>();

        for (int i = 0; i < servers.length; i++) {
            JPanel row = card(null);
            row.setLayout(new BorderLayout());

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            left.setBackground(BG_CARD);
            JLabel dot = new JLabel("●");
            dot.setForeground(servers[i][1].equals("RUNNING") ? GREEN : ORANGE);
            dot.setFont(new Font("SansSerif", Font.PLAIN, 16));
            JLabel name = label(servers[i][0], FONT_HEADING, TEXT_WHT);
            left.add(dot); left.add(name);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
            right.setBackground(BG_CARD);
            JLabel wl = label("", FONT_SMALL, RED); warnLabels.add(wl);
            JLabel sl = label(servers[i][1], FONT_BODY, servers[i][1].equals("RUNNING") ? GREEN : ORANGE);
            statLabels.add(sl);
            JLabel pl = label(basePings[i] + " ms", FONT_BODY, YELLOW); pingLabels.add(pl);
            right.add(wl); right.add(sl); right.add(pl);

            row.add(left, BorderLayout.CENTER);
            row.add(right, BorderLayout.EAST);
            serverList.add(row);
        }

        JLabel alertBanner = label("", FONT_BODY, RED);
        alertBanner.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JButton refreshBtn = styledButton("↻ Refresh", ACCENT, Color.WHITE);
        Runnable doRefresh = () -> {
            boolean anyBad = false;
            for (int i = 0; i < servers.length; i++) {
                int jitter = rng.nextInt(120) - 10;
                int cur = Math.max(10, basePings[i] + jitter);
                pingLabels.get(i).setText(cur + " ms");
                if (cur > 150) {
                    pingLabels.get(i).setForeground(RED);
                    statLabels.get(i).setText("DEGRADED"); statLabels.get(i).setForeground(RED);
                    warnLabels.get(i).setText("⚠ High latency! ");
                    anyBad = true;
                } else if (cur > 100) {
                    pingLabels.get(i).setForeground(ORANGE);
                    statLabels.get(i).setText("ELEVATED"); statLabels.get(i).setForeground(ORANGE);
                    warnLabels.get(i).setText("");
                } else {
                    pingLabels.get(i).setForeground(YELLOW);
                    statLabels.get(i).setText("RUNNING"); statLabels.get(i).setForeground(GREEN);
                    warnLabels.get(i).setText("");
                }
            }
            alertBanner.setText(anyBad
                ? "⚠  Network Optimization in Progress — attempting automatic rerouting..."
                : "✓ All servers operating normally.");
            alertBanner.setForeground(anyBad ? RED : GREEN);
        };
        doRefresh.run();
        refreshBtn.addActionListener(e -> doRefresh.run());

        // Auto-refresh timer
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() { SwingUtilities.invokeLater(doRefresh); }
        }, 5000, 5000);

        content.add(serverList, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(BG_MID);
        south.add(alertBanner, BorderLayout.NORTH);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setBackground(BG_MID);
        JButton back = styledButton("← Back", BG_CARD, TEXT_GRAY);
        back.addActionListener(e -> { timer.cancel(); showMainDashboard(); });
        btnRow.add(back); btnRow.add(refreshBtn);
        south.add(btnRow, BorderLayout.SOUTH);
        content.add(south, BorderLayout.SOUTH);

        root.add(content, BorderLayout.CENTER);
        setPanel(root);
    }

    // ─── Utility ────────────────────────────────────────────────────────────

    private static String now() {
        java.time.LocalTime t = java.time.LocalTime.now();
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    // ─── Entry Point ────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            UnifiedGamingPlatformGUI app = new UnifiedGamingPlatformGUI();
            app.showLoginScreen();
        });
    }
}
