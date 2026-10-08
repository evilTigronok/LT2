package game.avatar;

import java.awt.image.BufferedImage;

public class AvatarLayerExtractor {

    public enum SourcePart {

        HEAD,

        LEFT_EAR,
        RIGHT_EAR,

        NECK,

        LEFT_UPPER_ARM,
        LEFT_FOREARM,
        LEFT_HAND,

        RIGHT_UPPER_ARM,
        RIGHT_FOREARM,
        RIGHT_HAND,

        LEFT_LEG,
        RIGHT_LEG,

        LEFT_FOOT,
        RIGHT_FOOT,

        SHIRT,

        PANTS_PELVIS,
        PANTS_LEFT_LEG,
        PANTS_RIGHT_LEG,

        SHOES_LEFT,
        SHOES_RIGHT,

        HAIR
    }


    /*
     * =========================================================
     * SOURCE GEOMETRY
     * =========================================================
     *
     * Исходные placeholder-ассеты имеют размер 24×32.
     *
     * В отличие от старой версии, здесь каждый SourceRect
     * определяет область, а maskPixel() дополнительно удаляет
     * пиксели, которые принадлежат соседней части.
     */


    private static final SourceRect HEAD =
            new SourceRect(8, 1, 8, 8);


    private static final SourceRect LEFT_EAR =
            new SourceRect(3, 10, 4, 7);

    private static final SourceRect RIGHT_EAR =
            new SourceRect(18, 10, 4, 7);


    private static final SourceRect NECK =
            new SourceRect(10, 9, 4, 8);


    private static final SourceRect LEFT_UPPER_ARM =
            new SourceRect(3, 10, 4, 7);

    private static final SourceRect LEFT_FOREARM =
            new SourceRect(1, 17, 4, 2);

    private static final SourceRect LEFT_HAND =
            new SourceRect(1, 17, 6, 2);


    private static final SourceRect RIGHT_UPPER_ARM =
            new SourceRect(18, 10, 4, 7);

    private static final SourceRect RIGHT_FOREARM =
            new SourceRect(19, 17, 4, 2);

    private static final SourceRect RIGHT_HAND =
            new SourceRect(18, 17, 6, 2);


    private static final SourceRect LEFT_LEG =
            new SourceRect(6, 20, 5, 9);

    private static final SourceRect RIGHT_LEG =
            new SourceRect(13, 20, 5, 9);


    private static final SourceRect LEFT_FOOT =
            new SourceRect(5, 29, 7, 3);

    private static final SourceRect RIGHT_FOOT =
            new SourceRect(12, 29, 7, 3);


    private static final SourceRect SHIRT =
            new SourceRect(3, 9, 18, 10);


    private static final SourceRect PANTS_PELVIS =
            new SourceRect(7, 18, 10, 4);

    private static final SourceRect PANTS_LEFT_LEG =
            new SourceRect(6, 20, 6, 9);

    private static final SourceRect PANTS_RIGHT_LEG =
            new SourceRect(12, 20, 6, 9);


    private static final SourceRect SHOES_LEFT =
            new SourceRect(5, 29, 7, 3);

    private static final SourceRect SHOES_RIGHT =
            new SourceRect(12, 29, 7, 3);


    private static final SourceRect HAIR =
            new SourceRect(7, 0, 10, 10);


    // =========================================================
    // PUBLIC API
    // =========================================================

    public BufferedImage extract(
            BufferedImage source,
            SourcePart part
    ) {

        if (source == null || part == null) {
            return null;
        }

        SourceRect rect = getSourceRect(part);

        if (rect == null) {
            return null;
        }

        int startX = Math.max(0, rect.x());
        int startY = Math.max(0, rect.y());

        int endX = Math.min(
                source.getWidth(),
                rect.x() + rect.width()
        );

        int endY = Math.min(
                source.getHeight(),
                rect.y() + rect.height()
        );

        if (endX <= startX || endY <= startY) {
            return null;
        }

        int width =
                endX - startX;

        int height =
                endY - startY;

        BufferedImage result =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_ARGB
                );


        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                int sourceX =
                        startX + x;

                int sourceY =
                        startY + y;

                int argb =
                        source.getRGB(
                                sourceX,
                                sourceY
                        );


                /*
                 * Полностью прозрачный пиксель
                 * сразу пропускаем.
                 */
                if (((argb >>> 24) & 0xFF) == 0) {
                    continue;
                }


