package game.avatar;

import game.avatar.asset.AccessoryAsset;
import game.avatar.asset.AssetImageLoader;
import game.avatar.asset.BodyAsset;
import game.avatar.asset.ClothingAsset;
import game.avatar.asset.HairAsset;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

public class AvatarAssetComposer {

    private static final int WIDTH =
            PixelCharacterTemplate.WIDTH;

    private static final int HEIGHT =
            PixelCharacterTemplate.HEIGHT;


    private final AssetImageLoader imageLoader;

    private final AvatarLayerExtractor extractor;


    public AvatarAssetComposer() {

        imageLoader =
                new AssetImageLoader();

        extractor =
                new AvatarLayerExtractor();
    }


    /*
     * Основной метод.
     */
    public BufferedImage compose(
            PixelCharacter character,
            PixelPose pose,
            TransformedTemplate template
    ) {

        BufferedImage result =
                new BufferedImage(
                        WIDTH,
                        HEIGHT,
                        BufferedImage.TYPE_INT_ARGB
                );

        Graphics2D g =
                result.createGraphics();

        configure(g);


        Direction direction =
                pose != null
                        ? pose.getDirection()
                        : Direction.DOWN;


        /*
         * =====================================================
         * 1. BODY
         * =====================================================
         */

        drawBody(
                g,
                character.getBody(),
                template
        );


        /*
         * =====================================================
         * 2. PANTS
         * =====================================================
         */

        drawPants(
                g,
                character.getPants(),
                template
        );


        /*
         * =====================================================
         * 3. SHIRT
         * =====================================================
         */

        drawShirt(
                g,
                character.getShirt(),
                template
        );


        /*
         * =====================================================
         * 4. OUTERWEAR
         * =====================================================
         *
         * Пока outerwear использует ту же схему,
         * что и shirt.
         */

        drawShirt(
                g,
                character.getOuterwear(),
                template
        );


        /*
         * =====================================================
         * 5. SHOES
         * =====================================================
         */

        drawShoes(
                g,
                character.getShoes(),
                template
        );


        /*
         * =====================================================
         * 6. HAIR
         * =====================================================
         */

        drawHair(
                g,
                character.getHair(),
                template,
                direction
        );


        /*
         * =====================================================
         * 7. ACCESSORIES
         * =====================================================
         */

        drawAccessories(
                g,
                character.getAccessories(),
                template,
                direction
        );


        g.dispose();

        return result;
    }


    /*
     * Совместимость со старым вызовом.
     */
    public BufferedImage compose(
            PixelCharacter character,
            TransformedTemplate template
    ) {

        Direction direction =
                template.getDirection();

        PixelPose pose =
                PixelPose.idle(direction);

        return compose(
                character,
                pose,
                template
        );
    }


    // =========================================================
    // BODY
    // =========================================================

    private void drawBody(
            Graphics2D g,
            BodyAsset body,
            TransformedTemplate template
    ) {

        if (body == null) {
            return;
        }

        BufferedImage image =
                imageLoader.load(
                        body.getSpritePath()
                );

        if (image == null) {
            return;
        }


        /*
         * Голова.
         */
        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.HEAD,
                PixelPart.HEAD,
                template
        );


