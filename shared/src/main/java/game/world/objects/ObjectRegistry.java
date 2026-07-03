package game.world.objects;

import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ObjectRegistry {

    private final List<ObjectDefinition> objects =
            new ArrayList<>();

    public void register(
            ObjectDefinition definition
    ) {

        objects.add(definition);
    }

    public List<ObjectDefinition> getAll() {

        return objects;
    }

    public void load(String path) {

        objects.clear();

        File root =
                new File(path);

        if (!root.exists()) {
            return;
        }

        loadDirectory(root);
    }

    private void loadDirectory(File dir) {

        File[] files =
                dir.listFiles();

        if (files == null) {
            return;
        }

        Gson gson =
                new Gson();

        for (File file : files) {

            if (file.isDirectory()) {

                loadDirectory(file);

                continue;
            }

            if (!file.getName().endsWith(".json")) {
                continue;
            }

            try {

                ObjectDefinition definition =
                        gson.fromJson(
                                new FileReader(file),
                                ObjectDefinition.class
                        );

                if (definition != null) {
                    objects.add(definition);
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    public ObjectDefinition getById(String id) {

        for (ObjectDefinition def : objects) {

            if (def.getId().equals(id)) {
                return def;
            }
        }

        return null;
    }

    public void loadFromResources() {

        objects.clear();

        try (
                InputStream stream =
                        getClass()
                                .getClassLoader()
                                .getResourceAsStream(
                                        "data/objects/registry.txt"
                                )
        ) {

            if (stream == null) {
                throw new RuntimeException(
                        "registry.txt not found"
                );
            }

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(stream)
                    );

            String fileName;

            Gson gson = new Gson();

            while ((fileName = reader.readLine()) != null) {

                fileName = fileName.trim();

                if (fileName.isEmpty()) {
                    continue;
                }

                try (
                        InputStream jsonStream =
                                getClass()
                                        .getClassLoader()
                                        .getResourceAsStream(
                                                "data/objects/"
                                                        + fileName
                                        )
                ) {

                    if (jsonStream == null) {
                        continue;
                    }

                    ObjectDefinition definition =
                            gson.fromJson(
                                    new InputStreamReader(jsonStream),
                                    ObjectDefinition.class
                            );

                    if (definition != null) {
                        objects.add(definition);
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}