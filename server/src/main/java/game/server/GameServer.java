package game.server;

import game.network.dto.PlayerState;
import game.network.packets.WorldStatePacket;
import game.world.WorldManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GameServer {

    private final int port;

    private ServerSocket serverSocket;

    private volatile boolean running = false;

    /*
     * Сигнал того, что ServerSocket действительно
     * создан и сервер начал слушать порт.
     */
    private final CountDownLatch serverReady =
            new CountDownLatch(1);

    private final List<ClientHandler> clients =
            new CopyOnWriteArrayList<>();

    /*
     * =====================================================
     * WORLD
     * =====================================================
     */

    private final WorldManager worldManager =
            new WorldManager();

    /*
     * Отдельный поток игрового мира.
     */
    private Thread worldThread;

    /*
     * 20 обновлений мира в секунду.
     */
    private static final long WORLD_TICK_MS = 50;

    /*
     * Максимальное время ожидания готовности сервера.
     */
    private static final long SERVER_READY_TIMEOUT_MS = 5000;

    public GameServer(int port) throws IOException {

        this.port = port;

        worldManager.setCombatHitListener(
                this::broadcastCombatHit
        );
    }

    /*
     * =====================================================
     * WORLD MANAGER
     * =====================================================
     */

    public WorldManager getWorldManager() {

        return worldManager;
    }

    /*
     * =====================================================
     * START
     * =====================================================
     */

    public void start() {

        if (running) {
            return;
        }

        try {

            /*
             * ВАЖНО:
             * ServerSocket создаётся ДО установки running = true.
             *
             * Как только эта строка успешно выполнится,
             * порт гарантированно занят нашим сервером.
             */
            serverSocket =
                    new ServerSocket(port);

            running = true;

            /*
             * Сообщаем HostManager:
             *
             * "ServerSocket уже создан,
             * сервер действительно готов принимать подключения."
             */
            serverReady.countDown();

            System.out.println(
                    "GameServer started on port "
                            + port
            );

            /*
             * Запускаем игровой цикл.
             */
            startWorldLoop();

            /*
             * =================================================
             * CONNECTION LOOP
             * =================================================
             */

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

            /*
             * Если запуск провалился,
             * не оставляем HostManager ждать вечно.
             */
            serverReady.countDown();

        } finally {

            running = false;
        }
    }

    /*
     * =====================================================
     * WAIT FOR READY
     * =====================================================
     *
     * Вызывается HostManager после запуска потока сервера.
     *
     * Метод возвращает true только тогда,
     * когда ServerSocket реально создан.
     */

    public boolean awaitReady() {

        try {

            boolean ready =
                    serverReady.await(
                            SERVER_READY_TIMEOUT_MS,
                            TimeUnit.MILLISECONDS
                    );

            return ready && running;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            return false;
        }
    }

    /*
     * =====================================================
     * WORLD LOOP
     * =====================================================
     *
     * Это главный игровой цикл сервера.
     *
     * INPUT
     *      ↓
     * ServerPlayer
     *      ↓
     * worldManager.update()
     *      ↓
     * новые координаты
     *      ↓
     * WORLD_STATE
     *      ↓
     * клиент
     *
     */

    private void startWorldLoop() {

        if (
                worldThread != null
                        && worldThread.isAlive()
        ) {
            return;
        }

        worldThread =
                new Thread(
                        () -> {

                            System.out.println(
                                    "World loop started"
                            );

                            while (running) {

                                long tickStart =
                                        System.currentTimeMillis();

                                try {

                                    /*
                                     * =================================
                                     * ОБНОВЛЯЕМ ИГРОВОЙ МИР
                                     * =================================
                                     */

                                    worldManager.update();

                                    /*
                                     * =================================
                                     * ОТПРАВЛЯЕМ НОВЫЕ КООРДИНАТЫ
                                     * =================================
                                     */

                                    broadcastWorldState();

                                    /*
                                     * =================================
                                     * ЖДЁМ СЛЕДУЮЩИЙ TICK
                                     * =================================
                                     */

                                    long elapsed =
                                            System.currentTimeMillis()
                                                    - tickStart;

                                    long sleep =
                                            WORLD_TICK_MS
                                                    - elapsed;

                                    if (sleep > 0) {

                                        Thread.sleep(
                                                sleep
                                        );
                                    }

                                } catch (
                                        InterruptedException e
                                ) {

                                    Thread.currentThread()
                                            .interrupt();

                                    break;

                                } catch (Exception e) {

                                    System.err.println(
                                            "World loop error:"
                                    );

                                    e.printStackTrace();
                                }
                            }

                            System.out.println(
                                    "World loop stopped"
                            );

                        },
                        "WorldLoop"
                );

        worldThread.setDaemon(true);

        worldThread.start();
    }

    /*
     * =====================================================
     * WORLD STATE
     * =====================================================
     */

    private void broadcastWorldState() {

        if (clients.isEmpty()) {
            return;
        }

        WorldStatePacket packet =
                worldManager.buildStatePacket();

        StringBuilder message =
                new StringBuilder(
                        "WORLD_STATE"
                );

        for (PlayerState player :
                packet.players) {

            message
                    .append(":")
                    .append(player.username)

                    .append(":")
                    .append(player.x)

                    .append(":")
                    .append(player.y);
        }

        broadcast(
                message.toString()
        );
    }

    /*
     * =====================================================
     * STOP
     * =====================================================
     */

    public synchronized void stop() {

        if (!running) {
            return;
        }

        System.out.println(
                "Stopping GameServer..."
        );

        running = false;

        /*
         * Останавливаем игровой цикл.
         */
        if (worldThread != null) {

            worldThread.interrupt();

            worldThread = null;
        }

        /*
         * Отключаем клиентов.
         */
        for (ClientHandler client :
                clients) {

            client.disconnect();
        }

        clients.clear();

        /*
         * Закрываем серверный сокет.
         */
        if (serverSocket != null) {

            try {

                serverSocket.close();

            } catch (IOException ignored) {
            }

            serverSocket = null;
        }

        System.out.println(
                "GameServer stopped"
        );
    }

    /*
     * =====================================================
     * STATUS
     * =====================================================
     */

    public boolean isRunning() {

        return running;
    }

    public int getPort() {

        return port;
    }

    /*
     * =====================================================
     * CLIENTS
     * =====================================================
     */

    public void removeClient(
            ClientHandler client
    ) {

        clients.remove(client);

        broadcastLobbyState();
    }

    public List<ClientHandler> getClients() {

        return clients;
    }

    /*
     * =====================================================
     * BROADCAST
     * =====================================================
     */

    public void broadcast(
            String message
    ) {

        for (ClientHandler client :
                clients) {

            client.send(message);
        }
    }

    /*
     * =====================================================
     * LOBBY
     * =====================================================
     */

    public void broadcastLobbyState() {

        StringBuilder message =
                new StringBuilder(
                        "LOBBY_STATE"
                );

        for (ClientHandler client :
                clients) {

            if (
                    client.getPlayerId() == null
            ) {
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

        broadcast(
                message.toString()
        );
    }

    private void broadcastCombatHit(
            game.combat.CombatHit hit
    ) {

        if (hit == null) {
            return;
        }

        broadcast(
                "COMBAT_HIT:"
                        + hit.getTargetId()
                        + ":"
                        + hit.getX()
                        + ":"
                        + hit.getY()
                        + ":"
                        + hit.getDamage()
        );
    }
}