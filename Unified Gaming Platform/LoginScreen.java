import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginScreen {
    public static JPanel create(UnifiedGamingPlatformGUI mainApp) {
        JPanel root = Theme.darkPanel(new BorderLayout());
        root.add(Theme.topBar(null), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Theme.BG_MID);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);

        g.gridx=0; g.gridy=0; g.gridwidth=2; g.anchor=GridBagConstraints.CENTER;
        center.add(Theme.label("Sign In", Theme.FONT_TITLE, Theme.TEXT_WHT), g);

        g.gridy=1; center.add(Theme.label("Demo password: pass", Theme.FONT_SMALL, Theme.TEXT_GRAY), g);

        g.gridwidth=1; g.anchor=GridBagConstraints.EAST;
        g.gridx=0; g.gridy=2; center.add(Theme.label("Account:", Theme.FONT_BODY, Theme.TEXT_WHT), g);

        String[] options = PlatformData.PLAYERS.values().stream()
            .map(p -> p.username + " [" + p.platform + "]")
            .toArray(String[]::new);
        JComboBox<String> accountBox = new JComboBox<>(options);
        g.gridx=1; g.anchor=GridBagConstraints.WEST; center.add(accountBox, g);

        g.gridx=0; g.gridy=3; g.anchor=GridBagConstraints.EAST;
        center.add(Theme.label("Password:", Theme.FONT_BODY, Theme.TEXT_WHT), g);
        JPasswordField pwField = new JPasswordField(15);
        g.gridx=1; g.anchor=GridBagConstraints.WEST; center.add(pwField, g);

        JLabel errLabel = Theme.label("", Theme.FONT_SMALL, Theme.RED);
        g.gridx=0; g.gridy=4; g.gridwidth=2; g.anchor=GridBagConstraints.CENTER;
        center.add(errLabel, g);

        JButton loginBtn = Theme.styledButton("LOGIN →", Theme.ACCENT, Color.WHITE);
        g.gridy=5; center.add(loginBtn, g);

        loginBtn.addActionListener(e -> {
            Player selected = (Player) PlatformData.PLAYERS.values().toArray()[accountBox.getSelectedIndex()];
            if (new String(pwField.getPassword()).equals(selected.password)) {
                mainApp.setCurrentPlayer(selected);
                mainApp.showMainDashboard();
            } else {
                errLabel.setText("⚠ Incorrect password.");
            }
        });

        root.add(center, BorderLayout.CENTER);
        return root;
    }
}