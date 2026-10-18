package game.ui;

import game.client.HostManager;
import game.client.NetworkClient;
import game.save.Difficulty;
import game.save.SaveData;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private final Stage stage;

    private final NetworkClient client;

    private final HostManager hostManager;

    private SaveData pendingSave;

    private Difficulty pendingDifficulty =
            Difficulty.EXHAUSTING;

    private WorldScene currentWorldScene;

    private String username;

    public SceneManager(
            Stage stage,
            NetworkClient client
    ) {

        this.stage = stage;
        this.client = client;

        this.hostManager =
                new HostManager();
    }

    public void startNewGame(
            Difficulty difficulty
    ) {

        pendingSave = null;

        pendingDifficulty =
                difficulty;

        show(SceneType.WORLD);
    }
    public void continueGame(
            SaveData save
    ) {

        if (save == null) {
            return;
        }

        pendingSave =
                save;

        pendingDifficulty =
                save.difficulty;

        show(SceneType.WORLD);
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

    public void setUsername(String username) {
        this.username = username;
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

            case SINGLEPLAYER:

                root =
                        new SingleplayerScene(
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
                currentWorldScene = new WorldScene(
                        this,
                        client,
                        username,
                        pendingSave,
                        pendingDifficulty
                );

                client.setWorldScene(currentWorldScene);
                root = currentWorldScene.getRoot();
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

    public void saveCurrentGame() {
        if (currentWorldScene != null) {
            currentWorldScene.saveGame();
        }
    }
}