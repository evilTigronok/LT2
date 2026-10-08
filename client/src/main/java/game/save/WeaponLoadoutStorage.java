package game.save;

import game.inventory.Inventory;
import game.weapon.Weapon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Локальное сохранение двух ведущих слотов оружия игрока.
 *
 * Сохраняются только стабильные ID оружия, а не сами объекты.
 * Поэтому характеристики оружия остаются источником правды в WeaponFactory.
 */
public final class WeaponLoadoutStorage {

    private static final String MELEE_KEY = "melee";
    private static final String RANGED_KEY = "ranged";

    private WeaponLoadoutStorage() {
    }

    /**
     * Загружает сохранённую экипировку.
     * Если файл отсутствует или оружие больше недоступно,
     * соответствующий слот остаётся с текущим значением Inventory.
     */
    public static void load(Inventory inventory, String username) {
        if (inventory == null) {
            return;
        }

        Path file = getSavePath(username);
        if (file == null || !Files.isRegularFile(file)) {
            return;
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Не удалось загрузить экипировку: " + e.getMessage());
            return;
        }

        equipIfAvailable(
                inventory,
                properties.getProperty(MELEE_KEY),
                true
        );

        equipIfAvailable(
                inventory,
                properties.getProperty(RANGED_KEY),
                false
        );
    }

    /**
     * Сохраняет текущие ведущие слоты.
     */
    public static void save(Inventory inventory, String username) {
        if (inventory == null) {
            return;
        }

        Path file = getSavePath(username);
        if (file == null) {
            return;
        }

        try {
            Files.createDirectories(file.getParent());

            Properties properties = new Properties();

            Weapon melee = inventory.getEquippedMeleeWeapon();
            Weapon ranged = inventory.getEquippedRangedWeapon();

            if (melee != null) {
                properties.setProperty(MELEE_KEY, melee.getId());
            }

            if (ranged != null) {
                properties.setProperty(RANGED_KEY, ranged.getId());
            }

            try (OutputStream output = Files.newOutputStream(
                    file,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            )) {
                properties.store(
                        output,
                        "LT2 weapon loadout"
                );
            }
        } catch (IOException e) {
            System.err.println("Не удалось сохранить экипировку: " + e.getMessage());
        }
    }

    private static void equipIfAvailable(
            Inventory inventory,
            String weaponId,
            boolean melee
    ) {
        if (weaponId == null || weaponId.isBlank()) {
            return;
        }

        Weapon weapon = inventory.findWeaponById(weaponId);
        if (weapon == null) {
            return;
        }

        if (melee) {
            inventory.equipMeleeWeapon(weapon);
        } else {
            inventory.equipRangedWeapon(weapon);
        }
    }

    private static Path getSavePath(String username) {
        String safeUsername = sanitizeUsername(username);
        if (safeUsername == null) {
            return null;
        }

        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isBlank()) {
            return null;
        }

        return Path.of(userHome, ".lt2", "saves", safeUsername + ".properties");
    }

    private static String sanitizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        String safe = username.replaceAll("[^a-zA-Z0-9._-]", "_");
        return safe.isBlank() ? null : safe;
    }
}
