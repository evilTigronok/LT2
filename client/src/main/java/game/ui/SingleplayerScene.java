package game.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SingleplayerScene {

    private final VBox root =
            new VBox(20);

    public SingleplayerScene(
            SceneManager sceneManager
    ) {

        root.setAlignment(
                Pos.CENTER
        );

        Label title =
                new Label(
                        "ОДИНОЧНАЯ ИГРА"
                );

        Button start =
                new Button(
                        "НАЧАТЬ ИГРУ"
                );

        Button back =
                new Button(
                        "НАЗАД"
                );

        start.setOnAction(e ->
                sceneManager.show(
                        SceneType.WORLD
                )
        );

        back.setOnAction(e ->
                sceneManager.show(
                        SceneType.MAIN_MENU
                )
        );

        root.getChildren().addAll(
                title,
                start,
                back
        );
    }

    public Parent getRoot() {
        return root;
    }
}