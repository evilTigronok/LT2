package game.ui;

import game.client.HostManager;
import game.client.NetworkClient;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class CreateGameScene {

    private final VBox root =
            new VBox(15);

    private final SceneManager sceneManager;
    private final NetworkClient client;

    public CreateGameScene(
            SceneManager sceneManager,
            NetworkClient client
    ) {

        this.sceneManager =
                sceneManager;

        this.client =
                client;

        root.setAlignment(
                Pos.CENTER
        );

        Label title =
                new Label(
                        "СОЗДАНИЕ ИГРЫ"
                );

        Label portLabel =
                new Label(
                        "Порт:"
                );

        TextField portField =
                new TextField("25565");

        portField.setMaxWidth(200);

        Label status =
                new Label();

        Button create =
                new Button(
                        "СОЗДАТЬ ЛОББИ"
                );

        Button back =
                new Button(
                        "НАЗАД"
                );

        create.setOnAction(e -> {

            int port;

            try {

                port =
                        Integer.parseInt(
                                portField
                                        .getText()
                                        .trim()
                        );

            } catch (NumberFormatException ex) {

                status.setText(
                        "Некорректный порт"
                );

                return;
            }

            if (port < 1 || port > 65535) {

                status.setText(
                        "Порт должен быть от 1 до 65535"
                );

                return;
            }

            HostManager hostManager =
                    sceneManager
                            .getHostManager();

            boolean started =
                    hostManager.startServer(
                            port
                    );

            if (!started) {

                status.setText(
                        "Не удалось запустить сервер"
                );

                return;
            }

            /*
             * Хост подключается к собственному
             * встроенному серверу.
             *
             * Это важно:
             * хост является обычным клиентом
             * собственного сервера.
             */

            boolean connected =
                    client.connect(
                            "127.0.0.1",
                            port,
                            true
                    );

            if (!connected) {

                status.setText(
                        "Не удалось подключиться к серверу"
                );

                hostManager.stopServer();

                return;
            }

            client.sendHello(
                    client.getUsername()
            );

            sceneManager.show(
                    SceneType.LOBBY
            );
        });

        back.setOnAction(e ->
                sceneManager.show(
                        SceneType.MULTIPLAYER
                )
        );

        root.getChildren().addAll(
                title,
                portLabel,
                portField,
                status,
                create,
                back
        );
    }

    public Parent getRoot() {
        return root;
    }
}