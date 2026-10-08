package game.combat;

public class LocalCombatTarget {

    private final String id;

    private float x;
    private float y;

    private final float width;
    private final float height;

    private final float maxHp;
    private float hp;

    private int speedDebuff;
    private int defenseDebuff;

    public LocalCombatTarget(
            String id,
            float x,
            float y,
            float width,
            float height,
            float maxHp
    ) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxHp = maxHp;
        this.hp = maxHp;
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

    public boolean isAlive() {
        return hp > 0f;
    }

    public void damage(float amount) {
        if (amount <= 0f || !isAlive()) {
            return;
        }

        float actualDamage =
                Math.max(
                        0f,
                        amount - defenseDebuff
                );

        hp = Math.max(
                0f,
                hp - actualDamage
        );
    }

    public void reset() {
        hp = maxHp;
        speedDebuff = 0;
        defenseDebuff = 0;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setHp(
            float hp
    ) {

        this.hp =
                Math.max(
                        0f,
                        Math.min(
                                maxHp,
                                hp
                        )
                );
    }

    public int applySpeedDebuff(int amount) {

        if (amount <= 0) {
            return speedDebuff;
        }

        speedDebuff =
                Math.max(
                        -48,
                        speedDebuff - amount
                );

        return speedDebuff;
    }

    public int applyDefenseDebuff(int amount) {

        if (amount <= 0) {
            return defenseDebuff;
        }

        defenseDebuff =
                Math.max(
                        -20,
                        defenseDebuff - amount
                );

        return defenseDebuff;
    }

    public int getSpeedDebuff() {
        return speedDebuff;
    }

    public int getDefenseDebuff() {
        return defenseDebuff;
    }
}