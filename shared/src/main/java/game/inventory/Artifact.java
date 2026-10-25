package game.inventory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Каталог артефактов. Состояние использования (например, блок снаряда)
 * хранится в игровом экземпляре/боевой системе, а не в определении предмета.
 */
public enum Artifact {
    WOODEN_BUCKLER(
            "wooden_buckler", "Деревянный щиток", 0, "Слабый",
            2, 0, -0f,
            "Первое получение урона от атаки за бой снижает его на 5. Один раз за бой блокирует снаряд."
    ),
    LUCKY_CLOVER(
            "lucky_clover", "Клевер удачи", 3, "Редкий",
            0, 0, 0f,
            "Неблагоприятные вероятности −18 процентных пунктов; благоприятные вероятности и критический шанс +15 п.п."
    ),
    SHARP_MOTIVATIONAL_AMULET(
            "sharp_motivational_amulet", "Мотивационный амулет с острым наконечником", 1, "Классический",
            0, 6, -2f,
            "Внешний аффикс урона +6 (baseDamage), АТК −2."
    ),
    RELIABLE_SHIELD(
            "reliable_shield", "Надёжный щит", 1, "Классический",
            5, 0, 0f,
            "5 защиты. Нажмите E, чтобы парировать обычную атаку или снаряд. Перезарядка 10 секунд; мощные атаки не парируются."
    ),
    WORN_ETHEREAL_DOLL(
            "worn_ethereal_doll", "Ветхая Эфирная кукла", 3, "Редкий",
            0, 0, 0f,
            "Максимальное ХП −10. При успешном попадании с шансом 10% "
                    + "создаёт фантом на 6 секунд. Фантом повторяет атаку "
                    + "и наносит 50% итогового урона. Перезарядка 20 секунд. "
                    + "Требует восстановления."
    ),
    WORN_BLOOD_CHALICE(
            "worn_blood_chalice", "Ветхий Кубок крови", 2, "Элитный",
            0, 0, 0f,
            "Максимальное ХП +10. Требует восстановления."
    ),
    WORN_LIGHTNING_BRACELET(
            "worn_lightning_bracelet", "Ветхий Браслет молнии", 3, "Редкий",
            0, 0, 0f,
            "Критический шанс +30 процентных пунктов. Конвертирует "
                    + "критический шанс в шанс уклонения по соотношению 2:1."
    ),
    TIN_WALL(
            "tin_wall", "Оловянная стенка", 1, "Классический",
            0, 0, 0f,
            "Скорость −40%. Защитная дуга за игроком длиной в 1/5 окружности. "
                    + "Блокирует любые снаряды и атаки. Дугу можно вращать "
                    + "круговым тараном: 10 + 25% физического урона."
    ),
    COLORED_METAL_STAR(
            "colored_metal_star", "Цветная металлическая звезда", 3, "Редкий",
            0, 5, 0f,
            "Внешний аффикс урона ×2 и дополнительно +5 внешнего урона."
    ),
    DRIED_HEART(
            "dried_heart", "Засохшее сердце", 1, "Классический",
            0, 0, 0f,
            "При первом получении урона за бой повышает максимальное ХП на 7 "
                    + "и регенерацию на 1 до конца боя."
    ),
    ANCIENTS_SHIELD(
            "ancients_shield", "Щит Древних", 3, "Редкий",
            14, 0, 0f,
            "14 защиты, получаемый урон −8%, скорость −15%, скорость атаки −12%. "
                    + "Можно разместить щит в выбранной точке; повторное нажатие перемещает его. "
                    + "Сопротивляемость эффектам +15%."
    ),
    MATERIAL_CURSE_SHIELD(
            "material_curse_shield", "Щит из материального проклятия", 4, "Могущественный",
            0, 0, 0f,
            "Получаемый урон +15%. Периодически появляются проклятые снаряды. "
                    + "Space парирует атаки с перезарядкой 0,25 сек. Парирование обычной атаки "
                    + "выпускает неотражаемый мстительный спектр; парирование проклятого снаряда "
                    + "даёт случайный бафф до 4 стаков на 6 секунд."
    ),
    CYBER_BOOTS(
            "cyber_boots", "Киберсапоги", 2, "Элитный",
            0, 0, 0f,
            "Рывок двойным нажатием WASD в выбранном направлении; перезарядка 1,5 сек. "
                    + "Рывок не проходит сквозь цели и опасные объекты."
    ),
    CONTINUUM_SPEAR(
            "continuum_spear", "Копьё Континуума", 3, "Редкий",
            0, 0, 0f,
            "Рывок вперёд с перезарядкой 5 сек. Во время рывка прошивает опасные объекты "
                    + "и избегает урона; прошивает цели, нанося 20 + 100% физического урона."
    ),
    AMMO_MODIFIER(
            "ammo_modifier", "Модификатор для патронов", 4, "Могущественный",
            0, 0, 0f,
            "Все снаряды дальнего боя самонаводятся на цели, но урон дальнего боя −15%."
    ),
    MECHANICAL_SQUIRE(
            "mechanical_squire", "Механический оруженосец", 5, "Божественный",
            0, 0, 0f,
            "Выставляет всё оружие ближнего боя из инвентаря вокруг персонажа. "
                    + "Оружия вращаются и наносят урон каждым тиком; щёлкните по оружию, "
                    + "чтобы заменить действующее оружие прямо в бою."
    ),
    UNSTABLE_AUTOMATION_MECHANISM(
            "unstable_automation_mechanism", "Нестабильный механизм автоматизации", 3, "Редкий",
            0, 0, 0f,
            "Удваивает скорость атаки дальнего боя. Создаёт разброс или увеличивает уже имеющийся."
    ),
    WILD_HEMOKINESIS(
            "wild_hemokinesis", "Дикий гемокинез", 3, "Редкий",
            0, 0, 0f,
            "При каждом получении урона выпускает 3–4 снаряда, наносящих 22% урона. "
                    + "Носитель получает на 2 единицы урона больше от любых атак."
    ),
    CURSED_SOUL_IMITATION(
            "cursed_soul_imitation",
            "Проклятая имитация души",
            4,
            "Могущественный",
            0,
            0,
            0.10f,
            "АТК +10%, защита −10%. Эффекты нескольких копий складываются. "
                    + "При использовании Клинка из Материального проклятия "
                    + "занимает все слоты артефактов и не может быть снята."
    );

