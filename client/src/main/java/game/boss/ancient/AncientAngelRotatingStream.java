package game.boss.ancient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AncientAngelRotatingStream {

    private static final float DEFAULT_LIFETIME = 400.0f;
    private static final float DEFAULT_SPAWN_INTERVAL = 0.1f;
    private static final float DEFAULT_ROTATION_SPEED =
            (float) Math.toRadians(30.0);
    private static final float DEFAULT_PROJECTILE_SPEED = 420f;
    private static final float DEFAULT_PROJECTILE_DAMAGE = 1f;
    private static final float DEFAULT_PROJECTILE_MAX_DISTANCE = 3000f;
    private static final float DEFAULT_SPREAD_ANGLE =
            (float) Math.toRadians(5.0);

    private final float lifetime;
    private final float spawnInterval;
    private final float rotationSpeed;
    private final float projectileSpeed;
    private final float projectileDamage;
    private final float projectileMaxDistance;
    private final float spreadAngle;

    private float angle;
    private float elapsed;
    private float spawnTimer;
    private boolean finished;

    private final List<AncientAngelStreamProjectile> projectiles =
            new ArrayList<>();

    public AncientAngelRotatingStream(float initialAngle) {
        this(
                initialAngle,
                DEFAULT_LIFETIME,
                DEFAULT_SPAWN_INTERVAL,
                DEFAULT_ROTATION_SPEED,
                DEFAULT_PROJECTILE_SPEED,
                DEFAULT_PROJECTILE_DAMAGE,
                DEFAULT_PROJECTILE_MAX_DISTANCE,
                DEFAULT_SPREAD_ANGLE
        );
    }

    public AncientAngelRotatingStream(
            float initialAngle,
            float lifetime,
            float spawnInterval,
            float rotationSpeed,
            float projectileSpeed,
            float projectileDamage,
            float projectileMaxDistance,
            float spreadAngle
    ) {
        this.angle = initialAngle;
        this.lifetime = Math.max(0.01f, lifetime);
        this.spawnInterval = Math.max(0.01f, spawnInterval);
        this.rotationSpeed = rotationSpeed;
        this.projectileSpeed = projectileSpeed;
        this.projectileDamage = projectileDamage;
        this.projectileMaxDistance = projectileMaxDistance;
        this.spreadAngle = Math.max(0f, spreadAngle);
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

            angle += rotationSpeed * delta;

            while (spawnTimer >= spawnInterval) {
                spawnTimer -= spawnInterval;

                spawnProjectile(
                        currentOriginX,
                        currentOriginY
                );
            }

            if (elapsed >= lifetime) {
                elapsed = lifetime;
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
                        .nextDouble(-spreadAngle, spreadAngle);

        float projectileAngle = angle + spread;

        float directionX = (float) Math.cos(projectileAngle);
        float directionY = (float) Math.sin(projectileAngle);

        AncientAngelStreamProjectile projectile =
                new AncientAngelStreamProjectile(
                        originX,
                        originY,
                        directionX,
                        directionY,
                        projectileSpeed,
                        projectileMaxDistance,
                        projectileDamage
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