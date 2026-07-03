package game.ui;

import game.client.NetworkClient;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginScene {

    private final VBox root = new VBox(15);

    public LoginScene(SceneManager sceneManager) {

        root.setAlignment(Pos.CENTER);

        Label title = new Label("LT2 RPG");

        TextField loginField = new TextField();
        loginField.setPromptText("Логин");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");

        Button loginButton = new Button("Войти");

        Button registerButton = new Button("Регистрация");

        loginButton.setOnAction(e -> {

            String login =
                    loginField.getText().trim();

            sceneManager.setUsername(login);

            sceneManager.getClient().send(
                    "LOGIN:" +
                            login + ":" +
                            passwordField.getText().trim()
            );
        });

        registerButton.setOnAction(e -> {
            sceneManager.startRegistration();
        });

        root.getChildren().addAll(
                title,
                loginField,
                passwordField,
                loginButton,
                registerButton
        );
    }

    public Parent getRoot() {
        return root;
    }
}