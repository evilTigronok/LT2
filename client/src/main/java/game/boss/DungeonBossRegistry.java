package game.boss;

import game.boss.ancient.AncientAngelBoss;
import game.combat.LocalCombatSystem;

import java.util.function.Consumer;

/**
 * Единая точка регистрации боссов подземелья.
 *
 * WorldScene не должна знать о конкретных классах боссов.
 * Здесь создаются боссы, добавляются в BossManager и подключаются
 * к локальной боевой системе.
 */
public final class DungeonBossRegistry {

    public static final String ANCIENT_ANGEL_ID = "ancient_angel";

    private static final float ANCIENT_ANGEL_X = 800f - 256f / 2f;
    private static final float ANCIENT_ANGEL_Y = 600f - 256f / 2f;
    private static final float ANCIENT_ANGEL_SIZE = 256f;
    private static final float ANCIENT_ANGEL_MAX_HP = 7878f;
    private static final float ANCIENT_ANGEL_CENTER_X = 800f;
    private static final float ANCIENT_ANGEL_CENTER_Y = 600f;

    private DungeonBossRegistry() {
        // Utility class.
    }

    /**
     * Регистрирует всех боссов подземелья.
     *
     * @param bossManager менеджер боссов
     * @param combatSystem локальная боевая система
     * @param playerDamageListener обработчик урона по игроку
     */
    public static void register(
            BossManager bossManager,
            LocalCombatSystem combatSystem,
            Consumer<Float> playerDamageListener
    ) {
        if (bossManager == null) {
            throw new IllegalArgumentException("BossManager cannot be null");
        }
        if (combatSystem == null) {
            throw new IllegalArgumentException("LocalCombatSystem cannot be null");
        }

        AncientAngelBoss ancientAngel = new AncientAngelBoss(
                ANCIENT_ANGEL_X,
                ANCIENT_ANGEL_Y,
                ANCIENT_ANGEL_SIZE,
                ANCIENT_ANGEL_SIZE,
                ANCIENT_ANGEL_MAX_HP,
                ANCIENT_ANGEL_CENTER_X,
                ANCIENT_ANGEL_CENTER_Y
        );

        if (playerDamageListener != null) {
            ancientAngel.setPlayerDamageListener(playerDamageListener);
        }

        bossManager.addBoss(ancientAngel);
        combatSystem.addTarget(new BossCombatTarget(ancientAngel));
    }
}
