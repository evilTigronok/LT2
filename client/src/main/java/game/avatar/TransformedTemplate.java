package game.avatar;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class TransformedTemplate {

    private final EnumMap<PixelPart, TransformedPart> parts =
            new EnumMap<>(PixelPart.class);

    private final HashMap<String, PixelPoint> anchors =
            new HashMap<>();

    /*
     * Информация о текущей ориентации персонажа.
     *
     * Она хранится здесь, чтобы AvatarAssetComposer
     * не был вынужден повторно вычислять направление.
     */
    private Direction direction;
    private BodyOrientation orientation;

    public TransformedTemplate() {
    }

    public void putPart(
            PixelPart part,
            TransformedPart transformedPart
    ) {
        if (part == null || transformedPart == null) {
            return;
        }

        parts.put(part, transformedPart);
    }

    public void putPart(
            PixelPart part,
            PixelRect bounds,
            boolean mirrored,
            int layer
    ) {
        if (part == null || bounds == null) {
            return;
        }

        parts.put(
                part,
                new TransformedPart(
                        part,
                        bounds,
                        mirrored,
                        layer
                )
        );
    }

    public void putAnchor(
            String name,
            PixelPoint point
    ) {
        if (name == null || point == null) {
            return;
        }

        anchors.put(name, point);
    }

    public TransformedPart getPart(
            PixelPart part
    ) {
        return parts.get(part);
    }

    public PixelPoint getAnchor(
            String name
    ) {
        return anchors.get(name);
    }

    public Map<PixelPart, TransformedPart> getParts() {
        return Map.copyOf(parts);
    }

    public Map<String, PixelPoint> getAnchors() {
        return Map.copyOf(anchors);
    }

    public void removePart(
            PixelPart part
    ) {
        parts.remove(part);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(
            Direction direction
    ) {
        this.direction = direction;
    }

    public BodyOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(
            BodyOrientation orientation
    ) {
        this.orientation = orientation;
    }
}