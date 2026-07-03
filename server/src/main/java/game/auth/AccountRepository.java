package game.auth;

import java.io.*;

public class AccountRepository {


    public void save(Account account) {

        File file = new File("server/data/accounts.txt");

        file.getParentFile().mkdirs();

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(file, true)
                     )) {

            writer.write(account.toLine());

            writer.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}