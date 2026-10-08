package game.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainMenuScene {

    private final StackPane root =
            new StackPane();

    private final VBox menu =
            new VBox(20);

    private final SceneManager sceneManager;

    private SettingsScene settingsScene;

    public MainMenuScene(
            SceneManager sceneManager
    ) {

        this.sceneManager =
                sceneManager;

        /*
         * =================================================
         * ОСНОВНОЕ МЕНЮ
         * =================================================
         */

        menu.setAlignment(
                Pos.CENTER
        );

        Label title =
                new Label("LT2 RPG");

        Button singleplayer =
                new Button("ОДИНОЧНАЯ ИГРА");

        Button multiplayer =
                new Button("МУЛЬТИПЛЕЕР (ЗАМОРОЖЕН)");

        Button settings =
                new Button("НАСТРОЙКИ");

        Button exit =
                new Button("ВЫХОД");


        /*
         * =================================================
         * ОДИНОЧНАЯ ИГРА
         * =================================================
         */

        singleplayer.setOnAction(e ->
                sceneManager.show(
                        SceneType.SINGLEPLAYER
                )
        );


        /*
         * =================================================
         * МУЛЬТИПЛЕЕР
         * =================================================
         */

        multiplayer.setOnAction(e ->
                sceneManager.show(
                        SceneType.MULTIPLAYER
                )
        );


        /*
         * =================================================
         * НАСТРОЙКИ
         * =================================================
         */

        settings.setOnAction(e ->
                openSettings()
        );


        /*
         * =================================================
         * ВЫХОД
         * =================================================
         */

        exit.setOnAction(e ->
                sceneManager.getStage().close()
        );


        /*
         * =================================================
         * ДОБАВЛЯЕМ КНОПКИ
         * =================================================
         */

        menu.getChildren().addAll(
                title,
                singleplayer,
                multiplayer,
                settings,
                exit
        );


        /*
         * =================================================
         * ROOT
         * =================================================
         */

        root.getChildren().add(
                menu
        );
    }

    private void openSettings() {

        settingsScene =
                new SettingsScene(
                        this::closeSettings
                );

        root.getChildren().add(
                settingsScene.getRoot()
        );
    }

    private void closeSettings() {

        if (settingsScene != null) {

            root.getChildren().remove(
                    settingsScene.getRoot()
            );

            settingsScene = null;
        }
    }

    public Parent getRoot() {

        return root;
    }
}