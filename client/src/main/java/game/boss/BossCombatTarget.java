package game.boss;

import game.boss.ancient.AncientAngelBoss;
import game.combat.LocalCombatTarget;

/**
 * Адаптер между универсальным Boss и LocalCombatSystem.
 *
 * LocalCombatSystem работает с LocalCombatTarget и не знает,
 * какой именно тип босса находится перед ним.
 */
public class BossCombatTarget extends LocalCombatTarget {

    private final Boss boss;

    public BossCombatTarget(Boss boss) {
        super(
                requireBoss(boss).getId(),
                boss.getX(),
                boss.getY(),
                boss.getWidth(),
                boss.getHeight(),
                boss.getMaxHp()
        );

        this.boss = boss;
    }

    private static Boss requireBoss(Boss boss) {
        if (boss == null) {
            throw new IllegalArgumentException("Boss cannot be null");
        }
        return boss;
    }

    public Boss getBoss() {
        return boss;
    }

    @Override
    public float getX() {
        return boss.getX();
    }

    @Override
    public float getY() {
        return boss.getY();
    }

    @Override
    public float getWidth() {
        return boss.getWidth();
    }

    @Override
    public float getHeight() {
        return boss.getHeight();
    }

    @Override
    public float getHp() {
        return boss.getHp();
    }

    @Override
    public float getMaxHp() {
        return boss.getMaxHp();
    }

    @Override
    public boolean isAlive() {
        return boss.isAlive();
    }

    @Override
    public void damage(float amount) {
        boss.damage(amount);
    }

    @Override
    public void reset() {
        boss.reset();
    }

    @Override
    public void setPosition(float x, float y) {
        boss.setPosition(x, y);
    }

    @Override
    public void setHp(float hp) {
        boss.setHp(hp);
    }

    @Override
    public int applySpeedDebuff(int amount) {
        int result = super.applySpeedDebuff(amount);

        if (boss instanceof AncientAngelBoss angel) {
            angel.setSpeedDebuff(result);
        }

        return result;
    }
}
