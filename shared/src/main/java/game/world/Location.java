package game.world;

import java.util.ArrayList;
import java.util.List;

public class Location {

    public String id;

    public String name;

    public String backgroundPath;

    public List<LocationObject> objects =
            new ArrayList<>();

    public Location(
            String id,
            String name,
            String backgroundPath
    ) {

        this.id = id;
        this.name = name;
        this.backgroundPath = backgroundPath;
    }
}