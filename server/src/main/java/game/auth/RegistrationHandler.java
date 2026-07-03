package game.auth;

import game.characters.Player;

import java.io.*;
import java.io.IOException;

public class RegistrationHandler {

    private final TokenService tokenService;

    private final AccountRepository repository =
            new AccountRepository();

    public RegistrationHandler(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public boolean register(
            String login,
            String password,
            String fullName,
            String eyeColor,
            String starterItem,
            boolean spectator
    ) {

        if (login == null || login.isBlank()) return false;
        if (password == null || password.isBlank()) return false;

        Account account = new Account(
                login,
                password,
                fullName,
                eyeColor,
                starterItem,
                spectator
        );

        repository.save(account);

        System.out.println("ACCOUNT CREATED: " + login);

        return true;
    }

    public boolean authenticate(
            String login,
            String password
    ) {

        File file = new File("server/data/accounts.txt");

        if (!file.exists()) {
            return false;
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(file)
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {

                System.out.println("CHECKING: " + line);

                String[] p = line.split(":");

                if (p.length < 2) {
                    continue;
                }

                String storedLogin = p[0].trim();
                String storedPassword = p[1].trim();

                if (storedLogin.equals(login.trim())
                        && storedPassword.equals(password.trim())) {

                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}