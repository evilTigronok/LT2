package game.combat;

public class CombatTarget {

    private final String id;

    private float x;
    private float y;

    private final float width;
    private final float height;

    private float hp;
    private final float maxHp;

    public CombatTarget(
            String id,
            float x,
            float y,
            float width,
            float height,
            float maxHp
    ) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Target id cannot be empty"
            );
        }

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Target size must be positive"
            );
        }

        if (maxHp <= 0) {
            throw new IllegalArgumentException(
                    "Target max HP must be positive"
            );
        }

        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    public void damage(float amount) {
        if (amount <= 0 || !isAlive()) {
            return;
        }

        hp -= amount;

        if (hp < 0) {
            hp = 0;
        }
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public String getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getHp() {
        return hp;
    }

    public float getMaxHp() {
        return maxHp;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}