package game.world.data;

import java.util.ArrayList;
import java.util.List;

public class WorldData {

    /**
     * Версия формата мира.
     * Нужна для будущих миграций world.json.
     */
    public int version = 1;

    /**
     * Размер всего открытого мира.
     */
    public int width = 10000;
    public int height = 10000;

    /**
     * Основной биом мира.
     */
    public String biome = "Forest";

    /**
     * Погодные параметры.
     */
    public boolean rain;
    public boolean snow;
    public boolean fog;

    /**
     * Все размещённые объекты мира.
     *
     * Координаты объектов являются мировыми.
     */
    public List<PlacedObjectData> objects =
            new ArrayList<>();

    public WorldData() {
    }

    public WorldData(int width, int height) {
        this.width = width;
        this.height = height;
    }
}