package game.combat;

import java.util.HashSet;
import java.util.Set;

public class RangedProjectile {

    private final float damage;
    private final float speed;
    private final float maxRange;
    private final boolean piercing;

    private final DamageType damageType;
    private final AngelicDoomMode angelicDoomMode;
    private final float defenseIgnore;

    private float x;
    private float y;

    private final float directionX;
    private final float directionY;

    private float travelledDistance;

    private boolean finished;

    /*
     * Цели, которые этот конкретный снаряд
     * уже поразил.
     *
     * Нужно для пробивающих снарядов:
     * один и тот же снаряд не должен несколько
     * раз подряд наносить урон одной цели.
     */
    private final Set<String> hitTargets =
            new HashSet<>();

    public RangedProjectile(
            float originX,
            float originY,
            float directionX,
            float directionY,
            float damage,
            float speed,
            float maxRange,
            boolean piercing
    ) {

        this.x = originX;
        this.y = originY;

        float length =
                (float) Math.sqrt(
                        directionX * directionX
                                +
                                directionY * directionY
                );

        if (length < 0.0001f) {

            this.directionX = 1f;
            this.directionY = 0f;

        } else {

            this.directionX =
                    directionX / length;

            this.directionY =
                    directionY / length;
        }

        this.damage =
                Math.max(0f, damage);

        this.speed =
                Math.max(0f, speed);

        this.maxRange =
                Math.max(0f, maxRange);

        this.piercing =
                piercing;

        this.travelledDistance =
                0f;

        this.finished =
                false;

        this.damageType = DamageType.PHYSICAL;
        this.angelicDoomMode = AngelicDoomMode.SOLAR;
        this.defenseIgnore = 0f;
    }

    public RangedProjectile(
            float originX,
            float originY,
            float directionX,
            float directionY,
            float damage,
            float speed,
            float maxRange,
            boolean piercing,
            DamageType damageType,
            AngelicDoomMode angelicDoomMode
    ) {

        this.x = originX;
        this.y = originY;

        float length =
                (float) Math.sqrt(
                        directionX * directionX
                                +
                                directionY * directionY
                );

        if (length < 0.0001f) {

            this.directionX = 1f;
            this.directionY = 0f;

        } else {

            this.directionX =
                    directionX / length;

            this.directionY =
                    directionY / length;
        }

        this.damage =
                Math.max(0f, damage);

        this.speed =
                Math.max(0f, speed);

        this.maxRange =
                Math.max(0f, maxRange);

        this.piercing =
                piercing;

        this.damageType =
                damageType != null
                        ? damageType
                        : DamageType.PHYSICAL;

        this.angelicDoomMode =
                angelicDoomMode != null
                        ? angelicDoomMode
                        : AngelicDoomMode.SOLAR;

        this.defenseIgnore = 0f;
        this.travelledDistance = 0f;

        this.finished = false;
    }

    public RangedProjectile(
            float originX,
            float originY,
            float directionX,
            float directionY,
            float damage,
            float speed,
            float maxRange,
            boolean piercing,
            DamageType damageType,
            AngelicDoomMode angelicDoomMode,
            float defenseIgnore
    ) {
        this.x = originX;
        this.y = originY;

        float length = (float) Math.sqrt(
                directionX * directionX + directionY * directionY
        );
        if (length < 0.0001f) {
            this.directionX = 1f;
            this.directionY = 0f;
        } else {
            this.directionX = directionX / length;
            this.directionY = directionY / length;
        }

        this.damage = Math.max(0f, damage);
        this.speed = Math.max(0f, speed);
        this.maxRange = Math.max(0f, maxRange);
        this.piercing = piercing;
        this.damageType = damageType != null ? damageType : DamageType.PHYSICAL;
        this.angelicDoomMode = angelicDoomMode != null
                ? angelicDoomMode : AngelicDoomMode.SOLAR;
        this.defenseIgnore = Math.max(0f, defenseIgnore);
        this.travelledDistance = 0f;
        this.finished = false;
    }

    public void update(
            float deltaSeconds
    ) {

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

        x +=
                directionX * distance;

        y +=
                directionY * distance;

        travelledDistance +=
                distance;

        if (travelledDistance >= maxRange) {

            travelledDistance =
                    maxRange;

            finished = true;
        }
    }

    public boolean hasHitTarget(
            String targetId
    ) {

        return targetId != null
                && hitTargets.contains(
                targetId
        );
    }

    public void markTargetHit(
            String targetId
    ) {

        if (targetId != null) {
            hitTargets.add(
                    targetId
            );
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

    public float getDamage() {
        return damage;
    }

    public float getSpeed() {
        return speed;
    }

    public float getMaxRange() {
        return maxRange;
    }

    public float getTravelledDistance() {
        return travelledDistance;
    }

    public boolean isPiercing() {
        return piercing;
    }

    public boolean isFinished() {
        return finished;
    }

    public void finish() {
        finished = true;
    }

    public DamageType getDamageType() {
        return damageType;
    }

    public AngelicDoomMode getAngelicDoomMode() {
        return angelicDoomMode;
    }

    public float getDefenseIgnore() {
        return defenseIgnore;
    }
}