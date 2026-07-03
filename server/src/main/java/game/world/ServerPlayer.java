package game.world;

import java.io.File;

public class ServerPlayer {

    private final String username;

    private float x;
    private float y;

    private int locationX;
    private int locationY;

    private int lastLocationX;
    private int lastLocationY;

    private boolean locationChanged;

    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;

    private final float speed = 24f;

    public ServerPlayer(String username) {
        this.username = username;

        this.x = 500;
        this.y = 300;

        this.locationX = 0;
        this.locationY = 0;

        locationChanged = true;

    }

    public int getLocationX() {
        return locationX;
    }

    public int getLocationY() {
        return locationY;
    }

    public boolean isLocationChanged() {
        return locationChanged;
    }

    public void resetLocationChanged() {
        locationChanged = false;
    }

    public void update() {

        float dx = 0;
        float dy = 0;

        if (up) dy -= 1;
        if (down) dy += 1;
        if (left) dx -= 1;
        if (right) dx += 1;

        float length = (float)Math.sqrt(dx * dx + dy * dy);

        if (length != 0) {
            dx /= length;
            dy /= length;
        }

        x += dx * speed;
        y += dy * speed;

        final float WORLD_WIDTH = 1920;
        final float WORLD_HEIGHT = 1080;

        if (x < 0) {

            if (locationExists(
                    locationX - 1,
                    locationY
            )) {

                locationX--;
                x = WORLD_WIDTH - 60;

            } else {

                x = 0;
            }
        }
        if (x > WORLD_WIDTH - 40) {

            if (locationExists(
                    locationX + 1,
                    locationY
            )) {

                locationX++;
                x = 60;

                System.out.println(
                        username
                                + " -> "
                                + locationX
                                + ","
                                + locationY
                );

            } else {

                x = WORLD_WIDTH - 40;
            }
        }
        if (y < 0) {

            if (locationExists(
                    locationX,
                    locationY - 1
            )) {

                locationY--;
                y = WORLD_HEIGHT - 60;

            } else {

                y = 0;
            }
        }
        if (y > WORLD_HEIGHT - 40) {

            if (locationExists(
                    locationX,
                    locationY + 1
            )) {

                locationY++;
                y = 60;

            } else {

                y = WORLD_HEIGHT - 40;
            }
        }
        if (locationX != lastLocationX || locationY != lastLocationY) {
            locationChanged = true;

            lastLocationX = locationX;
            lastLocationY = locationY;
        }
    }

    private boolean locationExists(
            int x,
            int y
    ) {

        File file =
                new File(
                        "server/data/locations/"
                                + x
                                + "_"
                                + y
                                + ".json"
                );

        return file.exists();
    }

    // ===== getters/setters =====

    public String getUsername() {
        return username;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setRight(boolean right) {
        this.right = right;
    }
}