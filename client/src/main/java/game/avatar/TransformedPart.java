package game.avatar;

public record TransformedPart(
        PixelPart part,
        PixelRect bounds,
        boolean mirrored,
        int layer
) {
}