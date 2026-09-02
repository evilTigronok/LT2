package game.client;

import com.google.gson.Gson;
import game.network.dto.PlayerState;
import game.network.packets.LocationPacket;
import game.network.packets.WorldStatePacket;
import game.ui.LobbyScene;
import game.ui.WorldScene;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;

public class NetworkClient {

    private final Gson gson =
            new Gson();

    private Socket socket;

    private BufferedReader in;

    private PrintWriter out;

    private Thread listenerThread;

    private Thread pingThread;

    private volatile boolean connected;

    private boolean host;

    private String playerId;

    private long ping;

    private WorldScene worldScene;

    private LobbyScene lobbyScene;

    private Runnable gameStartHandler;

    private String lastLobbyState;

    private String username;

    public long getPing() {
        return ping;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getPlayerId() {
        return playerId;
    }

    public boolean isHost() {
        return host;
    }

    public void setHost(boolean host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // =====================================
    // CONNECTION
    // =====================================

    public synchronized boolean connect(
            String address,
            int port,
            boolean host
    ) {

        if (connected) {

            System.out.println(
                    "Already connected"
            );

            return true;
        }

        try {

            System.out.println(
                    "Connecting to "
                            + address
                            + ":"
                            + port
            );

            socket =
                    new Socket(
                            address,
                            port
                    );

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

            this.host = host;

            connected = true;

            startListener();

            startPingLoop();

            return true;

        } catch (IOException e) {

            System.err.println(
                    "Connection failed"
            );

            e.printStackTrace();

            disconnect();

            return false;
        }
    }

    public synchronized void disconnect() {

        connected = false;

        if (pingThread != null) {
            pingThread.interrupt();
            pingThread = null;
        }

        try {

            if (socket != null) {
                socket.close();
            }

        } catch (IOException ignored) {
        }

        socket = null;
        in = null;
        out = null;

        System.out.println(
                "Network disconnected"
        );
    }

    // =====================================
    // HELLO
    // =====================================

    public void sendHello(
            String playerName
    ) {

        if (playerName == null
                || playerName.isBlank()) {

            playerName =
                    "Player-" + playerId;
        }

        send(
                "HELLO:"
                        + (host ? "HOST" : "CLIENT")
                        + ":"
                        + playerName
        );
    }

    // =====================================
    // LISTENER
    // =====================================

    private void startListener() {

        listenerThread =
                new Thread(
                        () -> {

                            try {

                                String msg;

                                while (
                                        connected
                                                && (msg = in.readLine()) != null
                                ) {

                                    handle(msg);
                                }

                            } catch (IOException e) {

                                if (connected) {
                                    System.out.println(
                                            "Server connection lost"
                                    );
                                }

                            } finally {

                                connected = false;
                            }

                        },
                        "NetworkClient-Listener"
                );

        listenerThread.setDaemon(true);

        listenerThread.start();
    }

    // =====================================
    // HANDLE
    // =====================================

    private void handle(
            String msg
    ) {

        System.out.println(
                "SERVER: " + msg
        );

        if (msg.startsWith("CONNECTED:")) {

            playerId =
                    msg.substring(
                            "CONNECTED:".length()
                    );

            return;
        }

        if (msg.startsWith("HELLO_OK:")) {

            String[] p =
                    msg.split(":");

            if (p.length >= 3) {

                playerId = p[1];

                host =
                        Boolean.parseBoolean(
                                p[2]
                        );
            }

            return;
        }

        if (msg.startsWith("PONG:")) {

            String[] p =
                    msg.split(":");

            if (p.length >= 2) {

                long sent =
                        Long.parseLong(
                                p[1]
                        );

                ping =
                        System.currentTimeMillis()
                                - sent;
            }

            return;
        }

        if (msg.startsWith("LOBBY_STATE")) {

            String lobbyData =
                    msg.substring(
                            "LOBBY_STATE".length()
                    );

            lastLobbyState = lobbyData;

            if (lobbyScene != null) {

                Platform.runLater(() ->
                        lobbyScene.updateLobby(
                                lobbyData
                        )
                );
            }

            return;
        }

        if (msg.equals("GAME_START")) {

            if (gameStartHandler != null) {

                Platform.runLater(
                        gameStartHandler
                );
            }

            return;
        }

        if (msg.startsWith("LOCATION:")) {

            String json =
                    msg.substring(
                            "LOCATION:".length()
                    );

            LocationPacket packet =
                    gson.fromJson(
                            json,
                            LocationPacket.class
                    );

            if (worldScene != null) {

                Platform.runLater(() ->
                        worldScene.loadLocation(
                                packet.location
                        )
                );
            }

            return;
        }

        if (msg.startsWith("WORLD_STATE")) {

            handleWorldState(msg);
        }
    }

    private void handleWorldState(
            String msg
    ) {

        WorldStatePacket packet =
                new WorldStatePacket();

        String[] p =
                msg.split(":");

        for (
                int i = 1;
                i + 4 < p.length;
                i += 5
        ) {

            String username = p[i];

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

            packet.players.add(
                    new PlayerState(
                            username,
                            x,
                            y,
                            locationX,
                            locationY
                    )
            );
        }

        if (worldScene != null) {

            Platform.runLater(() ->
                    worldScene.updateWorld(
                            packet
                    )
            );
        }
    }

    // =====================================
    // SEND
    // =====================================

    public synchronized void send(
            String message
    ) {

        if (!connected
                || out == null) {

            return;
        }

        out.println(message);
    }

    // =====================================
    // PING
    // =====================================

    private void startPingLoop() {

        pingThread =
                new Thread(
                        () -> {

                            while (
                                    connected
                                            && !Thread.currentThread()
                                            .isInterrupted()
                            ) {

                                try {

                                    long now =
                                            System.currentTimeMillis();

                                    send(
                                            "PING:" + now
                                    );

                                    Thread.sleep(
                                            2000
                                    );

                                } catch (
                                        InterruptedException e
                                ) {

                                    Thread.currentThread()
                                            .interrupt();

                                    break;
                                }
                            }

                        },
                        "NetworkClient-Ping"
                );

        pingThread.setDaemon(true);

        pingThread.start();
    }

    // =====================================
    // SCENES
    // =====================================

    public void setLobbyScene(
            LobbyScene lobbyScene
    ) {

        this.lobbyScene = lobbyScene;

        if (lastLobbyState != null) {

            Platform.runLater(() ->
                    lobbyScene.updateLobby(
                            lastLobbyState
                    )
            );
        }
    }

    public void setWorldScene(
            WorldScene worldScene
    ) {

        this.worldScene =
                worldScene;
    }

    public void setGameStartHandler(
            Runnable handler
    ) {

        this.gameStartHandler =
                handler;
    }
}