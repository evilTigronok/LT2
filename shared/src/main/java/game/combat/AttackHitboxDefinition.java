package game.combat;

import game.weapon.AttackStyle;

public class AttackHitboxDefinition {

    public enum Shape {
        SECTOR,
        RECTANGLE,
        CIRCLE
    }

    private final Shape shape;

    private final float range;
    private final float halfWidth;
    private final float halfAngle;
    private final float radius;

    private AttackHitboxDefinition(
            Shape shape,
            float range,
            float halfWidth,
            float halfAngle,
            float radius
    ) {
        this.shape = shape;
        this.range = range;
        this.halfWidth = halfWidth;
        this.halfAngle = halfAngle;
        this.radius = radius;
    }

    public static AttackHitboxDefinition forStyle(AttackStyle style) {

        if (style == null) {
            style = AttackStyle.SWING;
        }

        return switch (style) {

            // Широкий взмах оружием перед персонажем.
            case SWING -> new AttackHitboxDefinition(
                    Shape.SECTOR,
                    100f,
                    0f,
                    37.5f,
                    0f
            );

            // Прямой выпад.
            case THRUST -> new AttackHitboxDefinition(
                    Shape.RECTANGLE,
                    110f,
                    18f,
                    0f,
                    0f
            );

            // Удар сверху вниз.
            case OVERHEAD -> new AttackHitboxDefinition(
                    Shape.SECTOR,
                    80f,
                    0f,
                    35f,
                    0f
            );

            // Широкий диагональный/боковой порез.
            case SLASH -> new AttackHitboxDefinition(
                    Shape.SECTOR,
                    90f,
                    0f,
                    50f,
                    0f
            );

            // Узкий быстрый колющий удар.
            case STAB -> new AttackHitboxDefinition(
                    Shape.RECTANGLE,
                    125f,
                    10f,
                    0f,
                    0f
            );

            // Удар по области вокруг персонажа.
            case SMASH -> new AttackHitboxDefinition(
                    Shape.CIRCLE,
                    0f,
                    0f,
                    0f,
                    80f
            );

            // Дальнее оружие не использует данный хитбокс.
            // Попадание обрабатывается RangedProjectileSystem.
            case SHOOT -> new AttackHitboxDefinition(
                    Shape.RECTANGLE,
                    0f,
                    0f,
                    0f,
                    0f
            );
        };
    }

    public Shape getShape() {
        return shape;
    }

    public float getRange() {
        return range;
    }

    public float getHalfWidth() {
        return halfWidth;
    }

    public float getHalfAngle() {
        return halfAngle;
    }

    public float getRadius() {
        return radius;
    }
}