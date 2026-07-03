package game.ui.register;

import game.ui.SceneManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class CharacterStep {

    private final VBox root = new VBox(20);

    public CharacterStep(SceneManager sceneManager,
                         RegistrationSession session) {

        root.setAlignment(Pos.CENTER);

        Label info = new Label(
                "Шаг 5/6\n\n" +
                        "Создание персонажа"
        );

        TextField nameField = new TextField();
        nameField.setPromptText("Имя персонажа");

        ComboBox<String> eyeColorBox =
                new ComboBox<>();

        eyeColorBox.getItems().addAll(
                "Blue",
                "Green",
                "Brown",
                "Gray"
        );

        eyeColorBox.setValue("Blue");

        Button next = new Button("Далее");

        next.setOnAction(e -> {

            session.setFullName(
                    nameField.getText()
            );

            session.setEyeColor(
                    eyeColorBox.getValue()
            );

            sceneManager.showStarterItem(session);
        });

        root.getChildren().addAll(
                info,
                nameField,
                eyeColorBox,
                next
        );
    }

    public Parent getRoot() {
        return root;
    }
}