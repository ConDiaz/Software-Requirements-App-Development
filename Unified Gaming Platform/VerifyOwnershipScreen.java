import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VerifyOwnershipScreen {
    public static JPanel create(UnifiedGamingPlatformGUI mainApp) {
        JPanel root = Theme.darkPanel(new BorderLayout());
        root.add(Theme.topBar("① Verify Game Ownership"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBackground(Theme.BG_MID);
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel controls = Theme.card("Select Game & Players");
        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(Theme.BG_CARD);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6); g.anchor = GridBagConstraints.WEST;

        g.gridx=0; g.gridy=0; inner.add(Theme.label("Game:", Theme.FONT_BODY, Theme.TEXT_WHT), g);
        String[] titles = PlatformData.GAMES.values().stream().map(gm -> gm.title).toArray(String[]::new);
        JComboBox<String> gameBox = new JComboBox<>(titles);
        g.gridx=1; inner.add(gameBox, g);

        JButton verifyBtn = Theme.styledButton("Run Verification →", Theme.GREEN, Theme.BG_DARK);
        g.gridx=0; g.gridy=2; g.gridwidth=2; inner.add(verifyBtn, g);
        controls.add(inner, BorderLayout.CENTER);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Player", "Platform", "Status"}, 0);
        JTable table = new JTable(model);
        content.add(controls, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton back = Theme.styledButton("← Back", Theme.BG_CARD, Theme.TEXT_GRAY);
        back.addActionListener(e -> mainApp.showMainDashboard());
        content.add(back, BorderLayout.SOUTH);

        root.add(content, BorderLayout.CENTER);
        return root;
    }
}