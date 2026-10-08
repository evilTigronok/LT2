package game.avatar.test;

import game.avatar.Direction;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PlaceholderAssetGenerator {

    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 40;

    private static final int SHEET_WIDTH = FRAME_WIDTH * 8;
    private static final int SHEET_HEIGHT = FRAME_HEIGHT;

    /*
     * Палитра.
     */
    private static final Color SKIN =
            new Color(196, 139, 96);

    private static final Color SKIN_LIGHT =
            new Color(220, 164, 117);

    private static final Color SKIN_SHADOW =
            new Color(151, 101, 68);

    private static final Color HAIR =
            new Color(61, 39, 29);

    private static final Color HAIR_LIGHT =
            new Color(87, 55, 38);

    private static final Color SHIRT =
            new Color(35, 38, 42);

    private static final Color SHIRT_LIGHT =
            new Color(49, 53, 58);

    private static final Color PANTS =
            new Color(43, 78, 145);

    private static final Color PANTS_LIGHT =
            new Color(55, 96, 172);

    private static final Color PANTS_SHADOW =
            new Color(29, 54, 105);

    private static final Color SHOES =
            new Color(27, 28, 30);

    private static final Color SHOES_LIGHT =
            new Color(46, 47, 50);


    public static void main(String[] args) throws IOException {

        File outputDirectory = new File(
                "client/src/main/resources/assets/avatar/generated"
        );

        if (!outputDirectory.exists()
                && !outputDirectory.mkdirs()) {

            throw new IOException(
                    "Не удалось создать директорию: "
                            + outputDirectory.getAbsolutePath()
            );
        }

        BufferedImage idleSheet =
                createSheet(false);

        BufferedImage walkSheet =
                createSheet(true);


        File idleOutput = new File(
                outputDirectory,
                "character_idle.png"
        );

        File walkOutput = new File(
                outputDirectory,
                "character_walk_2.png"
        );


        ImageIO.write(
                idleSheet,
                "PNG",
                idleOutput
        );

        ImageIO.write(
                walkSheet,
                "PNG",
                walkOutput
        );


        System.out.println(
                "Generated: "
                        + idleOutput.getAbsolutePath()
        );

        System.out.println(
                "Generated: "
                        + walkOutput.getAbsolutePath()
        );


    }


    private static BufferedImage createSheet(
            boolean walk
    ) {

        BufferedImage sheet =
                new BufferedImage(
                        SHEET_WIDTH,
                        SHEET_HEIGHT,
                        BufferedImage.TYPE_INT_ARGB
                );

        Graphics2D g = sheet.createGraphics();

        configure(g);

        Direction[] directions =
                Direction.values();

        for (int i = 0;
             i < directions.length;
             i++) {

            drawCharacter(
                    g,
                    i * FRAME_WIDTH,
                    0,
                    directions[i],
                    walk
            );
        }

        g.dispose();

        return sheet;
    }


    private static void drawCharacter(
            Graphics2D g,
            int ox,
            int oy,
            Direction direction,
            boolean walk
    ) {

        switch (direction) {

            case DOWN ->
                    drawDown(
                            g,
                            ox,
                            oy,
                            walk
                    );

            case DOWN_RIGHT ->
                    drawDiagonalFront(
                            g,
                            ox,
                            oy,
                            false,
                            walk
                    );

            case RIGHT ->
                    drawRight(
                            g,
                            ox,
                            oy,
                            walk
                    );

            case UP_RIGHT ->
                    drawDiagonalBack(
                            g,
                            ox,
                            oy,
                            false,
                            walk
                    );

            case UP ->
                    drawUp(
                            g,
                            ox,
                            oy,
                            walk
                    );

            case UP_LEFT ->
                    drawDiagonalBack(
                            g,
                            ox,
                            oy,
                            true,
                            walk
                    );

            case LEFT ->
                    drawLeft(
                            g,
                            ox,
                            oy,
                            walk
                    );

            case DOWN_LEFT ->
                    drawDiagonalFront(
                            g,
                            ox,
                            oy,
                            true,
                            walk
                    );
        }
    }


    // =========================================================
    // DOWN
    // =========================================================

    private static void drawDown(
            Graphics2D g,
            int ox,
            int oy,
            boolean walk
    ) {

        /*
         * Волосы.
         */
        rect(
                g,
                HAIR,
                ox + 9,
                oy + 2,
                14,
                12
        );

        rect(
                g,
                HAIR_LIGHT,
                ox + 11,
                oy + 3,
                9,
                5
        );


        /*
         * Лицо.
         */
        rect(
                g,
                SKIN_LIGHT,
                ox + 11,
                oy + 6,
                10,
                8
        );

        rect(
                g,
                SKIN,
                ox + 10,
                oy + 10,
                12,
                5
        );


        /*
         * Уши.
         */
        rect(
                g,
                SKIN,
                ox + 8,
                oy + 8,
                3,
                5
        );

        rect(
                g,
                SKIN,
                ox + 21,
                oy + 8,
                3,
                5
        );


        /*
         * Шея.
         */
        rect(
                g,
                SKIN,
                ox + 13,
                oy + 14,
                6,
                3
        );


        /*
         * Плечи и торс.
         */
        rect(
                g,
                SHIRT,
                ox + 7,
                oy + 16,
                18,
                11
        );

        rect(
                g,
                SHIRT_LIGHT,
                ox + 10,
                oy + 17,
                12,
                3
        );


        /*
         * Руки.
         */
        drawArm(
                g,
                ox + 4,
                oy + 17,
                false
        );

        drawArm(
                g,
                ox + 23,
                oy + 17,
                true
        );


        /*
         * Таз.
         */
        rect(
                g,
                PANTS,
                ox + 8,
                oy + 26,
                16,
                6
        );


        /*
         * Ноги.
         */
        drawLeg(
                g,
                ox + 8,
                oy + 31
        );

        drawLeg(
                g,
                ox + 16,
                oy + 31
        );


        /*
         * Обувь.
         */
        drawFoot(
                g,
                ox + 5,
                oy + 37
        );

        drawFoot(
                g,
                ox + 17,
                oy + 37
        );
    }


    // =========================================================
    // RIGHT / LEFT
    // =========================================================

    private static void drawRight(
            Graphics2D g,
            int ox,
            int oy,
            boolean walk
    ) {

        drawSide(
                g,
                ox,
                oy,
                false
        );
    }


    private static void drawLeft(
            Graphics2D g,
            int ox,
            int oy,
            boolean walk
    ) {

        drawSide(
                g,
                ox,
                oy,
                true
        );
    }


    private static void drawSide(
            Graphics2D g,
            int ox,
            int oy,
            boolean mirror
    ) {

        int center = ox + 16;

        int dir = mirror ? -1 : 1;


        /*
         * Задняя часть волос.
         */
        rect(
                g,
                HAIR,
                center - 6,
                oy + 2,
                12,
                13
        );

        rect(
                g,
                HAIR_LIGHT,
                center - 4,
                oy + 3,
                8,
                5
        );


        /*
         * Лицо смещено в сторону взгляда.
         */
        rect(
                g,
                SKIN_LIGHT,
                center - 4 + dir * 2,
                oy + 6,
                8,
                8
        );

        rect(
                g,
                SKIN,
                center - 3 + dir * 3,
                oy + 10,
                7,
                5
        );


        /*
         * Нос.
         */
        rect(
                g,
                SKIN_LIGHT,
                center + dir * 6,
                oy + 9,
                2,
                3
        );


        /*
         * Ухо с дальней стороны головы.
         */
        rect(
                g,
                SKIN,
                center - 5,
                oy + 8,
                3,
                5
        );


        /*
         * Шея.
         */
        rect(
                g,
                SKIN,
                center - 3,
                oy + 14,
                6,
                3
        );


        /*
         * Корпус.
         */
        rect(
                g,
                SHIRT,
                center - 6,
                oy + 16,
                12,
                11
        );

        rect(
                g,
                SHIRT_LIGHT,
                center - 4,
                oy + 17,
                7,
                3
        );


        /*
         * Ближняя рука.
         */
        int armX =
                center + dir * 6 - 2;

        rect(
                g,
                SHIRT,
                armX,
                oy + 17,
                5,
                7
        );

        rect(
                g,
                SKIN_LIGHT,
                armX,
                oy + 23,
                5,
                7
        );


        /*
         * Таз.
         */
        rect(
                g,
                PANTS,
                center - 5,
                oy + 26,
                10,
                6
        );


        /*
         * Две ноги видны немного раздельно.
         */
        drawSideLeg(
                g,
                center - 4,
                oy + 31
        );

        drawSideLeg(
                g,
                center + 1,
                oy + 31
        );


        /*
         * Ступни направлены в сторону движения.
         */
        drawSideFoot(
                g,
                center - 4,
                oy + 37,
                dir
        );

        drawSideFoot(
                g,
                center + 1,
                oy + 37,
                dir
        );
    }


    private static void drawSideLeg(
            Graphics2D g,
            int x,
            int y
    ) {

        rect(
                g,
                PANTS,
                x,
                y,
                6,
                7
        );

        rect(
                g,
                PANTS_LIGHT,
                x + 1,
                y + 1,
                3,
                3
        );

        rect(
                g,
                PANTS_SHADOW,
                x + 4,
                y + 4,
                2,
                3
        );
    }


    private static void drawSideFoot(
            Graphics2D g,
            int x,
            int y,
            int direction
    ) {

        int footX =
                direction > 0
                        ? x
                        : x - 3;

        rect(
                g,
                SHOES,
                footX,
                y,
                8,
                3
        );

        rect(
                g,
                SHOES_LIGHT,
                footX + 1,
                y,
                5,
                1
        );
    }


    // =========================================================
    // UP
    // =========================================================

    private static void drawUp(
            Graphics2D g,
            int ox,
            int oy,
            boolean walk
    ) {

        /*
         * Затылок.
         */
        rect(
                g,
                HAIR,
                ox + 8,
                oy + 2,
                16,
                14
        );

        rect(
                g,
                HAIR_LIGHT,
                ox + 11,
                oy + 4,
                10,
                7
        );


        /*
         * Небольшая видимая шея.
         */
        rect(
                g,
                SKIN_SHADOW,
                ox + 13,
                oy + 14,
                6,
                3
        );


        /*
         * Спина.
         */
        rect(
                g,
                SHIRT,
                ox + 7,
                oy + 16,
                18,
                11
        );

        rect(
                g,
                SHIRT_LIGHT,
                ox + 10,
                oy + 17,
                12,
                3
        );


        /*
         * Руки.
         */
        drawBackArm(
                g,
                ox + 4,
                oy + 17
        );

        drawBackArm(
                g,
                ox + 23,
                oy + 17
        );


        /*
         * Таз.
         */
        rect(
                g,
                PANTS,
                ox + 8,
                oy + 26,
                16,
                6
        );


        /*
         * Ноги.
         */
        drawLeg(
                g,
                ox + 8,
                oy + 31
        );

        drawLeg(
                g,
                ox + 16,
                oy + 31
        );


        /*
         * Обувь.
         */
        drawFoot(
                g,
                ox + 5,
                oy + 37
        );

        drawFoot(
                g,
                ox + 17,
                oy + 37
        );
    }


    private static void drawBackArm(
            Graphics2D g,
            int x,
            int y
    ) {

        rect(
                g,
                SHIRT,
                x,
                y,
                5,
                8
        );

        rect(
                g,
                SKIN_SHADOW,
                x,
                y + 7,
                5,
                9
        );

        rect(
                g,
                SKIN,
                x + 1,
                y + 8,
                3,
                6
        );
    }


    // =========================================================
    // DIAGONAL FRONT
    // =========================================================

    private static void drawDiagonalFront(
            Graphics2D g,
            int ox,
            int oy,
            boolean mirror,
            boolean walk
    ) {

        int center = ox + 16;

        int dir = mirror ? -1 : 1;


        /*
         * Голова.
         */
        rect(
                g,
                HAIR,
                center - 7,
                oy + 2,
                14,
                14
        );

        rect(
                g,
                HAIR_LIGHT,
                center - 5,
                oy + 3,
                9,
                6
        );


        /*
         * Лицо смещено к направлению.
         */
        int faceX =
                center - 5 + dir * 2;

        rect(
                g,
                SKIN_LIGHT,
                faceX,
                oy + 6,
                9,
                8
        );

        rect(
                g,
                SKIN,
                faceX + dir,
                oy + 10,
                8,
                5
        );


        /*
         * Дальнее ухо.
         */
        rect(
                g,
                SKIN_SHADOW,
                center - 7 - dir,
                oy + 8,
                3,
                5
        );


        /*
         * Ближнее ухо.
         */
        rect(
                g,
                SKIN,
                center + 5 * dir,
                oy + 8,
                3,
                5
        );


        /*
         * Шея.
         */
        rect(
                g,
                SKIN,
                center - 3,
                oy + 14,
                6,
                3
        );


        /*
         * Торс.
         */
        rect(
                g,
                SHIRT,
                center - 8,
                oy + 16,
                16,
                11
        );

        rect(
                g,
                SHIRT_LIGHT,
                center - 5,
                oy + 17,
                9,
                3
        );


        /*
         * Дальняя рука.
         */
        rect(
                g,
                SKIN_SHADOW,
                center - 10 * dir,
                oy + 19,
                4,
                9
        );


        /*
         * Ближняя рука.
         */
        rect(
                g,
                SHIRT,
                center + 5 * dir,
                oy + 17,
                5,
                7
        );

        rect(
                g,
                SKIN_LIGHT,
                center + 5 * dir,
                oy + 23,
                5,
                7
        );


        /*
         * Таз.
         */
        rect(
                g,
                PANTS,
                center - 7,
                oy + 26,
                14,
                6
        );


        /*
         * Ноги.
         */
        drawDiagonalLeg(
                g,
                center - 5,
                oy + 31
        );

        drawDiagonalLeg(
                g,
                center + 1,
                oy + 31
        );


        /*
         * Обувь.
         */
        drawDiagonalFoot(
                g,
                center - 6,
                oy + 37,
                dir
        );

        drawDiagonalFoot(
                g,
                center + 1,
                oy + 37,
                dir
        );
    }


    // =========================================================
    // DIAGONAL BACK
    // =========================================================

    private static void drawDiagonalBack(
            Graphics2D g,
            int ox,
            int oy,
            boolean mirror,
            boolean walk
    ) {

        int center = ox + 16;

        int dir = mirror ? -1 : 1;


        /*
         * Затылок.
         */
        rect(
                g,
                HAIR,
                center - 7,
                oy + 2,
                14,
                15
        );

        rect(
                g,
                HAIR_LIGHT,
                center - 5,
                oy + 4,
                10,
                7
        );


        /*
         * Небольшой кусок боковой части лица.
         */
        rect(
                g,
                SKIN_SHADOW,
                center + 4 * dir,
                oy + 8,
                4,
                6
        );


        /*
         * Шея.
         */
        rect(
                g,
                SKIN_SHADOW,
                center - 3,
                oy + 14,
                6,
                3
        );


        /*
         * Спина.
         */
        rect(
                g,
                SHIRT,
                center - 8,
                oy + 16,
                16,
                11
        );

        rect(
                g,
                SHIRT_LIGHT,
                center - 5,
                oy + 17,
                9,
                3
        );


        /*
         * Дальняя рука.
         */
        rect(
                g,
                SKIN_SHADOW,
                center - 8 * dir,
                oy + 20,
                4,
                9
        );


        /*
         * Ближняя рука.
         */
        rect(
                g,
                SHIRT,
                center + 5 * dir,
                oy + 17,
                5,
                7
        );

        rect(
                g,
                SKIN_SHADOW,
                center + 5 * dir,
                oy + 23,
                5,
                7
        );


        /*
         * Таз.
         */
        rect(
                g,
                PANTS,
                center - 7,
                oy + 26,
                14,
                6
        );


        /*
         * Ноги.
         */
        drawDiagonalLeg(
                g,
                center - 5,
                oy + 31
        );

        drawDiagonalLeg(
                g,
                center + 1,
                oy + 31
        );


        /*
         * Обувь.
         */
        drawDiagonalFoot(
                g,
                center - 6,
                oy + 37,
                dir
        );

        drawDiagonalFoot(
                g,
                center + 1,
                oy + 37,
                dir
        );
    }


    // =========================================================
    // COMMON BODY PARTS
    // =========================================================

    private static void drawArm(
            Graphics2D g,
            int x,
            int y,
            boolean right
    ) {

        rect(
                g,
                SHIRT,
                x,
                y,
                5,
                8
        );

        rect(
                g,
                SKIN_LIGHT,
                x,
                y + 7,
                5,
                10
        );

        rect(
                g,
                SKIN,
                x + (right ? 1 : 0),
                y + 14,
                5,
                4
        );
    }


    private static void drawLeg(
            Graphics2D g,
            int x,
            int y
    ) {

        rect(
                g,
                PANTS,
                x,
                y,
                8,
                7
        );

        rect(
                g,
                PANTS_LIGHT,
                x + 1,
                y + 1,
                5,
                3
        );

        rect(
                g,
                PANTS_SHADOW,
                x + 5,
                y + 4,
                3,
                3
        );

        rect(
                g,
                PANTS,
                x,
                y + 6,
                8,
                4
        );
    }


    private static void drawDiagonalLeg(
            Graphics2D g,
            int x,
            int y
    ) {

        rect(
                g,
                PANTS,
                x,
                y,
                7,
                9
        );

        rect(
                g,
                PANTS_LIGHT,
                x + 1,
                y + 1,
                4,
                4
        );

        rect(
                g,
                PANTS_SHADOW,
                x + 4,
                y + 5,
                3,
                4
        );
    }


    private static void drawFoot(
            Graphics2D g,
            int x,
            int y
    ) {

        rect(
                g,
                SHOES,
                x,
                y,
                10,
                3
        );

        rect(
                g,
                SHOES_LIGHT,
                x + 1,
                y,
                7,
                1
        );
    }


    private static void drawDiagonalFoot(
            Graphics2D g,
            int x,
            int y,
            int direction
    ) {

        int footX =
                direction > 0
                        ? x
                        : x - 2;

        rect(
                g,
                SHOES,
                footX,
                y,
                9,
                3
        );
    }


    // =========================================================
    // UTILS
    // =========================================================

    private static void rect(
            Graphics2D g,
            Color color,
            int x,
            int y,
            int width,
            int height
    ) {

        g.setColor(color);

        g.fillRect(
                x,
                y,
                width,
                height
        );
    }


    private static void configure(
            Graphics2D g
    ) {

        g.setComposite(
                AlphaComposite.Src
        );

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF
        );

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        g.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED
        );
    }
}