package game.avatar;

public class CharacterAccessory {

    public enum Type {
        EARRING,
        NECKLACE,
        GLASSES,
        HAT,
        MASK,
        OTHER
    }

    private final Type type;

    public CharacterAccessory(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}