package game.avatar;

public record PixelRect(
        double x,
        double y,
        double width,
        double height
) {

    public PixelPoint center() {
        return new PixelPoint(
                x + width / 2.0,
                y + height / 2.0
        );
    }

    public PixelRect offset(double dx, double dy) {
        return new PixelRect(
                x + dx,
                y + dy,
                width,
                height
        );
    }
}