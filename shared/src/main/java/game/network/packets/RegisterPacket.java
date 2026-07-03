package game.network.packets;

import game.auth.AccountType;

public class RegisterPacket {

    private String username;

    private String password;

    private AccountType type;

    public RegisterPacket(
            String username,
            String password,
            AccountType type
    ) {

        this.username = username;
        this.password = password;
        this.type = type;
    }

    public String serialize() {

        return "REGISTER:" +
                username + ":" +
                password + ":" +
                type;
    }
}