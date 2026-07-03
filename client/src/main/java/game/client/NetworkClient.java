package game.client;

import game.network.dto.PlayerState;
import game.network.packets.WorldStatePacket;
import game.ui.WorldScene;

import game.world.data.LocationData;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;

import com.google.gson.Gson;
import game.network.packets.LocationPacket;

public class NetworkClient {

    private final Gson gson =
            new Gson();

    private Socket socket;

    private BufferedReader in;

    private PrintWriter out;

    private Runnable loginSuccessHandler;

    private Runnable tokenValidHandler;

    private Runnable tokenInvalidHandler;

    private WorldScene worldScene;

    private LocationData pendingLocation;

    private long ping = 0;

    public long getPing() {
        return ping;
    }

    public void setWorldScene(WorldScene worldScene) {

        this.worldScene = worldScene;

        if (pendingLocation != null) {

            System.out.println(
                    "LOAD PENDING LOCATION"
            );

            LocationData loc = pendingLocation;
            pendingLocation = null;

            Platform.runLater(() ->
                    worldScene.loadLocation(loc)
            );
        }
    }

    public void setLoginSuccessHandler(
            Runnable loginSuccessHandler
    ) {

        this.loginSuccessHandler =
                loginSuccessHandler;
    }

    public void setTokenValidHandler(
            Runnable tokenValidHandler
    ) {

        this.tokenValidHandler =
                tokenValidHandler;
    }

    public void setTokenInvalidHandler(
            Runnable tokenInvalidHandler
    ) {

        this.tokenInvalidHandler =
                tokenInvalidHandler;
    }

    public void connect(
            String host,
            int port
    ) {

        try {

            socket =
                    new Socket(host, port);

            in =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()
                            )
                    );

            out =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true
                    );

            System.out.println(
                    "Connected to server"
            );

            startListener();

            startPingLoop();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // =====================================
    // LISTENER THREAD
    // =====================================

    private void startListener() {

        Thread thread = new Thread(() -> {

            try {

                String msg;

                while ((msg = in.readLine()) != null) {

                    System.out.println(
                            "SERVER: " + msg
                    );

                    handle(msg);
                }

            } catch (IOException e) {

                e.printStackTrace();
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    // =====================================
    // HANDLE SERVER PACKETS
    // =====================================

    private void handle(String msg) {

        if (msg.startsWith("PONG:")) {

            String[] p = msg.split(":");

            long sentTime = Long.parseLong(p[1]);

            ping = System.currentTimeMillis() - sentTime;

            System.out.println("PING = " + ping + " ms");

            return;
        }

        // =====================================
        // WORLD STATE
        // =====================================
        if (msg.startsWith("LOCATION:")) {

            String json = msg.substring("LOCATION:".length());

            LocationPacket packet =
                    gson.fromJson(json, LocationPacket.class);

            if (worldScene != null) {

                Platform.runLater(() ->
                        worldScene.loadLocation(
                                packet.location
                        )
                );

            } else {

                System.out.println(
                        "WORLD SCENE IS NULL -> SAVE LOCATION"
                );

                pendingLocation = packet.location;
            }

            return;
        }

        if (msg.startsWith("WORLD_STATE")) {

            WorldStatePacket packet =
                    new WorldStatePacket();

            String[] p =
                    msg.split(":");

            for (int i = 1; i + 4 < p.length; i += 5) {

                String username =
                        p[i];

                float x =
                        Float.parseFloat(
                                p[i + 1]
                        );

                float y =
                        Float.parseFloat(
                                p[i + 2]
                        );

                int locationX =
                        Integer.parseInt(
                                p[i + 3]
                        );

                int locationY =
                        Integer.parseInt(
                                p[i + 4]
                        );

                PlayerState state =
                        new PlayerState(
                                username,
                                x,
                                y,
                                locationX,
                                locationY
                        );

                packet.players.add(state);
            }

            if (worldScene != null) {

                Platform.runLater(() -> {

                    worldScene.updateWorld(
                            packet
                    );
                });
            }

            return;
        }

        // =====================================
        // SIMPLE PACKETS
        // =====================================

        switch (msg) {

            case "LOGIN_SUCCESS":

                if (loginSuccessHandler != null) {

                    Platform.runLater(() -> {

                        loginSuccessHandler.run();
                    });
                }

                break;

            case "TOKEN_OK":

                if (tokenValidHandler != null) {

                    Platform.runLater(() -> {

                        tokenValidHandler.run();
                    });
                }

                break;

            case "TOKEN_FAIL":

                if (tokenInvalidHandler != null) {

                    Platform.runLater(() -> {

                        tokenInvalidHandler.run();
                    });
                }

                break;
        }
    }

    // =====================================
    // SEND
    // =====================================

    public void send(String message) {

        if (out == null) {

            System.out.println(
                    "ERROR: not connected"
            );

            return;
        }

        out.println(message);
    }

    private void startPingLoop() {

        Thread pingThread = new Thread(() -> {

            while (true) {

                try {

                    long now = System.currentTimeMillis();

                    send("PING:" + now);

                    Thread.sleep(2000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        pingThread.setDaemon(true);
        pingThread.start();
    }
}