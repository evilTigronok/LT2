package game.combat;

public class DamageTick {

    private final String weaponId;
    private final float damage;
    private final int tickNumber;
    private final int totalTicks;

    public DamageTick(
            String weaponId,
            float damage,
            int tickNumber,
            int totalTicks
    ) {
        this.weaponId = weaponId;
        this.damage = damage;
        this.tickNumber = tickNumber;
        this.totalTicks = totalTicks;
    }

    public String getWeaponId() {
        return weaponId;
    }

    public float getDamage() {
        return damage;
    }

    public int getTickNumber() {
        return tickNumber;
    }

    public int getTotalTicks() {
        return totalTicks;
    }
}