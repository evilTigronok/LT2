
package game.boss.ancient;

import game.boss.Boss;
import game.save.Difficulty;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class AncientAngelBoss extends Boss {

    private static final float MOVEMENT_RADIUS = 280f;
    private static final float PLAYER_SIZE = 40f;

    private static final float STREAM_PROJECTILE_MAX_DISTANCE = 3000f;

    private static final String SPRITE_PATH =
            "/assets/mobs/ancient_angel.png";

    private final Image sprite = loadRequiredImage(SPRITE_PATH);

    private final float centerX;
    private final float centerY;

    private final Difficulty difficulty;
    private final DifficultyProfile profile;

    private int speedDebuff = 0;

    private float moveDirectionX;
    private float moveDirectionY;

    private double movementTimer;
    private double projectileTimer;
    private double streamTimer;

    private final List<AncientAngelProjectile> projectiles =
            new ArrayList<>();

    private final AncientAngelRotatingStreamSystem rotatingStreamSystem =
            new AncientAngelRotatingStreamSystem();

    private Consumer<Float> playerDamageListener;

    /**
     * Параметры поведения Ангела на выбранной сложности.
     */
    private static final class DifficultyProfile {

        private final double movementChangeInterval;
        private final float moveSpeed;

        private final double projectileInterval;
        private final float projectileDamage;

        private final double streamInterval;
        private final float streamLifetime;
        private final float streamSpawnInterval;
        private final float streamRotationSpeed;
        private final float streamProjectileSpeed;
        private final float streamProjectileDamage;
        private final float streamSpreadAngle;

        private DifficultyProfile(
                double movementChangeInterval,
                float moveSpeed,
                double projectileInterval,
                float projectileDamage,
                double streamInterval,
                float streamLifetime,
                float streamSpawnInterval,
                float streamRotationSpeed,
                float streamProjectileSpeed,
                float streamProjectileDamage,
                float streamSpreadAngle
        ) {
            this.movementChangeInterval = movementChangeInterval;
            this.moveSpeed = moveSpeed;
            this.projectileInterval = projectileInterval;
            this.projectileDamage = projectileDamage;
            this.streamInterval = streamInterval;
            this.streamLifetime = streamLifetime;
            this.streamSpawnInterval = streamSpawnInterval;
            this.streamRotationSpeed = streamRotationSpeed;
            this.streamProjectileSpeed = streamProjectileSpeed;
            this.streamProjectileDamage = streamProjectileDamage;
            this.streamSpreadAngle = streamSpreadAngle;
        }
    }

    private static DifficultyProfile profileFor(Difficulty difficulty) {
        if (difficulty == null) {
            difficulty = Difficulty.EXHAUSTING;
        }

        return switch (difficulty) {
            case RELAXING -> new DifficultyProfile(
                    1.2,
                    65f,
                    0.35,
                    1f,
                    9.0,
                    4.0f,
                    0.16f,
                    (float) Math.toRadians(20),
                    300f,
                    1f,
                    (float) Math.toRadians(8)
            );

            case EXHAUSTING -> new DifficultyProfile(
                    0.7,
                    90f,
                    0.20,
                    1.5f,
                    6.0,
                    5.0f,
                    0.10f,
                    (float) Math.toRadians(30),
                    420f,
                    1.5f,
                    (float) Math.toRadians(5)
            );

            case DIVINE -> new DifficultyProfile(
                    0.4,
                    125f,
                    0.12,
                    2f,
                    3.5,
                    6.0f,
                    0.06f,
                    (float) Math.toRadians(50),
                    540f,
                    2f,
                    (float) Math.toRadians(3)
            );
        };
    }

    public AncientAngelBoss(
            float x,
            float y,
            float width,
            float height,
            float maxHp,
            float centerX,
            float centerY
    ) {
        this(
                x,
                y,
                width,
                height,
                maxHp,
                centerX,
                centerY,
                Difficulty.EXHAUSTING
        );
    }

    public AncientAngelBoss(
            float x,
            float y,
            float width,
            float height,
            float maxHp,
            float centerX,
            float centerY,
            Difficulty difficulty
    ) {
        super(
                "ancient_angel",
                "ANCIENT ANGEL",
                x,
                y,
                width,
                height,
                maxHp
        );

        this.centerX = centerX;
        this.centerY = centerY;

        this.difficulty = difficulty == null
                ? Difficulty.EXHAUSTING
                : difficulty;

        this.profile = profileFor(this.difficulty);

        chooseNewMovementDirection();

        rotatingStreamSystem.setDamageListener(
                this::damagePlayer
        );
    }

    @Override
    public void update(
            float deltaSeconds,
            float playerX,
            float playerY
    ) {
        if (!isAlive()) {
            return;
        }

        float delta = Math.max(0f, deltaSeconds);

        updateMovement(delta);
        updateNormalProjectiles(delta, playerX, playerY);
        updateRotatingStreams(delta, playerX, playerY);
    }

    private void updateMovement(float delta) {
        movementTimer += delta;

        while (movementTimer >= profile.movementChangeInterval) {
            movementTimer -= profile.movementChangeInterval;
            chooseNewMovementDirection();
        }

        float effectiveMoveSpeed =
                Math.max(0f, profile.moveSpeed + speedDebuff);

        float newX =
                getX()
                        + moveDirectionX
                        * effectiveMoveSpeed
                        * delta;

        float newY =
                getY()
                        + moveDirectionY
                        * effectiveMoveSpeed
                        * delta;

        setPosition(newX, newY);

        float bossCenterX = getX() + getWidth() / 2f;
        float bossCenterY = getY() + getHeight() / 2f;

        float dx = bossCenterX - centerX;
        float dy = bossCenterY - centerY;

        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > MOVEMENT_RADIUS && distance > 0.0001) {
            moveDirectionX = -dx / (float) distance;
            moveDirectionY = -dy / (float) distance;
        }
    }

    private void updateNormalProjectiles(
            float delta,
            float playerX,
            float playerY
    ) {
        projectileTimer += delta;

        while (projectileTimer >= profile.projectileInterval) {
            projectileTimer -= profile.projectileInterval;
            spawnProjectile();
        }

        updateProjectiles(delta, playerX, playerY);
    }

    private void updateRotatingStreams(
            float delta,
            float playerX,
            float playerY
    ) {
        streamTimer += delta;

        while (streamTimer >= profile.streamInterval) {
            streamTimer -= profile.streamInterval;

            rotatingStreamSystem.spawn(
                    0f,
                    profile.streamLifetime,
                    profile.streamSpawnInterval,
                    profile.streamRotationSpeed,
                    profile.streamProjectileSpeed,
                    profile.streamProjectileDamage,
                    STREAM_PROJECTILE_MAX_DISTANCE,
                    profile.streamSpreadAngle
            );
        }

        float bossOriginX = getX() + getWidth() / 2f;
        float bossOriginY = getY() + getHeight() / 2f;

        rotatingStreamSystem.update(
                delta,
                bossOriginX,
                bossOriginY,
                playerX,
                playerY,
                PLAYER_SIZE,
                PLAYER_SIZE
        );
    }

    public void setSpeedDebuff(int speedDebuff) {
        this.speedDebuff = Math.max(
                -48,
                Math.min(0, speedDebuff)
        );
    }

    public int getSpeedDebuff() {
        return speedDebuff;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    private void spawnProjectile() {
        float spawnX = getX() + getWidth() / 2f;
        float spawnY = getY() + getHeight() / 2f;

        double angle = ThreadLocalRandom.current()
                .nextDouble(0, Math.PI * 2);

        float directionX = (float) Math.cos(angle);
        float directionY = (float) Math.sin(angle);

        AncientAngelProjectile projectile =
                new AncientAngelProjectile(
                        spawnX,
                        spawnY,
                        directionX,
                        directionY,
                        profile.projectileDamage
                );

        projectiles.add(projectile);
    }

    private void updateProjectiles(
            float deltaSeconds,
            float playerX,
            float playerY
    ) {
        Iterator<AncientAngelProjectile> iterator =
                projectiles.iterator();

        while (iterator.hasNext()) {
            AncientAngelProjectile projectile = iterator.next();

            projectile.update(deltaSeconds);

            if (projectile.isExpired()) {
                iterator.remove();
                continue;
            }

            float projectileX = projectile.getX();
            float projectileY = projectile.getY();
            float radius = projectile.getRadius();

            boolean hit =
                    projectileX + radius >= playerX
                            && projectileX - radius <= playerX + PLAYER_SIZE
                            && projectileY + radius >= playerY
                            && projectileY - radius <= playerY + PLAYER_SIZE;

            if (hit) {
                damagePlayer(projectile.getDamage());
                projectile.expire();
                iterator.remove();
            }
        }
    }

    private void chooseNewMovementDirection() {
        double angle = ThreadLocalRandom.current()
                .nextDouble(0, Math.PI * 2);

        moveDirectionX = (float) Math.cos(angle);
        moveDirectionY = (float) Math.sin(angle);
    }

    private void damagePlayer(float damage) {
        if (damage <= 0f) {
            return;
        }

        if (playerDamageListener != null) {
            playerDamageListener.accept(damage);
        }
    }

    @Override
    protected void onReset() {
        projectiles.clear();
        rotatingStreamSystem.clear();

        projectileTimer = 0.0;
        movementTimer = 0.0;
        streamTimer = 0.0;

        setPosition(
                centerX - getWidth() / 2f,
                centerY - getHeight() / 2f
        );

        speedDebuff = 0;

        chooseNewMovementDirection();
    }

    @Override
    protected void onDefeated() {
        projectiles.clear();
        rotatingStreamSystem.clear();

        projectileTimer = 0.0;
        movementTimer = 0.0;
        streamTimer = 0.0;

        System.out.println("ANCIENT ANGEL DEFEATED");
    }

    public void setPlayerDamageListener(
            Consumer<Float> listener
    ) {
        this.playerDamageListener = listener;
    }

    public List<AncientAngelProjectile> getProjectiles() {
        return projectiles;
    }

    public AncientAngelRotatingStreamSystem getRotatingStreamSystem() {
        return rotatingStreamSystem;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!isAlive()) {
            return;
        }

        gc.save();
        gc.setImageSmoothing(false);

        gc.drawImage(
                sprite,
                getX(),
                getY(),
                getWidth(),
                getHeight()
        );

        renderProjectiles(gc);
        renderRotatingStreams(gc);

        gc.restore();
    }

    private void renderProjectiles(GraphicsContext gc) {
        gc.setGlobalAlpha(1.0);

        for (AncientAngelProjectile projectile : projectiles) {
            if (projectile == null || projectile.isExpired()) {
                continue;
            }

            float x = projectile.getX();
            float y = projectile.getY();
            float radius = projectile.getRadius();

            gc.setGlobalAlpha(0.35);
            gc.setFill(Color.rgb(255, 80, 80));
            gc.fillOval(
                    x - radius * 2.5f,
                    y - radius * 2.5f,
                    radius * 5f,
                    radius * 5f
            );

            gc.setGlobalAlpha(1.0);
            gc.setFill(Color.RED);
            gc.fillOval(
                    x - radius,
                    y - radius,
                    radius * 2f,
                    radius * 2f
            );

            gc.setFill(Color.WHITE);
            gc.fillOval(
                    x - radius * 0.4f,
                    y - radius * 0.4f,
                    radius * 0.8f,
                    radius * 0.8f
            );
        }

        gc.setGlobalAlpha(1.0);
    }

    private void renderRotatingStreams(GraphicsContext gc) {
        for (AncientAngelRotatingStream stream :
                rotatingStreamSystem.getStreams()) {

            if (stream == null) {
                continue;
            }

            for (AncientAngelStreamProjectile projectile :
                    stream.getProjectiles()) {

                if (projectile == null || projectile.isFinished()) {
                    continue;
                }

                float x = projectile.getX();
                float y = projectile.getY();

                gc.setFill(Color.rgb(30, 120, 255, 0.30));
                gc.fillOval(x - 9, y - 9, 18, 18);

                gc.setFill(Color.DODGERBLUE);
                gc.fillOval(x - 5, y - 5, 10, 10);

                gc.setFill(Color.LIGHTBLUE);
                gc.fillOval(x - 2.5, y - 2.5, 5, 5);
            }
        }
    }

    private static Image loadRequiredImage(String resourcePath) {
        var stream = AncientAngelBoss.class
                .getResourceAsStream(resourcePath);

        if (stream == null) {
            throw new IllegalStateException(
                    "Required image not found: " + resourcePath
            );
        }

        return new Image(stream);
    }
}
