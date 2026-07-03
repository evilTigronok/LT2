package game.world;

public class LocationObject {

    public String id;

    public String name;

    public LocationObjectType type;

    public LocationObject(
            String id,
            String name,
            LocationObjectType type
    ) {

        this.id = id;
        this.name = name;
        this.type = type;
    }
}