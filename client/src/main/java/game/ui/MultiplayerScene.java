package game.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MultiplayerScene {

    private final VBox root =
            new VBox(20);

    public MultiplayerScene(
            SceneManager sceneManager
    ) {

        root.setAlignment(
                Pos.CENTER
        );

        Label title =
                new Label("МУЛЬТИПЛЕЕР");

        Button create =
                new Button("СОЗДАТЬ ИГРУ");

        Button join =
                new Button("ПОДКЛЮЧИТЬСЯ");

        Button back =
                new Button("НАЗАД");

        create.setOnAction(e ->
                sceneManager.show(
                        SceneType.CREATE_GAME
                )
        );

        join.setOnAction(e ->
                sceneManager.show(
                        SceneType.JOIN_GAME
                )
        );

        back.setOnAction(e ->
                sceneManager.show(
                        SceneType.MAIN_MENU
                )
        );

        root.getChildren().addAll(
                title,
                create,
                join,
                back
        );
    }

    public Parent getRoot() {
        return root;
    }
}