package game.boss.ancient;

public class AncientAngelStreamProjectile {

    private static final float RADIUS = 70f;

    private float x;
    private float y;

    private final float directionX;
    private final float directionY;

    private final float speed;
    private final float maxDistance;
    private final float damage;

    private float travelledDistance;
    private boolean finished;

    public AncientAngelStreamProjectile(
            float x,
            float y,
            float directionX,
            float directionY,
            float speed,
            float maxDistance,
            float damage
    ) {
        this.x = x;
        this.y = y;

        float length =
                (float) Math.sqrt(
                        directionX * directionX
                                + directionY * directionY
                );

        if (length < 0.0001f) {
            this.directionX = 1f;
            this.directionY = 0f;
        } else {
            this.directionX = directionX / length;
            this.directionY = directionY / length;
        }

        this.speed = Math.max(0f, speed);
        this.maxDistance = Math.max(0f, maxDistance);
        this.damage = Math.max(0f, damage);

        this.travelledDistance = 0f;
        this.finished = false;
    }

    public void update(float deltaSeconds) {

        if (finished) {
            return;
        }

        float delta =
                Math.max(
                        0f,
                        deltaSeconds
                );

        float distance =
                speed * delta;

        x += directionX * distance;
        y += directionY * distance;

        travelledDistance += distance;

        if (travelledDistance >= maxDistance) {
            finished = true;
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getDirectionX() {
        return directionX;
    }

    public float getDirectionY() {
        return directionY;
    }

    public float getSpeed() {
        return speed;
    }

    public float getMaxDistance() {
        return maxDistance;
    }

    public float getTravelledDistance() {
        return travelledDistance;
    }

    public float getDamage() {
        return damage;
    }

    public float getRadius() {
        return RADIUS;
    }

    public boolean isFinished() {
        return finished;
    }

    public void finish() {
        finished = true;
    }
}