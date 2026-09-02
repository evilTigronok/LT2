package game.ui;

import game.client.HostManager;
import game.client.NetworkClient;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class LobbyScene {

    private final VBox root =
            new VBox(15);

    private final NetworkClient client;

    private final ListView<String> players =
            new ListView<>();

    private final Label connectionStatus =
            new Label(
                    "Подключение..."
            );

    private final Label pingLabel =
            new Label(
                    "Ping: -"
            );

    private final Label serverAddressLabel =
            new Label();

    private final Button startButton =
            new Button(
                    "НАЧАТЬ ИГРУ"
            );

    public LobbyScene(
            SceneManager sceneManager,
            NetworkClient client
    ) {

        this.client = client;

        client.setLobbyScene(this);

        root.setAlignment(
                Pos.CENTER
        );

        Label title =
                new Label(
                        "ЛОББИ"
                );

        players.setPrefWidth(500);
        players.setPrefHeight(300);

        // =====================================
        // SERVER ADDRESS
        // =====================================

        if (client.isHost()) {

            HostManager hostManager =
                    sceneManager
                            .getHostManager();

            List<String> addresses =
                    hostManager
                            .getConnectionAddresses();

            if (addresses.isEmpty()) {

                serverAddressLabel.setText(
                        "Адрес сервера не найден"
                );

            } else {

                StringBuilder text =
                        new StringBuilder();

                text.append(
                        "АДРЕСА ДЛЯ ПОДКЛЮЧЕНИЯ:\n"
                );

                for (String address :
                        addresses) {

                    text.append(
                            address
                    ).append("\n");
                }

                serverAddressLabel.setText(
                        text.toString()
                );
            }
        }

        // =====================================
        // START GAME
        // =====================================

        startButton.setVisible(
                client.isHost()
        );

        startButton.setManaged(
                client.isHost()
        );

        startButton.setOnAction(e -> {

            if (client.isHost()) {

                client.send(
                        "START_GAME"
                );
            }
        });

        // =====================================
        // DISCONNECT
        // =====================================

        Button disconnect =
                new Button(
                        "ОТКЛЮЧИТЬСЯ"
                );

        disconnect.setOnAction(e -> {

            boolean wasHost =
                    client.isHost();

            client.disconnect();

            if (wasHost) {

                sceneManager
                        .getHostManager()
                        .stopServer();
            }

            sceneManager.show(
                    SceneType.MULTIPLAYER
            );
        });

        // =====================================
        // GAME START
        // =====================================

        client.setGameStartHandler(() -> {

            System.out.println(
                    "GAME START"
            );

            sceneManager.show(
                    SceneType.WORLD
            );
        });

        root.getChildren().addAll(
                title,
                serverAddressLabel,
                connectionStatus,
                pingLabel,
                players,
                startButton,
                disconnect
        );
    }

    public Parent getRoot() {
        return root;
    }

    public void updateLobby(
            String data
    ) {

        players.getItems().clear();

        if (
                data == null
                        ||
                        data.isBlank()
        ) {
            return;
        }

        String[] parts =
                data.split(":");

        /*
         * Формат:
         *
         * LOBBY_STATE:
         * id:name:host:
         * id:name:host...
         */

        for (
                int i = 1;
                i + 2 < parts.length;
                i += 3
        ) {

            String id =
                    parts[i];

            String name =
                    parts[i + 1];

            boolean host =
                    Boolean.parseBoolean(
                            parts[i + 2]
                    );

            String entry =
                    (host
                            ? "[HOST] "
                            : "")
                            + name
                            + "  "
                            + id;

            players.getItems().add(
                    entry
            );
        }

        connectionStatus.setText(
                "CONNECTED"
        );

        pingLabel.setText(
                "Ping: "
                        + client.getPing()
                        + " ms"
        );
    }
}