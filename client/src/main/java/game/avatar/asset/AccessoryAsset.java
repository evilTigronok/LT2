package game.avatar.asset;

public class AccessoryAsset implements AvatarAsset {

    public enum Type {
        EARRING,
        NECKLACE,
        GLASSES,
        HAT,
        MASK,
        OTHER
    }

    private final String id;
    private final String name;
    private final String spritePath;
    private final Type type;

    public AccessoryAsset(
            String id,
            String name,
            String spritePath,
            Type type
    ) {
        this.id = id;
        this.name = name;
        this.spritePath = spritePath;
        this.type = type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getSpritePath() {
        return spritePath;
    }

    public Type getType() {
        return type;
    }
}