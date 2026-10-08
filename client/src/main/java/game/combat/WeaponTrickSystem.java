package game.combat;

import game.item.AbilityDefinition;
import game.weapon.Weapon;

public class WeaponTrickSystem {

    public static final String GUNGNIR_DRILL_ID =
            "gungnir_drill";

    /*
     * 22% от обычного урона Гунгира
     * за каждый тик.
     */
    public static final float GUNGNIR_DRILL_DAMAGE_PERCENT =
            0.22f;

    /*
     * 3.5 тика в секунду.
     */
    public static final float GUNGNIR_DRILL_TICK_INTERVAL =
            1.0f / 3.5f;

    /*
     * Длительность способности.
     */
    public static final float GUNGNIR_DRILL_DURATION =
            4.0f;

    /*
     * Перезарядка.
     */
    public static final float GUNGNIR_DRILL_COOLDOWN =
            20.0f;

    /*
     * Медленный рывок.
     */
    public static final float GUNGNIR_DRILL_SPEED =
            100.0f;

    /*
     * Ширина области перед игроком.
     */
    public static final float GUNGNIR_DRILL_WIDTH =
            48.0f;

    /*
     * Длина бура перед игроком.
     */
    public static final float GUNGNIR_DRILL_LENGTH =
            90.0f;

    public boolean isGungnirDrill(Weapon weapon) {

        if (weapon == null || !weapon.hasTrick()) {
            return false;
        }

        AbilityDefinition trick =
                weapon.getTrick();

        return trick != null
                && GUNGNIR_DRILL_ID.equals(
                trick.getId()
        );
    }
}