package game.ui;

import game.client.NetworkClient;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class JoinGameScene {

    private final VBox root =
            new VBox(15);

    public JoinGameScene(
            SceneManager sceneManager,
            NetworkClient client
    ) {

        root.setAlignment(
                Pos.CENTER
        );

        Label title =
                new Label(
                        "ПОДКЛЮЧЕНИЕ К ИГРЕ"
                );

        TextField addressField =
                new TextField();

        addressField.setPromptText(
                "IP-адрес"
        );

        addressField.setMaxWidth(300);

        TextField portField =
                new TextField("25565");

        portField.setPromptText(
                "Порт"
        );

        portField.setMaxWidth(300);

        Label status =
                new Label();

        Button connect =
                new Button(
                        "ПОДКЛЮЧИТЬСЯ"
                );

        Button back =
                new Button(
                        "НАЗАД"
                );

        connect.setOnAction(e -> {

            String address =
                    addressField
                            .getText()
                            .trim();

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

            if (address.isBlank()) {

                status.setText(
                        "Введите IP-адрес"
                );

                return;
            }


            boolean connected =
                    client.connect(
                            address,
                            port,
                            false
                    );

            if (!connected) {

                status.setText(
                        "Не удалось подключиться к серверу"
                );

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
                addressField,
                portField,
                status,
                connect,
                back
        );
    }

    public Parent getRoot() {
        return root;
    }
}