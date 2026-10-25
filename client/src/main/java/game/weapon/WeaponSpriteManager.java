
package game.weapon;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Хранит и загружает спрайты оружия.
 * Ключ — стабильный ID оружия.
 */
public final class WeaponSpriteManager {

    private static final String BASE_PATH =
            "/assets/weapons/";

    private final Map<String, Image> sprites =
            new HashMap<>();

    public WeaponSpriteManager() {
        register("gungnir", "spears/gungnir.png");
        register("reliable_spear", "spears/reliable_spear.png");

        register("wooden_sword", "swords/wooden_sword.png");
        register("zen", "swords/zen.png");
        register("zenith", "swords/zenith.png");
        register(
                "material_curse_blade",
                "swords/material_curse_blade.png"
        );
        register(
                "magmatic_claymore",
                "swords/magmatic_claymore.png"
        );
        register("dawn", "swords/dawn.png");
        register("twilight", "swords/twilight.png");
        register(
                "weighted_claymore",
                "swords/weighted_claymore.png"
        );
        register("angelic_doom", "ranged/angelic_doom.png");
        register("mourning", "ranged/mourning.png");
        register("ice_shooter", "ranged/ice_shooter.png");
        register("doom_star_shooter", "ranged/doom_star_shooter.png");
        register("gungnir_launcher", "ranged/gungnir_launcher.png");
    }

    private void register(String weaponId, String resourcePath) {
        String path = BASE_PATH + resourcePath;

        try (InputStream stream =
                     getClass().getResourceAsStream(path)) {

            if (stream == null) {
                System.err.println(
                        "Не найден спрайт оружия: " + path
                );
                return;
            }

            Image image = new Image(stream);

            if (image.isError()) {
                System.err.println(
                        "Ошибка загрузки спрайта: " + path
                );
                return;
            }

            sprites.put(weaponId, image);

        } catch (Exception e) {
            System.err.println(
                    "Не удалось загрузить спрайт "
                            + weaponId + ": " + e.getMessage()
            );
        }
    }

    public Image getSprite(String weaponId) {
        return sprites.get(weaponId);
    }

    public boolean hasSprite(String weaponId) {
        return sprites.containsKey(weaponId);
    }
}
