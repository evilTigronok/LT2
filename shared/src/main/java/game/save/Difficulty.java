package game.save;

public enum Difficulty {

    RELAXING(
            "Расслабляющая",
            0.75f,
            0.75f,
            1.25f,
            0.75f
    ),

    EXHAUSTING(
            "Изнурительная",
            1.0f,
            1.0f,
            1.0f,
            1.0f
    ),

    DIVINE(
            "Божественная",
            1.50f,
            1.50f,
            0.75f,
            1.50f
    );

    private final String displayName;

    private final float enemyHealthMultiplier;
    private final float enemyDamageMultiplier;
    private final float playerDamageMultiplier;
    private final float worldPressureMultiplier;

    Difficulty(
            String displayName,
            float enemyHealthMultiplier,
            float enemyDamageMultiplier,
            float playerDamageMultiplier,
            float worldPressureMultiplier
    ) {
        this.displayName = displayName;
        this.enemyHealthMultiplier = enemyHealthMultiplier;
        this.enemyDamageMultiplier = enemyDamageMultiplier;
        this.playerDamageMultiplier = playerDamageMultiplier;
        this.worldPressureMultiplier = worldPressureMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getEnemyHealthMultiplier() {
        return enemyHealthMultiplier;
    }

    public float getEnemyDamageMultiplier() {
        return enemyDamageMultiplier;
    }

    public float getPlayerDamageMultiplier() {
        return playerDamageMultiplier;
    }

    public float getWorldPressureMultiplier() {
        return worldPressureMultiplier;
    }

    @Override
    public String toString() {
        return displayName;
    }
}