package game.ui.world;

import javafx.scene.image.Image;

public class WorldAssets {

    private static Image load(String path) {

        return new Image(
                WorldAssets.class.getResourceAsStream(path)
        );
    }

    public static final Image TREE =
            load("assets/world/trees/tree.png");
}