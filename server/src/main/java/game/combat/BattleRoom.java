package game.combat;

import game.sessions.ClientSession;

import java.util.*;

public class BattleRoom {

    private String roomId;

    private List<ClientSession> players = new ArrayList<>();
    private List<Object> enemies = new ArrayList<>();

    private boolean started = false;

    public BattleRoom(String roomId) {
        this.roomId = roomId;
    }

    public void addPlayer(ClientSession session) {

        if (!players.contains(session)) {
            players.add(session);
        }
    }

    public void removePlayer(ClientSession session) {
        players.remove(session);
    }

    public void startBattle() {

        if (players.size() == 0) return;

        started = true;

        System.out.println("Battle started in room: " + roomId);
    }

    public List<ClientSession> getPlayers() {
        return players;
    }

    public boolean isStarted() {
        return started;
    }
}