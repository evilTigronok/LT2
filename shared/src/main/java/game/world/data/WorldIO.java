package game.world.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public final class WorldIO {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private WorldIO() {
    }

    public static void save(
            WorldData world,
            File file
    ) throws IOException {

        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs() && !parent.exists()) {
                throw new IOException(
                        "Cannot create directory: "
                                + parent.getAbsolutePath()
                );
            }
        }

        try (Writer writer =
                     new FileWriter(file)) {

            GSON.toJson(
                    world,
                    writer
            );
        }
    }

    public static WorldData load(
            File file
    ) throws IOException {

        if (!file.exists()) {
            throw new FileNotFoundException(
                    file.getAbsolutePath()
            );
        }

        try (Reader reader =
                     new FileReader(file)) {

            WorldData world =
                    GSON.fromJson(
                            reader,
                            WorldData.class
                    );

            if (world == null) {
                throw new IOException(
                        "World file is empty: "
                                + file.getAbsolutePath()
                );
            }

            return world;
        }
    }
}