import javax.swing.*;
import java.awt.*;

public class ServerStatusScreen {
    public static JPanel create(UnifiedGamingPlatformGUI mainApp) {
        JPanel root = Theme.darkPanel(new BorderLayout());
        root.add(Theme.topBar("④ Server Status"), BorderLayout.NORTH);

        JPanel list = new JPanel(new GridLayout(3, 1, 10, 10));
        list.setBackground(Theme.BG_MID);
        list.add(Theme.label("Game Server: ONLINE (40ms)", Theme.FONT_HEADING, Theme.GREEN));
        list.add(Theme.label("Voice Server: ONLINE (12ms)", Theme.FONT_HEADING, Theme.GREEN));
        list.add(Theme.label("Auth Server: ONLINE (55ms)", Theme.FONT_HEADING, Theme.GREEN));

        JButton back = Theme.styledButton("← Back", Theme.BG_CARD, Theme.TEXT_GRAY);
        back.addActionListener(e -> mainApp.showMainDashboard());
        
        root.add(list, BorderLayout.CENTER);
        root.add(back, BorderLayout.SOUTH);
        return root;
    }
}