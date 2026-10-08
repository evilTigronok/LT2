package game.combat;

import game.weapon.Weapon;
import game.weapon.WeaponType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class LocalCombatSystem {

    private final Map<String, LocalCombatTarget> targets =
            new HashMap<>();

    private final List<SwordSwing> activeSwings =
            new ArrayList<>();

    private final Map<SwordSwing, Set<String>> hitTargets =
            new HashMap<>();

    private Consumer<LocalCombatHit> hitListener;

    public LocalCombatSystem() {
    }

    public void setHitListener(
            Consumer<LocalCombatHit> hitListener
    ) {
        this.hitListener = hitListener;
    }

    public void addTarget(
            LocalCombatTarget target
    ) {

        if (target == null) {
            return;
        }

        targets.put(
                target.getId(),
                target
        );
    }

    public LocalCombatTarget getTarget(
            String id
    ) {
        return targets.get(id);
    }

    public Collection<LocalCombatTarget> getTargets() {
        return targets.values();
    }

    public Collection<SwordSwing> getActiveSwings() {
        return activeSwings;
    }

    /**
     * Создаёт атаку ближнего боя
     * с использованием конкретного оружия.
     */
    public boolean attack(
            Weapon weapon,
            float playerAttack,
            float originX,
            float originY,
            float directionX,
            float directionY
    ) {

        if (weapon == null) {
            return false;
        }

        if (weapon.getType() != WeaponType.MELEE) {
            return false;
        }

        float damage =
                DamageCalculator.calculateTickDamage(
                        weapon,
                        playerAttack
                );

        SwordSwing swing =
                new SwordSwing(
                        originX,
                        originY,
                        directionX,
                        directionY,
                        damage,
                        weapon.getAttackStyle()
                );

        activeSwings.add(
                swing
        );

        hitTargets.put(
                swing,
                new HashSet<>()
        );

        processSwingCollision(
                swing
        );

        return true;
    }

    public void update() {

        update(
                Float.NaN,
                Float.NaN,
                1f / 60f
        );
    }

    public void update(
            float playerX,
            float playerY,
            float deltaSeconds
    ) {

        if (activeSwings.isEmpty()) {
            return;
        }

        List<SwordSwing> snapshot =
                new ArrayList<>(
                        activeSwings
                );

        for (SwordSwing swing :
                snapshot) {

            if (!Float.isNaN(playerX)
                    && !Float.isNaN(playerY)) {

                swing.setOrigin(
                        playerX,
                        playerY
                );
            }

            swing.update(
                    deltaSeconds
            );

            /*
             * Проверяем попадание во время
             * существования взмаха.
             */
            processSwingCollision(
                    swing
            );

            if (swing.isFinished()) {

                activeSwings.remove(
                        swing
                );

                hitTargets.remove(
                        swing
                );
            }
        }
    }

    private void processSwingCollision(
            SwordSwing swing
    ) {

        Set<String> alreadyHit =
                hitTargets.get(swing);

        if (alreadyHit == null) {
            return;
        }

        for (LocalCombatTarget target :
                targets.values()) {

            if (target == null
                    || !target.isAlive()) {

                continue;
            }

            String targetId =
                    target.getId();

            if (alreadyHit.contains(
                    targetId
            )) {
                continue;
            }

            boolean hit =
                    SwordGeometry.intersectsAabb(
                            swing,
                            target.getX(),
                            target.getY(),
                            target.getWidth(),
                            target.getHeight()
                    );

            if (!hit) {
                continue;
            }

            float damage =
                    swing.getDamage();

            target.damage(
                    damage
            );

            alreadyHit.add(
                    targetId
            );

            if (hitListener != null) {

                float hitX =
                        target.getX()
                                + target.getWidth()
                                / 2f;

                float hitY =
                        target.getY()
                                + target.getHeight()
                                / 2f;

                hitListener.accept(
                        new LocalCombatHit(
                                targetId,
                                hitX,
                                hitY,
                                damage
                        )
                );
            }
        }
    }

    public void damageArea(
            float centerX,
            float centerY,
            float radius,
            float damage
    ) {

        if (radius <= 0f || damage <= 0f) {
            return;
        }

        float radiusSquared =
                radius * radius;

        for (LocalCombatTarget target :
                targets.values()) {

            if (target == null || !target.isAlive()) {
                continue;
            }

            float targetCenterX =
                    target.getX()
                            + target.getWidth() / 2f;

            float targetCenterY =
                    target.getY()
                            + target.getHeight() / 2f;

            float dx =
                    targetCenterX - centerX;

            float dy =
                    targetCenterY - centerY;

            float distanceSquared =
                    dx * dx + dy * dy;

            if (distanceSquared <= radiusSquared) {

                /*
                 * Наносим AoE-урон.
                 */
                target.damage(
                        damage
                );

                /*
                 * Создаём обычный LocalCombatHit,
                 * чтобы существующая система попапов
                 * показала цифру урона над этой целью.
                 */
                if (hitListener != null) {

                    hitListener.accept(
                            new LocalCombatHit(
                                    target.getId(),
                                    targetCenterX,
                                    targetCenterY,
                                    damage
                            )
                    );
                }
            }
        }
    }

    public void damageLine(
            float startX,
            float startY,
            float directionX,
            float directionY,
            float length,
            float width,
            float damage
    ) {

        if (length <= 0f || width <= 0f || damage <= 0f) {
            return;
        }

        float directionLength =
                (float) Math.sqrt(
                        directionX * directionX
                                + directionY * directionY
                );

        if (directionLength < 0.0001f) {
            return;
        }

        directionX /= directionLength;
        directionY /= directionLength;

        float endX =
                startX + directionX * length;

        float endY =
                startY + directionY * length;

        float halfWidth =
                width / 2f;

        float minX =
                Math.min(startX, endX) - halfWidth;

        float maxX =
                Math.max(startX, endX) + halfWidth;

        float minY =
                Math.min(startY, endY) - halfWidth;

        float maxY =
                Math.max(startY, endY) + halfWidth;

        for (LocalCombatTarget target : targets.values()) {

            if (target == null || !target.isAlive()) {
                continue;
            }

            float left =
                    target.getX();

            float right =
                    target.getX()
                            + target.getWidth();

            float top =
                    target.getY();

            float bottom =
                    target.getY()
                            + target.getHeight();

            if (right < minX
                    || left > maxX
                    || bottom < minY
                    || top > maxY) {
                continue;
            }

            if (!segmentIntersectsRect(
                    startX,
                    startY,
                    endX,
                    endY,
                    left - halfWidth,
                    top - halfWidth,
                    right + halfWidth,
                    bottom + halfWidth
            )) {
                continue;
            }

            target.damage(damage);

            if (hitListener != null) {

                float hitX =
                        target.getX()
                                + target.getWidth() / 2f;

                float hitY =
                        target.getY()
                                + target.getHeight() / 2f;

                hitListener.accept(
                        new LocalCombatHit(
                                target.getId(),
                                hitX,
                                hitY,
                                damage
                        )
                );
            }
        }
    }

    private boolean segmentIntersectsRect(
            float x1,
            float y1,
            float x2,
            float y2,
            float left,
            float top,
            float right,
            float bottom
    ) {

        float dx =
                x2 - x1;

        float dy =
                y2 - y1;

        float tMin = 0f;
        float tMax = 1f;

        if (Math.abs(dx) < 0.00001f) {

            if (x1 < left || x1 > right) {
                return false;
            }

        } else {

            float tx1 =
                    (left - x1) / dx;

            float tx2 =
                    (right - x1) / dx;

            if (tx1 > tx2) {
                float tmp = tx1;
                tx1 = tx2;
                tx2 = tmp;
            }

            tMin =
                    Math.max(tMin, tx1);

            tMax =
                    Math.min(tMax, tx2);

            if (tMin > tMax) {
                return false;
            }
        }

        if (Math.abs(dy) < 0.00001f) {

            return y1 >= top
                    && y1 <= bottom;

        } else {

            float ty1 =
                    (top - y1) / dy;

            float ty2 =
                    (bottom - y1) / dy;

            if (ty1 > ty2) {
                float tmp = ty1;
                ty1 = ty2;
                ty2 = tmp;
            }

            tMin =
                    Math.max(tMin, ty1);

            tMax =
                    Math.min(tMax, ty2);

            return tMin <= tMax;
        }
    }

    public void clear() {

        activeSwings.clear();

        hitTargets.clear();
    }
}