import java.util.*;

public class Lobby {
    String lobbyID, gameID, gameTitle, hostID, hostName;
    int maxPlayers;
    List<String> playerIDs = new ArrayList<>();
    List<String> playerNames = new ArrayList<>();
    String status = "WAITING"; 
    List<String[]> chatMessages = new ArrayList<>(); // [time, sender, platform, message]
}