package game.world;

import game.network.dto.PlayerState;
import game.network.packets.WorldStatePacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import game.world.data.LocationData;
import game.world.data.LocationIO;

import java.io.File;
import java.util.HashMap;

public class WorldManager {

    private final Map<String, ServerPlayer> players =
            new ConcurrentHashMap<>();

    private final Map<String, LocationData> locations =
            new HashMap<>();

    public WorldManager() {

        loadLocations();
    }

    private void loadLocations() {


        try {

            File folder =
                    new File("server/data/locations");

            System.out.println(
                    folder.getAbsolutePath()
            );
            System.out.println(
                    folder.exists()
            );

            File[] files =
                    folder.listFiles(
                            (dir, name) ->
                                    name.endsWith(".json")
                    );

            if (files == null) {
                return;
            }

            for (File file : files) {

                LocationData location =
                        LocationIO.load(file);

                String name = file.getName().replace(".json", "");
                String key = name;

                locations.put(
                        key,
                        location
                );

                System.out.println(
                        "Loaded location "
                                + key
                                + " objects="
                                + location.objects.size()
                );
                System.out.println(
                        "LOCATIONS KEYS = " + locations.keySet()
                );
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public LocationData getLocation(
            int x,
            int y
    ) {

        String key = x + "_" + y;

        System.out.println(
                "REQUEST LOCATION = " + key
        );

        System.out.println(
                "FOUND = " + locations.containsKey(key)
        );

        return locations.get(key);
    }

    public LocationData getPlayerLocation(ServerPlayer player) {
        return getLocation(player.getLocationX(), player.getLocationY());
    }

    public void addPlayer(String username) {

        players.remove(username);

        players.put(
                username,
                new ServerPlayer(username)
        );

        System.out.println(
                "ADD PLAYER " + username
        );
    }

    public void removePlayer(String username) {

        players.remove(username);
    }

    public ServerPlayer getPlayer(String username) {

        return players.get(username);
    }

    public void update() {

        for (ServerPlayer player : players.values()) {
            player.update();
        }
    }



    public WorldStatePacket buildStatePacket() {

        WorldStatePacket packet =
                new WorldStatePacket();

        for (ServerPlayer player : players.values()) {

            packet.players.add(
                    new PlayerState(
                            player.getUsername(),
                            player.getX(),
                            player.getY(),
                            player.getLocationX(),
                            player.getLocationY()
                    )
            );
        }

        return packet;
    }


}