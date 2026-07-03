package game.network.packets;

public class ReadyPacket {

    private String username;

    public ReadyPacket(String username) {
        this.username = username;
    }

    public String serialize() {
        return "READY:" + username;
    }
}