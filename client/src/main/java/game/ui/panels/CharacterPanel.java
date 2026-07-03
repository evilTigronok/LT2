package game.ui.panels;

import game.characters.Character;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class CharacterPanel extends VBox {

    private Character character;

    private Label nameLabel;
    private Label hpLabel;

    public CharacterPanel(
            Character character,
            Runnable onClick
    ) {

        this.character = character;

        setSpacing(5);

        setPadding(new Insets(10));

        setStyle(
                "-fx-border-color: white;" +
                        "-fx-border-width: 2;"
        );

        nameLabel =
                new Label(character.getName());

        hpLabel =
                new Label(
                        "HP: " +
                                character.getHp()
                );

        getChildren().addAll(
                nameLabel,
                hpLabel
        );

        setOnMouseClicked(e -> onClick.run());
    }

    public void update() {

        hpLabel.setText(
                "HP: " +
                        character.getHp()
        );

        if (!character.isAlive()) {

            setOpacity(0.4);

            setStyle(
                    "-fx-border-color: gray;" +
                            "-fx-border-width: 2;"
            );
        }
    }

    public Character getCharacter() {
        return character;
    }

    public void select() {

        setStyle(
                "-fx-border-color: red;" +
                        "-fx-border-width: 3;"
        );
    }

    public void deselect() {

        setStyle(
                "-fx-border-color: white;" +
                        "-fx-border-width: 2;"
        );
    }
}