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

    public SwordSwing(
            float originX,
            float originY,
            float directionX,
            float directionY,
            float damage,
            AttackStyle style
    ) {

        this.originX = originX;
        this.originY = originY;

        float length =
                (float) Math.sqrt(
                        directionX * directionX +
                                directionY * directionY
                );

        if (length < 0.0001f) {

            this.directionX = 0f;
            this.directionY = 1f;

        } else {

            this.directionX =
                    directionX / length;

            this.directionY =
                    directionY / length;
        }

        this.damage = damage;

        this.style =
                style == null
                        ? AttackStyle.SWING
                        : style;

        this.elapsed = 0f;
        this.finished = false;
    }

    /**
     * Обновляет позицию игрока, относительно которой
     * должен находиться меч.
     */
    public void setOrigin(
            float x,
            float y
    ) {
        this.originX = x;
        this.originY = y;
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

        if (elapsed >= DURATION) {

            elapsed = DURATION;
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

        if (DURATION <= 0f) {
            return 1f;
        }

        return Math.max(
                0f,
                Math.min(
                        1f,
                        elapsed / DURATION
                )
        );
    }

    /**
     * Текущий угол меча относительно
     * первоначального направления атаки.
     */
    public float getCurrentAngleDegrees() {

        /*
         * Начинаем с левой части дуги
         * и заканчиваем на правой.
         */
        float start =
                -ARC_DEGREES / 2f;

        return start +
                ARC_DEGREES *
                        getProgress();
    }

    /**
     * Получает текущий угол направления меча
     * в мировых координатах.
     */
    public float getWorldAngleRadians() {

        float baseAngle =
                (float) Math.atan2(
                        directionY,
                        directionX
                );

        return baseAngle +
                (float) Math.toRadians(
                        getCurrentAngleDegrees()
                );
    }

    /**
     * Начальная точка лезвия.
     */
    /**
     * Начальная точка оружия.
     */
    public float getStartX() {

        /*
         * Для выпада оружие выходит непосредственно
         * из персонажа по направлению атаки.
         */
        if (style == AttackStyle.THRUST
                || style == AttackStyle.STAB) {

            return originX
                    + directionX * 12f;
        }

        float angle =
                getWorldAngleRadians();

        return originX
                + (float) Math.cos(angle) * 12f;
    }


    /**
     * Начальная точка оружия.
     */
    public float getStartY() {

        if (style == AttackStyle.THRUST
                || style == AttackStyle.STAB) {

            return originY
                    + directionY * 12f;
        }

        float angle =
                getWorldAngleRadians();

        return originY
                + (float) Math.sin(angle) * 12f;
    }


    /**
     * Конечная точка оружия.
     */
    public float getEndX() {

        /*
         * THRUST / STAB:
         *
         * оружие не вращается по дуге,
         * а совершает прямой выпад.
         *
         * В начале анимации копьё короткое,
         * затем полностью выдвигается.
         */
        if (style == AttackStyle.THRUST
                || style == AttackStyle.STAB) {

            float progress =
                    getProgress();

            /*
             * Небольшая задержка в начале,
             * затем быстрый выпад.
             */
            float thrustProgress =
                    Math.min(
                            1f,
                            progress * 1.6f
                    );

            float length =
                    20f + 90f * thrustProgress;

            return originX
                    + directionX * length;
        }

        float angle =
                getWorldAngleRadians();

        return originX
                + (float) Math.cos(angle) *
                SwordSwing.SWORD_LENGTH;
    }


    /**
     * Конечная точка оружия.
     */
    public float getEndY() {

        if (style == AttackStyle.THRUST
                || style == AttackStyle.STAB) {

            float progress =
                    getProgress();

            float thrustProgress =
                    Math.min(
                            1f,
                            progress * 1.6f
                    );

            float length =
                    20f + 90f * thrustProgress;

            return originY
                    + directionY * length;
        }

        float angle =
                getWorldAngleRadians();

        return originY
                + (float) Math.sin(angle) *
                SwordSwing.SWORD_LENGTH;
    }
}