package game.ui;

import game.client.HostManager;
import game.client.NetworkClient;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private final Stage stage;

    private final NetworkClient client;

    private final HostManager hostManager;

    public SceneManager(
            Stage stage,
            NetworkClient client
    ) {

        this.stage = stage;
        this.client = client;

        this.hostManager =
                new HostManager();
    }

    public HostManager getHostManager() {
        return hostManager;
    }

    public NetworkClient getClient() {
        return client;
    }

    public Stage getStage() {
        return stage;
    }

    // =====================================
    // SCENES
    // =====================================

    public void show(SceneType type) {

        Parent root;

        switch (type) {

            case MAIN_MENU:

                root =
                        new MainMenuScene(
                                this
                        ).getRoot();

                break;

            case MULTIPLAYER:

                root =
                        new MultiplayerScene(
                                this
                        ).getRoot();

                break;

            case CREATE_GAME:

                root =
                        new CreateGameScene(
                                this,
                                client
                        ).getRoot();

                break;

            case JOIN_GAME:

                root =
                        new JoinGameScene(
                                this,
                                client
                        ).getRoot();

                break;

            case LOBBY:

                root =
                        new LobbyScene(
                                this,
                                client
                        ).getRoot();

                break;

            case WORLD:

                WorldScene worldScene =
                        new WorldScene(
                                this,
                                client,
                                client.getUsername()
                        );

                client.setWorldScene(
                        worldScene
                );

                root =
                        worldScene.getRoot();

                break;

            default:

                throw new IllegalStateException(
                        "Unknown scene: " + type
                );
        }

        stage.setScene(
                new Scene(
                        root,
                        1920,
                        1080
                )
        );

        stage.setMaximized(true);
    }
}