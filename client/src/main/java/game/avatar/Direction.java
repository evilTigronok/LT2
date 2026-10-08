package game.avatar;

public enum Direction {

    DOWN,
    DOWN_RIGHT,
    RIGHT,
    UP_RIGHT,
    UP,
    UP_LEFT,
    LEFT,
    DOWN_LEFT;

    public boolean isDiagonal() {
        return switch (this) {
            case DOWN_RIGHT, UP_RIGHT, UP_LEFT, DOWN_LEFT -> true;
            default -> false;
        };
    }

    public Direction opposite() {
        return switch (this) {
            case DOWN -> UP;
            case DOWN_RIGHT -> UP_LEFT;
            case RIGHT -> LEFT;
            case UP_RIGHT -> DOWN_LEFT;
            case UP -> DOWN;
            case UP_LEFT -> DOWN_RIGHT;
            case LEFT -> RIGHT;
            case DOWN_LEFT -> UP_RIGHT;
        };
    }

    /**
     * Базовая ориентация тела.
     *
     * DOWN + нижние диагонали:
     * персонаж смотрит на игрока.
     *
     * LEFT / RIGHT:
     * профиль.
     *
     * UP + верхние диагонали:
     * персонаж спиной к игроку.
     */
    public BodyOrientation getBodyOrientation() {
        return switch (this) {
            case DOWN, DOWN_RIGHT, DOWN_LEFT ->
                    BodyOrientation.FRONT;

            case RIGHT, LEFT ->
                    BodyOrientation.SIDE;

            case UP, UP_RIGHT, UP_LEFT ->
                    BodyOrientation.BACK;
        };
    }

    /**
     * Горизонтальное направление.
     */
    public int horizontal() {
        return switch (this) {
            case RIGHT, DOWN_RIGHT, UP_RIGHT -> 1;
            case LEFT, DOWN_LEFT, UP_LEFT -> -1;
            default -> 0;
        };
    }

    /**
     * Вертикальное направление.
     */
    public int vertical() {
        return switch (this) {
            case DOWN, DOWN_RIGHT, DOWN_LEFT -> 1;
            case UP, UP_RIGHT, UP_LEFT -> -1;
            default -> 0;
        };
    }
}