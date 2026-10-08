package game.combat;

import game.weapon.AttackStyle;

/**
 * Единая геометрия боевого hitbox.
 *
 * Используется как для проверки попадания,
 * так и для визуализации hitbox на клиенте.
 */
public final class AttackHitboxGeometry {

    private static final int SECTOR_SEGMENTS = 24;
    private static final int CIRCLE_SEGMENTS = 32;

    private AttackHitboxGeometry() {
    }

    /**
     * Создаёт многоугольник hitbox в мировых координатах.
     *
     * Массив имеет формат:
     *
     * [x1, y1, x2, y2, x3, y3, ...]
     */
    public static float[] createPolygon(
            float originX,
            float originY,
            float directionX,
            float directionY,
            AttackStyle style
    ) {
        if (style == null) {
            style = AttackStyle.SWING;
        }

        float length = (float) Math.sqrt(
                directionX * directionX +
                        directionY * directionY
        );

        if (length < 0.0001f) {
            directionX = 0f;
            directionY = 1f;
        } else {
            directionX /= length;
            directionY /= length;
        }

        AttackHitboxDefinition definition =
                AttackHitboxDefinition.forStyle(style);

        return switch (definition.getShape()) {
            case RECTANGLE -> createRectangle(
                    originX,
                    originY,
                    directionX,
                    directionY,
                    definition
            );

            case SECTOR -> createSector(
                    originX,
                    originY,
                    directionX,
                    directionY,
                    definition
            );

            case CIRCLE -> createCircle(
                    originX,
                    originY,
                    directionX,
                    directionY,
                    definition
            );
        };
    }

    /**
     * Проверяет пересечение hitbox с прямоугольной целью.
     */
    public static boolean intersectsAabb(
            float originX,
            float originY,
            float directionX,
            float directionY,
            AttackStyle style,
            float targetX,
            float targetY,
            float targetWidth,
            float targetHeight
    ) {
        float[] attackPolygon = createPolygon(
                originX,
                originY,
                directionX,
                directionY,
                style
        );

        float[] targetPolygon = {
                targetX,
                targetY,

                targetX + targetWidth,
                targetY,

                targetX + targetWidth,
                targetY + targetHeight,

                targetX,
                targetY + targetHeight
        };

        return polygonsIntersect(
                attackPolygon,
                targetPolygon
        );
    }

    private static float[] createRectangle(
            float originX,
            float originY,
            float directionX,
            float directionY,
            AttackHitboxDefinition definition
    ) {
        float halfWidth = definition.getHalfWidth();
        float range = definition.getRange();

        float[] result = new float[8];

        // Левая ближняя точка
        setPoint(
                result,
                0,
                originX,
                originY,
                directionX,
                directionY,
                -halfWidth,
                0f
        );

        // Правая ближняя точка
        setPoint(
                result,
                1,
                originX,
                originY,
                directionX,
                directionY,
                halfWidth,
                0f
        );

        // Правая дальняя точка
        setPoint(
                result,
                2,
                originX,
                originY,
                directionX,
                directionY,
                halfWidth,
                range
        );

        // Левая дальняя точка
        setPoint(
                result,
                3,
                originX,
                originY,
                directionX,
                directionY,
                -halfWidth,
                range
        );

        return result;
    }

    private static float[] createSector(
            float originX,
            float originY,
            float directionX,
            float directionY,
            AttackHitboxDefinition definition
    ) {
        float range = definition.getRange();
        float halfAngle = definition.getHalfAngle();

        float[] result =
                new float[(SECTOR_SEGMENTS + 2) * 2];

        // Центр сектора
        result[0] = originX;
        result[1] = originY;

        for (int i = 0; i <= SECTOR_SEGMENTS; i++) {

            double angle =
                    -halfAngle +
                            (halfAngle * 2.0) *
                                    i / SECTOR_SEGMENTS;

            double radians =
                    Math.toRadians(angle);

            float side =
                    (float) Math.sin(radians) * range;

            float forward =
                    (float) Math.cos(radians) * range;

            setPoint(
                    result,
                    i + 1,
                    originX,
                    originY,
                    directionX,
                    directionY,
                    side,
                    forward
            );
        }

        return result;
    }

    private static float[] createCircle(
            float originX,
            float originY,
            float directionX,
            float directionY,
            AttackHitboxDefinition definition
    ) {
        float radius = definition.getRadius();

        float[] result =
                new float[CIRCLE_SEGMENTS * 2];

        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {

            double angle =
                    (Math.PI * 2.0 * i) /
                            CIRCLE_SEGMENTS;

            float side =
                    (float) Math.sin(angle) * radius;

            float forward =
                    (float) Math.cos(angle) * radius;

            setPoint(
                    result,
                    i,
                    originX,
                    originY,
                    directionX,
                    directionY,
                    side,
                    forward
            );
        }

        return result;
    }

    /**
     * Преобразует локальную координату hitbox
     * в мировую.
     *
     * forward — направление атаки.
     * side    — смещение влево/вправо.
     */
    private static void setPoint(
            float[] result,
            int index,
            float originX,
            float originY,
            float directionX,
            float directionY,
            float side,
            float forward
    ) {
        float worldX =
                originX +
                        directionX * forward -
                        directionY * side;

        float worldY =
                originY +
                        directionY * forward +
                        directionX * side;

        result[index * 2] = worldX;
        result[index * 2 + 1] = worldY;
    }

    /**
     * SAT — Separating Axis Theorem.
     *
     * Проверяет пересечение двух выпуклых многоугольников.
     */
    private static boolean polygonsIntersect(
            float[] first,
            float[] second
    ) {
        if (hasSeparatingAxis(first, second)) {
            return false;
        }

        return !hasSeparatingAxis(second, first);
    }

    private static boolean hasSeparatingAxis(
            float[] polygonA,
            float[] polygonB
    ) {
        int points = polygonA.length / 2;

        for (int i = 0; i < points; i++) {

            int next =
                    (i + 1) % points;

            float x1 = polygonA[i * 2];
            float y1 = polygonA[i * 2 + 1];

            float x2 = polygonA[next * 2];
            float y2 = polygonA[next * 2 + 1];

            float edgeX = x2 - x1;
            float edgeY = y2 - y1;

            // Перпендикуляр к ребру
            float axisX = -edgeY;
            float axisY = edgeX;

            float axisLength =
                    (float) Math.sqrt(
                            axisX * axisX +
                                    axisY * axisY
                    );

            if (axisLength < 0.0001f) {
                continue;
            }

            axisX /= axisLength;
            axisY /= axisLength;

            float minA = Float.POSITIVE_INFINITY;
            float maxA = Float.NEGATIVE_INFINITY;

            for (int j = 0; j < polygonA.length; j += 2) {

                float projection =
                        polygonA[j] * axisX +
                                polygonA[j + 1] * axisY;

                minA = Math.min(minA, projection);
                maxA = Math.max(maxA, projection);
            }

            float minB = Float.POSITIVE_INFINITY;
            float maxB = Float.NEGATIVE_INFINITY;

            for (int j = 0; j < polygonB.length; j += 2) {

                float projection =
                        polygonB[j] * axisX +
                                polygonB[j + 1] * axisY;

                minB = Math.min(minB, projection);
                maxB = Math.max(maxB, projection);
            }

            // Касание границ считаем попаданием.
            if (maxA < minB || maxB < minA) {
                return true;
            }
        }

        return false;
    }
}