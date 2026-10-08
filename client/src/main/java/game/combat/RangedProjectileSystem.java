package game.combat;

import game.weapon.Weapon;

import java.util.*;
import java.util.function.Consumer;

public class RangedProjectileSystem {

    @FunctionalInterface
    public interface ProjectileOriginProvider {
        float[] getOrigin();
    }

    @FunctionalInterface
    public interface ProjectileDirectionProvider {
        float[] getDirection();
    }

    /*
     * Интервал между выстрелами внутри одной очереди.
     */
    private static final float BURST_SHOT_INTERVAL = 0.2f;

    /*
     * Solar-эффект Ангельской погибели.
     */
    private static final float SOLAR_AOE_RADIUS = 100f;

    /*
     * Траур: дополнительные Dust-снаряды падают сверху
     * в точки вокруг места приземления основного снаряда.
     */
    private static final float MOURNING_DUST_OFFSET = 300f;
    private static final float MOURNING_DUST_HEIGHT = 320f;
    private static final float MOURNING_DUST_FALL_SPEED = 300f;
    /** Радиус собственного AoE каждого Dust-снаряда Траура. */
    private static final float MOURNING_DUST_AOE_RADIUS = 45f;
    /** Толщина электрического разряда Траура при проверке попадания. */
    private static final float MOURNING_LIGHTNING_THICKNESS = 8f;

    private final Map<RangedProjectile, AngelicDoomMode> projectileModes =
            new IdentityHashMap<>();

    private final List<RangedProjectile> projectiles =
            new ArrayList<>();

    private final List<PendingBurst> pendingBursts =
            new ArrayList<>();

    private final LocalCombatSystem combatSystem;

    private Consumer<LocalCombatHit> hitListener;

    private ProjectileOriginProvider originProvider;
    private ProjectileDirectionProvider directionProvider;

    private final List<SolarExplosion> solarExplosions =
            new ArrayList<>();

    private final List<SolarExplosion> starExplosions =
            new ArrayList<>();

    private final List<LightningArc> mourningLightningArcs =
            new ArrayList<>();

    private final List<PendingMourning> pendingMourning =
            new ArrayList<>();

    /* Основные Wind-снаряды Траура, для которых нужно
       определить фактическую точку приземления. */
    private final Set<RangedProjectile> mourningMainProjectiles =
            Collections.newSetFromMap(new IdentityHashMap<>());

    /* Dust-снаряд должен детонировать ровно один раз, даже если
       сначала пересёк цель, а затем завершил полёт. */
    private final Set<RangedProjectile> mourningDustDetonated =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private float lastMourningLandingX;
    private float lastMourningLandingY;
    private boolean hasMourningLanding;


    public RangedProjectileSystem(
            LocalCombatSystem combatSystem
    ) {

        if (combatSystem == null) {
            throw new IllegalArgumentException(
                    "Combat system cannot be null"
            );
        }

        this.combatSystem = combatSystem;
    }

    public void setHitListener(
            Consumer<LocalCombatHit> hitListener
    ) {
        this.hitListener = hitListener;
    }

    public void setOriginProvider(
            ProjectileOriginProvider originProvider
    ) {
        this.originProvider = originProvider;
    }

    public void setDirectionProvider(
            ProjectileDirectionProvider directionProvider
    ) {
        this.directionProvider = directionProvider;
    }

    public Collection<RangedProjectile> getProjectiles() {
        return projectiles;
    }

