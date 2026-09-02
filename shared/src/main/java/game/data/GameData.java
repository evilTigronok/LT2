package game.data;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class GameData {

    private GameData() {
    }

    /**
     * Корень игровых данных.
     *
     * Можно переопределить через:
     *
     * -Dlt2.data.dir=...
     */
    public static Path root() {

        String customPath =
                System.getProperty(
                        "lt2.data.dir"
                );

        if (
                customPath != null
                        &&
                        !customPath.isBlank()
        ) {

            return Paths.get(
                    customPath
            );
        }

        return Paths.get(
                "data"
        );
    }

    public static Path resolve(
            String path
    ) {

        return root().resolve(path);
    }
}