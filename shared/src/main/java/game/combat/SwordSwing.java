package game.combat;

import game.weapon.AttackStyle;

/**
 * Один взмах мечом.
 *
 * Направление атаки фиксируется в момент создания.
 * Положение самого меча во время взмаха следует
 * за текущим положением игрока.
 */
public class SwordSwing {

    /**
     * Полная продолжительность взмаха.
     */
    public static final float DURATION = 0.28f;

    /**
     * Длина меча.
     */
    public static final float SWORD_LENGTH = 78f;

    /**
     * Толщина меча для collision.
     */
    public static final float SWORD_THICKNESS = 12f;

    /**
     * Угол дуги взмаха.
     *
     * 130 градусов достаточно заметны визуально,
     * но при этом меч не делает полный круг.
     */
    public static final float ARC_DEGREES = 130f;

    private final float directionX;
    private final float directionY;

    private final float damage;

    private final AttackStyle style;

    private float originX;
    private float originY;

    private float elapsed;

    private boolean finished;

    private final boolean fixedOrigin;

    public SwordSwing(
            float originX,
            float originY,
            float directionX,
            float directionY,
            float damage,
            AttackStyle style
    ) {
        this(
                originX,
                originY,
                directionX,
                directionY,
                damage,
                style,
                false
        );
    }

    public SwordSwing(
            float originX,
            float originY,
            float directionX,
            float directionY,
            float damage,
            AttackStyle style,
            boolean fixedOrigin
    ) {
        this.originX = originX;
        this.originY = originY;

        float length = (float) Math.sqrt(
                directionX * directionX +
                        directionY * directionY
        );

        if (length < 0.0001f) {
            this.directionX = 0f;
            this.directionY = 1f;
        } else {
            this.directionX = directionX / length;
            this.directionY = directionY / length;
        }

        this.damage = damage;

        this.style = style == null
                ? AttackStyle.SWING
                : style;

        this.fixedOrigin = fixedOrigin;

        this.elapsed = 0f;
        this.finished = false;
    }

    /**
     * Обновляет позицию игрока, относительно которой
     * должен находиться меч.
     */
    public void setOrigin(float x, float y) {
        if (!fixedOrigin) {
            this.originX = x;
            this.originY = y;
        }
    }
    public float getDuration() {
        if (style == AttackStyle.THRUST
                || style == AttackStyle.STAB) {
            return 0.18f;
        }

        if (style == AttackStyle.QUICK_SWING) {
            return 0.15f;
        }

        return DURATION;
    }

    /**
     * Обновляет время существования взмаха.
     */
    public void update(float deltaSeconds) {

        if (finished) {
            return;
        }

        elapsed +=
                Math.max(
                        0f,
                        deltaSeconds
                );

        if (elapsed >= getDuration()) {
            elapsed = getDuration();
            finished = true;
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public float getOriginX() {
        return originX;
    }

    public float getOriginY() {
        return originY;
    }

    public float getDirectionX() {
        return directionX;
    }

    public float getDirectionY() {
        return directionY;
    }

    public float getDamage() {
        return damage;
    }

    public AttackStyle getStyle() {
        return style;
    }

    public float getProgress() {
        float duration = getDuration();

        if (duration <= 0f) {
            return 1f;
        }

        return Math.max(
                0f,
                Math.min(1f, elapsed / duration)
        );
    }

    /**
     * Текущий угол меча относительно
     * первоначального направления атаки.
     */
    public float getCurrentAngleDegrees() {
        float arc = style == AttackStyle.QUICK_SWING
                ? 85f
                : ARC_DEGREES;

        return -arc / 2f + arc * getProgress();
    }

    /**
     * Получает текущий угол направления меча
     * в мировых координатах.
     */
    public float getWorldAngleRadians() {
        float baseAngle = (float) Math.atan2(directionY, directionX);

        if (style == AttackStyle.THRUST
                || style == AttackStyle.STAB) {
            return baseAngle;
        }

        return baseAngle
                + (float) Math.toRadians(getCurrentAngleDegrees());
    }

    /**
     * Дистанция от запястья до центра спрайта.
     * Расчёт должен совпадать с renderEquippedWeapon().
     */
    private float getVisualDistance() {
        float progress = getProgress();

        if (style == AttackStyle.THRUST
                || style == AttackStyle.STAB) {

            float extension;

            if (progress < 0.65f) {
                extension = progress / 0.65f;
            } else {
                extension = 1.0f
                        - (progress - 0.65f) / 0.35f * 0.25f;
            }

            extension = Math.max(
                    0.0f,
                    Math.min(1.0f, extension)
            );

            return 24.0f + 38.0f * extension;
        }

        return 24.0f
                + (float) Math.sin(progress * Math.PI) * 8.0f;
    }

    /**
     * X-координата центра спрайта оружия.
     */
    private float getVisualHandX() {
        float angle = getWorldAngleRadians();

        float dirX = (float) Math.cos(angle);
        float dirY = (float) Math.sin(angle);

        float shoulderX = dirX >= 0.0f ? 22.0f : -22.0f;

        float handX = originX
                + shoulderX
                + dirX * (31.0f + getVisualDistance());

        if (dirY <= 0.65f && Math.abs(dirX) > 0.65f) {
            handX += Math.signum(dirX) * 7.0f;
        }

        return handX;
    }

    /**
     * Y-координата центра спрайта оружия.
     */
    private float getVisualHandY() {
        float angle = getWorldAngleRadians();

        float dirX = (float) Math.cos(angle);
        float dirY = (float) Math.sin(angle);

        float handY = originY
                - 19.0f
                + dirY * (31.0f + getVisualDistance());

        if (dirY < -0.35f) {
            // То же смещение, которое применяется к мечу при атаке вверх.
            handY -= 20.0f;
        } else if (dirY > 0.65f) {
            handY += 18.0f;
        } else if (Math.abs(dirX) > 0.65f) {
            handY += 8.0f;
        }

        return handY;
    }


    /**
     * Начало отрезка хитбокса, центрированного на спрайте.
     */
    public float getStartX() {
        float angle = getWorldAngleRadians();

        return getVisualHandX()
                - (float) Math.cos(angle) * SWORD_LENGTH / 2.0f;
    }

    public float getStartY() {
        float angle = getWorldAngleRadians();

        return getVisualHandY()
                - (float) Math.sin(angle) * SWORD_LENGTH / 2.0f;
    }

    /**
     * Конец отрезка хитбокса, центрированного на спрайте.
     */
    public float getEndX() {
        float angle = getWorldAngleRadians();

        return getVisualHandX()
                + (float) Math.cos(angle) * SWORD_LENGTH / 2.0f;
    }

    public float getEndY() {
        float angle = getWorldAngleRadians();

        return getVisualHandY()
                + (float) Math.sin(angle) * SWORD_LENGTH / 2.0f;
    }
}