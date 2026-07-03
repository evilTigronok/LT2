package game.world.data;

import java.util.ArrayList;
import java.util.List;

public class LocationData {

    public String id;

    public String biome;

    public boolean rain;

    public boolean snow;

    public boolean fog;

    public int worldX;

    public int worldY;

    public List<PlacedObjectData> objects =
            new ArrayList<>();
}