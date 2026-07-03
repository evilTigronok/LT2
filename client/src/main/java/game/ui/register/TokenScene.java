package game.ui.register;

import game.ui.SceneManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class TokenScene {

    private final VBox root = new VBox(20);

    public TokenScene(SceneManager sceneManager,
                      RegistrationSession session) {

        root.setAlignment(Pos.CENTER);

        Label info = new Label(
                "Шаг 2/6\n\n" +
                        "Сначала запросите токен у сервера.\n" +
                        "После получения токена введите его."
        );

        Button requestButton =
                new Button("Запросить токен");

        Label requestStatus =
                new Label();

        requestButton.setOnAction(e -> {

            sceneManager
                    .getClient()
                    .send("REQUEST_TOKEN");

            requestStatus.setText(
                    "Запрос отправлен.\n" +
                            "Ожидайте выдачи токена."
            );

            requestButton.setDisable(true);
        });

        TextField tokenField =
                new TextField();

        tokenField.setPromptText(
                "Введите токен"
        );

        Label errorLabel =
                new Label();

        Button next =
                new Button("Далее");

        next.setOnAction(e -> {

            String token = tokenField.getText();

            session.setToken(token);

            sceneManager.setPendingSession(session);

            sceneManager.getClient().send(
                    "CHECK_TOKEN:" + token
            );

        });

        root.getChildren().addAll(
                info,
                requestButton,
                requestStatus,
                tokenField,
                errorLabel,
                next
        );
    }

    public Parent getRoot() {
        return root;
    }
}