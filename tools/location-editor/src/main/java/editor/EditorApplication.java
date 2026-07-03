package editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EditorApplication
        extends Application {

    @Override
    public void start(Stage stage) {

        EditorScene editor =
                new EditorScene();

        Scene scene =
                new Scene(
                        editor.getRoot(),
                        1600,
                        900
                );

        stage.setTitle(
                "Location Editor"
        );

        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}