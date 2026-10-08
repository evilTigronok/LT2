package game.combat;

public class DamagePart {

    private final DamageType type;

    private final float baseDamage;

    private final float attackPercent;

    public DamagePart(
            DamageType type,
            float baseDamage,
            float attackPercent
    ) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "Damage type cannot be null"
            );
        }

        if (baseDamage < 0f) {
            throw new IllegalArgumentException(
                    "Base damage cannot be negative"
            );
        }

        if (attackPercent < 0f) {
            throw new IllegalArgumentException(
                    "Attack percent cannot be negative"
            );
        }

        this.type = type;
        this.baseDamage = baseDamage;
        this.attackPercent = attackPercent;
    }

    public DamageType getType() {
        return type;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public float getAttackPercent() {
        return attackPercent;
    }
}