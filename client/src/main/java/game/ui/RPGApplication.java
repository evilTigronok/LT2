package game.ui;

import game.client.Config;
import game.client.NetworkClient;
import javafx.application.Application;
import javafx.stage.Stage;

public class RPGApplication extends Application {

    private SceneManager sceneManager;

    @Override
    public void start(Stage stage) {

        NetworkClient client = new NetworkClient();
        client.connect(
                Config.SERVER_HOST,
                Config.SERVER_PORT
        );

        sceneManager = new SceneManager(stage, client);

        stage.setTitle("LT2 RPG");

        sceneManager.show(SceneType.LOGIN);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}