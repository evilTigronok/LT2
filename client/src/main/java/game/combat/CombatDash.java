package game.combat;

/**
 * Временное боевое перемещение персонажа.
 *
 * Класс не изменяет позицию игрока самостоятельно.
 * Он хранит состояние рывка, а WorldScene применяет
 * полученное смещение к позиции игрока.
 */
public class CombatDash {

    private final float directionX;
    private final float directionY;

    private final float speed;
    private final float maxDistance;
    private final float duration;

    private float elapsed;
    private float distance;

    private boolean finished;

    public CombatDash(
            float directionX,
            float directionY,
            float speed,
            float maxDistance,
            float duration
    ) {

        float length =
                (float) Math.sqrt(
                        directionX * directionX
                                + directionY * directionY
                );

        if (length < 0.0001f) {
            this.directionX = 0f;
            this.directionY = 1f;
        } else {
            this.directionX =
                    directionX / length;

            this.directionY =
                    directionY / length;
        }

        this.speed = Math.max(0f, speed);
        this.maxDistance = Math.max(0f, maxDistance);
        this.duration = Math.max(0.001f, duration);

        this.elapsed = 0f;
        this.distance = 0f;
        this.finished = false;
    }

    /**
     * Обновляет рывок и возвращает расстояние,
     * которое нужно пройти в этом кадре.
     */
    public float update(float deltaSeconds) {

        if (finished) {
            return 0f;
        }

        float delta =
                Math.max(0f, deltaSeconds);

        float remainingDistance =
                maxDistance - distance;

        float remainingTime =
                duration - elapsed;

        if (remainingDistance <= 0f
                || remainingTime <= 0f) {

            finish();

            return 0f;
        }

        float movement =
                Math.min(
                        speed * delta,
                        remainingDistance
                );

        distance += movement;
        elapsed += delta;

        if (distance >= maxDistance
                || elapsed >= duration) {

            finish();
        }

        return movement;
    }

    private void finish() {
        distance = maxDistance;
        elapsed = duration;
        finished = true;
    }

    public float getDirectionX() {
        return directionX;
    }

    public float getDirectionY() {
        return directionY;
    }

    public float getDistance() {
        return distance;
    }

    public float getProgress() {

        if (maxDistance <= 0f) {
            return 1f;
        }

        return Math.min(
                1f,
                distance / maxDistance
        );
    }

    public boolean isFinished() {
        return finished;
    }
}