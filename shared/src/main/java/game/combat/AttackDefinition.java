package game.combat;

import game.weapon.AttackStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttackDefinition {

    private final List<DamagePart> damageParts;

    private final AttackStyle attackStyle;

    private final int hitCount;

    private final int projectileCount;

    private final boolean piercing;

    /**
     * Скорость начала атак:
     * количество атак в секунду.
     */
    private final float attackSpeed;

    private final float projectileSpeed;

    private final float projectileRange;

    /**
     * Серия ударов.
     *
     * Если null — атака одиночная.
     */
    private final AttackSequence sequence;

    /** Количество защиты, игнорируемое этой атакой. */
    private final float defenseIgnore;

    public AttackDefinition(
            List<DamagePart> damageParts,
            AttackStyle attackStyle,
            int hitCount,
            int projectileCount,
            boolean piercing,
            float attackSpeed,
            float projectileSpeed,
            float projectileRange
    ) {

        this(
                damageParts,
                attackStyle,
                hitCount,
                projectileCount,
                piercing,
                attackSpeed,
                projectileSpeed,
                projectileRange,
                null,
                0f
        );
    }

    public AttackDefinition(
            List<DamagePart> damageParts,
            AttackStyle attackStyle,
            int hitCount,
            int projectileCount,
            boolean piercing,
            float attackSpeed,
            float projectileSpeed,
            float projectileRange,
            AttackSequence sequence
    ) {
        this(
                damageParts,
                attackStyle,
                hitCount,
                projectileCount,
                piercing,
                attackSpeed,
                projectileSpeed,
                projectileRange,
                sequence,
                0f
        );
    }

    public AttackDefinition(
            List<DamagePart> damageParts,
            AttackStyle attackStyle,
            int hitCount,
            int projectileCount,
            boolean piercing,
            float attackSpeed,
            float projectileSpeed,
            float projectileRange,
            AttackSequence sequence,
            float defenseIgnore
    ) {

        if (damageParts == null
                || damageParts.isEmpty()) {

            throw new IllegalArgumentException(
                    "Attack must contain damage"
            );
        }

        if (hitCount <= 0) {
            throw new IllegalArgumentException(
                    "Hit count must be positive"
            );
        }

        if (projectileCount < 0) {
            throw new IllegalArgumentException(
                    "Projectile count cannot be negative"
            );
        }

        if (attackSpeed <= 0f) {
            throw new IllegalArgumentException(
                    "Attack speed must be positive"
            );
        }

        if (projectileSpeed < 0f) {
            throw new IllegalArgumentException(
                    "Projectile speed cannot be negative"
            );
        }

        if (projectileRange < 0f) {
            throw new IllegalArgumentException(
                    "Projectile range cannot be negative"
            );
        }

        if (defenseIgnore < 0f) {
            throw new IllegalArgumentException(
                    "Defense ignore cannot be negative"
            );
        }

        this.damageParts =
                Collections.unmodifiableList(
                        new ArrayList<>(
                                damageParts
                        )
                );

        this.attackStyle =
                attackStyle;

        this.hitCount =
                hitCount;

        this.projectileCount =
                projectileCount;

        this.piercing =
                piercing;

        this.attackSpeed =
                attackSpeed;

        this.projectileSpeed =
                projectileSpeed;

        this.projectileRange =
                projectileRange;

        this.sequence =
                sequence;

        this.defenseIgnore =
                defenseIgnore;
    }

    public List<DamagePart> getDamageParts() {
        return damageParts;
    }

    public AttackStyle getAttackStyle() {
        return attackStyle;
    }

    public int getHitCount() {
        return hitCount;
    }

    public int getProjectileCount() {
        return projectileCount;
    }

    public boolean isPiercing() {
        return piercing;
    }

    public float getAttackSpeed() {
        return attackSpeed;
    }

    public double getAttackInterval() {
        return 1.0 / attackSpeed;
    }

    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    public float getProjectileRange() {
        return projectileRange;
    }

    public AttackSequence getSequence() {
        return sequence;
    }

    public boolean hasSequence() {
        return sequence != null;
    }

    public float getDefenseIgnore() {
        return defenseIgnore;
    }
}