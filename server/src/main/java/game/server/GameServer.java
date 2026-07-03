package game.server;

import game.auth.*;
import game.network.packets.LocationPacket;
import game.network.packets.WorldStatePacket;
import game.world.ServerPlayer;
import game.world.WorldManager;
import game.world.data.LocationData;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {

    private final ServerSocket serverSocket;

    private final RegistrationHandler registrationHandler;
    private final TokenService tokenService;
    private final AuthService authService;

    private final WorldManager worldManager = new WorldManager();

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public GameServer(int port) throws IOException {

        this.serverSocket = new ServerSocket(port);

        this.tokenService = new TokenService();
        this.authService = new AuthService();
        this.registrationHandler = new RegistrationHandler(tokenService);

        System.out.println("Server started on port " + port);
    }

    public void start() {

        startWorldLoop();

        while (true) {
            try {
                Socket socket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(
                        socket,
                        registrationHandler,
                        tokenService,
                        authService,
                        worldManager
                );

                clients.add(handler);

                new Thread(handler).start();

                System.out.println("Client connected");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startWorldLoop() {

        Thread t = new Thread(() -> {

            final int TPS = 20;
            final long delay = 1000 / TPS;

            while (true) {

                long start = System.currentTimeMillis();

                worldManager.update();

                broadcastWorldState();

                long sleep = delay - (System.currentTimeMillis() - start);

                if (sleep > 0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException ignored) {}
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }


    private void broadcastWorldState() {

        WorldStatePacket packet = worldManager.buildStatePacket();

        StringBuilder sb = new StringBuilder();
        sb.append("WORLD_STATE");

        packet.players.forEach(player -> {
            sb.append(":")
                    .append(player.username)
                    .append(":")
                    .append(player.x)
                    .append(":")
                    .append(player.y)
                    .append(":")
                    .append(player.locationX)
                    .append(":")
                    .append(player.locationY);
        });

        String worldMsg = sb.toString();

        for (ClientHandler client : clients) {
            client.send(worldMsg); // 🔥 теперь гарантированно есть
        }

        // LOCATION EVENTS
        for (ClientHandler client : clients) {

            String username =
                    client.getUsernameSafe();


            if (username == null) {
                continue;
            }

            ServerPlayer player =
                    worldManager.getPlayer(
                            username
                    );

            if (player == null) {
                continue;
            }

            System.out.println(
                    "PLAYER " + username
                            + " changed = "
                            + player.isLocationChanged()
                            + " loc="
                            + player.getLocationX()
                            + ","
                            + player.getLocationY()
            );

            if (player.isLocationChanged()) {
                System.out.println(
                        "SEND LOCATION TO " + username
                );

                LocationData loc =
                        worldManager.getPlayerLocation(player);

                client.sendLocation(loc);

                player.resetLocationChanged();
            }
        }
    }
}