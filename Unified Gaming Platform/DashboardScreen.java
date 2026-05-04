import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class DashboardScreen {
    public static JPanel create(UnifiedGamingPlatformGUI mainApp) {
        JPanel root = Theme.darkPanel(new BorderLayout());
        Player cp = mainApp.getCurrentPlayer();

        JPanel header = Theme.topBar("Logged in as: " + cp.username);
        JButton logoutBtn = Theme.styledButton("Logout", Theme.BG_CARD, Theme.TEXT_GRAY);
        logoutBtn.addActionListener(e -> { mainApp.setCurrentPlayer(null); mainApp.showLoginScreen(); });
        header.add(logoutBtn, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setBackground(Theme.BG_MID);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        grid.add(dashCard("① Verify Ownership", "Check game compatibility", e -> mainApp.showVerifyOwnership()));
        grid.add(dashCard("② Shared Lobby", "Create or join sessions", e -> mainApp.showLobbyManager()));
        grid.add(dashCard("③ Chat Channel", "Cross-platform communication", e -> mainApp.showChatLobbyPicker()));
        grid.add(dashCard("④ Server Status", "Monitor latency and health", e -> mainApp.showServerStatus()));

        root.add(grid, BorderLayout.CENTER);
        return root;
    }

    private static JPanel dashCard(String title, String desc, ActionListener action) {
        JPanel p = Theme.card(title);
        p.add(Theme.label("<html>" + desc + "</html>", Theme.FONT_BODY, Theme.TEXT_GRAY), BorderLayout.CENTER);
        JButton b = Theme.styledButton("Open →", Theme.ACCENT, Color.WHITE);
        b.addActionListener(action);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        bp.setBackground(Theme.BG_CARD); bp.add(b);
        p.add(bp, BorderLayout.SOUTH);
        return p;
    }
}