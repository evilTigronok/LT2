package game.avatar;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class AvatarAssetRenderer {

    private final PixelCharacterTemplate template;

    public AvatarAssetRenderer(
            PixelCharacterTemplate template
    ) {
        this.template = template;
    }

    public void drawRect(
            Graphics2D g,
            PixelPart part
    ) {

        PixelRect rect =
                template.getPart(part);

        g.fillRect(
                (int) rect.x(),
                (int) rect.y(),
                (int) rect.width(),
                (int) rect.height()
        );
    }

    public void drawImage(
            Graphics2D g,
            BufferedImage image,
            PixelPart part
    ) {

        PixelRect rect =
                template.getPart(part);

        g.drawImage(
                image,
                (int) rect.x(),
                (int) rect.y(),
                (int) rect.width(),
                (int) rect.height(),
                null
        );
    }

    public void drawImageAtAnchor(
            Graphics2D g,
            BufferedImage image,
            String anchor
    ) {

        PixelPoint point =
                template.getAnchor(anchor);

        int x =
                point.roundX()
                        - image.getWidth() / 2;

        int y =
                point.roundY()
                        - image.getHeight() / 2;

        g.drawImage(
                image,
                x,
                y,
                null
        );
    }

    public PixelCharacterTemplate getTemplate() {
        return template;
    }

    public void drawRect(
            Graphics2D g,
            TransformedTemplate template,
            PixelPart part
    ) {

        TransformedPart transformed =
                template.getPart(part);

        if (transformed == null) {
            return;
        }

        PixelRect rect =
                transformed.bounds();

        g.fillRect(
                (int) Math.round(rect.x()),
                (int) Math.round(rect.y()),
                (int) Math.round(rect.width()),
                (int) Math.round(rect.height())
        );
    }

    public void drawImage(
            Graphics2D g,
            TransformedTemplate template,
            BufferedImage image,
            PixelPart part
    ) {

        TransformedPart transformed =
                template.getPart(part);

        if (transformed == null) {
            return;
        }

        PixelRect rect =
                transformed.bounds();

        int x =
                (int) Math.round(rect.x());

        int y =
                (int) Math.round(rect.y());

        int width =
                (int) Math.round(rect.width());

        int height =
                (int) Math.round(rect.height());

        if (!transformed.mirrored()) {

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
         * Горизонтальное зеркалирование.
         */
        g.drawImage(
                image,
                x + width,
                y,
                x,
                y + height,
                0,
                0,
                image.getWidth(),
                image.getHeight(),
                null
        );
    }
}