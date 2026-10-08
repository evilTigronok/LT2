package game.item;

public enum ItemQuality {

    WEAK(
            0,
            "Слабый"
    ),

    CLASSIC(
            1,
            "Классический"
    ),

    ELITE(
            2,
            "Элитный"
    ),

    RARE(
            3,
            "Редкий"
    ),

    MIGHTY(
            4,
            "Могущественный"
    ),

    DIVINE(
            5,
            "Божественный"
    );

    private final int level;
    private final String displayName;

    ItemQuality(
            int level,
            String displayName
    ) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean hasTrick() {
        return level >= 2;
    }
}