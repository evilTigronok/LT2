package game.combat;

import game.weapon.AttackStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttackStep {

    private final List<DamagePart> damageParts;

    private final AttackStyle attackStyle;

    /**
     * Задержка перед выполнением этого шага.
     * В секундах.
     */
    private final float delay;

    /**
     * Количество отдельных попаданий
     * внутри шага.
     */
    private final int hitCount;

    public AttackStep(
            List<DamagePart> damageParts,
            AttackStyle attackStyle,
            float delay,
            int hitCount
    ) {

        if (damageParts == null
                || damageParts.isEmpty()) {

            throw new IllegalArgumentException(
                    "Attack step must contain damage"
            );
        }

        if (delay < 0f) {
            throw new IllegalArgumentException(
                    "Delay cannot be negative"
            );
        }

        if (hitCount <= 0) {
            throw new IllegalArgumentException(
                    "Hit count must be positive"
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

        this.delay =
                delay;

        this.hitCount =
                hitCount;
    }

    public List<DamagePart> getDamageParts() {
        return damageParts;
    }

    public AttackStyle getAttackStyle() {
        return attackStyle;
    }

    public float getDelay() {
        return delay;
    }

    public int getHitCount() {
        return hitCount;
    }
}