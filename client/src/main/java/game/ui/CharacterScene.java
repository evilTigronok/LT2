package game.ui;

import javafx.scene.layout.VBox;

public class CharacterScene {

    private final SceneManager sceneManager;

    private final VBox root = new VBox();

    public CharacterScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public VBox getRoot() {
        return root;
    }
}