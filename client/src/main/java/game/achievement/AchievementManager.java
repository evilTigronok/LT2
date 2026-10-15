package game.achievement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class AchievementManager {

    private final String username;

    private final Set<String> unlocked =
            new HashSet<>();

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    public AchievementManager(
            String username
    ) {
        this.username =
                username == null || username.isBlank()
                        ? "player"
                        : username;

        load();
    }

    public boolean isUnlocked(
            String id
    ) {
        return unlocked.contains(id);
    }

    public boolean unlock(
            String id
    ) {

        if (id == null || id.isBlank()) {
            return false;
        }

        boolean added =
                unlocked.add(id);

        if (added) {
            save();
        }

        return added;
    }

    public Set<String> getUnlocked() {

        return Set.copyOf(
                unlocked
        );
    }

    private Path getPath() {

        String safeUsername =
                username.replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );

        Path directory =
                Path.of(
                        System.getProperty("user.home"),
                        ".lt2",
                        "profiles"
                );

        try {
            Files.createDirectories(
                    directory
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return directory.resolve(
                safeUsername + "_achievements.json"
        );
    }

    private void load() {

        Path path =
                getPath();

        if (!Files.exists(path)) {
            return;
        }

        try {

            String json =
                    Files.readString(
                            path,
                            StandardCharsets.UTF_8
                    );

            AchievementData data =
                    GSON.fromJson(
                            json,
                            AchievementData.class
                    );

            if (data != null
                    && data.unlocked != null) {

                unlocked.clear();

                unlocked.addAll(
                        data.unlocked
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void save() {

        AchievementData data =
                new AchievementData();

        data.unlocked =
                new HashSet<>(
                        unlocked
                );

        try {

            Files.writeString(
                    getPath(),
                    GSON.toJson(data),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private static class AchievementData {

        Set<String> unlocked =
                new HashSet<>();
    }
}