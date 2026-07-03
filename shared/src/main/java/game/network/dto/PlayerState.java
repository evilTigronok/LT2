package game.network.dto;

public class PlayerState {

    public String username;

    public float x;
    public float y;

    public int locationX;
    public int locationY;

    public PlayerState() {
    }

    public PlayerState(
            String username,
            float x,
            float y,
            int locationX,
            int locationY
    ) {

        this.username = username;

        this.x = x;
        this.y = y;

        this.locationX = locationX;
        this.locationY = locationY;
    }
}