                /*
                 * Маска конкретной части.
                 */
                if (!maskPixel(
                        sourceX,
                        sourceY,
                        part
                )) {
                    continue;
                }


                result.setRGB(
                        x,
                        y,
                        argb
                );
            }
        }

        return result;
    }


    // =========================================================
    // PIXEL MASK
    // =========================================================

    private boolean maskPixel(
            int x,
            int y,
            SourcePart part
    ) {

        return switch (part) {

            case HEAD ->
                    isHeadPixel(x, y);

            case LEFT_EAR ->
                    isLeftEarPixel(x, y);

            case RIGHT_EAR ->
                    isRightEarPixel(x, y);

            case NECK ->
                    isNeckPixel(x, y);


            case LEFT_UPPER_ARM ->
                    isLeftUpperArmPixel(x, y);

            case LEFT_FOREARM ->
                    isLeftForearmPixel(x, y);

            case LEFT_HAND ->
                    isLeftHandPixel(x, y);


            case RIGHT_UPPER_ARM ->
                    isRightUpperArmPixel(x, y);

            case RIGHT_FOREARM ->
                    isRightForearmPixel(x, y);

            case RIGHT_HAND ->
                    isRightHandPixel(x, y);


            case LEFT_LEG ->
                    isLeftLegPixel(x, y);

            case RIGHT_LEG ->
                    isRightLegPixel(x, y);


            case LEFT_FOOT ->
                    isLeftFootPixel(x, y);

            case RIGHT_FOOT ->
                    isRightFootPixel(x, y);


            case SHIRT ->
                    isShirtPixel(x, y);


            case PANTS_PELVIS ->
                    isPantsPelvisPixel(x, y);

            case PANTS_LEFT_LEG ->
                    isLeftPantsLegPixel(x, y);

            case PANTS_RIGHT_LEG ->
                    isRightPantsLegPixel(x, y);


            case SHOES_LEFT ->
                    isLeftShoePixel(x, y);

            case SHOES_RIGHT ->
                    isRightShoePixel(x, y);


            case HAIR ->
                    isHairPixel(x, y);
        };
    }


    // =========================================================
    // HEAD
    // =========================================================

    private boolean isHeadPixel(
            int x,
            int y
    ) {

        /*
         * Голова:
         *
         *       ########
         *       ########
         *       ########
         *       ########
         *       ########
         *       ########
         *       ########
         *       ########
         *
         * Оставляем только центральную часть.
         */

        return x >= 8
                && x <= 15
                && y >= 1
                && y <= 8;
    }


    // =========================================================
    // EARS
    // =========================================================

    private boolean isLeftEarPixel(
            int x,
            int y
    ) {

        return x >= 3
                && x <= 6
                && y >= 10
                && y <= 16;
    }


    private boolean isRightEarPixel(
            int x,
            int y
    ) {

        return x >= 18
                && x <= 21
                && y >= 10
                && y <= 16;
    }


    // =========================================================
    // NECK
    // =========================================================

    private boolean isNeckPixel(
            int x,
            int y
    ) {

        return x >= 10
                && x <= 13
                && y >= 9
                && y <= 16;
    }


    // =========================================================
    // LEFT ARM
    // =========================================================

    private boolean isLeftUpperArmPixel(
            int x,
            int y
    ) {

        return x >= 3
                && x <= 6
                && y >= 10
                && y <= 16;
    }


    private boolean isLeftForearmPixel(
            int x,
            int y
    ) {

        return x >= 1
                && x <= 4
                && y >= 17
                && y <= 18;
    }


    private boolean isLeftHandPixel(
            int x,
            int y
    ) {

        return x >= 1
                && x <= 6
                && y >= 17
                && y <= 18;
    }


    // =========================================================
    // RIGHT ARM
    // =========================================================

    private boolean isRightUpperArmPixel(
            int x,
            int y
    ) {

        return x >= 18
                && x <= 21
                && y >= 10
                && y <= 16;
    }


    private boolean isRightForearmPixel(
            int x,
            int y
    ) {

        return x >= 19
                && x <= 22
                && y >= 17
                && y <= 18;
    }


    private boolean isRightHandPixel(
            int x,
            int y
    ) {

        return x >= 18
                && x <= 23
                && y >= 17
                && y <= 18;
    }


    // =========================================================
    // LEGS
    // =========================================================

    private boolean isLeftLegPixel(
            int x,
            int y
    ) {

        return x >= 6
                && x <= 10
                && y >= 20
                && y <= 28;
    }


    private boolean isRightLegPixel(
            int x,
            int y
    ) {

        return x >= 13
                && x <= 17
                && y >= 20
                && y <= 28;
    }


    // =========================================================
    // FEET
    // =========================================================

    private boolean isLeftFootPixel(
            int x,
            int y
    ) {

        return x >= 5
                && x <= 11
                && y >= 29
                && y <= 31;
    }


    private boolean isRightFootPixel(
            int x,
            int y
    ) {

        return x >= 12
                && x <= 18
                && y >= 29
                && y <= 31;
    }


    // =========================================================
    // SHIRT
    // =========================================================

    private boolean isShirtPixel(
            int x,
            int y
    ) {

        /*
         * Центральная область рубашки.
         *
         * Руки сюда не попадают.
         */

        if (x < 3 || x > 20) {
            return false;
        }

        if (y < 9 || y > 18) {
            return false;
        }


        /*
         * Вырезаем область шеи.
         */
        if (x >= 9
                && x <= 14
                && y == 9) {

            return false;
        }

        return true;
    }


    // =========================================================
    // PANTS
    // =========================================================

    private boolean isPantsPelvisPixel(
            int x,
            int y
    ) {

        return x >= 7
                && x <= 16
                && y >= 18
                && y <= 21;
    }


    private boolean isLeftPantsLegPixel(
            int x,
            int y
    ) {

        return x >= 6
                && x <= 11
                && y >= 20
                && y <= 28;
    }


    private boolean isRightPantsLegPixel(
            int x,
            int y
    ) {

        return x >= 12
                && x <= 17
                && y >= 20
                && y <= 28;
    }


    // =========================================================
    // SHOES
    // =========================================================

    private boolean isLeftShoePixel(
            int x,
            int y
    ) {

        return x >= 5
                && x <= 11
                && y >= 29
                && y <= 31;
    }


    private boolean isRightShoePixel(
            int x,
            int y
    ) {

        return x >= 12
                && x <= 18
                && y >= 29
                && y <= 31;
    }


    // =========================================================
    // HAIR
    // =========================================================

    private boolean isHairPixel(
            int x,
            int y
    ) {

        /*
         * Волосы:
         *
         *       ##########
         *       ##########
         *       ##########
         *       ##########
         *       ##########
         *       ##########
         *       ##########
         *       ##########
         *       ##########
         *       ##########
         *
         * Сохраняем всю область hair asset.
         */

        return x >= 7
                && x <= 16
                && y >= 0
                && y <= 9;
    }


    // =========================================================
    // SOURCE RECT
    // =========================================================

    private SourceRect getSourceRect(
            SourcePart part
    ) {

        return switch (part) {

            case HEAD ->
                    HEAD;

            case LEFT_EAR ->
                    LEFT_EAR;

            case RIGHT_EAR ->
                    RIGHT_EAR;

            case NECK ->
                    NECK;


            case LEFT_UPPER_ARM ->
                    LEFT_UPPER_ARM;

            case LEFT_FOREARM ->
                    LEFT_FOREARM;

            case LEFT_HAND ->
                    LEFT_HAND;


            case RIGHT_UPPER_ARM ->
                    RIGHT_UPPER_ARM;

            case RIGHT_FOREARM ->
                    RIGHT_FOREARM;

            case RIGHT_HAND ->
                    RIGHT_HAND;


            case LEFT_LEG ->
                    LEFT_LEG;

            case RIGHT_LEG ->
                    RIGHT_LEG;


            case LEFT_FOOT ->
                    LEFT_FOOT;

            case RIGHT_FOOT ->
                    RIGHT_FOOT;


            case SHIRT ->
                    SHIRT;


            case PANTS_PELVIS ->
                    PANTS_PELVIS;

            case PANTS_LEFT_LEG ->
                    PANTS_LEFT_LEG;

            case PANTS_RIGHT_LEG ->
                    PANTS_RIGHT_LEG;


            case SHOES_LEFT ->
                    SHOES_LEFT;

            case SHOES_RIGHT ->
                    SHOES_RIGHT;


            case HAIR ->
                    HAIR;
        };
    }


    private record SourceRect(
            int x,
            int y,
            int width,
            int height
    ) {
    }
}