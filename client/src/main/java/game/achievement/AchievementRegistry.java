package game.achievement;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AchievementRegistry {

    private static final Map<String, Achievement> ACHIEVEMENTS =
            new LinkedHashMap<>();

    static {

        register(
                new Achievement(
                        "first_save",
                        "Начало пути",
                        "Создать первое сохранение."
                )
        );

        register(
                new Achievement(
                        "first_weapon",
                        "Вооружён",
                        "Получить первое оружие."
                )
        );

        register(
                new Achievement(
                        "first_boss",
                        "Палач",
                        "Победить первого босса."
                )
        );

        register(
                new Achievement(
                        "divine_start",
                        "Вызов богам",
                        "Начать прохождение на Божественной сложности."
                )
        );
    }

    private AchievementRegistry() {
    }

    private static void register(
            Achievement achievement
    ) {
        ACHIEVEMENTS.put(
                achievement.id(),
                achievement
        );
    }

    public static Achievement get(
            String id
    ) {
        return ACHIEVEMENTS.get(id);
    }

    public static Collection<Achievement> getAll() {
        return ACHIEVEMENTS.values();
    }
}