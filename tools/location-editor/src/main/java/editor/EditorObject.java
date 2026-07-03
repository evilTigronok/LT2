package editor;

import game.world.objects.ObjectDefinition;

public class EditorObject {

    public ObjectDefinition definition;

    public float x;
    public float y;

    public EditorObject(
            ObjectDefinition definition,
            float x,
            float y
    ) {

        this.definition =
                definition;

        this.x = x;
        this.y = y;
    }

    public boolean contains(
            double mx,
            double my
    ) {

        return mx >= x
                && mx <= x + definition.getWidth()
                && my >= y
                && my <= y + definition.getHeight();
    }
}