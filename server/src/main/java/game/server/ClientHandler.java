package game.server;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

import game.world.ServerPlayer;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final GameServer server;

    private final String playerId;

    private String playerName;

    private boolean host;

    private volatile boolean connected = true;

    public ClientHandler(
            Socket socket,
            GameServer server
    ) throws IOException {

        this.socket = socket;
        this.server = server;

        this.in =
                new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream()
                        )
                );

        this.out =
                new PrintWriter(
                        socket.getOutputStream(),
                        true
                );

        this.playerId =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8);
    }

    @Override
    public void run() {

        try {

            send(
                    "CONNECTED:" + playerId
            );

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
                        "Client connection lost: "
                                + playerId
                );
            }

        } finally {

            disconnectInternal();
        }
    }

    private void handle(String msg) {

        if (
                msg == null
                        || msg.isEmpty()
        ) {
            return;
        }

        String[] parts =
                msg.split(":");

        switch (parts[0]) {

            case "HELLO":

                handleHello(parts);

                break;

            case "PING":

                if (parts.length >= 2) {

                    send(
                            "PONG:"
                                    + parts[1]
                    );
                }

                break;

            case "READY":

                send(
                        "READY_STATE:"
                                + playerId
                );

                break;

            case "START_GAME":

                if (host) {

                    server.broadcast(
                            "GAME_START"
                    );
                }

                break;

            case "ATTACK":

                handleAttack();

                break;

            /*
             * ==========================================
             * ДВИЖЕНИЕ
             * ==========================================
             *
             * Клиент отправляет:
             *
             * INPUT:true:false:false:false
             *
             * где:
             *
             * [1] W
             * [2] S
             * [3] A
             * [4] D
             */
            case "INPUT":

                handleInput(parts);

                break;

            default:

                System.out.println(
                        "Unknown packet: "
                                + msg
                );
        }
    }

    private void handleInput(
            String[] parts
    ) {

        /*
         * INPUT:
         *   0 = INPUT
         *   1 = up
         *   2 = down
         *   3 = left
         *   4 = right
         */
        if (parts.length < 5) {

            return;
        }

        if (
                playerName == null
                        || playerName.isBlank()
        ) {

            return;
        }

        ServerPlayer player =
                server.getWorldManager()
                        .getPlayer(
                                playerName
                        );

        if (player == null) {

            return;
        }

        try {

            boolean up =
                    Boolean.parseBoolean(
                            parts[1]
                    );

            boolean down =
                    Boolean.parseBoolean(
                            parts[2]
                    );

            boolean left =
                    Boolean.parseBoolean(
                            parts[3]
                    );

            boolean right =
                    Boolean.parseBoolean(
                            parts[4]
                    );

            player.setUp(up);
            player.setDown(down);
            player.setLeft(left);
            player.setRight(right);

        } catch (Exception e) {

            System.out.println(
                    "Invalid INPUT packet: "
                            + String.join(
                            ":",
                            parts
                    )
            );
        }
    }

    private void handleHello(
            String[] parts
    ) {

        if (parts.length < 3) {
            return;
        }

        host =
                "HOST".equals(
                        parts[1]
                );

        playerName =
                parts[2];

        if (
                playerName == null
                        || playerName.isBlank()
        ) {

            playerName =
                    "Player-" + playerId;
        }

        /*
         * Создаём игрока в игровом мире
         * после успешного HELLO.
         */
        server.getWorldManager()
                .addPlayer(
                        playerName
                );

        send(
                "HELLO_OK:"
                        + playerId
                        + ":"
                        + host
        );

        server.broadcastLobbyState();
    }

    public void send(
            String message
    ) {

        if (!connected) {
            return;
        }

        out.println(message);
    }

    public void disconnect() {

        disconnectInternal();
    }

    private synchronized void disconnectInternal() {

        if (!connected) {
            return;
        }

        connected = false;

        /*
         * Удаляем игрока из игрового мира.
         */
        if (
                playerName != null
                        && !playerName.isBlank()
        ) {

            server.getWorldManager()
                    .removePlayer(
                            playerName
                    );
        }

        server.removeClient(this);

        try {

            socket.close();

        } catch (IOException ignored) {
        }

        System.out.println(
                "Client disconnected: "
                        + playerId
        );
    }

    private void handleAttack() {
        if (playerName == null || playerName.isBlank()) {
            System.out.println("ATTACK IGNORED: player name is empty");
            return;
        }

        System.out.println("ATTACK REQUEST: " + playerName);

        server.getWorldManager().startAttack(playerName);
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isHost() {
        return host;
    }
}