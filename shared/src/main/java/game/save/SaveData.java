package game.save;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SaveData {

    public int version = 1;

    public int slot;

    public String saveName = "";

    public Difficulty difficulty =
            Difficulty.EXHAUSTING;

    public long createdAt;

    public long lastSavedAt;

    /*
     * =====================================================
     * PLAYER
     * =====================================================
     */

    public PlayerData player =
            new PlayerData();

    /*
     * =====================================================
     * WORLD
     * =====================================================
     */

    public WorldSaveData world =
            new WorldSaveData();

    /*
     * =====================================================
     * PLAYER PROGRESS
     * =====================================================
     */

    public Set<String> defeatedBosses =
            new HashSet<>();

    public Set<String> worldFlags =
            new HashSet<>();

    /*
     * =====================================================
     * WEAPONS
     * =====================================================
     */

    public List<String> weaponIds =
            new ArrayList<>();

    public String equippedMelee;

    public String equippedRanged;

    public boolean meleeActive = true;

    public SaveData() {
    }

    public static class PlayerData {

        public float x;
        public float y;

        public float hp = 100f;
        public float maxHp = 100f;

        public int locationX;
        public int locationY;
    }

    public static class WorldSaveData {

        public int width = 10000;
        public int height = 10000;

        public String biome = "Forest";

        public boolean rain;
        public boolean snow;
        public boolean fog;

        public String currentLocationId;

        /*
         * Объекты, которые были удалены/изменены
         * во время прохождения.
         */
        public Set<String> removedObjects =
                new HashSet<>();
    }
}