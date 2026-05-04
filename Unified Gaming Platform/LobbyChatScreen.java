import javax.swing.*;
import java.awt.*;

public class LobbyChatScreen {
    public static JPanel create(UnifiedGamingPlatformGUI mainApp, String lobbyID) {
        Lobby lb = PlatformData.LOBBIES.get(lobbyID);
        JPanel root = Theme.darkPanel(new BorderLayout());
        root.add(Theme.topBar("③ Chat — Lobby " + lobbyID), BorderLayout.NORTH);

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(Color.BLACK);
        chatArea.setForeground(Color.GREEN);

        JTextField input = new JTextField();
        input.addActionListener(e -> {
            lb.chatMessages.add(new String[]{Theme.now(), mainApp.getCurrentPlayer().username, "", input.getText()});
            chatArea.append("[" + Theme.now() + "] " + input.getText() + "\n");
            input.setText("");
        });

        root.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        root.add(input, BorderLayout.SOUTH);

        return root;
    }
}