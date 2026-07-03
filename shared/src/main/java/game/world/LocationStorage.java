package game.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import game.world.data.LocationData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class LocationStorage {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final File LOCATION_FOLDER =
            new File("locations");

    public static void save(
            LocationData data
    ) {

        try {

            if (!LOCATION_FOLDER.exists()) {
                LOCATION_FOLDER.mkdirs();
            }

            File file =
                    new File(
                            LOCATION_FOLDER,
                            data.id + ".json"
                    );

            FileWriter writer =
                    new FileWriter(file);

            GSON.toJson(data, writer);

            writer.flush();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static LocationData load(
            String name
    ) {

        try {

            File file =
                    new File(
                            LOCATION_FOLDER,
                            name + ".json"
                    );

            if (!file.exists()) {
                return null;
            }

            return GSON.fromJson(
                    new FileReader(file),
                    LocationData.class
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}