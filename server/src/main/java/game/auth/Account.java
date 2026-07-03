package game.auth;

public class Account {

    private final String login;
    private final String password;

    private final String fullName;
    private final String eyeColor;
    private final String starterItem;

    private final boolean spectator;

    public Account(
            String login,
            String password,
            String fullName,
            String eyeColor,
            String starterItem,
            boolean spectator
    ) {

        this.login = login;
        this.password = password;

        this.fullName = fullName;
        this.eyeColor = eyeColor;
        this.starterItem = starterItem;

        this.spectator = spectator;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String toLine() {

        return login + ":" +
                password + ":" +
                fullName + ":" +
                eyeColor + ":" +
                starterItem + ":" +
                spectator;
    }
}