    private final String id;
    private final String displayName;
    private final int cost;
    private final String rarity;
    private final int defense;
    private final float baseDamageBonus;
    private final float attackBonus;
    private final String description;

    Artifact(String id, String displayName, int cost, String rarity,
             int defense, float baseDamageBonus, float attackBonus, String description) {
        this.id = id;
        this.displayName = displayName;
        this.cost = cost;
        this.rarity = rarity;
        this.defense = defense;
        this.baseDamageBonus = baseDamageBonus;
        this.attackBonus = attackBonus;
        this.description = description;
    }

    public int getMaxHpBonus() {
        return switch (this) {
            case WORN_ETHEREAL_DOLL -> -10;
            case WORN_BLOOD_CHALICE -> 10;
            default -> 0;
        };
    }

    public float getCriticalChanceBonus() {
        return this == WORN_LIGHTNING_BRACELET ? 0.30f : 0f;
    }

    public float getDodgeConversionRatio() {
        return this == WORN_LIGHTNING_BRACELET ? 0.5f : 0f;
    }

    public boolean requiresRestoration() {
        return this == WORN_ETHEREAL_DOLL || this == WORN_BLOOD_CHALICE;
    }

    /** Множитель внешнего аффикса урона. */
    public float getExternalDamageMultiplier() {
        return this == COLORED_METAL_STAR ? 2.0f : 1.0f;
    }

    /** Множитель скорости передвижения. */
    public float getMovementSpeedMultiplier() {
        return switch (this) {
            case TIN_WALL -> 0.60f;
            case ANCIENTS_SHIELD -> 0.85f;
            default -> 1.0f;
        };
    }

    public float getAttackSpeedMultiplier() {
        return this == ANCIENTS_SHIELD ? 0.88f : 1.0f;
    }

    /** Множитель получаемого урона до плоской защиты. */
    public float getIncomingDamageMultiplier() {
        return switch (this) {
            case ANCIENTS_SHIELD -> 0.92f;
            case MATERIAL_CURSE_SHIELD -> 1.15f;
            case WILD_HEMOKINESIS -> 1.0f;
            default -> 1.0f;
        };
    }

    public int getEffectResistancePercent() {
        return this == ANCIENTS_SHIELD ? 15 : 0;
    }

    public float getFlatIncomingDamagePenalty() {
        return this == WILD_HEMOKINESIS ? 2f : 0f;
    }

    public float getRangedDamageMultiplier() {
        return this == AMMO_MODIFIER ? 0.85f : 1.0f;
    }

    public float getRangedAttackSpeedMultiplier() {
        return this == UNSTABLE_AUTOMATION_MECHANISM ? 2.0f : 1.0f;
    }

    public float getDefensePercentBonus() {
        return this == CURSED_SOUL_IMITATION ? -0.10f : 0f;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getCost() { return cost; }
    public String getRarity() { return rarity; }
    public int getDefense() { return defense; }
    public float getBaseDamageBonus() { return baseDamageBonus; }
    /**
     * Плоский бонус атаки.
     * Например, мотивационный амулет даёт -2 АТК.
     */
    public float getAttackBonus() {
        return this == CURSED_SOUL_IMITATION ? 0f : attackBonus;
    }

    /**
     * Процентный бонус атаки.
     * Значения 0.10 и -0.10 соответствуют +10% и -10%.
     */
    public float getAttackPercentBonus() {
        return this == CURSED_SOUL_IMITATION ? 0.10f : 0f;
    }


    public String getDescription() { return description; }

    public static Artifact byId(String id) {
        if (id == null) return null;
        return Arrays.stream(values()).filter(a -> a.id.equals(id)).findFirst().orElse(null);
    }

    public static List<Artifact> all() {
        return Collections.unmodifiableList(Arrays.asList(values()));
    }
}
