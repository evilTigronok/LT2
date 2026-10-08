package game.boss.ancient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AncientAngelRotatingStream {

    /*
     * Как долго Ангел испускает поток.
     */
    private static final float LIFETIME = 400.0f;

    /*
     * Интервал между выстрелами.
     */
    private static final float SPAWN_INTERVAL = 0.1f;

    /*
     * Скорость вращения направления потока.
     */
    private static final float ROTATION_SPEED =
            (float) Math.toRadians(30.0);

    /*
     * Скорость синих снарядов.
     */
    private static final float PROJECTILE_SPEED = 420f;

    private static final float PROJECTILE_DAMAGE = 1f;

    /*
     * Максимальная дальность снаряда.
     */
    private static final float PROJECTILE_MAX_DISTANCE = 3000f;

    /*
     * Максимальный разброс относительно
     * текущего направления потока.
     *
     * 5 градусов — небольшой разброс.
     */
    private static final float SPREAD_ANGLE =
            (float) Math.toRadians(5.0);

    /*
     * Текущее направление, в котором будет
     * выпущен СЛЕДУЮЩИЙ снаряд.
     */
    private float angle;

    private float elapsed;
    private float spawnTimer;

    private boolean finished;

    private final List<AncientAngelStreamProjectile> projectiles =
            new ArrayList<>();

    public AncientAngelRotatingStream(
            float initialAngle
    ) {
        this.angle = initialAngle;

        this.elapsed = 0f;
        this.spawnTimer = 0f;

        this.finished = false;
    }

    public void update(
            float deltaSeconds,
            float currentOriginX,
            float currentOriginY
    ) {

        float delta = Math.max(0f, deltaSeconds);

        /*
         * Пока генерация активна —
         * вращаем направление и создаём новые снаряды.
         */
        if (!finished) {

            elapsed += delta;
            spawnTimer += delta;

            angle += ROTATION_SPEED * delta;

            while (spawnTimer >= SPAWN_INTERVAL) {

                spawnTimer -= SPAWN_INTERVAL;

                spawnProjectile(
                        currentOriginX,
                        currentOriginY
                );
            }

            if (elapsed >= LIFETIME) {

                elapsed = LIFETIME;
                finished = true;
            }
        }

        /*
         * Уже выпущенные снаряды продолжают лететь
         * даже после завершения генерации.
         */
        for (AncientAngelStreamProjectile projectile :
                projectiles) {

            projectile.update(delta);
        }

        projectiles.removeIf(
                AncientAngelStreamProjectile::isFinished
        );
    }

    private void spawnProjectile(
            float originX,
            float originY
    ) {

        /*
         * Небольшой случайный разброс
         * вокруг направления потока.
         *
         * Например:
         *
         * поток = 90°
         *
         * снаряд может получить:
         * 87°
         * 91°
         * 94°
         * 88°
         * и т.д.
         */
        float spread =
                (float) ThreadLocalRandom.current()
                        .nextDouble(
                                -SPREAD_ANGLE,
                                SPREAD_ANGLE
                        );

        float projectileAngle =
                angle + spread;

        float directionX =
                (float) Math.cos(projectileAngle);

        float directionY =
                (float) Math.sin(projectileAngle);

        AncientAngelStreamProjectile projectile =
                new AncientAngelStreamProjectile(
                        originX,
                        originY,
                        directionX,
                        directionY,
                        PROJECTILE_SPEED,
                        PROJECTILE_MAX_DISTANCE,
                        PROJECTILE_DAMAGE
                );

        projectiles.add(projectile);
    }

    public boolean isFinished() {
        return finished;
    }

    public float getAngle() {
        return angle;
    }

    public float getElapsed() {
        return elapsed;
    }

    public Collection<AncientAngelStreamProjectile>
    getProjectiles() {
        return projectiles;
    }
}