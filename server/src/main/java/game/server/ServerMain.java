package game.server;

public class ServerMain {

    public static void main(String[] args) {

        try {
            GameServer server = new GameServer(7777);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}