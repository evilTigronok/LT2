package game.avatar.asset;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class AssetImageLoader {

    public BufferedImage load(
            String path
    ) {

        try (InputStream stream =
                     getClass().getResourceAsStream(path)) {

            if (stream == null) {
                throw new IllegalArgumentException(
                        "Avatar asset not found: " + path
                );
            }

            BufferedImage image =
                    ImageIO.read(stream);

            if (image == null) {
                throw new IllegalArgumentException(
                        "Unable to read avatar asset: " + path
                );
            }

            return image;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to load avatar asset: " + path,
                    e
            );
        }
    }
}