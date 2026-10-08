package game.world;

public class GameMap {

    public static final int DEFAULT_WIDTH = 10000;
    public static final int DEFAULT_HEIGHT = 10000;

    private final int width;
    private final int height;

    public GameMap() {
        this(
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT
        );
    }

    public GameMap(
            int width,
            int height
    ) {

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "World dimensions must be positive"
            );
        }

        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Проверяет, находится ли точка внутри мира.
     */
    public boolean isValid(
            float x,
            float y
    ) {

        return x >= 0
                && y >= 0
                && x < width
                && y < height;
    }

    /**
     * Ограничивает X границами мира.
     */
    public float clampX(float x) {

        return Math.max(
                0,
                Math.min(
                        width - 1,
                        x
                )
        );
    }

    /**
     * Ограничивает Y границами мира.
     */
    public float clampY(float y) {

        return Math.max(
                0,
                Math.min(
                        height - 1,
                        y
                )
        );
    }
}