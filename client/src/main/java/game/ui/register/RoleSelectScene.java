package game.ui.register;

import game.auth.AccountType;
import game.ui.SceneManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class RoleSelectScene {

    private final VBox root = new VBox(20);

    public RoleSelectScene(
            SceneManager sceneManager,
            RegistrationSession session
    ) {


        root.setAlignment(Pos.CENTER);

        Label info = new Label(
                "Шаг 1/6\n\n" +
                        "Выберите роль"
        );

        Button player = new Button("Игрок");
        Button spectator = new Button("Зритель");

        player.setOnAction(e -> {
            session.setSpectator(false);
            session.setAccountType(AccountType.PLAYER);
            sceneManager.showToken(session);
        });

        spectator.setOnAction(e -> {
            session.setSpectator(true);
            session.setAccountType(AccountType.SPECTATOR);
            sceneManager.showLoginPassword(session);
        });

        root.getChildren().addAll(info, player, spectator);
    }

    public Parent getRoot() {
        return root;
    }
}