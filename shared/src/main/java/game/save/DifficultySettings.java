package game.save;

import game.item.ItemQuality;

public final class DifficultySettings {

    private DifficultySettings() {
    }

    public static float enemyHealth(
            Difficulty difficulty,
            float baseHealth
    ) {
        return baseHealth
                * difficulty.getEnemyHealthMultiplier();
    }

    public static float enemyDamage(
            Difficulty difficulty,
            float baseDamage
    ) {
        return baseDamage
                * difficulty.getEnemyDamageMultiplier();
    }

    public static float playerDamage(
            Difficulty difficulty,
            float baseDamage
    ) {
        return baseDamage
                * difficulty.getPlayerDamageMultiplier();
    }

    public static float weaponDamage(
            Difficulty difficulty,
            float baseDamage,
            ItemQuality quality
    ) {
        float damage = playerDamage(
                difficulty,
                baseDamage
        );

        if (difficulty == Difficulty.EXHAUSTING
                && quality != null
                && quality.getLevel() >= ItemQuality.ELITE.getLevel()) {

            damage *= 0.70f;
        }

        return damage;
    }

    public static double playerSpeed(
            Difficulty difficulty,
            double baseSpeed
    ) {
        return baseSpeed
                * difficulty.getPlayerSpeedMultiplier();
    }

    public static float worldPressure(
            Difficulty difficulty
    ) {
        return difficulty.getWorldPressureMultiplier();
    }
}