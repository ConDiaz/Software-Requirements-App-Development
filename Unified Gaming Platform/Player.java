import java.util.*;

public class Player {
    String playerID, username, platform, password;
    List<String> ownedGameIDs = new ArrayList<>();
    Map<String, String> gameVersions = new HashMap<>();

    Player(String id, String username, String platform, String password) {
        this.playerID = id; this.username = username;
        this.platform = platform; this.password = password;
    }
}