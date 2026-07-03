package game.network.packets;

public class LoginPacket {

    private String username;

    private String password;

    public LoginPacket(
            String username,
            String password
    ) {

        this.username = username;
        this.password = password;
    }

    public String serialize() {

        return "LOGIN:" +
                username + ":" +
                password;
    }
}