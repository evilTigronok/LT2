package game.combat;

public enum DamageType {

    PHYSICAL("Физический"),
    FIRE("Огонь"),
    WIND("Ветер"),
    DUST("Пыль"),
    ICE("Лёд"),
    ELECTRIC("Электричество");

    private final String displayName;

    DamageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}