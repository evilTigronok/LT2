package game.auth;

import java.io.*;
import java.util.UUID;

public class TokenService {

    private static final String TOKENS_FILE =
            "server/src/main/java/game/data/tokens.txt";

    private static final String REQUESTS_FILE =
            "server/src/main/java/game/data/token_requests.txt";

    public boolean isValid(String token) {

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(
                                     TOKENS_FILE
                             )
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts =
                        line.split("\\|");

                String savedToken =
                        parts[0];

                String status =
                        parts.length > 1
                                ? parts[1]
                                : "UNUSED";

                if (savedToken.equals(token)
                        && status.equals("UNUSED")) {

                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void markUsed(String token) {

        File input =
                new File(TOKENS_FILE);

        File temp =
                new File(TOKENS_FILE + ".tmp");

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(input)
                     );

             BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(temp)
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts =
                        line.split("\\|");

                if (parts[0].equals(token)) {

                    writer.write(parts[0] + "|USED");

                } else {

                    writer.write(line);
                }

                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        input.delete();
        temp.renameTo(input);
    }

    public void removeToken(
            String token
    ) {

        File inputFile =
                new File(TOKENS_FILE);

        File tempFile =
                new File(TOKENS_FILE + ".tmp");

        try (

                BufferedReader reader =
                        new BufferedReader(
                                new FileReader(inputFile)
                        );

                BufferedWriter writer =
                        new BufferedWriter(
                                new FileWriter(tempFile)
                        )

        ) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (!line.trim().equals(token)) {

                    writer.write(line);

                    writer.newLine();
                }
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

        inputFile.delete();

        tempFile.renameTo(inputFile);
    }

    public void addToken(
            String token
    ) {

        try (

                BufferedWriter writer =
                        new BufferedWriter(
                                new FileWriter(
                                        TOKENS_FILE,
                                        true
                                )
                        )

        ) {

            writer.write(token);

            writer.newLine();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // =====================================
    // TOKEN REQUEST
    // =====================================

    public String createRequest() {

        String token = generateToken();

        issueToken(token);

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(
                                     REQUESTS_FILE,
                                     true
                             )
                     )) {

            writer.write(token);
            writer.newLine();

            System.out.println(
                    "Generated token: " + token
            );

            return token;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String generateToken() {

        return UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    public void issueToken(String token) {

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(
                                     TOKENS_FILE,
                                     true
                             )
                     )) {

            String line =
                    token + "|UNUSED";

            writer.write(line);
            writer.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}