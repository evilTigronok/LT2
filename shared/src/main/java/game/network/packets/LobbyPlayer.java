package game.network.packets;

public class LobbyPlayer {

    public String username;
    public boolean ready;

    public LobbyPlayer() {
    }

    public LobbyPlayer(String username, boolean ready) {
        this.username = username;
        this.ready = ready;
    }
}