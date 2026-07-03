package game.ui;

import com.google.gson.Gson;
import game.client.NetworkClient;
import game.network.dto.PlayerState;
import game.network.packets.WorldStatePacket;
import game.ui.world.RemotePlayer;
import game.ui.world.WorldObject;
import game.world.data.LocationData;
import game.world.data.PlacedObjectData;
import game.world.objects.ObjectDefinition;
import game.world.objects.ObjectRegistry;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.*;

public class WorldScene {

    private final Pane root = new Pane();
    private final Canvas canvas = new Canvas();
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    private final NetworkClient client;
    private final String username;
    private final ObjectRegistry objectRegistry = new ObjectRegistry();

    private final Map<String, RemotePlayer> players = new HashMap<>();
    private final List<WorldObject> objects = new ArrayList<>();

    private boolean up, down, left, right;

    private final Label pingLabel = new Label();

    private int locationX = 0;
    private int locationY = 0;

    private static final double WORLD_WIDTH = 1920;
    private static final double WORLD_HEIGHT = 1080;

    private RemotePlayer localPlayer;

    public WorldScene(SceneManager sceneManager, NetworkClient client, String username) {
        this.client = client;
        this.username = username;

        root.setPrefSize(1920, 1080);

        root.getChildren().add(canvas);

        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        root.getChildren().add(pingLabel);

        pingLabel.setLayoutX(20);
        pingLabel.setLayoutY(10);
        pingLabel.setTextFill(Color.RED);

        Timeline pingTimeline =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(0.5),
                                e -> pingLabel.setText(
                                        "Ping: "
                                                + client.getPing()
                                                + " ms"
                                )
                        )
                );

        pingTimeline.setCycleCount(
                Animation.INDEFINITE
        );

        System.out.println(
                "WORLD USERNAME = [" +
                        username +
                        "]"
        );

        pingTimeline.play();

        objectRegistry.loadFromResources();

        setupInput();

        Platform.runLater(() ->
                canvas.requestFocus()
        );

        localPlayer =
                new RemotePlayer(
                        username,
                        300,
                        300
                );

        players.put(
                username,
                localPlayer
        );


        startLoop();
    }

    public Parent getRoot() {
        return root;
    }

    // ---------------- INPUT ----------------

    private void setupInput() {
        canvas.setFocusTraversable(true);

        canvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W) up = true;
            if (e.getCode() == KeyCode.S) down = true;
            if (e.getCode() == KeyCode.A) left = true;
            if (e.getCode() == KeyCode.D) right = true;
        });

        canvas.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.W) up = false;
            if (e.getCode() == KeyCode.S) down = false;
            if (e.getCode() == KeyCode.A) left = false;
            if (e.getCode() == KeyCode.D) right = false;
        });

        canvas.focusedProperty().addListener(
                (obs, oldValue, focused) -> {

                    if (!focused) {

                        up = false;
                        down = false;
                        left = false;
                        right = false;
                    }
                }
        );
    }

    // ---------------- LOOP ----------------

    private void startLoop() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        }.start();
    }

    private void update() {

        if (localPlayer != null) {

            // берём координаты сервера
            localPlayer.renderX = localPlayer.serverX;
            localPlayer.renderY = localPlayer.serverY;

        }

        // остальные игроки сглаживаются
        for (RemotePlayer p : players.values()) {

            if (p == localPlayer) {
                continue;
            }

            p.update();
        }

        sendInput();
    }

    private void sendInput() {
        client.send("INPUT:" + up + ":" + down + ":" + left + ":" + right);
    }

    // ---------------- BORDER ----------------



    // ---------------- RENDER ----------------

    private void render() {

        gc.clearRect(
                0,
                0,
                canvas.getWidth(),
                canvas.getHeight()
        );

        double scaleX =
                canvas.getWidth()
                        / WORLD_WIDTH;

        double scaleY =
                canvas.getHeight()
                        / WORLD_HEIGHT;

        double scale =
                Math.min(
                        scaleX,
                        scaleY
                );

        double offsetX =
                (canvas.getWidth()
                        - WORLD_WIDTH * scale)
                        / 2;

        double offsetY =
                (canvas.getHeight()
                        - WORLD_HEIGHT * scale)
                        / 2;

        gc.save();

        gc.translate(
                offsetX,
                offsetY
        );

        gc.scale(
                scale,
                scale
        );

        // Фон мира
        gc.setFill(
                Color.web("#2f6b3f")
        );

        gc.fillRect(
                0,
                0,
                WORLD_WIDTH,
                WORLD_HEIGHT
        );

        renderWorldObjects();
        renderPlayers();

        gc.setFill(Color.WHITE);

        gc.fillText(
                "LOC: " + locationX + "," + locationY,
                20,
                50
        );

        gc.fillText(
                "PLAYERS: " + players.size(),
                20,
                70
        );

        if (localPlayer != null) {

            gc.fillText(
                    "X: " + (int)localPlayer.renderX,
                    20,
                    90
            );

            gc.fillText(
                    "Y: " + (int)localPlayer.renderY,
                    20,
                    110
            );

            gc.fillText(
                    "SERVER X: " + (int)localPlayer.serverX,
                    20,
                    130
            );

            gc.fillText(
                    "SERVER Y: " + (int)localPlayer.serverY,
                    20,
                    150
            );
        }

        gc.restore();
    }

    private void renderWorldObjects() {
        for (WorldObject o : objects) {
            gc.drawImage(o.image, o.x, o.y, o.width, o.height);
        }
    }

    private void renderPlayers() {

        for (RemotePlayer player : players.values()) {

            if (
                    player.locationX != locationX
                            ||
                            player.locationY != locationY
            ) {
                continue;
            }

            gc.setFill(Color.RED);

            gc.fillRect(
                    player.renderX,
                    player.renderY,
                    40,
                    40
            );

            gc.setFill(Color.WHITE);

            gc.fillText(
                    player.username,
                    player.renderX,
                    player.renderY - 5
            );
        }
    }

    // ---------------- WORLD ----------------

    private void loadLocationByCoords(int x, int y) {


        try {
            File file = new File("server/data/locations/" + x + "_" + y + ".json");

            System.out.println(
                    file.getAbsolutePath()
            );
            System.out.println(
                    "TRY LOAD "
                            + x + "_" + y
            );
            System.out.println(
                    "EXISTS = "
                            + file.exists()
            );

            objects.clear();

            if (!file.exists()) {
                return;
            }

            Gson gson = new Gson();
            LocationData data = gson.fromJson(new FileReader(file), LocationData.class);

            loadLocation(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadLocation(LocationData data) {
        System.out.println(
                "LOAD LOCATION CALLED"
        );
        System.out.println(
                "WORLD OBJECTS = "
                        + objects.size()
        );

        objects.clear();

        if (data == null) return;

        for (PlacedObjectData placed : data.objects) {

            ObjectDefinition def =
                    objectRegistry.getById(
                            placed.objectId
                    );

            if (def == null) {

                System.out.println(
                        "OBJECT NOT FOUND: "
                                + placed.objectId
                );

                continue;
            }

            try {
                System.out.println(
                        "Texture = " + def.getTexture()
                );

                System.out.println(
                        "Stream = "
                                + getClass()
                                .getClassLoader()
                                .getResource(
                                        def.getTexture()
                                )
                );
                InputStream stream =
                        getClass().getClassLoader().getResourceAsStream(def.getTexture());

                if (stream == null) {
                    System.out.println("Missing texture: " + def.getTexture());
                    continue;
                }

                Image img = new Image(stream);

                System.out.println(
                        "Loaded: "
                                + def.getTexture()
                                + " width="
                                + img.getWidth()
                                + " height="
                                + img.getHeight()
                );


                objects.add(new WorldObject(
                        placed.x,
                        placed.y,
                        def.getWidth(),
                        def.getHeight(),
                        img
                ));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ---------------- NETWORK ----------------


    public void updateWorld(
            WorldStatePacket packet
    ) {

        Platform.runLater(() -> {

            for (PlayerState s : packet.players) {

                System.out.println(
                        "CHANGE LOCATION TO "
                                + locationX + ","
                                + locationY
                );

                RemotePlayer p =
                        players.get(s.username);

                if (p == null) {

                    p = new RemotePlayer(
                            s.username,
                            s.x,
                            s.y
                    );

                    players.put(
                            s.username,
                            p
                    );
                }

                p.serverX = s.x;
                p.serverY = s.y;

                p.locationX = s.locationX;
                p.locationY = s.locationY;

                if (s.username.equals(username)) {

                    p.renderX = s.x;
                    p.renderY = s.y;
                }

                if (
                        s.username.equals(
                                username
                        )
                ) {

                    if (
                            locationX != s.locationX
                                    ||
                                    locationY != s.locationY
                    ) {

                        locationX = s.locationX;
                        locationY = s.locationY;

                        client.send(
                                "REQUEST_LOCATION:"
                                        + locationX + ":"
                                        + locationY
                        );

                        System.out.println(
                                "CLIENT CHANGED LOCATION TO "
                                        + locationX + ","
                                        + locationY
                        );
                    }
                }
            }
        });
    }


}