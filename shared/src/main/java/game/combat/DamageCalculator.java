package game.combat;

import game.weapon.Weapon;

public final class DamageCalculator {

    private DamageCalculator() {
    }

    /**
     * Рассчитывает суммарный урон одного попадания
     * основной атаки оружия.
     *
     * Формула каждой составляющей:
     *
     * baseDamage
     * +
     * attackPercent * (playerAttack + weaponAttack)
     */
    public static float calculateTickDamage(
            Weapon weapon,
            float playerAttack
    ) {
        if (weapon == null) {
            return 0f;
        }
        return calculateTickDamage(
                weapon.getPrimaryAttack(),
                playerAttack,
                weapon.getAttack()
        );
    }

    /** Рассчитывает урон указанного профиля атаки. */
    public static float calculateTickDamage(
            AttackDefinition attack,
            float playerAttack,
            float weaponAttack
    ) {
        if (attack == null) {
            return 0f;
        }

        float totalAttack =
                Math.max(0f, playerAttack + weaponAttack);

        float totalDamage = 0f;

        for (DamagePart damagePart : attack.getDamageParts()) {
            if (damagePart == null) {
                continue;
            }
            totalDamage +=
                    damagePart.getBaseDamage()
                            + damagePart.getAttackPercent() * totalAttack;
        }

        return totalDamage;
    }
}