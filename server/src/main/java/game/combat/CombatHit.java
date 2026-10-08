package game.combat;

public class CombatHit {

    private final String attacker;
    private final String targetId;

    private final float x;
    private final float y;

    private final float damage;

    public CombatHit(
            String attacker,
            String targetId,
            float x,
            float y,
            float damage
    ) {

        this.attacker = attacker;
        this.targetId = targetId;
        this.x = x;
        this.y = y;
        this.damage = damage;
    }

    public String getAttacker() {
        return attacker;
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