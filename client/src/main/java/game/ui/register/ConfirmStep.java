package game.ui.register;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ConfirmStep {

    private VBox root;

    public ConfirmStep(
            RegistrationSession session,
            Runnable finish
    ) {

        root = new VBox(15);

        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);

        Label summary =
                new Label(
                        "Confirm Registration\n\n" +
                                "Login: " + session.getLogin() + "\n" +
                                "Character: " + session.getFullName() + "\n" +
                                "Eye Color: " + session.getEyeColor() + "\n" +
                                "Starter Item: " + session.getStarterItem()
                );

        Button confirm =
                new Button("Confirm & Register");

        confirm.setOnAction(e -> {

            finish.run();
        });

        root.getChildren().addAll(
                summary,
                confirm
        );
    }

    public Parent getRoot() {
        return root;
    }
}