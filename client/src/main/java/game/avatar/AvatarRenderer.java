package game.avatar;

import game.avatar.asset.WeaponAsset;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class AvatarRenderer {

    private final PixelCharacterTemplate template;
    private final PixelPoseTransform poseTransform;
    private final AvatarAssetRenderer assetRenderer;
    private final AvatarAssetComposer assetComposer;

    public AvatarRenderer() {

        this.template =
                new PixelCharacterTemplate();

        this.poseTransform =
                new PixelPoseTransform(
                        template
                );

        this.assetRenderer =
                new AvatarAssetRenderer(
                        template
                );

        this.assetComposer =
                new AvatarAssetComposer();
    }

    /**
     * Основной метод генерации одного кадра персонажа.
     *
     * Результат всегда имеет размер 24x32 пикселя.
     */
    public BufferedImage render(
            PixelCharacter character,
            PixelPose pose
    ) {

        BufferedImage result =
                new BufferedImage(
                        PixelCharacterTemplate.WIDTH,
                        PixelCharacterTemplate.HEIGHT,
                        BufferedImage.TYPE_INT_ARGB
                );

        Graphics2D g =
                result.createGraphics();

        configureGraphics(g);

        TransformedTemplate transformed =
                poseTransform.transform(pose);

        /*
         * ---------------------------------------------------------
         * 1. Оружие за персонажем
         * ---------------------------------------------------------
         */
        drawWeapon(
                g,
                character,
                pose,
                transformed,
                WeaponAsset.Layer.BEHIND_CHARACTER
        );

        /*
         * ---------------------------------------------------------
         * 2. Персонаж
         *
         * Здесь используется новая asset-based система.
         *
         * AvatarAssetComposer:
         * - тело
         * - одежда
         * - волосы
         * - аксессуары
         *
         * собираются в единый 24x32 sprite.
         * ---------------------------------------------------------
         */
        BufferedImage characterImage =
                assetComposer.compose(character, pose, transformed);

        g.drawImage(
                characterImage,
                0,
                0,
                null
        );

        /*
         * ---------------------------------------------------------
         * 3. Оружие перед персонажем
         * ---------------------------------------------------------
         */
        drawWeapon(
                g,
                character,
                pose,
                transformed,
                WeaponAsset.Layer.IN_FRONT_OF_CHARACTER
        );

        g.dispose();

        return result;
    }

    /**
     * Настройка Graphics2D для pixel-art.
     */
    private void configureGraphics(
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

        g.setRenderingHint(
                RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_SPEED
        );
    }

    /**
     * Отрисовка оружия.
     */
    private void drawWeapon(
            Graphics2D g,
            PixelCharacter character,
            PixelPose pose,
            TransformedTemplate transformed,
            WeaponAsset.Layer requiredLayer
    ) {

        WeaponAsset weapon =
                character.getWeapon();

        if (weapon == null) {
            return;
        }

        /*
         * Проверяем, на каком слое должно
         * находиться оружие.
         */
        if (weapon.getLayer() != requiredLayer) {
            return;
        }

        /*
         * Получаем точку крепления оружия
         * в зависимости от направления персонажа.
         */
        PixelPoint anchor =
                getWeaponAnchor(
                        pose,
                        transformed
                );

        if (anchor == null) {
            return;
        }

        BufferedImage weaponImage =
                weapon.getImage();

        if (weaponImage == null) {
            return;
        }

        /*
         * Положение оружия относительно
         * точки крепления.
         */
        double x =
                anchor.x()
                        - weapon.getAnchorX();

        double y =
                anchor.y()
                        - weapon.getAnchorY();

        /*
         * Поворот оружия.
         */
        double rotation =
                Math.toRadians(
                        weapon.getRotation()
                );

        /*
         * Если оружие не имеет поворота,
         * используем обычную отрисовку.
         */
        if (rotation == 0) {

            g.drawImage(
                    weaponImage,
                    (int) Math.round(x),
                    (int) Math.round(y),
                    null
            );

            return;
        }

        /*
         * Если оружие имеет поворот,
         * вращаем его вокруг точки крепления.
         */
        java.awt.geom.AffineTransform transform =
                new java.awt.geom.AffineTransform();

        transform.translate(
                anchor.x(),
                anchor.y()
        );

        transform.rotate(
                rotation
        );

        transform.translate(
                -weapon.getAnchorX(),
                -weapon.getAnchorY()
        );

        g.drawImage(
                weaponImage,
                transform,
                null
        );
    }

    /**
     * Выбор точки крепления оружия
     * в зависимости от направления.
     *
     * Пока используются две базовые точки:
     *
     * weapon_left
     * weapon_right
     *
     * Позже заменим их полноценными
     * 8-direction weapon anchors.
     */
    private PixelPoint getWeaponAnchor(
            PixelPose pose,
            TransformedTemplate transformed
    ) {
        PixelPart handPart =
                switch (pose.getDirection()) {
                    case DOWN, DOWN_RIGHT, RIGHT, UP_RIGHT ->
                            PixelPart.RIGHT_HAND;

                    case UP, UP_LEFT, LEFT, DOWN_LEFT ->
                            PixelPart.LEFT_HAND;
                };

        TransformedPart hand =
                transformed.getPart(handPart);

        if (hand != null) {
            return hand.bounds().center();
        }

        // Fallback: центр персонажа.
        PixelPoint center = transformed.getAnchor("center");

        if (center != null) {
            return center;
        }

        return new PixelPoint(
                PixelCharacterTemplate.WIDTH / 2.0,
                PixelCharacterTemplate.HEIGHT / 2.0
        );
    }

    public PixelCharacterTemplate getTemplate() {
        return template;
    }

    public PixelPoseTransform getPoseTransform() {
        return poseTransform;
    }

    public AvatarAssetRenderer getAssetRenderer() {
        return assetRenderer;
    }
}