package game.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainMenuScene {

    private final VBox root = new VBox(20);

    public MainMenuScene(
            SceneManager sceneManager
    ) {

        root.setAlignment(
                Pos.CENTER
        );

        Label title =
                new Label("LT2 RPG");

        Button singleplayer =
                new Button("ОДИНОЧНАЯ ИГРА (НЕ РАБОТАЕТ)");

        Button multiplayer =
                new Button("МУЛЬТИПЛЕЕР");

        Button settings =
                new Button("НАСТРОЙКИ (НЕ РАБОТАЕТ)");

        Button exit =
                new Button("ВЫХОД (в окно)");

        singleplayer.setOnAction(e -> {

            System.out.println(
                    "Singleplayer пока не реализован"
            );

        });

        multiplayer.setOnAction(e ->
                sceneManager.show(
                        SceneType.MULTIPLAYER
                )
        );

        settings.setOnAction(e -> {

            System.out.println(
                    "Settings пока не реализованы"
            );

        });

        exit.setOnAction(e ->
                sceneManager.getStage().close()
        );

        root.getChildren().addAll(
                title,
                singleplayer,
                multiplayer,
                settings,
                exit
        );
    }

    public Parent getRoot() {
        return root;
    }
}