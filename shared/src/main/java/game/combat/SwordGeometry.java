package game.combat;

/**
 * Геометрия меча.
 *
 * Проверяет столкновение сегмента меча
 * с AABB цели.
 */
public final class SwordGeometry {

    private SwordGeometry() {
    }

    /**
     * Проверяет пересечение меча с прямоугольной целью.
     *
     * Меч рассматривается как отрезок с небольшой толщиной.
     */
    public static boolean intersectsAabb(
            SwordSwing swing,
            float targetX,
            float targetY,
            float targetWidth,
            float targetHeight
    ) {

        if (swing == null) {
            return false;
        }

        float startX =
                swing.getStartX();

        float startY =
                swing.getStartY();

        float endX =
                swing.getEndX();

        float endY =
                swing.getEndY();

        float thickness =
                SwordSwing.SWORD_THICKNESS;

        /*
         * Сначала расширяем AABB цели на половину
         * толщины меча.
         *
         * Это превращает collision сегмента
         * в более удобную проверку линии.
         */
        float half =
                thickness / 2f;

        float minX =
                targetX - half;

        float maxX =
                targetX +
                        targetWidth +
                        half;

        float minY =
                targetY - half;

        float maxY =
                targetY +
                        targetHeight +
                        half;

        /*
         * Если начало или конец меча уже
         * находится внутри расширенного AABB.
         */
        if (pointInside(
                startX,
                startY,
                minX,
                minY,
                maxX,
                maxY
        )) {
            return true;
        }

        if (pointInside(
                endX,
                endY,
                minX,
                minY,
                maxX,
                maxY
        )) {
            return true;
        }

        /*
         * Проверяем пересечение сегмента меча
         * с четырьмя сторонами AABB.
         */
        if (segmentsIntersect(
                startX,
                startY,
                endX,
                endY,
                minX,
                minY,
                maxX,
                minY
        )) {
            return true;
        }

        if (segmentsIntersect(
                startX,
                startY,
                endX,
                endY,
                maxX,
                minY,
                maxX,
                maxY
        )) {
            return true;
        }

        if (segmentsIntersect(
                startX,
                startY,
                endX,
                endY,
                maxX,
                maxY,
                minX,
                maxY
        )) {
            return true;
        }

        return segmentsIntersect(
                startX,
                startY,
                endX,
                endY,
                minX,
                maxY,
                minX,
                minY
        );
    }

    private static boolean pointInside(
            float x,
            float y,
            float minX,
            float minY,
            float maxX,
            float maxY
    ) {
        return x >= minX &&
                x <= maxX &&
                y >= minY &&
                y <= maxY;
    }

    private static boolean segmentsIntersect(
            float x1,
            float y1,
            float x2,
            float y2,
            float x3,
            float y3,
            float x4,
            float y4
    ) {

        float d1 =
                direction(
                        x3,
                        y3,
                        x4,
                        y4,
                        x1,
                        y1
                );

        float d2 =
                direction(
                        x3,
                        y3,
                        x4,
                        y4,
                        x2,
                        y2
                );

        float d3 =
                direction(
                        x1,
                        y1,
                        x2,
                        y2,
                        x3,
                        y3
                );

        float d4 =
                direction(
                        x1,
                        y1,
                        x2,
                        y2,
                        x4,
                        y4
                );

        if (
                ((d1 > 0f && d2 < 0f) ||
                        (d1 < 0f && d2 > 0f)) &&
                        ((d3 > 0f && d4 < 0f) ||
                                (d3 < 0f && d4 > 0f))
        ) {
            return true;
        }

        if (Math.abs(d1) < 0.0001f &&
                onSegment(
                        x3,
                        y3,
                        x4,
                        y4,
                        x1,
                        y1
                )) {
            return true;
        }

        if (Math.abs(d2) < 0.0001f &&
                onSegment(
                        x3,
                        y3,
                        x4,
                        y4,
                        x2,
                        y2
                )) {
            return true;
        }

        if (Math.abs(d3) < 0.0001f &&
                onSegment(
                        x1,
                        y1,
                        x2,
                        y2,
                        x3,
                        y3
                )) {
            return true;
        }

        return Math.abs(d4) < 0.0001f &&
                onSegment(
                        x1,
                        y1,
                        x2,
                        y2,
                        x4,
                        y4
                );
    }

    private static float direction(
            float ax,
            float ay,
            float bx,
            float by,
            float px,
            float py
    ) {
        return
                (px - ax) * (by - ay) -
                        (py - ay) * (bx - ax);
    }

    private static boolean onSegment(
            float ax,
            float ay,
            float bx,
            float by,
            float px,
            float py
    ) {
        return
                px >= Math.min(ax, bx) - 0.0001f &&
                        px <= Math.max(ax, bx) + 0.0001f &&
                        py >= Math.min(ay, by) - 0.0001f &&
                        py <= Math.max(ay, by) + 0.0001f;
    }
}