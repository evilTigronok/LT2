package game.boss.ancient;

/**
 * Снаряд Ancient Angel.
 */
public class AncientAngelProjectile {

    private float x;
    private float y;

    private final float directionX;
    private final float directionY;

    private final float damage;

    private static final float SPEED = 260f;

    private static final float RADIUS = 7f;

    /**
     * Максимальное время жизни снаряда.
     */
    private static final double MAX_LIFETIME = 8.0;

    private double lifetime;

    private boolean expired;

    public AncientAngelProjectile(
            float x,
            float y,
            float directionX,
            float directionY,
            float damage
    ) {
        this.x = x;
        this.y = y;

        this.directionX = directionX;
        this.directionY = directionY;

        this.damage = damage;
    }

    public void update(
            double deltaSeconds
    ) {
        if (expired) {
            return;
        }

        x +=
                directionX
                        * SPEED
                        * (float) deltaSeconds;

        y +=
                directionY
                        * SPEED
                        * (float) deltaSeconds;

        lifetime += deltaSeconds;

        if (lifetime >= MAX_LIFETIME) {
            expired = true;
        }
    }

    public void expire() {
        expired = true;
    }

    public boolean isExpired() {
        return expired;
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

    public float getRadius() {
        return RADIUS;
    }
}