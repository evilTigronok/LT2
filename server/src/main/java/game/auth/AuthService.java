package game.auth;

import java.io.*;

public class AuthService {

    private static final String FILE =
            "server/data/accounts.txt";

    public boolean register(
            String username,
            String password,
            String type
    ) {

        try {

            File file =
                    new File(FILE);

            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedReader reader =
                    new BufferedReader(
                            new FileReader(file)
                    );

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts =
                        line.split(":");

                if (parts[0].equals(username)) {

                    return false;
                }
            }

            reader.close();

            BufferedWriter writer =
                    new BufferedWriter(
                            new FileWriter(file, true)
                    );

            writer.write(
                    username + ":" +
                            password + ":" +
                            type
            );

            writer.newLine();

            writer.close();

            return true;

        } catch (IOException e) {

            e.printStackTrace();
        }

        return false;
    }

    public boolean login(
            String username,
            String password
    ) {

        try {

            BufferedReader reader =
                    new BufferedReader(
                            new FileReader(FILE)
                    );

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts =
                        line.split(":");

                if (parts[0].equals(username) &&
                        parts[1].equals(password)) {

                    return true;
                }
            }

            reader.close();

        } catch (IOException e) {

            e.printStackTrace();
        }

        return false;
    }
}