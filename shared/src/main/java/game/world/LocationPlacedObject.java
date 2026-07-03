package game.world;

public class LocationPlacedObject {

    public String objectId;

    public int x;

    public int y;

    public LocationPlacedObject() {

    }

    public LocationPlacedObject(
            String objectId,
            int x,
            int y
    ) {

        this.objectId = objectId;
        this.x = x;
        this.y = y;
    }
}