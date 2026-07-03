package game.combat;

import java.util.*;

public class CombatManager {

    private Map<String, BattleRoom> rooms = new HashMap<>();

    public BattleRoom createRoom(String id) {

        BattleRoom room = new BattleRoom(id);

        rooms.put(id, room);

        return room;
    }

    public BattleRoom getRoom(String id) {
        return rooms.get(id);
    }
}