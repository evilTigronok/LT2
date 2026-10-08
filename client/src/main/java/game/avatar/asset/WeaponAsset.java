package game.avatar.asset;

import java.awt.image.BufferedImage;

public class WeaponAsset implements AvatarAsset {

    public enum Layer {
        BEHIND_CHARACTER,
        IN_FRONT_OF_CHARACTER
    }

    private final String id;
    private final String name;

    private final BufferedImage image;

    private final double anchorX;
    private final double anchorY;

    private final double rotation;

    private final Layer layer;

    public WeaponAsset(
            String id,
            String name,
            BufferedImage image,
            double anchorX,
            double anchorY,
            double rotation,
            Layer layer
    ) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.rotation = rotation;
        this.layer = layer;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public BufferedImage getImage() {
        return image;
    }

    public double getAnchorX() {
        return anchorX;
    }

    public double getAnchorY() {
        return anchorY;
    }

    public double getRotation() {
        return rotation;
    }

    public Layer getLayer() {
        return layer;
    }
}