package game.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Локальное сохранение двух ведущих слотов оружия.
 * Сохраняются только стабильные ID оружия, а не сами объекты Weapon.
 */
public final class WeaponLoadoutStorage {

    private static final String MELEE_KEY = "melee";
    private static final String RANGED_KEY = "ranged";

    private final Path file;

    public WeaponLoadoutStorage(String username) {
        String safeUsername = sanitizeUsername(username);
        this.file = Path.of(
                System.getProperty("user.home"),
                ".lt2",
                "saves",
                safeUsername + ".properties"
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
        } catch (IOException ignored) {
            // Ошибка сохранения не должна ломать игровой процесс.
        }
    }

    private Properties load() {
        Properties properties = new Properties();

        if (!Files.isRegularFile(file)) {
            return properties;
        }

        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
        } catch (IOException ignored) {
            // Повреждённое/недоступное сохранение трактуем как отсутствие сохранения.
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
