package game.server;

import com.google.gson.Gson;
import game.auth.AuthService;
import game.auth.RegistrationHandler;
import game.auth.TokenService;
import game.network.packets.LocationPacket;
import game.sessions.ClientSession;
import game.world.ServerPlayer;
import game.world.WorldManager;
import game.world.data.LocationData;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    private final RegistrationHandler registrationHandler;
    private final TokenService tokenService;
    private final AuthService authService;
    private final WorldManager worldManager;

    private final ClientSession session = new ClientSession();

    public ClientHandler(
            Socket socket,
            RegistrationHandler registrationHandler,
            TokenService tokenService,
            AuthService authService,
            WorldManager worldManager
    ) throws IOException {

        this.socket = socket;
        this.registrationHandler = registrationHandler;
        this.tokenService = tokenService;
        this.authService = authService;
        this.worldManager = worldManager;

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                handle(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (session.getLogin() != null) {
                worldManager.removePlayer(session.getLogin());
            }
        }
    }

    // =========================
    // 🔥 ЕДИНЫЙ МЕТОД ОТПРАВКИ
    // =========================
    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void sendLocation(LocationData location) {

        LocationPacket packet = new LocationPacket();
        packet.location = location;

        String json = new Gson().toJson(packet);

        System.out.println("SEND JSON = " + json);

        send("LOCATION:" + json);
    }

    public String getUsernameSafe() {
        return session.getLogin();
    }

    // =========================
    // HANDLE
    // =========================
    private void handle(String msg) {

        String[] p = msg.split(":");
        if (p.length == 0) return;

        switch (p[0]) {

            case "INPUT":
                handleInput(p);
                break;

            case "LOGIN":
                handleLogin(p);
                break;

            case "REGISTER":
                handleRegister(p);
                break;

            case "PING":
                send("PONG:" + p[1]);
                break;
        }
    }

    private void handleInput(String[] p) {

        if (session.getLogin() == null) return;
        if (p.length < 5) return;

        ServerPlayer player = worldManager.getPlayer(session.getLogin());
        if (player == null) return;

        player.setUp(Boolean.parseBoolean(p[1]));
        player.setDown(Boolean.parseBoolean(p[2]));
        player.setLeft(Boolean.parseBoolean(p[3]));
        player.setRight(Boolean.parseBoolean(p[4]));
    }

    private void handleLogin(String[] p) {

        if (p.length < 3) {
            out.println("LOGIN_FAIL");
            return;
        }

        String login = p[1];
        String password = p[2];

        boolean ok =
                registrationHandler.authenticate(
                        login,
                        password
                );

        if (!ok) {
            out.println("LOGIN_FAIL");
            return;
        }

        // если игрок уже есть на сервере
        worldManager.removePlayer(login);

        session.setLogin(login);

        worldManager.addPlayer(login);

        out.println("LOGIN_SUCCESS");

        ServerPlayer player =
                worldManager.getPlayer(login);

        if (player != null) {

            LocationData location =
                    worldManager.getPlayerLocation(player);

            System.out.println(
                    "FORCE SEND LOCATION TO " + login
            );

            LocationPacket packet =
                    new LocationPacket();

            packet.location = location;

            sendLocation(location);
        }
    }

    private void handleRegister(String[] p) {

        if (p.length < 6) {
            send("REGISTER_FAIL");
            return;
        }

        boolean ok = registrationHandler.register(
                p[1], p[2], p[3], p[4], p[5], false
        );

        send(ok ? "REGISTER_OK" : "REGISTER_FAIL");
    }
}