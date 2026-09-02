package game.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {

    private final int port;

    private ServerSocket serverSocket;

    private volatile boolean running = false;

    private final List<ClientHandler> clients =
            new CopyOnWriteArrayList<>();

    public GameServer(int port) throws IOException {

        this.port = port;
    }

    public void start() {

        if (running) {
            return;
        }

        try {

            serverSocket =
                    new ServerSocket(port);

            running = true;

            System.out.println(
                    "GameServer started on port "
                            + port
            );

            while (running) {

                try {

                    Socket socket =
                            serverSocket.accept();

                    if (!running) {
                        break;
                    }

                    ClientHandler handler =
                            new ClientHandler(
                                    socket,
                                    this
                            );

                    clients.add(handler);

                    Thread thread =
                            new Thread(
                                    handler,
                                    "ClientHandler-"
                                            + socket.getRemoteSocketAddress()
                            );

                    thread.setDaemon(true);
                    thread.start();

                    System.out.println(
                            "Client connected: "
                                    + socket.getRemoteSocketAddress()
                    );

                } catch (IOException e) {

                    if (running) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (IOException e) {

            System.err.println(
                    "Could not start server on port "
                            + port
            );

            e.printStackTrace();

        } finally {

            running = false;
        }
    }

    public synchronized void stop() {

        if (!running) {
            return;
        }

        System.out.println(
                "Stopping GameServer..."
        );

        running = false;

        for (ClientHandler client : clients) {
            client.disconnect();
        }

        clients.clear();

        if (serverSocket != null) {

            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }

        System.out.println(
                "GameServer stopped"
        );
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    public void removeClient(
            ClientHandler client
    ) {

        clients.remove(client);

        broadcastLobbyState();
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public void broadcast(String message) {

        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    public void broadcastLobbyState() {

        StringBuilder message =
                new StringBuilder("LOBBY_STATE");

        for (ClientHandler client : clients) {

            if (client.getPlayerId() == null) {
                continue;
            }

            message
                    .append(":")
                    .append(client.getPlayerId())
                    .append(":")
                    .append(client.getPlayerName())
                    .append(":")
                    .append(client.isHost());
        }

        broadcast(message.toString());
    }
}