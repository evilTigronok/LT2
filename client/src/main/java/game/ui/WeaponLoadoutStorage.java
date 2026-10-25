package game.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Локальное сохранение ведущих слотов оружия.
 * Экипировка хранится отдельно для каждого игрового слота.
 */
public final class WeaponLoadoutStorage {

    private static final String MELEE_KEY = "melee";
    private static final String RANGED_KEY = "ranged";

    private final Path file;

    public WeaponLoadoutStorage(String username, int saveSlot) {
        if (saveSlot < 1 || saveSlot > 3) {
            throw new IllegalArgumentException(
                    "Номер слота должен быть от 1 до 3"
            );
        }

        String safeUsername = sanitizeUsername(username);

        this.file = Path.of(
                System.getProperty("user.home"),
                ".lt2",
                "saves",
                safeUsername + "_slot_" + saveSlot + "_loadout.properties"
        );
    }

    public String loadMeleeId() {
        return load().getProperty(MELEE_KEY);
    }

    public String loadRangedId() {
        return load().getProperty(RANGED_KEY);
    }

    public void save(String meleeId, String rangedId) {
        Properties properties = new Properties();

        if (meleeId != null && !meleeId.isBlank()) {
            properties.setProperty(MELEE_KEY, meleeId);
        }

        if (rangedId != null && !rangedId.isBlank()) {
            properties.setProperty(RANGED_KEY, rangedId);
        }

        try {
            Files.createDirectories(file.getParent());

            try (OutputStream output = Files.newOutputStream(
                    file,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            )) {
                properties.store(output, "LT2 weapon loadout");
            }
        } catch (IOException e) {
            System.err.println(
                    "Не удалось сохранить экипировку в " + file
            );
            e.printStackTrace();
        }
    }

    private Properties load() {
        Properties properties = new Properties();

        if (!Files.isRegularFile(file)) {
            return properties;
        }

        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println(
                    "Не удалось загрузить экипировку из " + file
            );
            e.printStackTrace();
        }

        return properties;
    }

    private static String sanitizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return "default";
        }

        return username.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}