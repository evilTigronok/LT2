package game.world.location;

public class Portal {

    private final String id;
    private final String targetLocationId;

    private final double x;
    private final double y;

    private final double width;
    private final double height;

    private final double targetSpawnX;
    private final double targetSpawnY;

    private final String interactionText;

    public Portal(
            String id,
            String targetLocationId,
            double x,
            double y,
            double width,
            double height,
            double targetSpawnX,
            double targetSpawnY,
            String interactionText
    ) {
        this.id = id;
        this.targetLocationId = targetLocationId;

        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;

        this.targetSpawnX = targetSpawnX;
        this.targetSpawnY = targetSpawnY;

        this.interactionText = interactionText;
    }

    public String getId() {
        return id;
    }

    public String getTargetLocationId() {
        return targetLocationId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getTargetSpawnX() {
        return targetSpawnX;
    }

    public double getTargetSpawnY() {
        return targetSpawnY;
    }

    public String getInteractionText() {
        return interactionText;
    }

    public double getCenterX() {
        return x + width / 2.0;
    }

    public double getCenterY() {
        return y + height / 2.0;
    }
}