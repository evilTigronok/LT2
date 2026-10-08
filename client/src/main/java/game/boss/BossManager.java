package game.boss;

import javafx.scene.canvas.GraphicsContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BossManager {

    private final Map<String, Boss> bosses =
            new HashMap<>();

    public void addBoss(Boss boss) {

        if (boss == null) {
            return;
        }

        bosses.put(
                boss.getId(),
                boss
        );
    }

    public Boss getBoss(String id) {
        return bosses.get(id);
    }

    public Collection<Boss> getBosses() {
        return bosses.values();
    }

    public void update(
            float deltaSeconds,
            float playerX,
            float playerY
    ) {

        for (Boss boss : bosses.values()) {

            if (boss == null) {
                continue;
            }

            if (!boss.isAlive()) {
                continue;
            }

            boss.update(
                    deltaSeconds,
                    playerX,
                    playerY
            );
        }
    }

    public void render(
            GraphicsContext gc
    ) {

        for (Boss boss : bosses.values()) {

            if (boss == null) {
                continue;
            }

            if (!boss.isAlive()) {
                continue;
            }

            boss.render(gc);
        }
    }

    public void resetBoss(String id) {

        Boss boss =
                bosses.get(id);

        if (boss != null) {
            boss.reset();
        }
    }

    public void resetAll() {

        for (Boss boss : bosses.values()) {

            if (boss != null) {
                boss.reset();
            }
        }
    }

    public void clear() {
        bosses.clear();
    }
}