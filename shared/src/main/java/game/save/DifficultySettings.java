package game.save;

public final class DifficultySettings {

    private DifficultySettings() {
    }

    public static float enemyHealth(
            Difficulty difficulty,
            float base
    ) {
        return base *
                difficulty.getEnemyHealthMultiplier();
    }

    public static float enemyDamage(
            Difficulty difficulty,
            float base
    ) {
        return base *
                difficulty.getEnemyDamageMultiplier();
    }

    public static float playerDamage(
            Difficulty difficulty,
            float base
    ) {
        return base *
                difficulty.getPlayerDamageMultiplier();
    }

    public static float worldPressure(
            Difficulty difficulty
    ) {
        return difficulty.getWorldPressureMultiplier();
    }
}