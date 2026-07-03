package game.world;

public class GameMap {

    private int size;

    public GameMap(int size) {
        this.size = size;
    }

    public boolean isValid(int x, int y) {
        return x >= 0 && y >= 0 && x < size && y < size;
    }
}