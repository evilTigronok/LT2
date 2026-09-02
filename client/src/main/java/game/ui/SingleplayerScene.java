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
                        "SINGLEPLAYER"
                );

        title.setStyle(
                "-fx-font-size: 36px;"
        );

        Label info =
                new Label(
                        "Singleplayer mode will be implemented separately."
                );

        Button back =
                new Button("BACK");

        back.setOnAction(e ->
                sceneManager.show(
                        SceneType.MAIN_MENU
                )
        );

        root.getChildren().addAll(
                title,
                info,
                back
        );
    }

    public Parent getRoot() {
        return root;
    }
}