package game.avatar.asset;

import game.avatar.BodyOrientation;

import java.util.EnumMap;
import java.util.Map;

public class HairAsset implements AvatarAsset {

    private final String id;
    private final String name;
    private final String spritePath;

    private final Map<BodyOrientation, String> orientationSprites =
            new EnumMap<>(BodyOrientation.class);

    public HairAsset(
            String id,
            String name,
            String spritePath
    ) {
        this.id = id;
        this.name = name;
        this.spritePath = spritePath;

        orientationSprites.put(
                BodyOrientation.FRONT,
                spritePath
        );
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

    public void setOrientationSprite(
            BodyOrientation orientation,
            String path
    ) {
        if (orientation == null || path == null) {
            return;
        }

        orientationSprites.put(
                orientation,
                path
        );
    }

    public String getSpritePath(
            BodyOrientation orientation
    ) {
        if (orientation == null) {
            return spritePath;
        }

        return orientationSprites.getOrDefault(
                orientation,
                spritePath
        );
    }
}