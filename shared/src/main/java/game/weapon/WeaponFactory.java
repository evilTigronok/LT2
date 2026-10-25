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


    /** Клинок из Материального проклятия. */
    public static Weapon createMaterialCurseBlade() {
        AttackDefinition attack = new AttackDefinition(
                List.of(new DamagePart(DamageType.PHYSICAL, 5f, 0.25f)),
                AttackStyle.QUICK_SWING, 1, 0, false, 4f, 0f, 0f
        );

        AbilityDefinition passive = new AbilityDefinition(
                "material_curse_artifact_lock",
                "Проклятие артефактов",
                "Занимает все четыре слота артефактов и заменяет их "
                        + "неудаляемыми копиями «Проклятой имитации души»."
        );

        return new Weapon(
                "material_curse_blade",
                "Клинок из Материального проклятия",
                ItemQuality.MIGHTY,
                WeaponType.MELEE,
                40f,
                attack,
                new AbilityDefinition(
                        "material_curse_blade_trick",
                        "Трюк",
                        "Способность ещё не определена."
                ),
                passive
        );
    }

    /** Деревянный меч. */
    public static Weapon createWoodenSword() {
        AttackDefinition attack = new AttackDefinition(
                List.of(new DamagePart(DamageType.PHYSICAL, 3f, 1.0f)),
                AttackStyle.QUICK_SWING, 1, 0, false, 1f, 0f, 0f
        );

        return new Weapon(
                "wooden_sword",
                "Деревянный меч",
                ItemQuality.WEAK,
                WeaponType.MELEE,
                4f,
                attack,
                null,
                null
        );
    }

    /** Магматический клеймор. */
    public static Weapon createMagmaticClaymore() {
        AttackDefinition attack = new AttackDefinition(
                List.of(new DamagePart(DamageType.FIRE, 8f, 2.0f)),
                AttackStyle.SWING, 1, 0, false, 0.25f, 0f, 0f
        );

        AbilityDefinition trick = new AbilityDefinition(
                "magmatic_claymore_field",
                "Магматическое поле",
                "На 7 секунд создаёт поле: все существа получают "
                        + "7 + 70% огненного урона в секунду. "
                        + "Владелец получает на 90% меньше урона. "
                        + "Перезарядка: 17 секунд."
        );

        AbilityDefinition passive = new AbilityDefinition(
                "magmatic_claymore_passive",
                "Магматическое пламя",
                "Скорость передвижения −25%. Огненный урон +25%. "
                        + "Попадания ближними неогненными стихийными "
                        + "умениями дополнительно наносят 30% урона умения огнём."
        );

        return new Weapon(
                "magmatic_claymore",
                "Магматический клеймор",
                ItemQuality.RARE,
                WeaponType.MELEE,
                22f,
                attack,
                trick,
                passive
        );
    }

    /** Рассвет. */
    public static Weapon createDawn() {
        AttackDefinition attack = new AttackDefinition(
                List.of(new DamagePart(DamageType.FIRE, 5f, 1.0f)),
                AttackStyle.SWING, 1, 0, false, 1f, 0f, 0f
        );

        AbilityDefinition trick = new AbilityDefinition(
                "dawn_finisher",
                "Финальный удар",
                "Немедленно выполняет финальную атаку серии. "
                        + "Перезарядка: 9 секунд."
        );

        return new Weapon(
                "dawn",
                "Рассвет",
                ItemQuality.MIGHTY,
                WeaponType.MELEE,
                20f,
                attack,
                trick,
                null
        );
    }

    /** Зенит. */
    public static Weapon createZenith() {
        AttackDefinition attack = new AttackDefinition(
                List.of(new DamagePart(DamageType.PHYSICAL, 9f, 0.60f)),
                AttackStyle.SWING, 1, 0, false, 2f, 0f, 0f
        );

        AbilityDefinition trick = new AbilityDefinition(
                "zenith_overdrive",
                "Перегрузка",
                "На 6 секунд увеличивает размер атак и скорость атак на 50%; "
                        + "атаки дополнительно наносят 6% чистого урона. "
                        + "Перезарядка: 18 секунд."
        );

        AbilityDefinition passive = new AbilityDefinition(
                "zenith_spin",
                "Вращение",
                "При удержании ЛКМ меч вращается и постепенно увеличивает "
                        + "скорость владельца. При бонусе скорости +100% "
                        + "владелец получает на 33% меньше урона. "
                        + "ПКМ во время вращения парирует атаку; "
                        + "перезарядка парирования — 7 секунд."
        );

        return new Weapon(
                "zenith",
                "Зенит",
                ItemQuality.MIGHTY,
                WeaponType.MELEE,
                20f,
                attack,
                trick,
                passive
        );
    }

    /** Сумерки. */
    public static Weapon createTwilight() {
        AttackDefinition attack = new AttackDefinition(
                List.of(new DamagePart(DamageType.PHYSICAL, 17f, 1.0f)),
                AttackStyle.QUICK_SWING, 1, 0, false, 0.5f, 0f, 0f
        );

        AbilityDefinition trick = new AbilityDefinition(
                "twilight_trick",
                "Трюк",
                "Способность ещё не определена."
        );

        AbilityDefinition passive = new AbilityDefinition(
                "twilight_rush",
                "Сумеречный рывок",
                "Удержание ЛКМ наносит урон аурой. Отпускание ЛКМ "
                        + "выполняет круговой удар с рывком. Попавшая цель "
                        + "на 3 секунды теряет 40% скорости передвижения "
                        + "и 33% скорости атак; затем получает 10 урона "
                        + "каждые 0,25 секунды в течение 8 секунд. "
                        + "Повторное наложение обновляет длительность."
        );

        return new Weapon(
                "twilight",
                "Сумерки",
                ItemQuality.MIGHTY,
                WeaponType.MELEE,
                17f,
                attack,
                trick,
                passive
        );
    }

    /** Завтрашний день. */
    public static Weapon createTomorrow() {
        AttackDefinition attack = new AttackDefinition(
                List.of(new DamagePart(DamageType.PHYSICAL, 100f, 1.0f)),
                AttackStyle.SWING, 1, 0, false, 0.25f, 0f, 0f
        );

        AbilityDefinition trick = new AbilityDefinition(
                "tomorrow_trick",
                "Трюк",
                "Способность ещё не определена."
        );

        AbilityDefinition passive = new AbilityDefinition(
                "tomorrow_aura",
                "Аура Завтрашнего дня",
                "Пока оружие находится в руках, получаемый урон "
                        + "снижен на 33%, скорость передвижения снижена на 25%."
        );

        return new Weapon(
                "tomorrow",
                "Завтрашний день",
                ItemQuality.DIVINE,
                WeaponType.MELEE,
                57f,
                attack,
                trick,
                passive
        );
    }


    @FunctionalInterface
    public interface ProjectileOriginProvider {
        float[] getOrigin();
    }
}