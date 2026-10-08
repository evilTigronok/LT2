package game.world;

import game.combat.AttackSystem;
import game.combat.TrainingDummy;
import game.data.GameData;
import game.network.dto.PlayerState;
import game.network.packets.WorldStatePacket;
import game.world.data.WorldData;
import game.world.data.WorldIO;
import game.combat.CombatTarget;
import game.combat.TrainingDummy;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldManager {

    private static final String WORLD_FILE =
            "world/world.json";

    private final Map<String, ServerPlayer> players =
            new ConcurrentHashMap<>();

    private final AttackSystem attackSystem =
            new AttackSystem();

    private WorldData world;
    private final GameMap gameMap;


    private final TrainingDummy trainingDummy;

    public WorldManager() {

        world = loadWorld();

        gameMap =
                new GameMap(
                        world.width,
                        world.height
                );

        trainingDummy =
                new TrainingDummy(
                        world.width / 2f - 20f,
                        world.height / 2f + 90f
                );

        attackSystem.addTarget(
                trainingDummy
        );

        System.out.println(
                "WORLD SIZE = "
                        + world.width
                        + "x"
                        + world.height
        );

        System.out.println(
                "WORLD OBJECTS = "
                        + world.objects.size()
        );

        attackSystem.addTarget(
                new CombatTarget(
                        "training_dummy",
                        gameMap.getWidth() / 2f - 20f,
                        gameMap.getHeight() / 2f + 40f,
                        40f,
                        40f,
                        1000f
                )
        );
    }

    public TrainingDummy getTrainingDummy() {

        return trainingDummy;
    }

    // =====================================================
    // WORLD
    // =====================================================

    private WorldData loadWorld() {

        try {

            File file =
                    GameData
                            .resolve(WORLD_FILE)
                            .toFile();

            System.out.println(
                    "WORLD FILE = "
                            + file.getAbsolutePath()
            );

            if (!file.exists()) {

                System.out.println(
                        "World file not found. "
                                + "Creating default world."
                );

                WorldData newWorld =
                        new WorldData(
                                GameMap.DEFAULT_WIDTH,
                                GameMap.DEFAULT_HEIGHT
                        );

                WorldIO.save(
                        newWorld,
                        file
                );

                return newWorld;
            }

            return WorldIO.load(file);

        } catch (Exception e) {

            e.printStackTrace();

            /*
             * Даже если world.json повреждён,
             * сервер получает безопасный fallback.
             */
            return new WorldData(
                    GameMap.DEFAULT_WIDTH,
                    GameMap.DEFAULT_HEIGHT
            );
        }
    }

    public WorldData getWorld() {
        return world;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    // =====================================================
    // PLAYERS
    // =====================================================

    public void addPlayer(String username) {

        players.remove(username);

        ServerPlayer player =
                new ServerPlayer(
                        username,
                        gameMap.getWidth(),
                        gameMap.getHeight()
                );

        players.put(
                username,
                player
        );

        System.out.println(
                "ADD PLAYER "
                        + username
                        + " @ "
                        + player.getX()
                        + ","
                        + player.getY()
        );
    }

    public void removePlayer(String username) {

        players.remove(username);
    }

    public ServerPlayer getPlayer(
            String username
    ) {

        return players.get(username);
    }

    // =====================================================
    // UPDATE
    // =====================================================

    public void update() {

        for (ServerPlayer player :
                players.values()) {

            player.update();
        }

        attackSystem.update();
    }

    // =====================================================
    // STATE
    // =====================================================

    public WorldStatePacket buildStatePacket() {

        WorldStatePacket packet =
                new WorldStatePacket();

        for (ServerPlayer player :
                players.values()) {

            /*
             * Старый PlayerState пока содержит
             * locationX/locationY.
             *
             * До полной переделки DTO отправляем
             * туда 0,0.
             *
             * Следующим этапом мы уберём эти поля
             * из сетевого протокола полностью.
             */
            packet.players.add(
                    new PlayerState(
                            player.getUsername(),
                            player.getX(),
                            player.getY()
                    )
            );
        }

        return packet;
    }

    public void startAttack(String username) {

        ServerPlayer player =
                players.get(username);

        if (player == null) {
            return;
        }

        var attack =
                player.attack();

        if (attack == null) {
            return;
        }

        attackSystem.startAttack(
                player,
                attack
        );
    }

    public void setCombatHitListener(
            java.util.function.Consumer<game.combat.CombatHit> listener
    ) {

        attackSystem.setHitListener(
                listener
        );
    }
}