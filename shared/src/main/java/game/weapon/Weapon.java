package game.weapon;

import game.combat.AttackDefinition;
import game.item.AbilityDefinition;
import game.item.ItemQuality;

public class Weapon {

    private final String id;
    private final String name;

    private final ItemQuality quality;
    private final WeaponType type;

    private final float attack;

    private final AttackDefinition primaryAttack;
    private final AttackDefinition alternateAttack;
    private final AttackDefinition chargedFireAttack;
    private final AttackDefinition chargedElectricAttack;
    private final WeaponSpecial special;

    private final AbilityDefinition trick;
    private final AbilityDefinition passiveEffect;

    public Weapon(
            String id,
            String name,
            ItemQuality quality,
            WeaponType type,
            float attack,
            AttackDefinition primaryAttack,
            AbilityDefinition trick,
            AbilityDefinition passiveEffect
    ) {
        this(
                id, name, quality, type, attack, primaryAttack,
                null, null, null, WeaponSpecial.NONE, trick, passiveEffect
        );
    }

    public Weapon(
            String id,
            String name,
            ItemQuality quality,
            WeaponType type,
            float attack,
            AttackDefinition primaryAttack,
            AttackDefinition alternateAttack,
            AttackDefinition chargedFireAttack,
            AttackDefinition chargedElectricAttack,
            WeaponSpecial special,
            AbilityDefinition trick,
            AbilityDefinition passiveEffect
    ) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Weapon id cannot be empty"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Weapon name cannot be empty"
            );
        }

        if (quality == null) {
            throw new IllegalArgumentException(
                    "Weapon quality cannot be null"
            );
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "Weapon type cannot be null"
            );
        }

        if (attack < 0f) {
            throw new IllegalArgumentException(
                    "Weapon attack cannot be negative"
            );
        }

        if (primaryAttack == null) {
            throw new IllegalArgumentException(
                    "Primary attack cannot be null"
            );
        }

        /*
         * Трюк обязателен для оружия
         * ближнего боя качества 2+.
         */
        if (type == WeaponType.MELEE
                && quality.hasTrick()
                && trick == null) {

            throw new IllegalArgumentException(
                    "Melee weapon of quality 2+ must have a trick"
            );
        }

        this.id = id;
        this.name = name;
        this.quality = quality;
        this.type = type;
        this.attack = attack;
        this.primaryAttack = primaryAttack;
        this.alternateAttack = alternateAttack;
        this.chargedFireAttack = chargedFireAttack;
        this.chargedElectricAttack = chargedElectricAttack;
        this.special = special == null ? WeaponSpecial.NONE : special;
        this.trick = trick;
        this.passiveEffect = passiveEffect;
    }

    /*
     * =====================================================
     * BASIC DATA
     * =====================================================
     */

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ItemQuality getQuality() {
        return quality;
    }

    public WeaponType getType() {
        return type;
    }

    public float getAttack() {
        return attack;
    }

    /*
     * =====================================================
     * ATTACK
     * =====================================================
     */

    public AttackDefinition getPrimaryAttack() {
        return primaryAttack;
    }

    public AttackStyle getAttackStyle() {
        return primaryAttack.getAttackStyle();
    }

    public AttackDefinition getAlternateAttack() {
        return alternateAttack;
    }

    public AttackDefinition getChargedFireAttack() {
        return chargedFireAttack;
    }

    public AttackDefinition getChargedElectricAttack() {
        return chargedElectricAttack;
    }

    public WeaponSpecial getSpecial() {
        return special;
    }

    public float getAttackSpeed() {
        return primaryAttack.getAttackSpeed();
    }

    public double getAttackInterval() {
        return primaryAttack.getAttackInterval();
    }

    /*
     * =====================================================
     * SPECIAL
     * =====================================================
     */

    public AbilityDefinition getTrick() {
        return trick;
    }

    public AbilityDefinition getPassiveEffect() {
        return passiveEffect;
    }

    public boolean hasTrick() {
        return trick != null;
    }

    public boolean hasPassiveEffect() {
        return passiveEffect != null;
    }

    /*
     * =====================================================
     * RANGED COMPATIBILITY
     * =====================================================
     */

    public float getProjectileSpeed() {

        if (type != WeaponType.RANGED) {
            return 0f;
        }

        return primaryAttack
                .getProjectileSpeed();
    }

    public float getProjectileRange() {

        if (type != WeaponType.RANGED) {
            return 0f;
        }

        return primaryAttack
                .getProjectileRange();
    }

    public boolean isRanged() {
        return type == WeaponType.RANGED;
    }
}