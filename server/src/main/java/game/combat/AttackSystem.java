package game.combat;

import game.world.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AttackSystem {

    private final List<ActiveAttack> activeAttacks =
            new ArrayList<>();

    private final List<CombatTarget> targets =
            new ArrayList<>();

    private Consumer<CombatHit> hitListener;

    public synchronized void addTarget(CombatTarget target) {
        if (target == null) {
            return;
        }

        targets.add(target);

        System.out.println(
                "COMBAT TARGET ADDED: "
                        + target.getId()
                        + " @ "
                        + target.getX()
                        + ","
                        + target.getY()
        );
    }

    public synchronized List<CombatTarget> getTargets() {
        return new ArrayList<>(targets);
    }

    public synchronized void startAttack(
            ServerPlayer attacker,
            Attack attack
    ) {
        if (attacker == null || attack == null) {
            return;
        }

        ActiveAttack activeAttack =
                new ActiveAttack(attacker, attack);

        activeAttacks.add(activeAttack);

        processTick(
                activeAttack,
                System.nanoTime()
        );
    }

    public synchronized void update() {

        if (activeAttacks.isEmpty()) {
            return;
        }

        long now = System.nanoTime();

        List<ActiveAttack> finished =
                new ArrayList<>();

        for (ActiveAttack activeAttack : activeAttacks) {

            if (activeAttack.completedTicks
                    >= activeAttack.attack.getTotalTicks()) {

                finished.add(activeAttack);
                continue;
            }

            if (now >= activeAttack.nextTickTime) {

                processTick(
                        activeAttack,
                        now
                );
            }

            if (activeAttack.completedTicks
                    >= activeAttack.attack.getTotalTicks()) {

                finished.add(activeAttack);
            }
        }

        activeAttacks.removeAll(finished);
    }

    public synchronized void setHitListener(
            Consumer<CombatHit> hitListener
    ) {
        this.hitListener = hitListener;
    }

    private void processTick(
            ActiveAttack activeAttack,
            long now
    ) {

        Attack attack =
                activeAttack.attack;

        activeAttack.completedTicks++;

        DamageTick tick =
                new DamageTick(
                        attack.getWeaponId(),
                        attack.getDamagePerTick(),
                        activeAttack.completedTicks,
                        attack.getTotalTicks()
                );

        System.out.println(
                "DAMAGE TICK: "
                        + activeAttack.attacker.getUsername()
                        + " -> "
                        + tick.getWeaponId()
                        + " | "
                        + tick.getDamage()
                        + " damage | tick "
                        + tick.getTickNumber()
                        + "/"
                        + tick.getTotalTicks()
        );

        /*
         * Проверяем все боевые цели.
         */
        for (CombatTarget target : targets) {

            if (target == null) {
                continue;
            }

            if (!target.isAlive()) {
                continue;
            }

            if (isHit(
                    activeAttack.attacker,
                    target,
                    activeAttack.attack
            )) {
                continue;
            }

            /*
             * Попадание.
             */
            target.damage(
                    tick.getDamage()
            );

            System.out.println(
                    "COMBAT HIT: "
                            + activeAttack.attacker.getUsername()
                            + " -> "
                            + target.getId()
                            + " | "
                            + tick.getDamage()
                            + " damage"
                            + " | HP "
                            + target.getHp()
                            + "/"
                            + target.getMaxHp()
            );

            /*
             * Отправляем событие клиенту.
             */
            if (hitListener != null) {

                float hitX =
                        target.getX()
                                + target.getWidth() / 2f;

                float hitY =
                        target.getY()
                                + target.getHeight() / 2f;

                CombatHit hit =
                        new CombatHit(
                                activeAttack.attacker.getUsername(),
                                target.getId(),
                                hitX,
                                hitY,
                                tick.getDamage()
                        );

                hitListener.accept(hit);
            }
        }

        /*
         * Если это был последний тик —
         * удар завершён.
         */
        if (activeAttack.completedTicks
                >= attack.getTotalTicks()) {

            return;
        }

        /*
         * Время следующего тика.
         */
        activeAttack.nextTickTime =
                now
                        + (long) (
                        attack.getTickInterval()
                                * 1_000_000.0
                );
    }

    private boolean isHit(
            ServerPlayer attacker,
            CombatTarget target,
            Attack attack
    ) {
        float attackerCenterX = attacker.getX() + 20f;
        float attackerCenterY = attacker.getY() + 20f;

        float targetCenterX =
                target.getX() + target.getWidth() / 2f;

        float targetCenterY =
                target.getY() + target.getHeight() / 2f;

        float deltaX = targetCenterX - attackerCenterX;
        float deltaY = targetCenterY - attackerCenterY;

        AttackHitboxDefinition definition =
                AttackHitboxDefinition.forStyle(attack.getStyle());

        // SMASH — круг вокруг игрока.
        if (definition.getShape() ==
                AttackHitboxDefinition.Shape.CIRCLE) {

            float distanceSquared =
                    deltaX * deltaX +
                            deltaY * deltaY;

            float radius = definition.getRadius();

            return distanceSquared <= radius * radius;
        }

        float directionX = attacker.getDirectionX();
        float directionY = attacker.getDirectionY();

        float directionLength =
                (float) Math.sqrt(
                        directionX * directionX +
                                directionY * directionY
                );

        if (directionLength < 0.001f) {
            directionX = 0f;
            directionY = 1f;
        } else {
            directionX /= directionLength;
            directionY /= directionLength;
        }

        /*
         * Локальная система координат атаки:
         *
         * forward = расстояние вперёд
         * side    = расстояние в сторону
         *
         * Направление игрока считается +forward.
         */
        float forward =
                deltaX * directionX +
                        deltaY * directionY;

        float side =
                deltaX * (-directionY) +
                        deltaY * directionX;

        // Нельзя попасть за спиной.
        if (forward < 0f) {
            return false;
        }

        // Прямоугольный хитбокс.
        if (definition.getShape() ==
                AttackHitboxDefinition.Shape.RECTANGLE) {

            return forward <= definition.getRange()
                    && Math.abs(side) <= definition.getHalfWidth();
        }

        // Сектор.
        if (definition.getShape() ==
                AttackHitboxDefinition.Shape.SECTOR) {

            float distance =
                    (float) Math.sqrt(
                            forward * forward +
                                    side * side
                    );

            if (distance > definition.getRange()) {
                return false;
            }

            if (distance < 0.001f) {
                return true;
            }

            double angle =
                    Math.toDegrees(
                            Math.atan2(
                                    Math.abs(side),
                                    forward
                            )
                    );

            return angle <= definition.getHalfAngle();
        }

        return false;
    }

    private static class ActiveAttack {

        private final ServerPlayer attacker;
        private final Attack attack;

        private final float directionX;
        private final float directionY;

        private int completedTicks;

        private long nextTickTime;

        private ActiveAttack(
                ServerPlayer attacker,
                Attack attack
        ) {

            this.attacker = attacker;
            this.attack = attack;

            this.directionX =
                    attacker.getDirectionX();

            this.directionY =
                    attacker.getDirectionY();

            this.completedTicks = 0;

            this.nextTickTime =
                    System.nanoTime();
        }
    }
}