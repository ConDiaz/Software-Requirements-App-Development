public class Game {
    String gameID, title, genre, requiredVersion;
    Game(String id, String title, String genre, String version) {
        this.gameID = id; this.title = title;
        this.genre = genre; this.requiredVersion = version;
    }
}