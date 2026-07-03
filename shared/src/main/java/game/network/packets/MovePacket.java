package game.network.packets;

import game.network.dto.Position;

public class MovePacket {

    private String username;
    private int dx;
    private int dy;

    public MovePacket(String username, int dx, int dy) {
        this.username = username;
        this.dx = dx;
        this.dy = dy;
    }

    public String serialize() {
        return "MOVE:" + username + ":" + dx + ":" + dy;
    }
}