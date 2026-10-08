package game.boss.ancient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class AncientAngelRotatingStreamSystem {

    private final List<AncientAngelRotatingStream> streams =
            new ArrayList<>();

    private Consumer<Float> damageListener;

    public void spawn(
            float initialAngle
    ) {
        streams.add(
                new AncientAngelRotatingStream(
                        initialAngle
                )
        );
    }

    public void setDamageListener(
            Consumer<Float> damageListener
    ) {
        this.damageListener = damageListener;
    }

    public void update(
            float deltaSeconds,

            // Позиция Ангела — источник снарядов
            float originX,
            float originY,

            // Позиция игрока — цель
            float playerX,
            float playerY,
            float playerWidth,
            float playerHeight
    ) {

        Iterator<AncientAngelRotatingStream> iterator =
                streams.iterator();

        while (iterator.hasNext()) {

            AncientAngelRotatingStream stream =
                    iterator.next();

            if (stream == null) {
                iterator.remove();
                continue;
            }

            /*
             * Снаряды появляются из центра Ангела.
             */
            stream.update(
                    deltaSeconds,
                    originX,
                    originY
            );

            /*
             * Проверяем столкновение каждого снаряда
             * с игроком.
             */
            for (AncientAngelStreamProjectile projectile :
                    stream.getProjectiles()) {

                if (projectile == null
                        || projectile.isFinished()) {
                    continue;
                }

                if (intersectsPlayer(
                        projectile,
                        playerX,
                        playerY,
                        playerWidth,
                        playerHeight
                )) {

                    if (damageListener != null) {
                        damageListener.accept(
                                projectile.getDamage()
                        );
                    }

                    projectile.finish();
                }
            }

            /*
             * Когда сам поток завершился и все его
             * снаряды исчезли — удаляем поток.
             */
            if (stream.isFinished()
                    && stream.getProjectiles().isEmpty()) {

                iterator.remove();
            }
        }
    }

    private boolean intersectsPlayer(
            AncientAngelStreamProjectile projectile,
            float playerX,
            float playerY,
            float playerWidth,
            float playerHeight
    ) {

        float px = projectile.getX();
        float py = projectile.getY();

        return px >= playerX
                && px <= playerX + playerWidth
                && py >= playerY
                && py <= playerY + playerHeight;
    }

    public Collection<AncientAngelRotatingStream>
    getStreams() {
        return streams;
    }

    public void clear() {
        streams.clear();
    }
}