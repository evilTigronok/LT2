package game.boss;

import javafx.scene.canvas.GraphicsContext;

public abstract class Boss {

    private final String id;
    private final String name;

    private final float maxHp;
    private float hp;

    private float x;
    private float y;

    private final float width;
    private final float height;

    private boolean defeated;

    protected Boss(
            String id,
            String name,
            float x,
            float y,
            float width,
            float height,
            float maxHp
    ) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Boss id cannot be empty"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Boss name cannot be empty"
            );
        }

        if (maxHp <= 0f) {
            throw new IllegalArgumentException(
                    "Boss max HP must be positive"
            );
        }

        this.id = id;
        this.name = name;

        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;

        this.maxHp = maxHp;
        this.hp = maxHp;

        this.defeated = false;
    }

    /**
     * Основная логика босса.
     */
    public abstract void update(
            float deltaSeconds,
            float playerX,
            float playerY
    );

    /**
     * Отрисовка босса.
     */
    public abstract void render(
            GraphicsContext gc
    );

    /**
     * Получение урона.
     */
    public void damage(float damage) {

        if (defeated) {
            return;
        }

        if (damage <= 0f) {
            return;
        }

        hp = Math.max(
                0f,
                hp - damage
        );

        if (hp <= 0f) {
            defeated = true;

            onDefeated();
        }
    }

    /**
     * Вызывается один раз при смерти босса.
     */
    protected void onDefeated() {
    }

    /**
     * Полный сброс босса.
     */
    public void reset() {

        hp = maxHp;
        defeated = false;

        onReset();
    }

    /**
     * Дополнительная логика после reset().
     */
    protected void onReset() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getHp() {
        return hp;
    }

    public float getMaxHp() {
        return maxHp;
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

    public void setPosition(
            float x,
            float y
    ) {
        this.x = x;
        this.y = y;
    }

    public void setHp(float hp) {

        this.hp = Math.max(
                0f,
                Math.min(
                        maxHp,
                        hp
                )
        );

        defeated = this.hp <= 0f;
    }

    public boolean isAlive() {
        return !defeated && hp > 0f;
    }

    public boolean isDefeated() {
        return defeated;
    }
}