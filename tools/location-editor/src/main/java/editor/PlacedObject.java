package editor;

import game.world.objects.ObjectDefinition;
import javafx.scene.image.ImageView;

public class PlacedObject {

    private final ObjectDefinition definition;

    private final ImageView view;

    private final double x;

    private final double y;

    public PlacedObject(
            ObjectDefinition definition,
            ImageView view,
            double x,
            double y
    ) {

        this.definition = definition;
        this.view = view;
        this.x = x;
        this.y = y;
    }

    public ObjectDefinition getDefinition() {
        return definition;
    }

    public ImageView getView() {
        return view;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}