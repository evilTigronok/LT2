package game.avatar;

public record PixelPoint(double x, double y) {

    public PixelPoint offset(double dx, double dy) {
        return new PixelPoint(
                x + dx,
                y + dy
        );
    }

    public int roundX() {
        return (int) Math.round(x);
    }

    public int roundY() {
        return (int) Math.round(y);
    }
}