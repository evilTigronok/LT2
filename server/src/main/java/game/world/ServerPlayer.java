package game.world;

import game.combat.Attack;
import game.combat.DamageCalculator;
import game.player.PlayerEquipment;
import game.weapon.AttackStyle;
import game.weapon.Weapon;
import game.weapon.WeaponFactory;

public class ServerPlayer {

    private final String username;

    private float x;
    private float y;

    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;

    /*
     * Базовый ATK игрока.
     *
     * Пока фиксированный.
     * Позже вынесем в PlayerStats.
     */
    private float attack = 5f;

    /*
     * Экипировка игрока.
     */
    private final PlayerEquipment equipment =
            new PlayerEquipment();

    /*
     * Последнее направление движения.
     *
     * Пока храним отдельно.
     * Позже можно заменить на общий Direction.
     */
    private float directionX = 0f;
    private float directionY = 1f;

    /*
     * Время следующей доступной атаки.
     */
    private long nextAttackTime = 0L;

    private final float speed = 6f;

    private final float worldWidth;
    private final float worldHeight;

    public ServerPlayer(
            String username,
            float worldWidth,
            float worldHeight
    ) {

        this.username = username;

        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        this.x =
                (worldWidth - 40f) / 2f;

        this.y =
                (worldHeight - 40f) / 2f;

        /*
         * Стартовое оружие.
         *
         * [3 + 100% ATK]
         *
         * При ATK игрока = 5:
         *
         * 3 + 5 * 1.0 = 8 урона.
         */
        equipment.equipWeapon(
                WeaponFactory.createIronSword()
        );
    }

    public void update() {

        float dx = 0;
        float dy = 0;

        if (up) {
            dy -= 1;
        }

        if (down) {
            dy += 1;
        }

        if (left) {
            dx -= 1;
        }

        if (right) {
            dx += 1;
        }

        if (dx == 0 && dy == 0) {
            return;
        }

        float length =
                (float) Math.sqrt(
                        dx * dx + dy * dy
                );

        dx /= length;
        dy /= length;

        /*
         * Запоминаем последнее направление.
         *
         * Оно используется при атаке,
         * даже если игрок стоит на месте.
         */
        directionX = dx;
        directionY = dy;

        x += dx * speed;
        y += dy * speed;

        x =
                Math.max(
                        0,
                        Math.min(
                                worldWidth - 40f,
                                x
                        )
                );

        y =
                Math.max(
                        0,
                        Math.min(
                                worldHeight - 40f,
                                y
                        )
                );
    }

    /**
     * Пытается выполнить атаку.
     *
     * Возвращает Attack, если атака разрешена.
     * Возвращает null, если:
     *
     * - оружия нет;
     * - cooldown ещё не закончился.
     */
    public synchronized Attack attack() {

        Weapon weapon =
                equipment.getWeapon();

        if (weapon == null) {
            return null;
        }

        long now =
                System.currentTimeMillis();

        if (now < nextAttackTime) {
            return null;
        }

        /*
         * Следующая доступная атака.
         *
         * attackSpeed трактуется как
         * количество атак в секунду.
         *
         * Например:
         *
         * 1.0 -> 1000 мс
         * 2.0 -> 500 мс
         * 0.5 -> 2000 мс
         */
        long cooldown =
                (long) (
                        1000.0
                                / weapon.getAttackSpeed()
                );

        nextAttackTime =
                now + cooldown;

        float damage =
                DamageCalculator
                        .calculateTickDamage(
                                weapon,
                                attack
                        );

        return new Attack(
                weapon.getId(),
                weapon.getAttackStyle(),
                damage,
                weapon.getPrimaryAttack().getHitCount(),
                (float) weapon.getAttackInterval()
        );
    }

    public String getUsername() {
        return username;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getAttack() {
        return attack;
    }

    public PlayerEquipment getEquipment() {
        return equipment;
    }

    public float getDirectionX() {
        return directionX;
    }

    public float getDirectionY() {
        return directionY;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setRight(boolean right) {
        this.right = right;
    }
}