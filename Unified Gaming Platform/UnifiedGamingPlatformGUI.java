import javax.swing.*;
import java.awt.*;

public class UnifiedGamingPlatformGUI {
    private JFrame mainFrame;
    private Player currentPlayer;

    public UnifiedGamingPlatformGUI() {
        mainFrame = new JFrame("Unified Gaming Platform");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1050, 720);
        mainFrame.setLocationRelativeTo(null);
    }

    public void setCurrentPlayer(Player p) { this.currentPlayer = p; }
    public Player getCurrentPlayer() { return this.currentPlayer; }

    private void setPanel(JPanel p) {
        mainFrame.setContentPane(p);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    public void showLoginScreen() { setPanel(LoginScreen.create(this)); mainFrame.setVisible(true); }
    public void showMainDashboard() { setPanel(DashboardScreen.create(this)); }
    public void showVerifyOwnership() { setPanel(VerifyOwnershipScreen.create(this)); }
    public void showLobbyManager() { setPanel(LobbyManagerScreen.create(this)); }
    public void showServerStatus() { setPanel(ServerStatusScreen.create(this)); }
    
    public void showChatLobbyPicker() {
        // Logic to pick a lobby then call showLobbyChat(id)
        if (PlatformData.LOBBIES.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "No active lobbies.");
        } else {
            String id = PlatformData.LOBBIES.keySet().iterator().next();
            showLobbyChat(id);
        }
    }
    
    public void showLobbyChat(String id) { setPanel(LobbyChatScreen.create(this, id)); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UnifiedGamingPlatformGUI().showLoginScreen();
        });
    }
}