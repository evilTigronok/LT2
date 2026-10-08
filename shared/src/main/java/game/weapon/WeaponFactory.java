package game.weapon;

import game.combat.AttackDefinition;
import game.combat.DamagePart;
import game.combat.DamageType;
import game.item.AbilityDefinition;
import game.item.ItemQuality;

import java.util.List;

public final class WeaponFactory {

    private ProjectileOriginProvider originProvider;
    public void setOriginProvider(
            ProjectileOriginProvider originProvider
    ) {
        this.originProvider = originProvider;
    }

    private WeaponFactory() {
    }

    public static Weapon createWeightedClaymore() {

        AttackDefinition attack =
                new AttackDefinition(
                        List.of(
                                new DamagePart(
                                        DamageType.PHYSICAL,
                                        7878f, //временный стат вместо 11
                                        1.0f
                                )
                        ),
                        AttackStyle.SWING,
                        1,
                        0,
                        false,
                        2.0f,
                        0f,
                        0f
                );

        AbilityDefinition artifactSlotPassive =
                new AbilityDefinition(
                        "artifact_slot",
                        "Артефактный слот",
                        "Отнимает слот артефакта, если экипирован"
                );

        return new Weapon(
                "weighted_claymore",
                "Утяжелённый клеймор",
                ItemQuality.CLASSIC,
                WeaponType.MELEE,
                11f,
                attack,
                null,
                artifactSlotPassive
        );
    }

    public static Weapon createIronSword() {

        AttackDefinition attack =
                new AttackDefinition(
                        List.of(
                                new DamagePart(
                                        DamageType.PHYSICAL,
                                        3f,
                                        1.0f
                                )
                        ),
                        AttackStyle.SWING,
                        1,
                        0,
                        false,
                        1.0f,
                        0f,
                        0f
                );

        return new Weapon(
                "iron_sword",
                "Iron Sword",
                ItemQuality.WEAK,
                WeaponType.MELEE,
                0f,
                attack,
                null,
                null
        );
    }

    public static Weapon createAngelicDoom() {

        AttackDefinition attack =
                new AttackDefinition(
                        List.of(
                                new DamagePart(
                                        DamageType.PHYSICAL,
                                        10f,
                                        0.90f
                                )
                        ),
                        AttackStyle.SHOOT,
                        1,
                        7,
                        true,
                        0.4f,
                        700f,
                        1000f
                );

        return new Weapon(
                "angelic_doom",
                "Ангельская погибель",
                ItemQuality.DIVINE,
                WeaponType.RANGED,
                13f,
                attack,
                null,
                null
        );
    }

    public static Weapon createGungnir() {

        AttackDefinition attack =
                new AttackDefinition(
                        List.of(
                                new DamagePart(
                                        DamageType.PHYSICAL,
                                        10f,
                                        1.0f
                                )
                        ),
                        AttackStyle.THRUST,
                        1,
                        0,
                        false,
                        1.0f,
                        0f,
                        0f
                );
        AbilityDefinition trick =
                new AbilityDefinition(
                        "gungnir_drill",
                        "Буровой рывок",
                        "Совершить рывок-бур, нанося встречным целям 22% урона (3,5 тика в секунду)\n" +
                                "В этом состоянии все снаряды не наносят урон игроку\n" +
                                "Длительность - 4 сек\n" +
                                "Перезарядка - 20 сек"
                );

        return new Weapon(
                "gungnir",
                "Гунгнир",
                ItemQuality.MIGHTY,
                WeaponType.MELEE,
                20f,
                attack,
                trick,
                null
        );
    }
    public static Weapon createGungnirLauncher() {

        AttackDefinition attack =
                new AttackDefinition(
                        List.of(
                                new DamagePart(
                                        DamageType.PHYSICAL,
                                        0f,
                                        1.0f
                                )
                        ),
                        AttackStyle.SHOOT,
                        1,
                        1,
                        false,
                        1.0f,
                        700f,
                        1000f
                );

        return new Weapon(
                "gungnir_launcher",
                "Гунгниромёт",
                ItemQuality.MIGHTY,
                WeaponType.RANGED,
                0f,
                attack,
                null,
                null
        );
    }
    public static Weapon createReliableSpear() {

        AttackDefinition attack =
                new AttackDefinition(
                        List.of(
                                new DamagePart(
                                        DamageType.PHYSICAL,
                                        7f,
                                        1.0f
                                )
                        ),
                        AttackStyle.THRUST,
                        1,
                        0,
                        false,
                        1.0f,
                        0f,
                        0f
                );

        return new Weapon(
                "reliable_spear",
                "Надёжное копьё",
                ItemQuality.CLASSIC,
                WeaponType.MELEE,
                6f,
                attack,
                null,
                null
        );
    }


    /** Траур. */
    public static Weapon createMourning() {
        AttackDefinition attack = new AttackDefinition(
                List.of(new DamagePart(DamageType.WIND, 4f, 1.30f)),
                AttackStyle.SHOOT, 1, 1, false, 1.2f, 900f, 1200f
        );
        return new Weapon(
                "mourning", "Траур", ItemQuality.MIGHTY, WeaponType.RANGED, 7f,
                attack, null, null, null, WeaponSpecial.MOURNING, null, null
        );
    }

    /** Ледострел. 30 льдин и восстановление магазина реализуются клиентом. */
    public static Weapon createIceShooter() {
        AttackDefinition attack = new AttackDefinition(
                List.of(new DamagePart(DamageType.ICE, 10f, 0.40f)),
                AttackStyle.SHOOT, 1, 6, false, 0.5f, 1000f, 1200f
        );
        return new Weapon(
                "ice_shooter", "Ледострел", ItemQuality.RARE, WeaponType.RANGED, 7f,
                attack, null, null, null, WeaponSpecial.ICE_SHOOTER, null, null
        );
    }

    /** Роковой Звездострел: обычная и две элементные версии заряженной звезды. */
    public static Weapon createDoomStarShooter() {
        AttackDefinition normal = new AttackDefinition(
                List.of(new DamagePart(DamageType.PHYSICAL, 4f, 0.50f)),
                AttackStyle.SHOOT, 1, 1, false, 0.25f, 900f, 1400f
        );
        AttackDefinition fire = new AttackDefinition(
                List.of(new DamagePart(DamageType.FIRE, 40f, 7.00f)),
                AttackStyle.SHOOT, 1, 1, false, 0.25f, 650f, 1400f, null, 40f
        );
        AttackDefinition electric = new AttackDefinition(
                List.of(new DamagePart(DamageType.ELECTRIC, 40f, 7.00f)),
                AttackStyle.SHOOT, 1, 1, false, 0.25f, 650f, 1400f, null, 40f
        );
        return new Weapon(
                "doom_star_shooter", "Роковой Звездострел", ItemQuality.RARE, WeaponType.RANGED, 4f,
                normal, null, fire, electric, WeaponSpecial.DOOM_STAR_SHOOTER, null, null
        );
    }

    @FunctionalInterface
    public interface ProjectileOriginProvider {
        float[] getOrigin();
    }
}