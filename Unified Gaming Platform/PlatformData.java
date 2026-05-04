import java.util.*;

public class PlatformData {
    public static final Map<String, Player> PLAYERS = new LinkedHashMap<>();
    public static final Map<String, Game>   GAMES   = new LinkedHashMap<>();
    public static final Map<String, Lobby>  LOBBIES = new LinkedHashMap<>();

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
        p3.gameVersions.put("g001","2.3.0"); 
        p3.gameVersions.put("g002","1.8.0");

        Player p4 = new Player("p004", "Sam_PC",     "PC",          "pass");
        p4.ownedGameIDs.addAll(List.of("g002","g003"));
        p4.gameVersions.put("g002","1.8.0"); p4.gameVersions.put("g003","3.1.2");

        PLAYERS.put(p1.playerID,p1); PLAYERS.put(p2.playerID,p2);
        PLAYERS.put(p3.playerID,p3); PLAYERS.put(p4.playerID,p4);
    }
}