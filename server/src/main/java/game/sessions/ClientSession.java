package game.sessions;

import game.characters.Player;

public class ClientSession {

    private String login;
    private String token;

    private Player character;

    private boolean readyToFight;

    // ===== LOGIN =====

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    // ===== TOKEN =====

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    // ===== CHARACTER =====

    public Player getCharacter() {
        return character;
    }

    public void setCharacter(Player character) {
        this.character = character;
    }

    // ===== READY =====

    public boolean isReadyToFight() {
        return readyToFight;
    }

    public void setReadyToFight(boolean readyToFight) {
        this.readyToFight = readyToFight;
    }
}