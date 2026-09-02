package game.ui;

import game.client.NetworkClient;
import javafx.application.Application;
import javafx.stage.Stage;

public class RPGApplication extends Application {

    private SceneManager sceneManager;

    @Override
    public void start(Stage stage) {

        NetworkClient client =
                new NetworkClient();

        sceneManager =
                new SceneManager(
                        stage,
                        client
                );

        stage.setTitle(
                "LT2 RPG"
        );

        sceneManager.show(
                SceneType.MAIN_MENU
        );

        stage.show();
    }

    @Override
    public void stop() {

        if (sceneManager != null) {

            sceneManager.getClient()
                    .disconnect();

            sceneManager.getHostManager()
                    .stopServer();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}