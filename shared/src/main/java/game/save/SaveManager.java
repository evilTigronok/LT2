package game.save;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class SaveManager {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private SaveManager() {
    }

    public static Path getSaveDirectory() {
        return Path.of(
                System.getProperty("user.home"),
                ".lt2",
                "saves"
        );
    }

    public static Path getSavePath(int slot) {
        if (slot <= 0) {
            throw new IllegalArgumentException(
                    "Номер слота должен быть больше 0"
            );
        }

        return getSaveDirectory().resolve("slot_" + slot + ".json");
    }

    public static boolean exists(int slot) {
        return Files.isRegularFile(getSavePath(slot));
    }

    public static void save(SaveData data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Данные сохранения не могут быть null"
            );
        }

        if (data.slot <= 0) {
            throw new IllegalArgumentException(
                    "Номер слота должен быть больше 0"
            );
        }

        long now = System.currentTimeMillis();

        if (data.createdAt <= 0) {
            data.createdAt = now;
        }

        data.lastSavedAt = now;

        Path path = getSavePath(data.slot);

        try {
            Files.createDirectories(path.getParent());

            Files.writeString(
                    path,
                    GSON.toJson(data),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "Не удалось сохранить игру в слот " + data.slot,
                    e
            );
        }
    }

    public static SaveData load(int slot) {
        Path path = getSavePath(slot);

        if (!Files.isRegularFile(path)) {
            return null;
        }

        try {
            String json = Files.readString(
                    path,
                    StandardCharsets.UTF_8
            );

            SaveData data = GSON.fromJson(json, SaveData.class);

            if (data != null) {
                data.slot = slot;
            }

            return data;
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException(
                    "Не удалось загрузить сохранение из слота " + slot,
                    e
            );
        }
    }

    public static boolean delete(int slot) {
        try {
            return Files.deleteIfExists(getSavePath(slot));
        } catch (IOException e) {
            throw new RuntimeException(
                    "Не удалось удалить сохранение из слота " + slot,
                    e
            );
        }
    }

    public static List<SaveData> getAllSaves() {
        List<SaveData> saves = new ArrayList<>();

        for (int slot = 1; slot <= 3; slot++) {
            SaveData data = load(slot);

            if (data != null) {
                saves.add(data);
            }
        }

        return saves;
    }
}