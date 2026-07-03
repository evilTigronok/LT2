package game.ui.world;

public class RemotePlayer {

    public String username;

    public float serverX;
    public float serverY;

    public float renderX;
    public float renderY;

    private final float smoothing = 0.15f;

    public int locationX;
    public int locationY;

    public RemotePlayer(String username, float x, float y) {

        this.username = username;

        this.serverX = x;
        this.serverY = y;

        this.renderX = x;
        this.renderY = y;
    }

    public void update() {

        renderX += (serverX - renderX) * smoothing;
        renderY += (serverY - renderY) * smoothing;
    }
}