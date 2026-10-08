package game.avatar;

public class PixelPose {

    private final PoseType type;
    private final Direction direction;
    private final int frame;

    public PixelPose(
            PoseType type,
            Direction direction,
            int frame
    ) {
        this.type = type;
        this.direction = direction;
        this.frame = frame;
    }

    public PoseType getType() {
        return type;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getFrame() {
        return frame;
    }

    public static PixelPose idle(Direction direction) {
        return new PixelPose(
                PoseType.IDLE,
                direction,
                0
        );
    }

    public static PixelPose walk(
            Direction direction,
            int frame
    ) {
        return new PixelPose(
                PoseType.WALK,
                direction,
                Math.floorMod(frame, 4)
        );
    }

    public static PixelPose dash(
            Direction direction,
            int frame
    ) {
        return new PixelPose(
                PoseType.DASH,
                direction,
                Math.floorMod(frame, 4)
        );
    }
}