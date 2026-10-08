package game.combat;

public class LocalCombatHit {

    private final String targetId;
    private final float x;
    private final float y;
    private final float damage;

    public LocalCombatHit(
            String targetId,
            float x,
            float y,
            float damage
    ) {
        this.targetId = targetId;
        this.x = x;
        this.y = y;
        this.damage = damage;
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
}