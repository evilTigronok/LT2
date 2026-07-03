package editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import game.world.LocationObject;
import game.world.LocationObjectType;
import game.world.data.LocationData;
import game.world.data.PlacedObjectData;
import game.world.objects.ObjectDefinition;
import game.world.objects.ObjectRegistry;

import editor.assets.AssetLoader;
import javafx.scene.image.Image;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.control.*;

import javafx.scene.input.MouseButton;

import javafx.scene.layout.*;

import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import java.util.ArrayList;
import java.util.List;

public class EditorScene {

    private final BorderPane root =
            new BorderPane();

    private final Canvas canvas =
            new Canvas(1920, 1080);

    private final GraphicsContext g =
            canvas.getGraphicsContext2D();

    private final ObjectRegistry registry =
            new ObjectRegistry();

    private final List<ObjectPosition> objectPositions =
            new ArrayList<>();

    private final ListView<ObjectDefinition> objectList =
            new ListView<>();

    private final TextField locationNameField =
            new TextField();

    private final ComboBox<String> biomeBox =
            new ComboBox<>();

    private final CheckBox rainCheck =
            new CheckBox("Rain");

    private final CheckBox snowCheck =
            new CheckBox("Snow");

    private final CheckBox fogCheck =
            new CheckBox("Fog");

    private final Spinner<Integer> worldXSpinner =
            new Spinner<>(-999, 999, 0);

    private final Spinner<Integer> worldYSpinner =
            new Spinner<>(-999, 999, 0);

    private ObjectDefinition selectedDefinition;

    public EditorScene() {

        loadObjects();

        createUI();

        render();
    }

    public Parent getRoot() {
        return root;
    }

    // =====================================================
    // UI
    // =====================================================

    private void createUI() {

        root.setStyle("""
                -fx-background-color: #1e1e1e;
                """);

        VBox leftPanel =
                createLeftPanel();

        StackPane center =
                createCenter();

        root.setLeft(leftPanel);
        root.setCenter(center);
    }

