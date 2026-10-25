
package game.combat;

public class LocalCombatHit {

    private final String targetId;
    private final float x;
    private final float y;
    private final float damage;
    private final DamageType damageType;

    // Совместимость со старыми вызовами.
    public LocalCombatHit(
            String targetId,
            float x,
            float y,
            float damage
    ) {
        this(
                targetId,
                x,
                y,
                damage,
                DamageType.PHYSICAL
        );
    }

    public LocalCombatHit(
            String targetId,
            float x,
            float y,
            float damage,
            DamageType damageType
    ) {
        this.targetId = targetId;
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.damageType = damageType != null
                ? damageType
                : DamageType.PHYSICAL;
    }

    public String getTargetId() {
        return targetId;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getDamage() {
        return damage;
    }

    public DamageType getDamageType() {
        return damageType;
    }
}
