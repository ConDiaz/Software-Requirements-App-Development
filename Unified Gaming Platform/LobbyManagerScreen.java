import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.UUID;

public class LobbyManagerScreen {
    public static JPanel create(UnifiedGamingPlatformGUI mainApp) {
        JPanel root = Theme.darkPanel(new BorderLayout());
        root.add(Theme.topBar("② Shared Lobby Manager"), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 10, 10));
        content.setBackground(Theme.BG_MID);

        // Create Section
        JPanel createCard = Theme.card("Create Lobby");
        JButton createBtn = Theme.styledButton("Create New Lobby", Theme.ACCENT, Color.WHITE);
        createBtn.addActionListener(e -> {
            String lid = UUID.randomUUID().toString().substring(0,6).toUpperCase();
            Lobby lb = new Lobby();
            lb.lobbyID = lid; lb.hostName = mainApp.getCurrentPlayer().username;
            PlatformData.LOBBIES.put(lid, lb);
            JOptionPane.showMessageDialog(root, "Lobby Created: " + lid);
        });
        createCard.add(createBtn, BorderLayout.SOUTH);

        content.add(createCard);
        
        JButton back = Theme.styledButton("← Back", Theme.BG_CARD, Theme.TEXT_GRAY);
        back.addActionListener(e -> mainApp.showMainDashboard());
        root.add(content, BorderLayout.CENTER);
        root.add(back, BorderLayout.SOUTH);

        return root;
    }
}