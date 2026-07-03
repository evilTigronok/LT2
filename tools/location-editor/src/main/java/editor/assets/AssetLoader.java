package editor.assets;

import javafx.scene.image.Image;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AssetLoader {

    private static final Map<String, Image> CACHE =
            new HashMap<>();

    public static Image load(String path) {

        if (CACHE.containsKey(path)) {
            return CACHE.get(path);
        }

        try {

            File file = new File(
                    "../../client/src/main/resources/" + path
            );

            Image image =
                    new Image(
                            file.toURI().toString()
                    );

            CACHE.put(path, image);

            return image;

        } catch (Exception e) {

            System.out.println(
                    "Failed to load texture: " + path
            );

            return null;
        }
    }
}