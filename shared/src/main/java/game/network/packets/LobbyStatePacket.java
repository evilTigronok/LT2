package game.network.packets;

import java.util.ArrayList;
import java.util.List;

public class LobbyStatePacket {

    public String roomId;
    public String host;
    public List<LobbyPlayer> players = new ArrayList<>();

    public LobbyStatePacket() {
    }
}