    /*
     * =====================================================
     * ОБЫЧНЫЙ ВЫСТРЕЛ
     * =====================================================
     *
     * ВАЖНО:
     *
     * Обычный fire() вообще не имеет AngelicDoomMode.
     *
     * Поэтому обычное оружие:
     * - не может создать SolarExplosion;
     * - не может применить Lunar;
     * - просто наносит обычный урон.
     */
    public boolean fire(
            float originX,
            float originY,
            float directionX,
            float directionY,
            Weapon weapon,
            float playerAttack
    ) {

        if (weapon == null) {
            return false;
        }

        if (!weapon.isRanged()) {
            return false;
        }

        AttackDefinition attack =
                weapon.getPrimaryAttack();

        if (attack == null) {
            return false;
        }

        float damage =
                DamageCalculator.calculateTickDamage(
                        weapon,
                        playerAttack
                );

        int projectileCount =
                Math.max(
                        1,
                        attack.getProjectileCount()
                );

        float projectileSpeed =
                attack.getProjectileSpeed();

        float projectileRange =
                attack.getProjectileRange();

        boolean piercing =
                attack.isPiercing();

        /*
         * Обычный projectile получает mode = null.
         */
        spawnProjectile(
                originX,
                originY,
                directionX,
                directionY,
                damage,
                projectileSpeed,
                projectileRange,
                piercing,
                null,
                attack.getDamageParts().get(0).getType(),
                attack.getDefenseIgnore()
        );

        /*
         * Остальные выстрелы.
         */
        if (projectileCount > 1) {

            PendingBurst burst =
                    new PendingBurst(
                            originX,
                            originY,
                            directionX,
                            directionY,
                            damage,
                            projectileSpeed,
                            projectileRange,
                            piercing,
                            projectileCount - 1,
                            null,
                            attack.getDamageParts().get(0).getType(),
                            attack.getDefenseIgnore()
                    );

            pendingBursts.add(burst);
        }

        return true;
    }

    /** Выстрел конкретным профилем атаки. Используется спец-оружием. */
    public boolean fire(
            float originX,
            float originY,
            float directionX,
            float directionY,
            Weapon weapon,
            AttackDefinition attack,
            float playerAttack
    ) {
        if (weapon == null || !weapon.isRanged() || attack == null) {
            return false;
        }

        float damage = DamageCalculator.calculateTickDamage(
                attack, playerAttack, weapon.getAttack()
        );

        int projectileCount = Math.max(1, attack.getProjectileCount());

        spawnProjectile(
                originX, originY, directionX, directionY, damage,
                attack.getProjectileSpeed(), attack.getProjectileRange(),
                attack.isPiercing(), null, attack.getDamageParts().get(0).getType(),
                attack.getDefenseIgnore()
        );

        if (projectileCount > 1) {
            pendingBursts.add(new PendingBurst(
                    originX, originY, directionX, directionY, damage,
                    attack.getProjectileSpeed(), attack.getProjectileRange(),
                    attack.isPiercing(), projectileCount - 1, null,
                    attack.getDamageParts().get(0).getType(),
                    attack.getDefenseIgnore()
            ));
        }
        return true;
    }

    /** Специальная атака оружия «Траур». */
    public boolean fireMourning(
            float originX, float originY,
            float directionX, float directionY,
            Weapon weapon, float playerAttack,
            boolean critical, float weaponAttackOverride
    ) {
        if (weapon == null || !weapon.isRanged()
                || weapon.getSpecial() != game.weapon.WeaponSpecial.MOURNING) {
            return false;
        }

        AttackDefinition attack = weapon.getPrimaryAttack();
        float mainDamage = DamageCalculator.calculateTickDamage(
                attack, playerAttack, weaponAttackOverride
        );

        /*
         * Основной снаряд летит как раньше.
         * Дополнительные снаряды НЕ привязываем к игроку:
         * сначала ждём фактическое место приземления этого снаряда.
         */
        RangedProjectile mainProjectile = spawnProjectile(
                originX, originY, directionX, directionY, mainDamage,
                attack.getProjectileSpeed(), attack.getProjectileRange(),
                attack.isPiercing(), null, DamageType.WIND, 0f
        );
        mourningMainProjectiles.add(mainProjectile);

        float dustDamage =
                Math.max(0f, playerAttack + weaponAttackOverride) * 0.50f;
        int count = critical ? 8 : 4;
        float baseAngle = (float) Math.atan2(directionY, directionX);

        /*
         * Геометрия остаётся той же, но порядок падения каждый раз
         * перемешивается. Благодаря этому Траур не повторяет одну
         * и ту же последовательность точек.
         */
        List<Float> angles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            float angle = critical
                    ? baseAngle + (float) (Math.PI / 4.0 * i)
                    : baseAngle + (float) (Math.PI / 4.0 + Math.PI / 2.0 * i);
            angles.add(angle);
        }
        Collections.shuffle(angles);

        for (int i = 0; i < count; i++) {
            /*
             * Центр пока неизвестен. angle хранит только положение
             * точки относительно будущего места приземления.
             */
            pendingMourning.add(new PendingMourning(
                    angles.get(i), dustDamage, i * 0.1f
            ));
        }

