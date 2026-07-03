package game.ui.register;

import game.auth.AccountType;
import game.ui.SceneManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginPasswordStep {

    private final VBox root = new VBox(20);

    public LoginPasswordStep(
            SceneManager sceneManager,
            RegistrationSession session
    ) {

        root.setAlignment(Pos.CENTER);

        TextField login = new TextField();
        login.setPromptText("Введите логин");

        PasswordField password = new PasswordField();
        password.setPromptText("Введите пароль");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button next = new Button("Далее");

        next.setOnAction(e -> {

            String loginText = login.getText().trim();
            String passwordText = password.getText().trim();

            if (loginText.isEmpty()) {
                errorLabel.setText("Введите логин");
                return;
            }

            if (passwordText.isEmpty()) {
                errorLabel.setText("Введите пароль");
                return;
            }

            session.setLogin(loginText);
            session.setPassword(passwordText);

            // 💥 ВАЖНАЯ ЛОГИКА:
            if (session.getAccountType() == AccountType.SPECTATOR) {
                sceneManager.finishRegistration(session);
            } else {
                sceneManager.showAgreement(session);
            }
        });

        root.getChildren().addAll(
                login,
                password,
                errorLabel,
                next
        );
    }

    public Parent getRoot() {
        return root;
    }
}