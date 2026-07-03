package game.ui.world;

import javafx.scene.image.Image;

public class WorldObject {

    public float x;
    public float y;

    public float width;
    public float height;

    public Image image;

    public WorldObject(
            float x,
            float y,
            float width,
            float height,
            Image image
    ) {

        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;

        this.image = image;
    }
}