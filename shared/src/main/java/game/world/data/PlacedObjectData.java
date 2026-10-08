package game.world.data;

public class PlacedObjectData {

    /**
     * ID объекта из ObjectRegistry.
     */
    public String objectId;

    /**
     * Мировая координата X.
     */
    public int x;

    /**
     * Мировая координата Y.
     */
    public int y;

    public PlacedObjectData() {
    }

    public PlacedObjectData(
            String objectId,
            int x,
            int y
    ) {
        this.objectId = objectId;
        this.x = x;
        this.y = y;
    }
}