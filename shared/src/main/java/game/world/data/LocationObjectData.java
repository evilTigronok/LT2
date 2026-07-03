package game.world.data;

public class LocationObjectData {

    private String type;

    private float x;
    private float y;

    private float width;
    private float height;

    public LocationObjectData() {
    }

    public LocationObjectData(
            String type,
            float x,
            float y,
            float width,
            float height
    ) {

        this.type = type;

        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}