        /*
         * Уши.
         */
        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.LEFT_EAR,
                PixelPart.LEFT_EAR,
                template
        );

        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.RIGHT_EAR,
                PixelPart.RIGHT_EAR,
                template
        );


        /*
         * Шея.
         */
        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.NECK,
                PixelPart.NECK,
                template
        );


        /*
         * Левая рука.
         */
        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.LEFT_UPPER_ARM,
                PixelPart.LEFT_UPPER_ARM,
                template
        );

        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.LEFT_FOREARM,
                PixelPart.LEFT_FOREARM,
                template
        );

        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.LEFT_HAND,
                PixelPart.LEFT_HAND,
                template
        );


        /*
         * Правая рука.
         */
        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.RIGHT_UPPER_ARM,
                PixelPart.RIGHT_UPPER_ARM,
                template
        );

        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.RIGHT_FOREARM,
                PixelPart.RIGHT_FOREARM,
                template
        );

        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.RIGHT_HAND,
                PixelPart.RIGHT_HAND,
                template
        );


        /*
         * Левая нога.
         */
        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.LEFT_LEG,
                PixelPart.LEFT_THIGH,
                template
        );


        /*
         * Правая нога.
         */
        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.RIGHT_LEG,
                PixelPart.RIGHT_THIGH,
                template
        );


        /*
         * Ступни.
         */
        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.LEFT_FOOT,
                PixelPart.LEFT_FOOT,
                template
        );

        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.RIGHT_FOOT,
                PixelPart.RIGHT_FOOT,
                template
        );
    }


    // =========================================================
    // SHIRT
    // =========================================================

    private void drawShirt(
            Graphics2D g,
            ClothingAsset clothing,
            TransformedTemplate template
    ) {

        if (clothing == null) {
            return;
        }

        if (clothing.getSpritePath() == null) {
            return;
        }

        BufferedImage image =
                imageLoader.load(
                        clothing.getSpritePath()
                );

        if (image == null) {
            return;
        }

        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.SHIRT,
                PixelPart.TORSO,
                template
        );

        /*
         * Нижняя часть рубашки немного
         * продолжает корпус.
         */
        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.SHIRT,
                PixelPart.WAIST,
                template
        );
    }


    // =========================================================
    // PANTS
    // =========================================================

    private void drawPants(
            Graphics2D g,
            ClothingAsset pants,
            TransformedTemplate template
    ) {

        if (pants == null) {
            return;
        }

        BufferedImage image =
                imageLoader.load(
                        pants.getSpritePath()
                );

        if (image == null) {
            return;
        }


        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.PANTS_PELVIS,
                PixelPart.PELVIS,
                template
        );


        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.PANTS_LEFT_LEG,
                PixelPart.LEFT_THIGH,
                template
        );

        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.PANTS_LEFT_LEG,
                PixelPart.LEFT_KNEE,
                template
        );

        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.PANTS_LEFT_LEG,
                PixelPart.LEFT_SHIN,
                template
        );


        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.PANTS_RIGHT_LEG,
                PixelPart.RIGHT_THIGH,
                template
        );

        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.PANTS_RIGHT_LEG,
                PixelPart.RIGHT_KNEE,
                template
        );

        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.PANTS_RIGHT_LEG,
                PixelPart.RIGHT_SHIN,
                template
        );
    }


    // =========================================================
    // SHOES
    // =========================================================

    private void drawShoes(
            Graphics2D g,
            ClothingAsset shoes,
            TransformedTemplate template
    ) {

        if (shoes == null) {
            return;
        }

        BufferedImage image =
                imageLoader.load(
                        shoes.getSpritePath()
                );

        if (image == null) {
            return;
        }


        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.SHOES_LEFT,
                PixelPart.LEFT_FOOT,
                template
        );

        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.SHOES_RIGHT,
                PixelPart.RIGHT_FOOT,
                template
        );
    }


    // =========================================================
    // HAIR
    // =========================================================

    private void drawHair(
            Graphics2D g,
            HairAsset hair,
            TransformedTemplate template,
            Direction direction
    ) {

        if (hair == null) {
            return;
        }

        BufferedImage image =
                imageLoader.load(
                        hair.getSpritePath()
                );

        if (image == null) {
            return;
        }


        /*
         * Волосы находятся поверх головы.
         */
        PixelPart targetPart =
                direction == Direction.UP
                        || direction == Direction.UP_LEFT
                        || direction == Direction.UP_RIGHT
                        ? PixelPart.HAIR_BACK
                        : PixelPart.HAIR;


        draw(
                g,
                image,
                AvatarLayerExtractor.SourcePart.HAIR,
                targetPart,
                template
        );
    }


    // =========================================================
    // ACCESSORIES
    // =========================================================

    private void drawAccessories(
            Graphics2D g,
            List<AccessoryAsset> accessories,
            TransformedTemplate template,
            Direction direction
    ) {

        if (accessories == null) {
            return;
        }


        /*
         * На данный момент аксессуары остаются
         * привязанными к голове.
         *
         * Более точное направление аксессуаров
         * сделаем после базовой геометрии.
         */

        for (AccessoryAsset accessory :
                accessories) {

            if (accessory == null) {
                continue;
            }

            BufferedImage image =
                    imageLoader.load(
                            accessory.getSpritePath()
                    );

            if (image == null) {
                continue;
            }


            switch (accessory.getType()) {

                case EARRING -> {

                    drawFullHeadAccessory(
                            g,
                            image,
                            template
                    );
                }

                case NECKLACE -> {

                    draw(
                            g,
                            image,
                            AvatarLayerExtractor.SourcePart.SHIRT,
                            PixelPart.NECK,
                            template
                    );
                }

                default -> {

                    drawFullHeadAccessory(
                            g,
                            image,
                            template
                    );
                }
            }
        }
    }


    private void drawFullHeadAccessory(
            Graphics2D g,
            BufferedImage image,
            TransformedTemplate template
    ) {

        TransformedPart head =
                template.getPart(
                        PixelPart.HEAD
                );

        if (head == null) {
            return;
        }

        PixelRect bounds =
                head.bounds();

        drawImage(
                g,
                image,
                bounds,
                head.mirrored()
        );
    }


    // =========================================================
    // DRAW PART
    // =========================================================

    private void draw(
            Graphics2D g,
            BufferedImage source,
            AvatarLayerExtractor.SourcePart sourcePart,
            PixelPart targetPart,
            TransformedTemplate template
    ) {

        TransformedPart transformed =
                template.getPart(targetPart);

        if (transformed == null) {
            return;
        }


        BufferedImage layer =
                extractor.extract(
                        source,
                        sourcePart
                );

        if (layer == null) {
            return;
        }


        drawImage(
                g,
                layer,
                transformed.bounds(),
                transformed.mirrored()
        );
    }


    // =========================================================
    // IMAGE TRANSFORM
    // =========================================================

    private void drawImage(
            Graphics2D g,
            BufferedImage image,
            PixelRect bounds,
            boolean mirrored
    ) {

        if (image == null) {
            return;
        }

        int x =
                (int) Math.round(
                        bounds.x()
                );

        int y =
                (int) Math.round(
                        bounds.y()
                );

        int width =
                Math.max(
                        1,
                        (int) Math.round(
                                bounds.width()
                        )
                );

        int height =
                Math.max(
                        1,
                        (int) Math.round(
                                bounds.height()
                        )
                );


        if (!mirrored) {

            g.drawImage(
                    image,
                    x,
                    y,
                    width,
                    height,
                    null
            );

            return;
        }


        /*
         * Горизонтальное зеркало непосредственно
         * внутри target bounds.
         */
        AffineTransform transform =
                new AffineTransform();

        transform.translate(
                x + width,
                y
        );

        transform.scale(
                -((double) width / image.getWidth()),
                (double) height / image.getHeight()
        );

        g.drawImage(
                image,
                transform,
                null
        );
    }


    // =========================================================
    // GRAPHICS
    // =========================================================

    private void configure(
            Graphics2D g
    ) {

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

        g.setRenderingHint(
                RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED
        );
    }
}