        return true;
    }

    /*
     * =====================================================
     * АНГЕЛЬСКАЯ ПОГИБЕЛЬ
     * =====================================================
     *
     * Только этот overload имеет доступ к Solar/Lunar.
     */
    public boolean fire(
            float originX,
            float originY,
            float directionX,
            float directionY,
            Weapon weapon,
            float playerAttack,
            AngelicDoomMode mode
    ) {

        if (weapon == null) {
            return false;
        }

        if (!weapon.isRanged()) {
            return false;
        }

        if (mode == null) {
            return false;
        }

        AttackDefinition attack =
                weapon.getPrimaryAttack();

        if (attack == null) {
            return false;
        }

        float damage =
                DamageCalculator.calculateTickDamage(
                        weapon,
                        playerAttack
                );

        int projectileCount =
                Math.max(
                        1,
                        attack.getProjectileCount()
                );

        float projectileSpeed =
                attack.getProjectileSpeed();

        float projectileRange =
                attack.getProjectileRange();

        boolean piercing =
                mode == AngelicDoomMode.SOLAR
                        && attack.isPiercing();

        /*
         * Только mode-aware атака получает режим.
         */
        spawnProjectile(
                originX,
                originY,
                directionX,
                directionY,
                damage,
                projectileSpeed,
                projectileRange,
                piercing,
                mode,
                attack.getDamageParts().get(0).getType(),
                attack.getDefenseIgnore()
        );

        if (projectileCount > 1) {

            PendingBurst burst =
                    new PendingBurst(
                            originX,
                            originY,
                            directionX,
                            directionY,
                            damage,
                            projectileSpeed,
                            projectileRange,
                            piercing,
                            projectileCount - 1,
                            mode,
                            attack.getDamageParts().get(0).getType(),
                            attack.getDefenseIgnore()
                    );

            pendingBursts.add(burst);
        }

        return true;
    }

    /*
     * =====================================================
     * UPDATE
     * =====================================================
     */
    public void update(
            float deltaSeconds
    ) {

        float delta =
                Math.max(
                        0f,
                        deltaSeconds
                );

        /*
         * =================================================
         * ТРАУР — ПОГРЕБАЛЬНЫЕ СНАРЯДЫ
         * =================================================
         */
        Iterator<PendingMourning> mourningIterator = pendingMourning.iterator();
        while (mourningIterator.hasNext()) {
            PendingMourning pending = mourningIterator.next();

            /* Дополнительные снаряды начинают отсчитывать 0.1 с
               только после того, как основной снаряд приземлился. */
            if (!pending.landingResolved) {
                continue;
            }

            pending.timer += delta;

            if (pending.timer >= pending.delay) {
                float targetX =
                        pending.landingX
                                + (float) Math.cos(pending.angle)
                                * MOURNING_DUST_OFFSET;
                float targetY =
                        pending.landingY
                                + (float) Math.sin(pending.angle)
                                * MOURNING_DUST_OFFSET;

                /* Появляется над точкой и падает строго вертикально. */
                float originY = targetY - MOURNING_DUST_HEIGHT;

                spawnProjectile(
                        targetX, originY,
                        0f, 1f,
                        pending.damage,
                        MOURNING_DUST_FALL_SPEED,
                        MOURNING_DUST_HEIGHT,
                        false, null,
                        DamageType.DUST, 0f
                );
                mourningIterator.remove();
            }
        }

        /*
         * =================================================
         * ОЧЕРЕДИ
         * =================================================
         */
        Iterator<PendingBurst> burstIterator =
                pendingBursts.iterator();

        while (burstIterator.hasNext()) {

            PendingBurst burst =
                    burstIterator.next();

            burst.timer += delta;

            while (
                    burst.timer >= BURST_SHOT_INTERVAL
                            && burst.remainingShots > 0
            ) {

                burst.timer -=
                        BURST_SHOT_INTERVAL;

                float[] origin;

                if (originProvider != null) {

                    origin =
                            originProvider.getOrigin();

                } else {

                    origin =
                            new float[]{
                                    burst.originX,
                                    burst.originY
                            };
                }

                float[] direction;

                if (directionProvider != null) {

                    direction =
                            directionProvider.getDirection();

                } else {

                    direction =
                            new float[]{
                                    burst.directionX,
                                    burst.directionY
                            };
                }

                /*
                 * Передаём режим конкретной очереди.
                 */
                spawnProjectile(
                        origin[0], origin[1], direction[0], direction[1],
                        burst.damage, burst.projectileSpeed, burst.projectileRange,
                        burst.piercing, burst.mode, burst.damageType, burst.defenseIgnore
                );

                burst.remainingShots--;
            }

            if (burst.remainingShots <= 0) {
                burstIterator.remove();
            }
        }

        /*
         * =================================================
         * СНАРЯДЫ
         * =================================================
         */
        if (projectiles.isEmpty()) {
            return;
        }

        List<RangedProjectile> snapshot =
                new ArrayList<>(
                        projectiles
                );

        for (RangedProjectile projectile :
                snapshot) {

            if (projectile == null) {

                projectiles.remove(
                        projectile
                );
                projectileModes.remove(
                        projectile
                );

                continue;
            }

            if (projectile.isFinished()) {

                if (projectile.getDamageType() == DamageType.DUST) {
                    detonateMourningDust(projectile);
                }

                registerMourningLanding(projectile);
                resolveMourningMainLanding(projectile);

                projectiles.remove(
                        projectile
                );

                projectileModes.remove(
                        projectile
                );

                continue;
            }

            projectile.update(
                    delta
            );

            processCollision(
                    projectile,
                    projectileModes.get(projectile)
            );

            if (projectile.isFinished()) {

                if (projectile.getDamageType() == DamageType.DUST) {
                    detonateMourningDust(projectile);
                }

                registerMourningLanding(projectile);
                resolveMourningMainLanding(projectile);

                projectiles.remove(
                        projectile
                );
                projectileModes.remove(
                        projectile
                );
            }
        }
    }

    /*
     * =====================================================
     * СОЗДАНИЕ СНАРЯДА
     * =====================================================
     */
    private RangedProjectile spawnProjectile(
            float originX,
            float originY,
            float directionX,
            float directionY,
            float damage,
            float projectileSpeed,
            float projectileRange,
            boolean piercing,
            AngelicDoomMode mode,
            DamageType damageType,
            float defenseIgnore
    ) {

        RangedProjectile projectile =
                new RangedProjectile(
                        originX,
                        originY,
                        directionX,
                        directionY,
                        damage,
                        projectileSpeed,
                        projectileRange,
                        piercing,
                        damageType,
                        mode,
                        defenseIgnore
                );

        projectiles.add(
                projectile
        );
        projectileModes.put(
                projectile,
                mode
        );

        /*
         * Режим передаём непосредственно
         * в обработчик попадания.
         */
        processCollision(
                projectile,
                mode
        );

        return projectile;
    }

    /*
     * =====================================================
     * COLLISION
     * =====================================================
     */
    private void processCollision(
            RangedProjectile projectile
    ) {

        /*
         * Старый вызов оставляем для обычного update().
         *
         * Здесь mode = null.
         */
        processCollision(
                projectile,
                null
        );
    }

    private void processCollision(
            RangedProjectile projectile,
            AngelicDoomMode mode
    ) {

        if (projectile == null
                || projectile.isFinished()) {

            return;
        }

        for (LocalCombatTarget target :
                combatSystem.getTargets()) {

            if (target == null
                    || !target.isAlive()) {

                continue;
            }

            String targetId =
                    target.getId();

            if (projectile.hasHitTarget(
                    targetId
            )) {

                continue;
            }

            if (!intersects(
                    projectile,
                    target
            )) {

                continue;
            }

            float damage =
                    projectile.getDamage();

            /*
             * Dust-снаряд Траура сам является AoE-снарядом.
             * Если он пересёк цель раньше окончания траектории,
             * детонируем сразу в текущей точке.
             */
            if (projectile.getDamageType() == DamageType.DUST) {
                detonateMourningDust(projectile);
                projectile.finish();
                return;
            }

            if (projectile.getDefenseIgnore() > 0f) {
                float hitX = target.getX() + target.getWidth() / 2f;
                float hitY = target.getY() + target.getHeight() / 2f;
                starExplosions.add(new SolarExplosion(hitX, hitY, 90f, 0.28f));
                combatSystem.damageArea(hitX, hitY, 90f, damage * 0.35f);
            }

            /*
             * =============================================
             * ОСНОВНОЙ УРОН
             * =============================================
             */
            target.damage(
                    damage
            );

            projectile.markTargetHit(
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

            /*
             * =============================================
             * СПЕЦИАЛЬНЫЕ ЭФФЕКТЫ
             * =============================================
             *
             * КЛЮЧЕВАЯ ПРОВЕРКА:
             *
             * mode == null
             *     -> обычное оружие
             *     -> НИКАКОЙ вспышки
             *
             * SOLAR
             *     -> вспышка + AoE
             *
             * LUNAR
             *     -> только Lunar-эффект
             */
            if (mode == AngelicDoomMode.SOLAR) {
                System.out.println(
                        "SOLAR EFFECT TRIGGERED"
                );

                float hitX =
                        target.getX()
                                + target.getWidth()
                                / 2f;

                float hitY =
                        target.getY()
                                + target.getHeight()
                                / 2f;

                /*
                 * Визуальная вспышка.
                 */
                solarExplosions.add(
                        new SolarExplosion(
                                hitX,
                                hitY,
                                SOLAR_AOE_RADIUS,
                                0.2f
                        )
                );

                /*
                 * Solar AoE:
                 * 15% от исходного урона.
                 */
                combatSystem.damageArea(
                        hitX,
                        hitY,
                        SOLAR_AOE_RADIUS,
                        damage * 0.15f
                );
            }

            if (mode == AngelicDoomMode.LUNAR) {

                /*
                 * Lunar:
                 * случайно накладываем один из эффектов.
                 *
                 * Здесь оставляем ту же механику,
                 * которая была у Ангельской погибели.
                 */
                if (Math.random() < 0.5) {

                    target.applySpeedDebuff(
                            2
                    );

                } else {

                    target.applyDefenseDebuff(
                            1
                    );
                }
            }

            /*
             * Непробивающий снаряд
             * заканчивается после попадания.
             */
            if (!projectile.isPiercing()) {

                projectile.finish();
                resolveMourningMainLanding(projectile);

                return;
            }
        }
    }

    private boolean intersects(
            RangedProjectile projectile,
            LocalCombatTarget target
    ) {

        float px =
                projectile.getX();

        float py =
                projectile.getY();

        return px >= target.getX()
                && px <= target.getX()
                + target.getWidth()

                && py >= target.getY()
                && py <= target.getY()
                + target.getHeight();
    }

    private void detonateMourningDust(RangedProjectile projectile) {
        if (projectile == null || projectile.getDamageType() != DamageType.DUST
                || !mourningDustDetonated.add(projectile)) {
            return;
        }

        float impactX = projectile.getX();
        float impactY = projectile.getY();
        float damage = projectile.getDamage();

        for (LocalCombatTarget areaTarget : combatSystem.getTargets()) {
            if (areaTarget == null || !areaTarget.isAlive()) {
                continue;
            }

            float targetCenterX = areaTarget.getX() + areaTarget.getWidth() / 2f;
            float targetCenterY = areaTarget.getY() + areaTarget.getHeight() / 2f;
            float dx = targetCenterX - impactX;
            float dy = targetCenterY - impactY;

            if (dx * dx + dy * dy <= MOURNING_DUST_AOE_RADIUS * MOURNING_DUST_AOE_RADIUS) {
                areaTarget.damage(damage);

                if (hitListener != null) {
                    hitListener.accept(new LocalCombatHit(
                            areaTarget.getId(),
                            targetCenterX,
                            targetCenterY,
                            damage
                    ));
                }
            }
        }
    }

    private void resolveMourningMainLanding(RangedProjectile projectile) {
        if (projectile == null || !mourningMainProjectiles.remove(projectile)) {
            return;
        }

        float landingX = projectile.getX();
        float landingY = projectile.getY();

        /*
         * Все четыре/восемь дополнительных снарядов получают
         * одну и ту же фактическую точку приземления основного.
         */
        for (PendingMourning pending : pendingMourning) {
            pending.landingX = landingX;
            pending.landingY = landingY;
            pending.landingResolved = true;
        }
    }

    private void registerMourningLanding(RangedProjectile projectile) {
        if (projectile == null || projectile.getDamageType() != DamageType.DUST) {
            return;
        }

        float x = projectile.getX();
        float y = projectile.getY();
        if (hasMourningLanding) {
            LightningArc arc = new LightningArc(
                    lastMourningLandingX, lastMourningLandingY, x, y,
                    0.35f,
                    getMourningLightningDamage(projectile)
            );
            mourningLightningArcs.add(arc);
            applyMourningLightningDamage(arc);
        }
        lastMourningLandingX = x;
        lastMourningLandingY = y;
        hasMourningLanding = true;
    }

    /**
     * Электрический урон разряда составляет 25% от базового урона
     * основного выстрела. Dust-снаряд наносит 50%, поэтому здесь
     * достаточно взять половину его урона.
     */
    private float getMourningLightningDamage(RangedProjectile dustProjectile) {
        if (dustProjectile == null) {
            return 0f;
        }
        return Math.max(0f, dustProjectile.getDamage() * 0.5f);
    }

    /**
     * Проверяет именно пересечение разряда с хитбоксом цели, а не
     * попадание только в её центр. Поэтому разряд наносит урон,
     * даже если проходит по краю объекта.
     */
    private void applyMourningLightningDamage(LightningArc arc) {
        if (arc == null || arc.getDamage() <= 0f) {
            return;
        }

        float minX = Math.min(arc.getX1(), arc.getX2()) - MOURNING_LIGHTNING_THICKNESS;
        float maxX = Math.max(arc.getX1(), arc.getX2()) + MOURNING_LIGHTNING_THICKNESS;
        float minY = Math.min(arc.getY1(), arc.getY2()) - MOURNING_LIGHTNING_THICKNESS;
        float maxY = Math.max(arc.getY1(), arc.getY2()) + MOURNING_LIGHTNING_THICKNESS;

        for (LocalCombatTarget target : combatSystem.getTargets()) {
            if (target == null || !target.isAlive()) {
                continue;
            }

            float left = target.getX();
            float right = target.getX() + target.getWidth();
            float top = target.getY();
            float bottom = target.getY() + target.getHeight();

            // Быстрый broad-phase тест.
            if (right < minX || left > maxX || bottom < minY || top > maxY) {
                continue;
            }

            if (segmentIntersectsExpandedRect(
                    arc.getX1(), arc.getY1(),
                    arc.getX2(), arc.getY2(),
                    left - MOURNING_LIGHTNING_THICKNESS,
                    top - MOURNING_LIGHTNING_THICKNESS,
                    right + MOURNING_LIGHTNING_THICKNESS,
                    bottom + MOURNING_LIGHTNING_THICKNESS)) {

                float hitX = Math.max(left, Math.min(right,
                        (arc.getX1() + arc.getX2()) * 0.5f));
                float hitY = Math.max(top, Math.min(bottom,
                        (arc.getY1() + arc.getY2()) * 0.5f));

                target.damage(arc.getDamage());

                if (hitListener != null) {
                    hitListener.accept(new LocalCombatHit(
                            target.getId(),
                            hitX,
                            hitY,
                            arc.getDamage()
                    ));
                }
            }
        }
    }

    private boolean segmentIntersectsExpandedRect(
            float x1, float y1, float x2, float y2,
            float left, float top, float right, float bottom) {

        float dx = x2 - x1;
        float dy = y2 - y1;
        // Используем стандартный slab-тест.
        float[] range = {0f, 1f};
        if (!clipAxis(x1, dx, left, right, range)) {
            return false;
        }
        if (!clipAxis(y1, dy, top, bottom, range)) {
            return false;
        }
        return range[0] <= range[1];
    }


    private boolean clipAxis(
            float start, float delta,
            float min, float max,
            float[] range) {

        if (Math.abs(delta) < 0.00001f) {
            return start >= min && start <= max;
        }

        float t1 = (min - start) / delta;
        float t2 = (max - start) / delta;
        if (t1 > t2) {
            float tmp = t1;
            t1 = t2;
            t2 = tmp;
        }

        range[0] = Math.max(range[0], t1);
        range[1] = Math.min(range[1], t2);
        return range[0] <= range[1];
    }

    public List<LightningArc> getMourningLightningArcs() {
        return mourningLightningArcs;
    }

    public void updateMourningLightningArcs(float deltaSeconds) {
        Iterator<LightningArc> it = mourningLightningArcs.iterator();
        while (it.hasNext()) {
            LightningArc arc = it.next();
            arc.remaining -= Math.max(0f, deltaSeconds);
            if (arc.remaining <= 0f) {
                it.remove();
            }
        }
    }

    /*
     * =====================================================
     * SOLAR EXPLOSIONS
     * =====================================================
     */
    public List<SolarExplosion> getSolarExplosions() {
        return solarExplosions;
    }

    public List<SolarExplosion> getStarExplosions() {
        return starExplosions;
    }

    public void updateSolarExplosions(
            float deltaSeconds
    ) {

        Iterator<SolarExplosion> iterator =
                solarExplosions.iterator();

        while (iterator.hasNext()) {

            SolarExplosion explosion =
                    iterator.next();

            explosion.update(
                    deltaSeconds
            );

            if (explosion.isFinished()) {

                iterator.remove();
            }
        }
    }

    public void updateStarExplosions(float deltaSeconds) {
        Iterator<SolarExplosion> iterator = starExplosions.iterator();
        while (iterator.hasNext()) {
            SolarExplosion explosion = iterator.next();
            explosion.update(deltaSeconds);
            if (explosion.isFinished()) iterator.remove();
        }
    }

    /*
     * =====================================================
     * CLEAR
     * =====================================================
     */
    public void clear() {

        projectiles.clear();
        pendingBursts.clear();
        projectileModes.clear();
        solarExplosions.clear();
        starExplosions.clear();
        mourningLightningArcs.clear();
        pendingMourning.clear();
        mourningMainProjectiles.clear();
        mourningDustDetonated.clear();
        hasMourningLanding = false;
    }

    public static class LightningArc {
        private final float x1, y1, x2, y2;
        private final float damage;
        private float remaining;

        private LightningArc(
                float x1, float y1, float x2, float y2,
                float duration, float damage) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.remaining = duration;
            this.damage = damage;
        }

        public float getX1() { return x1; }
        public float getY1() { return y1; }
        public float getX2() { return x2; }
        public float getDamage() { return damage; }
        public float getY2() { return y2; }
        public float getAlpha() { return Math.max(0f, Math.min(1f, remaining / 0.35f)); }
    }

    private static class PendingMourning {
        private final float angle;
        private final float damage;
        private final float delay;

        private float timer;
        private float landingX;
        private float landingY;
        private boolean landingResolved;

        private PendingMourning(
                float angle,
                float damage,
                float delay
        ) {
            this.angle = angle;
            this.damage = damage;
            this.delay = delay;
            this.timer = 0f;
            this.landingX = 0f;
            this.landingY = 0f;
            this.landingResolved = false;
        }
    }

    /*
     * =====================================================
     * PENDING BURST
     * =====================================================
     */
    private static class PendingBurst {

        private final float originX;
        private final float originY;

        private final float directionX;
        private final float directionY;

        private final float damage;

        private final float projectileSpeed;
        private final float projectileRange;

        private final boolean piercing;

        /*
         * null = обычное оружие
         * SOLAR/LUNAR = Ангельская погибель
         */
        private final AngelicDoomMode mode;
        private final DamageType damageType;
        private final float defenseIgnore;

        private int remainingShots;

        private float timer;

        private PendingBurst(
                float originX,
                float originY,
                float directionX,
                float directionY,
                float damage,
                float projectileSpeed,
                float projectileRange,
                boolean piercing,
                int remainingShots,
                AngelicDoomMode mode,
                DamageType damageType,
                float defenseIgnore
        ) {

            this.originX = originX;
            this.originY = originY;

            this.directionX = directionX;
            this.directionY = directionY;

            this.damage = damage;

            this.projectileSpeed =
                    projectileSpeed;

            this.projectileRange =
                    projectileRange;

            this.piercing =
                    piercing;

            this.remainingShots =
                    remainingShots;

            this.mode =
                    mode;
            this.damageType =
                    damageType != null ? damageType : DamageType.PHYSICAL;
            this.defenseIgnore =
                    Math.max(0f, defenseIgnore);

            this.timer = 0f;
        }
    }
}