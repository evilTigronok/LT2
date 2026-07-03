package game.ui.register;

import game.ui.SceneManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StarterItemStep {

    private final VBox root = new VBox(20);

    public StarterItemStep(SceneManager sceneManager,
                           RegistrationSession session) {

        root.setAlignment(Pos.CENTER);

        Label info = new Label(
                "Шаг 6/6\n\n" +
                        "Контракт почти подписан, выбери исток, мечтатель"
        );

        Button sword = new Button("Сапфировый исток");
        Button shield = new Button("Изумрудный исток");
        Button amulet = new Button("Рубиновый исток");

        sword.setOnAction(e -> {
            session.setStarterItem("Sword");
            sceneManager.finishRegistration(session);
        });

        shield.setOnAction(e -> {
            session.setStarterItem("Shield");
            sceneManager.finishRegistration(session);
        });

        amulet.setOnAction(e -> {
            session.setStarterItem("Amulet");
            sceneManager.finishRegistration(session);
        });

        root.getChildren().addAll(
                info,
                sword,
                shield,
                amulet
        );
    }

    public Parent getRoot() {
        return root;
    }
}