    private VBox createLeftPanel() {

        VBox panel = new VBox(12);

        Label posLabel =
                createLabel("World Position");

        panel.setPadding(
                new Insets(15)
        );

        panel.setPrefWidth(320);

        panel.getChildren().addAll(
                posLabel,
                worldXSpinner,
                worldYSpinner
        );

        panel.setStyle("""
                -fx-background-color: #2a2a2a;
                """);

        Label title = new Label(
                "LOCATION EDITOR"
        );

        title.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                """);

        Label locationLabel =
                createLabel("Location Name");

        styleTextField(locationNameField);

        Label biomeLabel =
                createLabel("Biome");

        biomeBox.getItems().addAll(
                "Forest",
                "Desert",
                "Snow",
                "Swamp"
        );

        biomeBox.getSelectionModel()
                .selectFirst();

        styleComboBox(biomeBox);

        Label weatherLabel =
                createLabel("Weather");

        styleCheckBox(rainCheck);
        styleCheckBox(snowCheck);
        styleCheckBox(fogCheck);

        Label objectsLabel =
                createLabel("Objects");

        objectList.setPrefHeight(400);

        objectList.setStyle("""
                -fx-control-inner-background: #2a2a2a;
                -fx-background-color: #2a2a2a;
                """);

        objectList.setCellFactory(param -> new ListCell<>() {

            @Override
            protected void updateItem(
                    ObjectDefinition item,
                    boolean empty
            ) {

                super.updateItem(item, empty);

                if (empty || item == null) {

                    setText(null);
                    return;
                }

                setText(
                        item.getName()
                                + " [" +
                                item.getType()
                                + "]"
                );

                setTextFill(Color.WHITE);

                setStyle("""
                        -fx-background-color: #2a2a2a;
                        """);
            }
        });

        objectList.getItems().addAll(
                registry.getAll()
        );

        objectList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldV, newV) -> {
                    selectedDefinition = newV;
                });

        Button saveButton =
                new Button("Save");

        styleButton(saveButton);

        saveButton.setOnAction(e -> {
            saveLocation();
        });

        Button loadButton =
                new Button("Open");

        styleButton(loadButton);

        loadButton.setOnAction(e -> {

            File file =
                    new File(
                            "../../server/data/locations/"
                                    + locationNameField.getText()
                                    + ".json"
                    );

            if (file.exists()) {
                loadLocation(file);
            }
        });

        panel.getChildren().addAll(
                title,

                locationLabel,
                locationNameField,

                biomeLabel,
                biomeBox,

                weatherLabel,
                rainCheck,
                snowCheck,
                fogCheck,

                objectsLabel,
                objectList,

                saveButton,
                loadButton
        );

        return panel;
    }

    private StackPane createCenter() {

        StackPane pane =
                new StackPane();

        pane.setPadding(
                new Insets(15)
        );

        pane.setAlignment(Pos.CENTER);

        canvas.setOnMouseClicked(e -> {

            int x = (int) e.getX();
            int y = (int) e.getY();

            // удалить объект
            if (e.getButton() == MouseButton.SECONDARY) {

                removeObjectAt(x, y);

                render();

                return;
            }

            // поставить объект
            if (selectedDefinition == null) {
                return;
            }

            LocationObject object =
                    new LocationObject(
                            selectedDefinition.getId(),
                            selectedDefinition.getName(),
                            null
                    );

            int objectX =
                    x - selectedDefinition.getWidth() / 2;

            int objectY =
                    y - selectedDefinition.getHeight() / 2;

            objectPositions.add(
                    new ObjectPosition(
                            object,
                            objectX,
                            objectY
                    )
            );

            render();
        });

        pane.getChildren().add(canvas);

        return pane;
    }

    // =====================================================
    // OBJECT POSITION
    // =====================================================

    private static class ObjectPosition {

        private final LocationObject object;

        private final int x;

        private final int y;

        public ObjectPosition(
                LocationObject object,
                int x,
                int y
        ) {

            this.object = object;
            this.x = x;
            this.y = y;
        }
    }

    // =====================================================
    // REMOVE
    // =====================================================

    private void removeObjectAt(
            int mouseX,
            int mouseY
    ) {

        ObjectPosition found = null;

        for (ObjectPosition pos : objectPositions) {

            ObjectDefinition definition =
                    registry.getById(
                            pos.object.id
                    );

            if (definition == null) {
                continue;
            }

            int width =
                    definition.getWidth();

            int height =
                    definition.getHeight();

            if (
                    mouseX >= pos.x
                            && mouseX <= pos.x + width
                            && mouseY >= pos.y
                            && mouseY <= pos.y + height
            ) {

                found = pos;

                break;
            }
        }

        if (found != null) {

            objectPositions.remove(found);
        }
    }

    // =====================================================
    // RENDER
    // =====================================================

    private void render() {

        g.setFill(
                Color.web("#5d8a52")
        );

        g.fillRect(
                0,
                0,
                canvas.getWidth(),
                canvas.getHeight()
        );

        // сетка
        g.setStroke(
                Color.rgb(255,255,255,0.08)
        );

        for (int x = 0; x < 1920; x += 64) {

            g.strokeLine(
                    x,
                    0,
                    x,
                    1080
            );
        }

        for (int y = 0; y < 1080; y += 64) {

            g.strokeLine(
                    0,
                    y,
                    1920,
                    y
            );
        }

        for (ObjectPosition pos : objectPositions) {

            drawObject(pos);
        }
    }

    private void drawObject(
            ObjectPosition pos
    ) {

        ObjectDefinition definition =
                registry.getById(
                        pos.object.id
                );

        if (definition == null) {
            return;
        }

        if (definition.getTexture() != null) {

            Image image =
                    AssetLoader.load(
                            definition.getTexture()
                    );

            if (image != null) {

                g.drawImage(
                        image,
                        pos.x,
                        pos.y,
                        definition.getWidth(),
                        definition.getHeight()
                );

                return;
            }
        }

        g.setFill(Color.YELLOW);

        g.fillRect(
                pos.x,
                pos.y,
                40,
                40
        );
    }

    // =====================================================
    // OBJECTS
    // =====================================================

    private void loadObjects() {

        registry.load("../../server/data/objects");

        objectList.getItems().setAll(
                registry.getAll()
        );
    }

    // =====================================================
    // SAVE / LOAD
    // =====================================================

    private void saveLocation() {

        try {

            File dir =
                    new File(
                            "../../server/data/locations"
                    );

            if (!dir.exists()) {
                dir.mkdirs();
            }

            LocationData data =
                    new LocationData();

            data.id =
                    locationNameField.getText();

            data.biome =
                    biomeBox.getValue();

            data.rain =
                    rainCheck.isSelected();

            data.snow =
                    snowCheck.isSelected();

            data.fog =
                    fogCheck.isSelected();

            data.worldX =
                    worldXSpinner.getValue();

            data.worldY =
                    worldYSpinner.getValue();

            for (ObjectPosition pos : objectPositions) {

                PlacedObjectData obj =
                        new PlacedObjectData();

                obj.objectId =
                        pos.object.id;

                obj.x = pos.x;
                obj.y = pos.y;

                data.objects.add(obj);
            }

            Gson gson =
                    new GsonBuilder()
                            .setPrettyPrinting()
                            .create();

            File file =
                    new File(
                            dir,
                            data.id + ".json"
                    );

            FileWriter writer =
                    new FileWriter(file);

            gson.toJson(
                    data,
                    writer
            );

            writer.close();

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setHeaderText(null);

            alert.setContentText(
                    "Location saved"
            );

            alert.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void loadLocation(File file) {

        try {

            Gson gson =
                    new Gson();

            FileReader reader =
                    new FileReader(file);

            LocationData data =
                    gson.fromJson(
                            reader,
                            LocationData.class
                    );

            reader.close();

            objectPositions.clear();

            locationNameField.setText(
                    data.id
            );

            biomeBox.setValue(
                    data.biome
            );

            rainCheck.setSelected(
                    data.rain
            );

            snowCheck.setSelected(
                    data.snow
            );

            fogCheck.setSelected(
                    data.fog
            );

            worldXSpinner.getValueFactory()
                    .setValue(data.worldX);

            worldYSpinner.getValueFactory()
                    .setValue(data.worldY);

            for (PlacedObjectData obj
                    : data.objects) {

                LocationObject object =
                        new LocationObject(
                                obj.objectId,
                                obj.objectId,
                                null
                        );

                objectPositions.add(
                        new ObjectPosition(
                                object,
                                obj.x,
                                obj.y
                        )
                );
            }

            render();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // =====================================================
    // STYLE
    // =====================================================

    private Label createLabel(String text) {

        Label label =
                new Label(text);

        label.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 15px;
                -fx-font-weight: bold;
                """);

        return label;
    }

    private void styleButton(Button button) {

        button.setStyle("""
                -fx-background-color: #4c8cff;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                """);
    }

    private void styleTextField(TextField field) {

        field.setStyle("""
                -fx-background-color: #3a3a3a;
                -fx-text-fill: white;
                """);
    }

    private void styleComboBox(ComboBox<?> box) {

        box.setStyle("""
                -fx-background-color: #3a3a3a;
                -fx-text-fill: white;
                """);
    }

    private void styleCheckBox(CheckBox box) {

        box.setStyle("""
                -fx-text-fill: white;
                """);
    }
}