package game.avatar;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class AvatarPartRenderer {

    private final AvatarLayerExtractor extractor;

    public AvatarPartRenderer() {
        this.extractor = new AvatarLayerExtractor();
    }

    /**
     * Извлекает указанную часть исходного ассета
     * и помещает её в соответствующую область
     * преобразованного шаблона.
     */
    public void draw(
            Graphics2D graphics,
            BufferedImage source,
            PixelPart targetPart,
            AvatarLayerExtractor.SourcePart sourcePart,
            TransformedTemplate template
    ) {

        if (graphics == null ||
                source == null ||
                targetPart == null ||
                sourcePart == null ||
                template == null) {
            return;
        }

        TransformedPart transformed =
                template.getParts().get(targetPart);

        if (transformed == null) {
            return;
        }

        BufferedImage image =
                extractor.extract(
                        source,
                        sourcePart
                );

        if (image == null) {
            return;
        }

        drawImage(
                graphics,
                image,
                transformed
        );
    }


    private void drawImage(
            Graphics2D graphics,
            BufferedImage image,
            TransformedPart transformed
    ) {

        PixelRect bounds =
                transformed.bounds();

        if (bounds == null ||
                bounds.width() <= 0 ||
                bounds.height() <= 0) {
            return;
        }

        double scaleX =
                bounds.width() /
                        image.getWidth();

        double scaleY =
                bounds.height() /
                        image.getHeight();

        /*
         * Pixel-art должен масштабироваться
         * без сглаживания.
         */
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF
        );

        AffineTransform transform =
                new AffineTransform();

        if (transformed.mirrored()) {

            transform.translate(
                    bounds.x() + bounds.width(),
                    bounds.y()
            );

            transform.scale(
                    -scaleX,
                    scaleY
            );

        } else {

            transform.translate(
                    bounds.x(),
                    bounds.y()
            );

            transform.scale(
                    scaleX,
                    scaleY
            );
        }

        graphics.drawImage(
                image,
                transform,
                null
        );
    }
}