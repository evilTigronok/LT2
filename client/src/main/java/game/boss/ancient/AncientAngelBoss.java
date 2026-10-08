package game.boss.ancient;

import game.boss.Boss;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class AncientAngelBoss extends Boss {

    private static final float MOVEMENT_RADIUS = 280f;
    private static final double MOVEMENT_CHANGE_INTERVAL = 0.7;
    private static final float MOVE_SPEED = 90f;

    private static final float PLAYER_SIZE = 40f;

    private final float centerX;
    private final float centerY;

    private int speedDebuff = 0;

    private static final String SPRITE_PATH =
            "/assets/mobs/ancient_angel.png";

    private final Image sprite =
            loadRequiredImage(SPRITE_PATH);

    private float moveDirectionX;
    private float moveDirectionY;

    private double movementTimer;

    private static final double PROJECTILE_INTERVAL = 0.2;
    private static final float PROJECTILE_DAMAGE = 1f;

    private final List<AncientAngelProjectile> projectiles =
            new ArrayList<>();

    private double projectileTimer;

    private static final double STREAM_INTERVAL = 6.0;

    private final AncientAngelRotatingStreamSystem rotatingStreamSystem =
            new AncientAngelRotatingStreamSystem();

    private double streamTimer;

    private Consumer<Float> playerDamageListener;

    public AncientAngelBoss(
            float x,
            float y,
            float width,
            float height,
            float maxHp,
            float centerX,
            float centerY
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

        movementTimer += delta;

        if (movementTimer >= MOVEMENT_CHANGE_INTERVAL) {
            movementTimer = 0.0;
            chooseNewMovementDirection();
        }

        float effectiveMoveSpeed =
                Math.max(0f, MOVE_SPEED + speedDebuff);

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

        float bossCenterX =
                getX() + getWidth() / 2f;

        float bossCenterY =
                getY() + getHeight() / 2f;

        float dx = bossCenterX - centerX;
        float dy = bossCenterY - centerY;

        double distance =
                Math.sqrt(dx * dx + dy * dy);

        if (distance > MOVEMENT_RADIUS
                && distance > 0.0001) {
            moveDirectionX = -dx / (float) distance;
            moveDirectionY = -dy / (float) distance;
        }

        projectileTimer += delta;

        while (projectileTimer >= PROJECTILE_INTERVAL) {
            projectileTimer -= PROJECTILE_INTERVAL;
            spawnProjectile();
        }

        updateProjectiles(
                delta,
                playerX,
                playerY
        );

        streamTimer += delta;

        if (streamTimer >= STREAM_INTERVAL) {
            streamTimer -= STREAM_INTERVAL;
            rotatingStreamSystem.spawn(0f);
        }

        float bossOriginX =
                getX() + getWidth() / 2f;

        float bossOriginY =
                getY() + getHeight() / 2f;

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
        this.speedDebuff = Math.max(-48, Math.min(0, speedDebuff));
    }

    public int getSpeedDebuff() {
        return speedDebuff;
    }

    private void spawnProjectile() {
        float spawnX =
                getX() + getWidth() / 2f;

        float spawnY =
                getY() + getHeight() / 2f;

        double angle =
                ThreadLocalRandom.current()
                        .nextDouble(0, Math.PI * 2);

        float directionX = (float) Math.cos(angle);
        float directionY = (float) Math.sin(angle);

        AncientAngelProjectile projectile =
                new AncientAngelProjectile(
                        spawnX,
                        spawnY,
                        directionX,
                        directionY,
                        PROJECTILE_DAMAGE
                );

        projectiles.add(projectile);
    }

    private void updateProjectiles(
            float deltaSeconds,
            float playerX,
            float playerY
    ) {
        float playerWidth = PLAYER_SIZE;
        float playerHeight = PLAYER_SIZE;

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
                            && projectileX - radius <= playerX + playerWidth
                            && projectileY + radius >= playerY
                            && projectileY - radius <= playerY + playerHeight;

            if (hit) {
                damagePlayer(projectile.getDamage());
                projectile.expire();
                iterator.remove();
            }
        }
    }

    private void chooseNewMovementDirection() {
        double angle =
                ThreadLocalRandom.current()
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

        System.out.println(
                "ANCIENT ANGEL DEFEATED"
        );
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

            for (var projectile : stream.getProjectiles()) {
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
