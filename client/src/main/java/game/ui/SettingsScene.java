package game.ui;

import game.audio.MusicManager;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SettingsScene {

    private final StackPane root =
            new StackPane();

    public SettingsScene(
            Runnable onBack
    ) {

        /*
         * =================================================
         * ЗАТЕМНЕНИЕ ФОНА
         * =================================================
         */

        Rectangle overlay =
                new Rectangle();

        overlay.setFill(
                Color.rgb(
                        0,
                        0,
                        0,
                        0.65
                )
        );

        overlay.widthProperty()
                .bind(root.widthProperty());

        overlay.heightProperty()
                .bind(root.heightProperty());


        /*
         * =================================================
         * ПАНЕЛЬ НАСТРОЕК
         * =================================================
         */

        VBox panel =
                new VBox(20);

        panel.setAlignment(
                Pos.CENTER
        );

        panel.setMaxWidth(500);
        panel.setMaxHeight(360);

        panel.setStyle("""
                -fx-background-color: rgba(25,25,30,0.96);
                -fx-background-radius: 16;
                -fx-border-color: rgba(255,255,255,0.18);
                -fx-border-width: 1;
                -fx-border-radius: 16;
                -fx-padding: 35;
                """);


        /*
         * =================================================
         * ЗАГОЛОВОК
         * =================================================
         */

        Label title =
                new Label("НАСТРОЙКИ");

        title.setTextFill(
                Color.WHITE
        );

        title.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        28
                )
        );


        /*
         * =================================================
         * ГРОМКОСТЬ
         * =================================================
         */

        Label volumeTitle =
                new Label("Громкость музыки");

        volumeTitle.setTextFill(
                Color.WHITE
        );

        volumeTitle.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );


        /*
         * =================================================
         * SLIDER
         * =================================================
         */

        Slider volumeSlider =
                new Slider(
                        0,
                        100,
                        MusicManager.getVolume() * 100
                );

        volumeSlider.setPrefWidth(400);

        volumeSlider.setShowTickMarks(true);
        volumeSlider.setShowTickLabels(true);

        volumeSlider.setMajorTickUnit(25);
        volumeSlider.setMinorTickCount(4);

        volumeSlider.setBlockIncrement(5);


        /*
         * =================================================
         * ПРОЦЕНТ ГРОМКОСТИ
         * =================================================
         */

        Label volumeValue =
                new Label();

        volumeValue.setTextFill(
                Color.WHITE
        );

        volumeValue.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        16
                )
        );


        updateVolumeLabel(
                volumeValue,
                volumeSlider.getValue()
        );


        /*
         * =================================================
         * ИЗМЕНЕНИЕ ГРОМКОСТИ
         * =================================================
         */

        volumeSlider.valueProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {

                            double value =
                                    newValue.doubleValue();

                            MusicManager.setVolume(
                                    value / 100.0
                            );

                            updateVolumeLabel(
                                    volumeValue,
                                    value
                            );
                        }
                );


        /*
         * =================================================
         * КНОПКА НАЗАД
         * =================================================
         */

        Button backButton =
                new Button("НАЗАД");

        backButton.setPrefWidth(180);
        backButton.setPrefHeight(42);

        backButton.setStyle("""
                -fx-font-size: 15px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                -fx-background-color: #38383f;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                """);

        backButton.setOnAction(
                event -> {

                    if (onBack != null) {
                        onBack.run();
                    }
                }
        );


        /*
         * =================================================
         * СОБИРАЕМ ПАНЕЛЬ
         * =================================================
         */

        panel.getChildren().addAll(
                title,
                volumeTitle,
                volumeSlider,
                volumeValue,
                backButton
        );


        /*
         * =================================================
         * ROOT
         * =================================================
         */

        root.getChildren().addAll(
                overlay,
                panel
        );
    }

    private void updateVolumeLabel(
            Label label,
            double value
    ) {

        label.setText(
                String.format(
                        "Громкость: %.0f%%",
                        value
                )
        );
    }

    public Parent getRoot() {

        return root;
    }
}