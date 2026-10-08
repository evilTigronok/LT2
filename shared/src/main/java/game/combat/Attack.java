package game.combat;

import game.weapon.AttackStyle;

public class Attack {

    private final String weaponId;
    private final AttackStyle style;

    private final float damagePerTick;
    private final int totalTicks;
    private final float tickInterval;

    public Attack(
            String weaponId,
            AttackStyle style,
            float damagePerTick,
            int totalTicks,
            float tickInterval
    ) {
        this.weaponId = weaponId;
        this.style = style;
        this.damagePerTick = damagePerTick;
        this.totalTicks = totalTicks;
        this.tickInterval = tickInterval;
    }

    public String getWeaponId() {
        return weaponId;
    }

    public AttackStyle getStyle() {
        return style;
    }

    public float getDamagePerTick() {
        return damagePerTick;
    }

    public int getTotalTicks() {
        return totalTicks;
    }

    public float getTickInterval() {
        return tickInterval;
    }
}