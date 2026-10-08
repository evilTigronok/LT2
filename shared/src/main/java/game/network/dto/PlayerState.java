package game.network.dto;

public class PlayerState {

    public String username;

    public float x;
    public float y;

    public PlayerState() {
    }

    public PlayerState(
            String username,
            float x,
            float y
    ) {
        this.username = username;
        this.x = x;
        this.y = y;
    }
}