package game.network.packets;

import game.network.dto.PlayerState;

import java.util.ArrayList;
import java.util.List;

public class WorldStatePacket {

    public List<PlayerState> players = new ArrayList<>();

}