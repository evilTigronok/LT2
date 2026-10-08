package game.world.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Location {

    private final String id;
    private final String name;

    private final double width;
    private final double height;

    private final String backgroundColor;

    private final List<Portal> portals =
            new ArrayList<>();

    public Location(
            String id,
            String name,
            double width,
            double height,
            String backgroundColor
    ) {
        this.id = id;
        this.name = name;

        this.width = width;
        this.height = height;

        this.backgroundColor = backgroundColor;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void addPortal(Portal portal) {
        if (portal != null) {
            portals.add(portal);
        }
    }

    public List<Portal> getPortals() {
        return Collections.unmodifiableList(portals);
    }
}