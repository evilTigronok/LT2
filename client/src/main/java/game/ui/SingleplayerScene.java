package game.ui;

import game.save.Difficulty;
import game.save.SaveData;
import game.save.SaveManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SingleplayerScene {

    private final VBox root =
            new VBox(15);

    private final SceneManager sceneManager;

    private final ComboBox<Difficulty> difficultyBox =
            new ComboBox<>();

    private final CheckBox oneHpModeBox =
            new CheckBox("Режим 1 HP (стикмен)");

    private final CheckBox hardcoreModeBox =
            new CheckBox("Режим Hardcore");

    public SingleplayerScene(
            SceneManager sceneManager
    ) {

        this.sceneManager =
                sceneManager;

        root.setAlignment(
                Pos.CENTER
        );

        Label title =
                new Label(
                        "ОДИНОЧНАЯ ИГРА"
                );

        difficultyBox
                .getItems()
                .addAll(
                        Difficulty.RELAXING,
                        Difficulty.EXHAUSTING,
                        Difficulty.DIVINE
                );

        difficultyBox.setValue(
                Difficulty.EXHAUSTING
        );

        root.getChildren().addAll(
                title,
                new Label("Сложность новой игры:"),
                difficultyBox,
                oneHpModeBox
        );

        for (int slot = 1; slot <= 3; slot++) {

            root.getChildren().add(
                    createSaveSlot(slot)
            );
        }

        Button back =
                new Button(
                        "НАЗАД"
                );

        back.setOnAction(e ->
                sceneManager.show(
                        SceneType.MAIN_MENU
                )
        );

        root.getChildren().add(
                back
        );
    }

    private VBox createSaveSlot(
            int slot
    ) {

        SaveData save =
                SaveManager.load(
                        slot
                );

        Label label;

        if (save == null) {

            label =
                    new Label(
                            "Слот " + slot
                                    + " — ПУСТО"
                    );

        } else {

            label =
                    new Label(
                            "Слот " + slot
                                    + " — "
                                    + save.difficulty
                                    + " — "
                                    + formatDate(
                                    save.lastSavedAt
                            )
                    );
        }

        Button newGame =
                new Button(
                        "НОВАЯ ИГРА"
                );

        newGame.setOnAction(e -> {
            sceneManager.startNewGame(
                    difficultyBox.getValue(),
                    slot,
                    oneHpModeBox.isSelected(),
                    hardcoreModeBox.isSelected()
            );
        });

        Button continueButton =
                new Button(
                        "ПРОДОЛЖИТЬ"
                );

        continueButton.setDisable(
                save == null
        );

        continueButton.setOnAction(e -> {

            SaveData current =
                    SaveManager.load(slot);

            if (current != null) {

                sceneManager.continueGame(
                        current
                );
            }
        });

        HBox buttons =
                new HBox(
                        10,
                        newGame,
                        continueButton
                );

        buttons.setAlignment(
                Pos.CENTER
        );

        VBox box =
                new VBox(
                        5,
                        label,
                        buttons
                );

        box.setAlignment(
                Pos.CENTER
        );

        return box;
    }

    private String formatDate(
            long timestamp
    ) {

        if (timestamp <= 0) {
            return "—";
        }

        return new java.text.SimpleDateFormat(
                "dd.MM.yyyy HH:mm"
        ).format(
                new java.util.Date(
                        timestamp
                )
        );
    }

    public Parent getRoot() {
        return root;
    }
}