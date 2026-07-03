package game.world.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class LocationIO {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    public static void save(
            LocationData location,
            File file
    ) throws IOException {

        try (Writer writer =
                     new FileWriter(file)) {

            GSON.toJson(
                    location,
                    writer
            );
        }
    }

    public static LocationData load(
            File file
    ) throws IOException {

        try (Reader reader =
                     new FileReader(file)) {

            return GSON.fromJson(
                    reader,
                    LocationData.class
            );
        }
    }
}