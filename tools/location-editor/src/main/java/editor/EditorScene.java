package editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import editor.assets.AssetLoader;
import game.world.data.PlacedObjectData;
import game.world.data.WorldData;
import game.world.data.WorldIO;
import game.world.objects.ObjectDefinition;
import game.world.objects.ObjectRegistry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorScene {

    private static final int DEFAULT_WORLD_WIDTH =
            10000;

    private static final int DEFAULT_WORLD_HEIGHT =
            10000;

    private static final double MIN_ZOOM =
            0.1;

    private static final double MAX_ZOOM =
            4.0;

    private final BorderPane root =
            new BorderPane();

    private final Canvas canvas =
            new Canvas(
                    1200,
                    800
            );

    private final GraphicsContext g =
            canvas.getGraphicsContext2D();

    private final ObjectRegistry registry =
            new ObjectRegistry();

    private final List<ObjectPosition> objects =
            new ArrayList<>();

    private final ListView<ObjectDefinition> objectList =
            new ListView<>();

    private final Label coordinatesLabel =
            new Label(
                    "X: 0  Y: 0"
            );

    private final Label zoomLabel =
            new Label(
                    "Zoom: 100%"
            );

    private final TextField worldFileField =
            new TextField(
                    "world.json"
            );

    private final ComboBox<String> biomeBox =
            new ComboBox<>();

    private final CheckBox rainCheck =
            new CheckBox("Rain");

    private final CheckBox snowCheck =
            new CheckBox("Snow");

    private final CheckBox fogCheck =
            new CheckBox("Fog");

    private ObjectDefinition selectedDefinition;

    private int worldWidth =
            DEFAULT_WORLD_WIDTH;

    private int worldHeight =
            DEFAULT_WORLD_HEIGHT;

    /**
     * Камера в мировых координатах.
     */
    private double cameraX =
            DEFAULT_WORLD_WIDTH / 2.0;

    private double cameraY =
            DEFAULT_WORLD_HEIGHT / 2.0;

    /**
     * Масштаб редактора.
     */
    private double zoom = 0.25;

    private boolean panning;

    private double lastMouseX;
    private double lastMouseY;

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

        root.setLeft(
                createLeftPanel()
        );

        root.setCenter(
                createCenter()
        );

        root.setBottom(
                createBottomBar()
        );
    }

    private VBox createLeftPanel() {

        VBox panel =
                new VBox(10);

        panel.setPadding(
                new Insets(15)
        );

        panel.setPrefWidth(
                320
        );

        panel.setStyle("""
                -fx-background-color: #2a2a2a;
                """);

        Label title =
                new Label(
                        "WORLD EDITOR"
                );

        title.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                """);

        Label worldLabel =
                createLabel(
                        "World"
                );

        Label sizeLabel =
                createLabel(
                        "Size: "
                                + worldWidth
                                + " × "
                                + worldHeight
                );

        Label biomeLabel =
                createLabel(
                        "Biome"
                );

        biomeBox.getItems().addAll(
                "Forest",
                "Desert",
                "Snow",
                "Swamp"
        );

        biomeBox.getSelectionModel()
                .selectFirst();

        styleComboBox(
                biomeBox
        );

        Label weatherLabel =
                createLabel(
                        "Weather"
                );

        styleCheckBox(
                rainCheck
        );

        styleCheckBox(
                snowCheck
        );

        styleCheckBox(
                fogCheck
        );

        Label objectsLabel =
                createLabel(
                        "Objects"
                );

        objectList.setPrefHeight(
                420
        );

        objectList.setStyle("""
                -fx-control-inner-background: #2a2a2a;
                -fx-background-color: #2a2a2a;
                """);

        objectList.setCellFactory(
                param ->
                        new ListCell<>() {

                            @Override
                            protected void updateItem(
                                    ObjectDefinition item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (
                                        empty
                                                || item == null
                                ) {

                                    setText(
                                            null
                                    );

                                    return;
                                }

                                setText(
                                        item.getName()
                                                + " ["
                                                + item.getType()
                                                + "]"
                                );

                                setTextFill(
                                        Color.WHITE
                                );

                                setStyle("""
                                        -fx-background-color: #2a2a2a;
                                        """);
                            }
                        }
        );

        objectList.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                selectedDefinition =
                                        newValue
                );

        Button newButton =
                new Button(
                        "New World"
                );

        Button openButton =
                new Button(
                        "Open World"
                );

        Button saveButton =
                new Button(
                        "Save World"
                );

        styleButton(
                newButton
        );

        styleButton(
                openButton
        );

        styleButton(
                saveButton
        );

        newButton.setOnAction(
                e -> newWorld()
        );

        openButton.setOnAction(
                e -> openWorld()
        );

        saveButton.setOnAction(
                e -> saveWorld()
        );

        panel.getChildren().addAll(
                title,

                worldLabel,
                sizeLabel,

                new Separator(),

                biomeLabel,
                biomeBox,

                weatherLabel,
                rainCheck,
                snowCheck,
                fogCheck,

                new Separator(),

                objectsLabel,
                objectList,

                newButton,
                openButton,
                saveButton
        );

        return panel;
    }

    private StackPane createCenter() {

        StackPane pane =
                new StackPane();

        pane.setAlignment(
                Pos.CENTER
        );

        canvas.widthProperty()
                .bind(
                        pane.widthProperty()
                );

        canvas.heightProperty()
                .bind(
                        pane.heightProperty()
                );

        canvas.setOnMousePressed(
                this::mousePressed
        );

        canvas.setOnMouseDragged(
                this::mouseDragged
        );

        canvas.setOnMouseReleased(
                e -> panning = false
        );

        canvas.setOnMouseClicked(
                this::mouseClicked
        );

        canvas.setOnScroll(
                this::mouseScrolled
        );

        pane.getChildren().add(
                canvas
        );

        return pane;
    }

    private HBox createBottomBar() {

        HBox bar =
                new HBox(30);

        bar.setPadding(
                new Insets(8)
        );

        bar.setStyle("""
                -fx-background-color: #252525;
                """);

        coordinatesLabel.setTextFill(
                Color.WHITE
        );

        zoomLabel.setTextFill(
                Color.WHITE
        );

        Label help =
                new Label(
                        "LMB: place | RMB: delete | " +
                                "MMB: pan | Wheel: zoom"
                );

        help.setTextFill(
                Color.LIGHTGRAY
        );

        bar.getChildren().addAll(
                coordinatesLabel,
                zoomLabel,
                help
        );

        return bar;
    }

    // =====================================================
    // MOUSE
    // =====================================================

    private void mousePressed(
            MouseEvent event
    ) {

        if (
                event.getButton()
                        == MouseButton.MIDDLE
        ) {

            panning = true;

            lastMouseX =
                    event.getX();

            lastMouseY =
                    event.getY();
        }
    }

    private void mouseDragged(
            MouseEvent event
    ) {

        if (!panning) {
            return;
        }

        double dx =
                event.getX()
                        - lastMouseX;

        double dy =
                event.getY()
                        - lastMouseY;

        cameraX -=
                dx / zoom;

        cameraY -=
                dy / zoom;

        clampCamera();

        lastMouseX =
                event.getX();

        lastMouseY =
                event.getY();

        render();
    }

    private void mouseClicked(
            MouseEvent event
    ) {

        if (
                event.getButton()
                        == MouseButton.MIDDLE
        ) {
            return;
        }

        double worldX =
                screenToWorldX(
                        event.getX()
                );

        double worldY =
                screenToWorldY(
                        event.getY()
                );

        updateCoordinates(
                worldX,
                worldY
        );

        if (
                event.getButton()
                        == MouseButton.SECONDARY
        ) {

            removeObjectAt(
                    worldX,
                    worldY
            );

            render();

            return;
        }

        if (
                event.getButton()
                        != MouseButton.PRIMARY
        ) {
            return;
        }

        if (
                selectedDefinition == null
        ) {
            return;
        }

        int objectX =
                (int) worldX
                        - selectedDefinition.getWidth()
                        / 2;

        int objectY =
                (int) worldY
                        - selectedDefinition.getHeight()
                        / 2;

        /*
         * Не разрешаем ставить объект
         * за пределы мира.
         */
        if (
                objectX < 0
                        ||
                        objectY < 0
                        ||
                        objectX
                                + selectedDefinition.getWidth()
                                > worldWidth
                        ||
                        objectY
                                + selectedDefinition.getHeight()
                                > worldHeight
        ) {
            return;
        }

        objects.add(
                new ObjectPosition(
                        selectedDefinition.getId(),
                        objectX,
                        objectY
                )
        );

        render();
    }

    private void mouseScrolled(
            ScrollEvent event
    ) {

        double oldZoom =
                zoom;

        if (event.getDeltaY() > 0) {
            zoom *= 1.1;
        } else {
            zoom /= 1.1;
        }

        zoom =
                Math.max(
                        MIN_ZOOM,
                        Math.min(
                                MAX_ZOOM,
                                zoom
                        )
                );

        /*
         * Сохраняем мировую точку
         * под курсором на том же месте.
         */
        double mouseWorldX =
                screenToWorldX(
                        event.getX(),
                        oldZoom
                );

        double mouseWorldY =
                screenToWorldY(
                        event.getY(),
                        oldZoom
                );

        double newMouseWorldX =
                screenToWorldX(
                        event.getX()
                );

        double newMouseWorldY =
                screenToWorldY(
                        event.getY()
                );

        cameraX +=
                mouseWorldX
                        - newMouseWorldX;

        cameraY +=
                mouseWorldY
                        - newMouseWorldY;

        clampCamera();

        zoomLabel.setText(
                "Zoom: "
                        + (int) (zoom * 100)
                        + "%"
        );

        render();
    }

    // =====================================================
    // COORDINATES
    // =====================================================

    private double screenToWorldX(
            double screenX
    ) {

        return screenToWorldX(
                screenX,
                zoom
        );
    }

    private double screenToWorldX(
            double screenX,
            double usedZoom
    ) {

        return cameraX
                + (
                screenX
                        - canvas.getWidth() / 2
        ) / usedZoom;
    }

    private double screenToWorldY(
            double screenY
    ) {

        return screenToWorldY(
                screenY,
                zoom
        );
    }

    private double screenToWorldY(
            double screenY,
            double usedZoom
    ) {

        return cameraY
                + (
                screenY
                        - canvas.getHeight() / 2
        ) / usedZoom;
    }

    private void updateCoordinates(
            double x,
            double y
    ) {

        coordinatesLabel.setText(
                "X: "
                        + (int) x
                        + "    Y: "
                        + (int) y
        );
    }

    // =====================================================
    // CAMERA
    // =====================================================

    private void clampCamera() {

        double visibleWidth =
                canvas.getWidth()
                        / zoom;

        double visibleHeight =
                canvas.getHeight()
                        / zoom;

        double halfWidth =
                visibleWidth / 2;

        double halfHeight =
                visibleHeight / 2;

        cameraX =
                Math.max(
                        halfWidth,
                        Math.min(
                                worldWidth
                                        - halfWidth,
                                cameraX
                        )
                );

        cameraY =
                Math.max(
                        halfHeight,
                        Math.min(
                                worldHeight
                                        - halfHeight,
                                cameraY
                        )
                );
    }

    // =====================================================
    // OBJECTS
    // =====================================================

    private void loadObjects() {

        registry.load(
                "../../server/data/objects"
        );

        objectList.getItems().setAll(
                registry.getAll()
        );
    }

    private void removeObjectAt(
            double mouseX,
            double mouseY
    ) {

        ObjectPosition found = null;

        /*
         * Идём с конца, чтобы при перекрытии
         * удалялся верхний объект.
         */
        for (
                int i = objects.size() - 1;
                i >= 0;
                i--
        ) {

            ObjectPosition object =
                    objects.get(i);

            ObjectDefinition definition =
                    registry.getById(
                            object.objectId
                    );

            if (definition == null) {
                continue;
            }

            if (
                    mouseX >= object.x
                            &&
                            mouseX <=
                                    object.x
                                            + definition.getWidth()
                            &&
                            mouseY >= object.y
                            &&
                            mouseY <=
                                    object.y
                                            + definition.getHeight()
            ) {

                found = object;

                break;
            }
        }

        if (found != null) {
            objects.remove(found);
        }
    }

    // =====================================================
    // RENDER
    // =====================================================

    private void render() {

        double width =
                canvas.getWidth();

        double height =
                canvas.getHeight();

        g.setFill(
                Color.web("#151515")
        );

        g.fillRect(
                0,
                0,
                width,
                height
        );

        g.save();

        /*
         * Камера.
         */
        g.translate(
                width / 2,
                height / 2
        );

        g.scale(
                zoom,
                zoom
        );

        g.translate(
                -cameraX,
                -cameraY
        );

        renderTerrain();
        renderGrid();
        renderObjects();
        renderBorder();

        g.restore();
    }

    private void renderTerrain() {

        g.setFill(
                Color.web("#5d8a52")
        );

        double visibleWidth =
                canvas.getWidth()
                        / zoom;

        double visibleHeight =
                canvas.getHeight()
                        / zoom;

        double left =
                Math.max(
                        0,
                        cameraX
                                - visibleWidth / 2
                );

        double top =
                Math.max(
                        0,
                        cameraY
                                - visibleHeight / 2
                );

        double right =
                Math.min(
                        worldWidth,
                        cameraX
                                + visibleWidth / 2
                );

        double bottom =
                Math.min(
                        worldHeight,
                        cameraY
                                + visibleHeight / 2
                );

        g.fillRect(
                left,
                top,
                right - left,
                bottom - top
        );
    }

    private void renderGrid() {

        double visibleWidth =
                canvas.getWidth()
                        / zoom;

        double visibleHeight =
                canvas.getHeight()
                        / zoom;

        double left =
                Math.max(
                        0,
                        cameraX
                                - visibleWidth / 2
                );

        double top =
                Math.max(
                        0,
                        cameraY
                                - visibleHeight / 2
                );

        double right =
                Math.min(
                        worldWidth,
                        cameraX
                                + visibleWidth / 2
                );

        double bottom =
                Math.min(
                        worldHeight,
                        cameraY
                                + visibleHeight / 2
                );

        int gridSize =
                zoom >= 0.5
                        ? 100
                        : 500;

        g.setStroke(
                Color.rgb(
                        255,
                        255,
                        255,
                        0.08
                )
        );

        int startX =
                ((int) left / gridSize)
                        * gridSize;

        int startY =
                ((int) top / gridSize)
                        * gridSize;

        for (
                int x = startX;
                x <= right;
                x += gridSize
        ) {

            g.strokeLine(
                    x,
                    top,
                    x,
                    bottom
            );
        }

        for (
                int y = startY;
                y <= bottom;
                y += gridSize
        ) {

            g.strokeLine(
                    left,
                    y,
                    right,
                    y
            );
        }
    }

    private void renderObjects() {

        for (ObjectPosition position :
                objects) {

            ObjectDefinition definition =
                    registry.getById(
                            position.objectId
                    );

            if (definition == null) {
                continue;
            }

            Image image =
                    null;

            if (
                    definition.getTexture()
                            != null
            ) {

                image =
                        AssetLoader.load(
                                definition.getTexture()
                        );
            }

            if (image != null) {

                g.drawImage(
                        image,
                        position.x,
                        position.y,
                        definition.getWidth(),
                        definition.getHeight()
                );

            } else {

                g.setFill(
                        Color.YELLOW
                );

                g.fillRect(
                        position.x,
                        position.y,
                        definition.getWidth(),
                        definition.getHeight()
                );
            }
        }
    }

    private void renderBorder() {

        g.setStroke(
                Color.WHITE
        );

        g.setLineWidth(
                4 / zoom
        );

        g.strokeRect(
                0,
                0,
                worldWidth,
                worldHeight
        );
    }

    // =====================================================
    // SAVE
    // =====================================================

    private void saveWorld() {

        try {

            WorldData world =
                    new WorldData(
                            worldWidth,
                            worldHeight
                    );

            world.biome =
                    biomeBox.getValue();

            world.rain =
                    rainCheck.isSelected();

            world.snow =
                    snowCheck.isSelected();

            world.fog =
                    fogCheck.isSelected();

            for (
                    ObjectPosition position :
                    objects
            ) {

                world.objects.add(
                        new PlacedObjectData(
                                position.objectId,
                                position.x,
                                position.y
                        )
                );
            }

            File file =
                    new File(
                            "../../server/data/world",
                            worldFileField.getText()
                    );

            WorldIO.save(
                    world,
                    file
            );

            showMessage(
                    "World saved:\n"
                            + file.getAbsolutePath()
            );

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Cannot save world:\n"
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // OPEN
    // =====================================================

    private void openWorld() {

        try {

            File file =
                    new File(
                            "../../server/data/world",
                            worldFileField.getText()
                    );

            if (!file.exists()) {

                showError(
                        "World file not found:\n"
                                + file.getAbsolutePath()
                );

                return;
            }

            WorldData world =
                    WorldIO.load(
                            file
                    );

            worldWidth =
                    world.width;

            worldHeight =
                    world.height;

            objects.clear();

            for (
                    PlacedObjectData object :
                    world.objects
            ) {

                objects.add(
                        new ObjectPosition(
                                object.objectId,
                                object.x,
                                object.y
                        )
                );
            }

            biomeBox.setValue(
                    world.biome
            );

            rainCheck.setSelected(
                    world.rain
            );

            snowCheck.setSelected(
                    world.snow
            );

            fogCheck.setSelected(
                    world.fog
            );

            cameraX =
                    worldWidth / 2.0;

            cameraY =
                    worldHeight / 2.0;

            clampCamera();

            render();

            showMessage(
                    "World loaded."
            );

        } catch (Exception e) {

            e.printStackTrace();

            showError(
                    "Cannot open world:\n"
                            + e.getMessage()
            );
        }
    }

    // =====================================================
    // NEW WORLD
    // =====================================================

    private void newWorld() {

        objects.clear();

        worldWidth =
                DEFAULT_WORLD_WIDTH;

        worldHeight =
                DEFAULT_WORLD_HEIGHT;

        cameraX =
                worldWidth / 2.0;

        cameraY =
                worldHeight / 2.0;

        zoom =
                0.25;

        biomeBox.getSelectionModel()
                .selectFirst();

        rainCheck.setSelected(
                false
        );

        snowCheck.setSelected(
                false
        );

        fogCheck.setSelected(
                false
        );

        zoomLabel.setText(
                "Zoom: 25%"
        );

        render();
    }

    // =====================================================
    // OBJECT POSITION
    // =====================================================

    private static class ObjectPosition {

        private final String objectId;

        private final int x;

        private final int y;

        private ObjectPosition(
                String objectId,
                int x,
                int y
        ) {

            this.objectId =
                    objectId;

            this.x = x;
            this.y = y;
        }
    }

    // =====================================================
    // DIALOGS
    // =====================================================

    private void showMessage(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.show();
    }

    private void showError(
            String message
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setHeaderText(
                "Error"
        );

        alert.setContentText(
                message
        );

        alert.show();
    }

    // =====================================================
    // STYLE
    // =====================================================

    private Label createLabel(
            String text
    ) {

        Label label =
                new Label(
                        text
                );

        label.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 15px;
                -fx-font-weight: bold;
                """);

        return label;
    }

    private void styleButton(
            Button button
    ) {

        button.setStyle("""
                -fx-background-color: #4c8cff;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                """);
    }

    private void styleComboBox(
            ComboBox<?> box
    ) {

        box.setStyle("""
                -fx-background-color: #3a3a3a;
                -fx-text-fill: white;
                """);
    }

    private void styleCheckBox(
            CheckBox box
    ) {

        box.setStyle("""
                -fx-text-fill: white;
                """);
    }
}