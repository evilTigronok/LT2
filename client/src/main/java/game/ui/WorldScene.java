
package game.ui;

import game.achievement.AchievementManager;
import game.audio.MusicManager;
import game.boss.BossManager;
import game.boss.Boss;
import game.boss.DungeonBossRegistry;
import game.combat.*;

import game.player.PlayerSkillLoadout;
import game.save.Difficulty;
import game.save.DifficultySettings;
import game.save.SaveData;
import game.save.SaveManager;
import game.weapon.*;
import game.world.location.Location;
import game.world.location.Portal;
import game.avatar.Direction;
import game.client.NetworkClient;
import game.network.dto.PlayerState;
import game.network.packets.WorldStatePacket;
import game.ui.world.RemotePlayer;
import game.ui.world.WorldObject;
import game.combat.AttackDefinition;
import game.inventory.Inventory;
import game.inventory.Artifact;
import game.item.AbilityDefinition;
import game.item.ItemQuality;
import game.world.data.PlacedObjectData;
import game.world.data.WorldData;
import game.world.objects.ObjectDefinition;
import game.world.objects.ObjectRegistry;
import javafx.animation.AnimationTimer;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.util.*;

public class WorldScene {

    private boolean showTrainingDummy = true; //temp

    private final SceneManager sceneManager;

    private final Pane root =
            new Pane();

    private final Canvas canvas =
            new Canvas();

    private final GraphicsContext gc =
            canvas.getGraphicsContext2D();

    private final NetworkClient client;

    private final String username;

    private final ObjectRegistry objectRegistry =
            new ObjectRegistry();

    private final Map<String, RemotePlayer> players =
            new HashMap<>();

    private final List<WorldObject> objects =
            new ArrayList<>();

    private final List<DamagePopup> damagePopups =
            new ArrayList<>();

    private static final float TRAINING_DUMMY_SIZE = 40f;

    private final LocalCombatSystem combatSystem;

    private final Difficulty difficulty;

    private final AchievementManager achievementManager;

    /*
     * =====================================================
     * LOCAL WEAPONS
     * =====================================================
     *
     * 9 слотов: клавиши 1-9.
     * Multiplayer-инвентарь пока не используется.
     */
    private final Inventory inventory =
            new Inventory();

    private final WeaponLoadoutStorage weaponLoadoutStorage;

    private final RangedProjectileSystem rangedProjectileSystem;

    private static final float PLAYER_ATTACK = 5f;

    private LocalCombatTarget trainingDummy;
    private LocalCombatTarget trainingDummy2;

    private final BossManager bossManager =
            new BossManager();


    private static final double DAMAGE_POPUP_DURATION = 0.8;

    /*
     * =====================================================
     * INPUT
     * =====================================================
     */

    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;

    /*
     * =====================================================
     * AIM
     * =====================================================
     *
     * Координаты курсора в Canvas.
     * Направление атаки вычисляется относительно
     * центра игрока.
     */
    private double mouseX;
    private double mouseY;

    private boolean attackHeld = false;
    private double attackTimer = 0.0;
    private boolean secondaryAttackHeld = false;
    private double secondaryAttackTimer = 0.0;

    private SwordSwing spearComboThrust;
    private boolean spearFollowupRequested = false;
    private double spearFollowupTimer = 0.0;

    private static final double SPEAR_FOLLOWUP_DELAY = 0.18;

    private Weapon spearComboWeapon;

    /* Специальные состояния оружия. */
    private int mourningStacks = 0;
    private double mourningStackTimer = 0.0;
    private boolean mourningCombatActive = false;

    private int iceAmmo = 30;
    private double iceReloadTimer = 0.0;
    private static final int ICE_MAX_AMMO = 30;
    private static final double ICE_RELOAD_TIME = 10.0;

    private boolean doomStarCharged = false;
    private boolean doomStarCharging = false;
    private double doomStarChargeTimer = 0.0;
    private static final double DOOM_STAR_CHARGE_TIME = 16.0;

    private double attackCooldown = 0.0;
    private int dawnComboIndex = 0;

    private static final double DAWN_COMBO_INTERVAL = 0.4;
    private static final double DAWN_FINISHER_DELAY = 1.2;
    private static final double DAWN_FINISHER_COOLDOWN = 9.0;

    private static final float[] DAWN_COMBO_MULTIPLIERS = {
            0.40f, 0.60f, 0.80f, 1.10f, 2.00f
    };


    private final WeaponTrickSystem weaponTrickSystem =
            new WeaponTrickSystem();

    private double gungnirDrillTimer = 0.0;
    private double gungnirDrillTickTimer = 0.0;
    private double gungnirDrillActiveDuration = 0.0;

    private float gungnirDrillDirectionX = 0f;
    private float gungnirDrillDirectionY = 0f;

    private boolean gungnirDrillActive = false;

    private double tinWallRamTimer = 0.0;
    private double tinWallRamCooldown = 0.0;
    private double tinWallArcAngle = Math.PI;
    private final java.util.Set<String> tinWallRamHitTargets = new java.util.HashSet<>();
    private final java.util.Map<String, Long> tinWallLastContactHitNanos = new java.util.HashMap<>();
    private double tinWallPreviousAimAngle = Double.NaN;

    private boolean projectileInvulnerability = false;

    private AnimationTimer gameTimer;

    /*
     * =====================================================
     * UI
     * =====================================================
     */

    private final Label infoLabel =
            new Label();

    /*
     * =====================================================
     * WORLD
     * =====================================================
     *
     * Размер мира больше не является константой.
     *
     * Он приходит из WorldData.
     */

    private double worldWidth = 10000;
    private double worldHeight = 10000;

    private Location currentLocation;

    private Portal nearbyPortal;

    private final Map<String, Location> locations =
            new LinkedHashMap<>();

    private static final double PORTAL_INTERACTION_DISTANCE = 120.0;

    private static final double PORTAL_FRAME_PADDING = 8.0;

    /*
     * =====================================================
     * PLAYER
     * =====================================================
     */

    private static final double PLAYER_SIZE = 40.0;

    // Визуальный размер персонажа увеличен в 2 раза.
    private static final double PLAYER_VISUAL_SIZE = 40.0;

    private boolean oneHpMode = false;
    private boolean hardcoreMode = false;

    // Время для анимации ходьбы и покоя.
    private double playerAnimationTime = 0.0;

    private double glitchAnimationTime = 0.0;

    // Оставшееся время анимации атаки.
    private double attackAnimationTimer = 0.0;

    private static final double ATTACK_ANIMATION_DURATION = 0.28;

    /*
     * =====================================================
     * PLAYER SPRITE
     * =====================================================
     */

    private static final int PLAYER_SPRITE_WIDTH =
            32;

    private static final int PLAYER_SPRITE_HEIGHT =
            40;

    private static final String PLAYER_SPRITE_PATH =
            "/assets/avatar/generated/character_walk_2.png";

    private int locationX = 0;
    private int locationY = 0;

    /*
     * Один spritesheet содержит 8 направлений.
     */
    private final Image playerSpriteSheet =
            loadPlayerSpriteSheet();

    private Image gungnirProjectileImage;

    private final WeaponSpriteManager weaponSpriteManager =
            new WeaponSpriteManager();

    /*
     * =====================================================
     * LOCAL PLAYER SPEED
     * =====================================================
     */

    private static final double LOCAL_PLAYER_SPEED =
            300.0;

    /*
     * =====================================================
     * CAMERA
     * =====================================================
     */

    private static final double CAMERA_ZOOM =
            1.0;

    /*
     * =====================================================
     * LOCAL PLAYER
     * =====================================================
     */

    private RemotePlayer localPlayer;

    /*
     * =====================================================
     * FRAME TIME
     * =====================================================
     */

    private long lastUpdateTime = 0;

    private final Button menuButton =
            new Button("☰");

    private final Button characterButton =
            new Button("☻");

    private final VBox gameMenu =
            new VBox(12);

    private boolean gameMenuVisible = false;

    private boolean characterPanelVisible = false;

    //temp
    private float playerHp = 100f;
    private float playerMaxHp = 100f;
    // Бонус максимального ХП от экипированных артефактов, уже применённый к игроку.
    private int appliedArtifactHpBonus = 0;
    // Эфирная кукла: время следующего допустимого срабатывания в наносекундах.
    private long etherealDollReadyAtNanos = 0L;
    private long etherealPhantomUntilNanos = 0L;
    private float etherealPhantomX = 0f;
    private float etherealPhantomY = 0f;

    private final VBox playerHud =
            new VBox(5);
    private final VBox characterPanel =
            new VBox(15);

    private final Label characterHpValue =
            new Label();

    private final Label characterAttackValue =
            new Label();

    private final Label characterSpeedValue =
            new Label();

    private final Label equippedMeleeValue =
            new Label();

    private final Label equippedRangedValue =
            new Label();

    private final GridPane inventoryGrid =
            new GridPane();

    // Настройки отображения инвентаря. Они не меняют саму модель Inventory.
    private String inventoryFilter = "Все предметы";
    private String inventorySort = "По порядку";
    private String inventorySearch = "";

    private final StackPane[] artifactSlotNodes = new StackPane[4];

    private final VBox itemDetails =
            new VBox(8);

    private final Label itemDetailsTitle =
            new Label("Выберите предмет");

    private final Label itemDetailsQuality =
            new Label();

    private final Label itemDetailsAttack =
            new Label();

    private final Label itemDetailsCharacteristics =
            new Label();

    private final Label itemDetailsMain =
            new Label();

    private final Label itemDetailsLore =
            new Label();

    private final Label playerHpLabel =
            new Label();

    private final ProgressBar playerHpBar =
            new ProgressBar();


    private final PlayerSkillLoadout skillLoadout =
            new PlayerSkillLoadout();



    /*
     * =====================================================
     * BOSS HUD
     * =====================================================
     */

    private final VBox bossHud =
            new VBox(4);

    private final Label bossNameLabel =
            new Label();

    private final Label bossHpLabel =
            new Label();

    private final ProgressBar bossHpBar =
            new ProgressBar();

    /*
     * =====================================================
     * ACTIVE WEAPON HUD
     * =====================================================
     */

    private final HBox weaponHud =
            new HBox(8);

    private final StackPane meleeWeaponSlot =
            createWeaponSlot();

    private final StackPane rangedWeaponSlot =
            createWeaponSlot();

    private final Label meleeWeaponLabel =
            new Label();

    private final Label rangedWeaponLabel =
            new Label();

    private final ProgressBar meleeCooldownBar =
            createCooldownBar();

    private final ProgressBar rangedCooldownBar =
            createCooldownBar();

    private final Label weaponSwitchLabel =
            new Label("[R]");

    private CombatDash activeCombatDash;

    /*
     * =====================================================
     * ACTIVE WEAPON
     * =====================================================
     *
     * true  -> активно оружие ближнего боя
     * false -> активно оружие дальнего боя
     */
    private boolean meleeActive = true;

    public boolean isProjectileInvulnerable() {
        return projectileInvulnerability;
    }
    private double gungnirDrillVisualAngle = 0.0;

    private int saveSlot = 1;


    /*
     * =====================================================
     * CONSTRUCTOR
     * =====================================================
     */

    public WorldScene(
            SceneManager sceneManager,
            NetworkClient client,
            String username,
            SaveData saveData,
            Difficulty difficulty,
            int saveSlot,
            boolean oneHpMode,
            boolean hardcoreMode
    ) {

        this.client = client;
        this.username = username;

        this.oneHpMode = saveData != null
                ? saveData.player.oneHpMode
                : oneHpMode;

        this.hardcoreMode = saveData != null
                ? saveData.player.hardcoreMode
                : hardcoreMode;

        this.saveSlot = saveData != null
                ? saveData.slot
                : Math.max(1, Math.min(3, saveSlot));

        this.difficulty =
                difficulty == null
                        ? Difficulty.EXHAUSTING
                        : difficulty;

        this.combatSystem = new LocalCombatSystem(this.difficulty);

        this.rangedProjectileSystem =
                new RangedProjectileSystem(this.combatSystem);

        this.achievementManager =
                new AchievementManager(
                        username
                );

        this.weaponLoadoutStorage =
                new WeaponLoadoutStorage(username, this.saveSlot);

        this.sceneManager =
                sceneManager;

        root.setPrefSize(
                1920,
                1080
        );

        root.getChildren().add(
                canvas
        );

        canvas.widthProperty()
                .bind(root.widthProperty());

        canvas.heightProperty()
                .bind(root.heightProperty());

        root.getChildren().add(
                infoLabel
        );

        infoLabel.setLayoutX(20);
        infoLabel.setLayoutY(10);

        infoLabel.setTextFill(
                Color.WHITE
        );

        infoLabel.setStyle("""
                -fx-font-size: 14px;
                -fx-background-color: rgba(0,0,0,0.55);
                -fx-padding: 8px;
                """);

        /*
         * Загружаем определения объектов.
         */
        objectRegistry.loadFromResources();

        setupLocations();

        setupInput();

        setupPlayerHud();
        setupBossHud();
        setupWeaponHud();
        setupMenuButton();
        setupCharacterButton();
        setupGameMenu();
        setupCharacterPanel();

        /*
         * =================================================
         * LOCAL PLAYER
         * =================================================
         *
         * Временная начальная позиция.
         *
         * После загрузки WorldData игрок будет
         * размещаться в центре мира.
         */

        localPlayer =
                new RemotePlayer(
                        username,
                        (float) (worldWidth / 2.0 - PLAYER_SIZE / 2.0),
                        (float) (worldHeight / 2.0 - PLAYER_SIZE / 2.0)
                );

        if (saveData != null) {

            applySave(
                    saveData
            );

        } else {

            achievementManager.unlock(
                    "first_save"
            );
            // Для тестирования весь каталог оружия доступен сразу,
            // но оба слота экипировки остаются пустыми.
            addAllWeaponsToInventory();

            // Стартовый набор артефактов.
            for (Artifact artifact : Artifact.all()) {
                inventory.addArtifact(artifact);
            }
        }

        // Манекены всегда располагаются в основном мире,
        // независимо от локации, в которой загружено сохранение.
        float dummyX =
                5000f - TRAINING_DUMMY_SIZE / 2f;

        float dummyY =
                5040f;

        trainingDummy =
                new LocalCombatTarget(
                        "training_dummy",
                        dummyX,
                        dummyY,
                        TRAINING_DUMMY_SIZE,
                        TRAINING_DUMMY_SIZE,
                        100000f
                );

        trainingDummy2 =
                new LocalCombatTarget(
                        "training_dummy_2",
                        dummyX + 80f,
                        dummyY,
                        TRAINING_DUMMY_SIZE,
                        TRAINING_DUMMY_SIZE,
                        100000f
                );

        combatSystem.addTarget(trainingDummy);
        combatSystem.addTarget(trainingDummy2);

        combatSystem.setHitListener(
                this::handleLocalCombatHit
        );

        rangedProjectileSystem.setHitListener(
                this::handleLocalCombatHit
        );

        rangedProjectileSystem.setOriginProvider(() -> {

            if (localPlayer == null) {
                return new float[]{0f, 0f};
            }

            return new float[]{
                    localPlayer.renderX
                            + (float) PLAYER_SIZE / 2f,

                    localPlayer.renderY
                            + (float) PLAYER_SIZE / 2f
            };
        });

        rangedProjectileSystem.setDirectionProvider(() -> {

            return getAimDirection(
                    mouseX,
                    mouseY
            );
        });

        /*
         * Новая игра начинается без оружия.
         * Оружие появляется в инвентаре только после получения игроком,
         * а слоты экипировки остаются пустыми до ручного экипирования.
         * Сохранённая кампания восстанавливает инвентарь и слоты через applySave().
         */
        doomStarCharged = true;
        meleeActive = true;
        refreshCharacterPanel();

        players.put(
                username,
                localPlayer
        );

        /*
         * Передаём фокус Canvas,
         * чтобы WASD работал сразу.
         */
        Platform.runLater(() -> {

            canvas.requestFocus();
        });

        startLoop();
    }

    public Parent getRoot() {

        return root;
    }

    private SaveData createSaveData(
            int slot
    ) {

        SaveData data =
                new SaveData();

        data.slot =
                slot;

        data.saveName =
                "Сохранение " + slot;

        data.difficulty =
                difficulty;

        /*
         * PLAYER
         */

        data.player.x =
                localPlayer.serverX;

        data.player.y =
                localPlayer.serverY;

        data.player.hp =
                playerHp;

        data.player.maxHp =
                playerMaxHp;

        data.player.locationX =
                localPlayer.locationX;

        data.player.locationY =
                localPlayer.locationY;

        /*
         * WORLD
         */

        data.world.width =
                (int) worldWidth;

        data.world.height =
                (int) worldHeight;

        /*
         * WEAPONS
         */

        data.weaponIds.clear();
        for (Weapon weapon :
                inventory.getWeapons()) {

            if (weapon != null) {

                data.weaponIds.add(
                        weapon.getId()
                );
            }
        }

        Weapon melee =
                inventory.getEquippedMeleeWeapon();

        Weapon ranged =
                inventory.getEquippedRangedWeapon();

        data.equippedMelee =
                melee == null
                        ? null
                        : melee.getId();

        data.equippedRanged =
                ranged == null
                        ? null
                        : ranged.getId();

        data.meleeActive =
                meleeActive;

        data.artifactIds.clear();
        data.equippedArtifactIds.clear();
        for (Artifact artifact : inventory.getArtifacts()) {
            data.artifactIds.add(artifact.getId());
        }
        for (int artifactSlot = 0; artifactSlot < inventory.getArtifactSlotCount(); artifactSlot++) {
            Artifact artifact = inventory.getEquippedArtifact(artifactSlot);
            data.equippedArtifactIds.add(artifact == null ? null : artifact.getId());
        }

        /*
         * BOSSES
         */

        for (Boss boss :
                bossManager.getBosses()) {

            if (boss != null
                    && boss.isDefeated()) {

                data.defeatedBosses.add(
                        boss.getId()
                );
            }
        }

        data.player.oneHpMode = oneHpMode;

        return data;
    }

    private boolean isAngelicDoom(Weapon weapon) {

        if (weapon == null) {
            return false;
        }

        String name = weapon.getName();

        return name != null
                && (
                name.toLowerCase(Locale.ROOT).contains("angel")
                        || name.toLowerCase(Locale.ROOT).contains("ангел")
        );
    }
    private boolean isGungnirLauncher(Weapon weapon) {
        return weapon != null
                && "gungnir_launcher".equals(weapon.getId());
    }

    private boolean isMourning(Weapon weapon) {
        return weapon != null && weapon.getSpecial() == WeaponSpecial.MOURNING;
    }

    private boolean isIceShooter(Weapon weapon) {
        return weapon != null && weapon.getSpecial() == WeaponSpecial.ICE_SHOOTER;
    }

    private boolean isDoomStarShooter(Weapon weapon) {
        return weapon != null && weapon.getSpecial() == WeaponSpecial.DOOM_STAR_SHOOTER;
    }

    private float getMourningAttack() {
        return 7f + mourningStacks * 4f;
    }

    private void resetSpecialWeaponStates() {
        mourningStacks = 0;
        mourningStackTimer = 0.0;
        mourningCombatActive = false;
        iceAmmo = ICE_MAX_AMMO;
        iceReloadTimer = 0.0;
        doomStarCharging = false;
        doomStarChargeTimer = 0.0;
        doomStarCharged = isDoomStarShooter(getCurrentWeapon());
    }

    private void updateSpecialWeaponStates(double deltaSeconds) {
        Weapon weapon = getCurrentWeapon();

        if (isMourning(weapon)) {
            if (mourningCombatActive) {
                mourningStackTimer += deltaSeconds;
                double threshold = 10.0 + mourningStacks;
                if (mourningStackTimer >= threshold) {
                    mourningStackTimer -= threshold;
                    mourningStacks++;
                }
            }
        } else {
            mourningStacks = 0;
            mourningStackTimer = 0.0;
            mourningCombatActive = false;
        }

        if (isIceShooter(weapon) && iceAmmo < ICE_MAX_AMMO) {
            iceReloadTimer += deltaSeconds;
            if (iceReloadTimer >= ICE_RELOAD_TIME) {
                iceAmmo = ICE_MAX_AMMO;
                iceReloadTimer = 0.0;
            }
        }

        if (isDoomStarShooter(weapon) && doomStarCharging && !doomStarCharged) {
            doomStarChargeTimer += deltaSeconds;
            if (doomStarChargeTimer >= DOOM_STAR_CHARGE_TIME) {
                doomStarChargeTimer = DOOM_STAR_CHARGE_TIME;
                doomStarCharging = false;
                doomStarCharged = true;
            }
        }
    }

    private boolean isGungnir(Weapon weapon) {
        return weapon != null
                && "gungnir".equals(weapon.getId());
    }
    private Weapon getGungnirLauncherSource() {

        /*
         * Гунгниромёт использует оружие,
         * установленное в ведущем слоте ближнего оружия.
         */
        Weapon meleeWeapon =
                inventory.getEquippedMeleeWeapon();

        if (!isSpear(meleeWeapon)) {
            return null;
        }

        return meleeWeapon;
    }


    private Weapon getCurrentWeapon() {

        if (meleeActive) {

            return inventory.getEquippedMeleeWeapon();

        } else {

            return inventory.getEquippedRangedWeapon();
        }
    }

    private boolean isSpear(Weapon weapon) {

        if (weapon == null) {
            return false;
        }

        /*
         * Все копья используют прямой колющий стиль.
         *
         * Поэтому любое будущее оружие с AttackStyle.THRUST
         * автоматически считается совместимым с Гунгниромётом.
         */
        return weapon.getType() == WeaponType.MELEE
                && weapon.getAttackStyle() == AttackStyle.THRUST;
    }
    private void switchActiveWeapon() {

        /*
         * Если сейчас меч —
         * пытаемся переключиться на дальнее оружие.
         */
        if (meleeActive) {

            if (inventory.hasEquippedRangedWeapon()) {

                meleeActive = false;

                attackCooldown = 0.0;
                attackTimer = 0.0;
                if (isMourning(getCurrentWeapon())) {
                    mourningStacks = 0;
                    mourningStackTimer = 0.0;
                    mourningCombatActive = false;
                }

                Weapon weapon =
                        inventory.getEquippedRangedWeapon();

                System.out.println(
                        "ACTIVE WEAPON: RANGED -> "
                                + weapon.getName()
                );
            }

            return;
        }

        /*
         * Если сейчас дальнее —
         * пытаемся переключиться на ближнее.
         */
        if (inventory.hasEquippedMeleeWeapon()) {

            meleeActive = true;

            attackCooldown = 0.0;
            attackTimer = 0.0;
            if (isMourning(getCurrentWeapon())) {
                mourningStacks = 0;
                mourningStackTimer = 0.0;
                mourningCombatActive = false;
            }

            Weapon weapon =
                    inventory.getEquippedMeleeWeapon();

            System.out.println(
                    "ACTIVE WEAPON: MELEE -> "
                            + weapon.getName()
            );
        }
    }

    private Button createMenuButton(
            String text
    ) {
        Button button =
                new Button(text);

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setPrefHeight(45);

        button.setStyle("""
        -fx-background-color: rgba(55,55,65,0.95);
        -fx-background-radius: 8;
        -fx-text-fill: white;
        -fx-font-size: 15px;
        """);

        return button;
    }
    private void setupPlayerHud() {

        playerHud.setPrefWidth(260);
        playerHud.setPadding(new Insets(12));

        playerHud.setStyle("""
        -fx-background-color: rgba(15,15,20,0.78);
        -fx-background-radius: 10;
        """);

        playerHpLabel.setTextFill(Color.WHITE);
        playerHpLabel.setStyle("""
        -fx-font-size: 16px;
        -fx-font-weight: bold;
        """);

        playerHpBar.setPrefWidth(230);
        playerHpBar.setPrefHeight(18);
        playerHpBar.setProgress(1.0);

        playerHud.getChildren().addAll(
                playerHpLabel,
                playerHpBar
        );

        root.getChildren().add(playerHud);

        /*
         * Немного правее старой позиции, чтобы не
         * пересекаться с информационной плашкой.
         */
        playerHud.setLayoutX(280);
        playerHud.setLayoutY(20);

        updatePlayerHud();
    }

    private void setupBossHud() {

        bossHud.setPrefWidth(520);
        bossHud.setAlignment(Pos.CENTER);
        bossHud.setPadding(new Insets(8, 14, 10, 14));

        bossHud.setStyle("""
        -fx-background-color: rgba(12,12,18,0.82);
        -fx-background-radius: 0 0 10 10;
        """);

        bossNameLabel.setTextFill(Color.WHITE);
        bossNameLabel.setStyle("""
        -fx-font-size: 16px;
        -fx-font-weight: bold;
        """);

        bossHpLabel.setTextFill(Color.LIGHTGRAY);
        bossHpLabel.setStyle("-fx-font-size: 12px;");

        bossHpBar.setPrefWidth(490);
        bossHpBar.setPrefHeight(14);
        bossHpBar.setProgress(1.0);

        bossHud.getChildren().addAll(
                bossNameLabel,
                bossHpBar,
                bossHpLabel
        );

        root.getChildren().add(bossHud);

        bossHud.layoutXProperty().bind(
                root.widthProperty()
                        .subtract(bossHud.prefWidthProperty())
                        .divide(2)
        );
        bossHud.setLayoutY(0);

        bossHud.setVisible(false);
        bossHud.setManaged(false);
    }

    private void setupWeaponHud() {

        setupWeaponSlot(
                meleeWeaponSlot,
                meleeWeaponLabel,
                meleeCooldownBar
        );

        setupWeaponSlot(
                rangedWeaponSlot,
                rangedWeaponLabel,
                rangedCooldownBar
        );

        weaponSwitchLabel.setTextFill(Color.WHITE);
        weaponSwitchLabel.setStyle("""
        -fx-font-size: 14px;
        -fx-font-weight: bold;
        """);

        weaponHud.setAlignment(Pos.CENTER_LEFT);
        weaponHud.getChildren().addAll(
                meleeWeaponSlot,
                rangedWeaponSlot,
                weaponSwitchLabel
        );

        root.getChildren().add(weaponHud);

        weaponHud.setLayoutX(555);
        weaponHud.setLayoutY(20);

        updateWeaponHud();
    }

    private StackPane createWeaponSlot() {
        StackPane slot = new StackPane();
        slot.setPrefSize(58, 58);
        slot.setMinSize(58, 58);
        slot.setMaxSize(58, 58);
        return slot;
    }

    private ProgressBar createCooldownBar() {
        ProgressBar bar = new ProgressBar(1.0);
        bar.setPrefWidth(50);
        bar.setPrefHeight(6);
        bar.setMaxWidth(50);
        bar.setMouseTransparent(true);
        return bar;
    }

    private void setupWeaponSlot(
            StackPane slot,
            Label weaponLabel,
            ProgressBar cooldownBar
    ) {
        weaponLabel.setTextFill(Color.WHITE);
        weaponLabel.setStyle("""
        -fx-font-size: 10px;
        -fx-font-weight: bold;
        -fx-text-alignment: center;
        """);
        weaponLabel.setWrapText(true);
        weaponLabel.setMaxWidth(50);

        StackPane.setAlignment(
                weaponLabel,
                Pos.CENTER
        );

        StackPane.setAlignment(
                cooldownBar,
                Pos.BOTTOM_CENTER
        );

        slot.getChildren().addAll(
                weaponLabel,
                cooldownBar
        );
    }

    private void updatePlayerHud() {
        applyEquippedArtifactHpBonuses();
        refreshPlayerHpDisplay();

        if (characterPanelVisible) {
            refreshCharacterPanel();
        }
    }

    /** Обновляет именно верхнюю полоску HP без повторного применения бонусов. */
    private void refreshPlayerHpDisplay() {
        double ratio = playerMaxHp <= 0 ? 0 : playerHp / playerMaxHp;
        ratio = Math.max(0, Math.min(1, ratio));
        playerHpBar.setProgress(ratio);
        playerHpLabel.setText(
                "HP   " + (int) playerHp + " / " + (int) playerMaxHp
        );
    }

    private void updateBossHud() {

        if (currentLocation == null
                || !"dungeon".equals(currentLocation.getId())) {
            bossHud.setVisible(false);
            bossHud.setManaged(false);
            return;
        }

        Boss activeBoss = null;

        for (Boss boss : bossManager.getBosses()) {
            if (boss != null && boss.isAlive()) {
                activeBoss = boss;
                break;
            }
        }

        if (activeBoss == null) {
            bossHud.setVisible(false);
            bossHud.setManaged(false);
            return;
        }

        bossHud.setVisible(true);
        bossHud.setManaged(true);

        double ratio =
                activeBoss.getMaxHp() <= 0
                        ? 0
                        : activeBoss.getHp() / activeBoss.getMaxHp();

        ratio = Math.max(0, Math.min(1, ratio));

        bossNameLabel.setText(activeBoss.getName());
        bossHpLabel.setText(
                (int) activeBoss.getHp()
                        + " / "
                        + (int) activeBoss.getMaxHp()
        );
        bossHpBar.setProgress(ratio);
    }

    private void updateWeaponHud() {

        Weapon melee = inventory.getEquippedMeleeWeapon();
        Weapon ranged = inventory.getEquippedRangedWeapon();

        meleeWeaponLabel.setText(
                melee == null ? "—" : melee.getName()
        );
        rangedWeaponLabel.setText(
                ranged == null ? "—" : ranged.getName()
        );

        boolean meleeSelected = meleeActive;

        meleeWeaponSlot.setStyle(weaponSlotStyle(meleeSelected));
        rangedWeaponSlot.setStyle(weaponSlotStyle(!meleeSelected));

        double interval = getAttackInterval();
        double progress =
                interval <= 0.0001
                        ? 1.0
                        : 1.0 - Math.max(0.0, Math.min(1.0, attackCooldown / interval));

        meleeCooldownBar.setProgress(
                meleeSelected ? progress : 1.0
        );
        rangedCooldownBar.setProgress(
                !meleeSelected ? progress : 1.0
        );
    }

    private String weaponSlotStyle(boolean active) {
        return active
                ? """
                  -fx-background-color: rgba(55,55,65,0.95);
                  -fx-background-radius: 8;
                  -fx-border-color: white;
                  -fx-border-width: 2;
                  -fx-border-radius: 8;
                  """
                : """
                  -fx-background-color: rgba(20,20,25,0.78);
                  -fx-background-radius: 8;
                  -fx-border-color: rgba(255,255,255,0.25);
                  -fx-border-width: 1;
                  -fx-border-radius: 8;
                  """;
    }

    private void setGameMenuVisible(
            boolean visible
    ) {
        gameMenuVisible = visible;

        gameMenu.setVisible(
                visible
        );

        gameMenu.setManaged(
                visible
        );
    }

    private void applySave(
            SaveData data
    ) {

        if (data == null) {
            return;
        }

        /*
         * PLAYER
         */

        localPlayer.serverX =
                data.player.x;

        localPlayer.serverY =
                data.player.y;

        localPlayer.renderX =
                data.player.x;

        localPlayer.renderY =
                data.player.y;

        localPlayer.locationX =
                data.player.locationX;

        localPlayer.locationY =
                data.player.locationY;

        // Режимы восстанавливаем до обработки HP: артефакты не влияют
        // на здоровье в режиме 1 HP.
        oneHpMode = data.player.oneHpMode;
        hardcoreMode = data.player.hardcoreMode;

        playerHp = data.player.hp;
        playerMaxHp = data.player.maxHp;

        /*
         * В сохранениях v2 хранятся эффективные HP с учётом экипированных
         * артефактов. В v1 HP могли храниться как базовые значения.
         */
        restoreWeaponsFromSave(data);
        if (oneHpMode) {
            playerHp = 1f;
            playerMaxHp = 1f;
        } else {
            int restoredArtifactHpBonus = getEquippedArtifactHpBonus();
            if (data.version >= 2) {
                playerMaxHp = Math.max(1f, playerMaxHp - restoredArtifactHpBonus);
            } else {
                playerMaxHp = Math.max(1f, playerMaxHp);
            }
            // Текущее HP — абсолютное значение; при повторном применении
            // бонуса максимума оно не должно уменьшаться/увеличиваться второй раз.
            playerHp = Math.max(0f, Math.min(playerHp, playerMaxHp + restoredArtifactHpBonus));
        }
        appliedArtifactHpBonus = 0;

        /*
         * ACTIVE WEAPON
         */

        meleeActive =
                data.meleeActive;

        /*
         * BOSSES
         */

        for (String bossId :
                data.defeatedBosses) {

            Boss boss =
                    bossManager.getBoss(
                            bossId
                    );

            if (boss != null) {
                boss.setHp(0f);
            }
        }

        oneHpMode = data.player.oneHpMode;
        hardcoreMode = data.player.hardcoreMode;

        // Восстанавливаем текущую локацию.
        String savedLocationId = data.world.currentLocationId;

        if (savedLocationId != null) {
            Location savedLocation = locations.get(savedLocationId);

            if (savedLocation != null) {
                currentLocation = savedLocation;

                worldWidth = savedLocation.getWidth();
                worldHeight = savedLocation.getHeight();
            } else {
                System.err.println(
                        "Не найдена сохранённая локация: "
                                + savedLocationId
                );
            }
        }

        refreshCharacterPanel();
    }
    private void restoreWeaponsFromSave(
            SaveData data
    ) {

        inventory.clearWeapons();

        for (String weaponId :
                data.weaponIds) {

            Weapon weapon =
                    createWeaponById(
                            weaponId
                    );

            if (weapon != null) {

                inventory.addWeapon(
                        weapon
                );
            }
        }

        if (data.equippedMelee != null) {

            Weapon weapon =
                    inventory.findWeaponById(
                            data.equippedMelee
                    );

            if (weapon != null) {
                inventory.equipMeleeWeapon(
                        weapon
                );
            }
        }

        if (data.equippedRanged != null) {

            Weapon weapon =
                    inventory.findWeaponById(
                            data.equippedRanged
                    );

            if (weapon != null) {
                inventory.equipRangedWeapon(
                        weapon
                );
            }
        }

        inventory.clearArtifacts();
        if (data.artifactIds != null) {
            for (String artifactId : data.artifactIds) {
                Artifact artifact = Artifact.byId(artifactId);
                if (artifact != null) inventory.addArtifact(artifact);
            }
        }
        if (inventory.getArtifacts().isEmpty()
                && (data.artifactIds == null || data.artifactIds.isEmpty())) {
            for (Artifact artifact : Artifact.all()) inventory.addArtifact(artifact);
        }
        if (data.equippedArtifactIds != null) {
            for (int slot = 0; slot < Math.min(data.equippedArtifactIds.size(),
                    inventory.getArtifactSlotCount()); slot++) {
                String artifactId = data.equippedArtifactIds.get(slot);
                Artifact artifact = inventory.findArtifactById(artifactId);
                if (artifact != null) inventory.equipArtifact(slot, artifact);
            }
        }
    }
    private Weapon createWeaponById(String id) {
        if (id == null) {
            return null;
        }

        return switch (id) {
            case "weighted_claymore" ->
                    WeaponFactory.createWeightedClaymore();

            case "iron_sword" ->
                    WeaponFactory.createIronSword();

            case "angelic_doom" ->
                    WeaponFactory.createAngelicDoom();

            case "gungnir" ->
                    WeaponFactory.createGungnir();

            case "gungnir_launcher" ->
                    WeaponFactory.createGungnirLauncher();

            case "reliable_spear" ->
                    WeaponFactory.createReliableSpear();

            case "mourning" ->
                    WeaponFactory.createMourning();

            case "ice_shooter" ->
                    WeaponFactory.createIceShooter();

            case "doom_star_shooter" ->
                    WeaponFactory.createDoomStarShooter();

            // Новое оружие
            case "material_curse_blade" ->
                    WeaponFactory.createMaterialCurseBlade();

            case "wooden_sword" ->
                    WeaponFactory.createWoodenSword();

            case "magmatic_claymore" ->
                    WeaponFactory.createMagmaticClaymore();

            case "dawn" ->
                    WeaponFactory.createDawn();

            case "zenith" ->
                    WeaponFactory.createZenith();

            case "twilight" ->
                    WeaponFactory.createTwilight();

            case "tomorrow" ->
                    WeaponFactory.createTomorrow();

            default -> {
                System.err.println(
                        "[WeaponFactory] Неизвестный ID оружия: " + id
                );
                yield null;
            }
        };
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    private void setupGameMenu() {

        gameMenu.setPrefWidth(260);

        gameMenu.setPadding(
                new Insets(20)
        );

        gameMenu.setAlignment(
                Pos.CENTER
        );

        gameMenu.setStyle("""
        -fx-background-color: rgba(20, 20, 25, 0.95);
        -fx-background-radius: 12;
        -fx-border-color: rgba(255,255,255,0.25);
        -fx-border-radius: 12;
        -fx-border-width: 1;
        """);

        Button saveButton =
                createMenuButton(
                        "СОХРАНИТЬ"
                );

        Button returnButton =
                createMenuButton("Вернуться");

        Button settingsButton =
                createMenuButton("Настройки");

        Button mainMenuButton =
                createMenuButton(
                        "Выйти в главное меню"
                );

        saveButton.setOnAction(e -> {

            SaveData data =
                    createSaveData(1);

            SaveManager.save(
                    data
            );

            achievementManager.unlock(
                    "first_save"
            );

            infoLabel.setText(
                    "Игра сохранена."
            );
        });

        returnButton.setOnAction(e ->
                setGameMenuVisible(false)
        );

        settingsButton.setOnAction(e -> {
            // Пока без функционала.
        });

        mainMenuButton.setOnAction(e -> {

            setGameMenuVisible(false);

            /*
             * Останавливаем игровой цикл,
             * чтобы мир не продолжал обновляться
             * после выхода.
             */
            stopGameLoop();

            /*
             * Переход в главное меню.
             */
            sceneManager.saveCurrentGame();
            sceneManager.show(SceneType.MAIN_MENU);
        });

        gameMenu.getChildren().addAll(
                returnButton,
                settingsButton,
                new Separator(),
                mainMenuButton
        );

        gameMenu.setVisible(false);

        root.getChildren().add(
                gameMenu
        );

        gameMenu.layoutXProperty().bind(
                root.widthProperty()
                        .subtract(gameMenu.widthProperty())
                        .divide(2)
        );

        gameMenu.layoutYProperty().bind(
                root.heightProperty()
                        .subtract(gameMenu.heightProperty())
                        .divide(2)
        );
    }

    private void setupMenuButton() {

        menuButton.setPrefSize(
                55,
                55
        );

        menuButton.setStyle("""
        -fx-background-color: rgba(20,20,25,0.85);
        -fx-background-radius: 10;
        -fx-text-fill: white;
        -fx-font-size: 24px;
        -fx-font-weight: bold;
        """);

        menuButton.setOnAction(e ->
                setGameMenuVisible(
                        !gameMenuVisible
                )
        );

        root.getChildren().add(
                menuButton
        );

        menuButton.layoutXProperty().bind(
                root.widthProperty().subtract(75)
        );

        menuButton.setLayoutY(20);
    }
    private void setupCharacterButton() {

        characterButton.setPrefSize(
                55,
                55
        );

        characterButton.setStyle("""
        -fx-background-color: rgba(20,20,25,0.85);
        -fx-background-radius: 10;
        -fx-text-fill: white;
        -fx-font-size: 24px;
        -fx-font-weight: bold;
        """);

        characterButton.setOnAction(e ->
                setCharacterPanelVisible(
                        !characterPanelVisible
                )
        );

        root.getChildren().add(
                characterButton
        );

        characterButton.layoutXProperty().bind(
                root.widthProperty().subtract(75)
        );

        characterButton.setLayoutY(90);
    }
    private void setupCharacterPanel() {

        characterPanel.setPrefWidth(1200);
        characterPanel.setPrefHeight(650);
        characterPanel.setPadding(new Insets(22));
        characterPanel.setAlignment(Pos.TOP_LEFT);

        characterPanel.setStyle("""
        -fx-background-color: rgba(18,18,24,0.98);
        -fx-background-radius: 14;
        -fx-border-color: rgba(255,255,255,0.25);
        -fx-border-radius: 14;
        -fx-border-width: 1;
        """);

        Label title = new Label("ПЕРСОНАЖ");
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label hint = new Label("B — закрыть");
        hint.setTextFill(Color.GRAY);
        hint.setStyle("-fx-font-size: 11px;");

        HBox header = new HBox(12, title, hint);
        header.setAlignment(Pos.CENTER_LEFT);

        /* =================================================
         * CHARACTER STATS
         * ================================================= */

        VBox stats = new VBox(8);
        stats.setPrefWidth(220);
        stats.setPadding(new Insets(14));
        stats.setStyle(panelSectionStyle());

        Label statsTitle = createSectionTitle("ХАРАКТЕРИСТИКИ");

        characterHpValue.setTextFill(Color.WHITE);
        characterAttackValue.setTextFill(Color.WHITE);
        characterSpeedValue.setTextFill(Color.WHITE);

        stats.getChildren().addAll(
                statsTitle,
                characterHpValue,
                characterAttackValue,
                characterSpeedValue
        );

        /* =================================================
         * EQUIPMENT
         * ================================================= */

        VBox equipment = new VBox(10);
        equipment.setPrefWidth(190);
        equipment.setPadding(new Insets(14));
        equipment.setStyle(panelSectionStyle());

        Label equipmentTitle =
                createSectionTitle("ЭКИПИРОВКА");

        equippedMeleeValue.setTextFill(Color.WHITE);
        equippedRangedValue.setTextFill(Color.WHITE);

        equippedMeleeValue.setWrapText(true);
        equippedRangedValue.setWrapText(true);

        equippedMeleeValue.setAlignment(Pos.CENTER);
        equippedRangedValue.setAlignment(Pos.CENTER);

        // Оружейные слоты теперь квадратные и визуально совпадают с ячейками инвентаря.
        equippedMeleeValue.setPrefSize(66, 66);
        equippedMeleeValue.setMinSize(66, 66);
        equippedMeleeValue.setMaxSize(66, 66);
        equippedRangedValue.setPrefSize(66, 66);
        equippedRangedValue.setMinSize(66, 66);
        equippedRangedValue.setMaxSize(66, 66);

        equippedMeleeValue.setStyle(
                equipmentSlotStyle(
                        inventory.getEquippedMeleeWeapon()
                )
        );

        equippedRangedValue.setStyle(
                equipmentSlotStyle(
                        inventory.getEquippedRangedWeapon()
                )
        );

        setupEquipmentDropTarget(
                equippedMeleeValue,
                WeaponType.MELEE
        );

        setupEquipmentDropTarget(
                equippedRangedValue,
                WeaponType.RANGED
        );

        // Просмотр описания оружия в слоте ближнего боя.
        equippedMeleeValue.setOnMouseClicked(event -> {
            Weapon weapon = inventory.getEquippedMeleeWeapon();

            if (weapon != null) {
                showItemDetails(weapon);
            } else {
                clearItemDetails();
            }
        });

        // Просмотр описания оружия в слоте дальнего боя.
        equippedRangedValue.setOnMouseClicked(event -> {
            Weapon weapon = inventory.getEquippedRangedWeapon();

            if (weapon != null) {
                showItemDetails(weapon);
            } else {
                clearItemDetails();
            }
        });

        VBox artifactSlotsBox = new VBox(8);
        Label artifactTitle = createSectionTitle("АРТЕФАКТЫ");
        GridPane artifactSlotsGrid = new GridPane();
        artifactSlotsGrid.setHgap(8);
        artifactSlotsGrid.setVgap(8);
        for (int slot = 0; slot < inventory.getArtifactSlotCount(); slot++) {
            StackPane artifactSlotNode = createArtifactSlot(slot);
            artifactSlotNodes[slot] = artifactSlotNode;
            refreshArtifactSlot(slot);
            artifactSlotsGrid.add(artifactSlotNode, slot % 2, slot / 2);
        }
        artifactSlotsBox.getChildren().addAll(artifactTitle, artifactSlotsGrid);

        equipment.getChildren().addAll(
                equipmentTitle, equippedMeleeValue, equippedRangedValue, artifactSlotsBox
        );

        /* =================================================
         * INVENTORY
         * ================================================= */

        VBox inventoryBox = new VBox(10);
        inventoryBox.setPrefWidth(300);
        inventoryBox.setPadding(new Insets(14));
        inventoryBox.setStyle(panelSectionStyle());

        Label inventoryTitle = createSectionTitle("ИНВЕНТАРЬ");

        TextField inventorySearchField = new TextField();
        inventorySearchField.setPromptText("Поиск предмета...");
        inventorySearchField.setMaxWidth(Double.MAX_VALUE);
        inventorySearchField.setStyle("-fx-background-color: rgba(15,15,20,0.9); -fx-text-fill: white; -fx-prompt-text-fill: #888; -fx-border-color: rgba(255,255,255,0.14); -fx-border-radius: 5; -fx-background-radius: 5;");
        inventorySearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            inventorySearch = newValue == null ? "" : newValue.trim().toLowerCase(Locale.ROOT);
            rebuildInventoryGrid();
        });

        ComboBox<String> inventoryFilterBox = new ComboBox<>();
        inventoryFilterBox.getItems().addAll("Все предметы", "Оружие", "Ближний бой", "Дальний бой", "Артефакты");
        inventoryFilterBox.setValue(inventoryFilter);
        inventoryFilterBox.setMaxWidth(Double.MAX_VALUE);
        inventoryFilterBox.setOnAction(event -> {
            inventoryFilter = inventoryFilterBox.getValue() == null ? "Все предметы" : inventoryFilterBox.getValue();
            rebuildInventoryGrid();
        });

        ComboBox<String> inventorySortBox = new ComboBox<>();
        inventorySortBox.getItems().addAll("По порядку", "Название А–Я", "Название Я–А", "Сила ↓", "Сила ↑");
        inventorySortBox.setValue(inventorySort);
        inventorySortBox.setMaxWidth(Double.MAX_VALUE);
        inventorySortBox.setOnAction(event -> {
            inventorySort = inventorySortBox.getValue() == null ? "По порядку" : inventorySortBox.getValue();
            rebuildInventoryGrid();
        });

        HBox filterSortRow = new HBox(6, inventoryFilterBox, inventorySortBox);
        filterSortRow.setFillHeight(true);
        inventoryFilterBox.setPrefWidth(135);
        inventorySortBox.setPrefWidth(135);
        for (ComboBox<String> box : List.of(inventoryFilterBox, inventorySortBox)) {
            box.setStyle("-fx-background-color: rgba(20,20,27,0.95); -fx-text-fill: white;");
        }

        inventoryGrid.setHgap(7);
        inventoryGrid.setVgap(7);
        inventoryGrid.setPadding(new Insets(4));
        // Оружие и артефакты отображаются в одной общей сетке.
        inventoryGrid.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                String value = event.getDragboard().getString();
                if (value != null && (value.startsWith("equipped-artifact:")
                        || value.startsWith("equipped-melee:")
                        || value.startsWith("equipped-ranged:"))) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
            }
            event.consume();
        });
        inventoryGrid.setOnDragDropped(event -> {
            boolean success = unequipDraggedItemToInventory(event.getDragboard().getString());
            event.setDropCompleted(success);
            event.consume();
        });

        ScrollPane inventoryScroll = new ScrollPane(inventoryGrid);

        inventoryScroll.setFitToWidth(true);
        inventoryScroll.setFitToHeight(false);
        inventoryScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        inventoryScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        inventoryScroll.setPrefViewportHeight(280);
        inventoryScroll.setMinHeight(0);
        inventoryScroll.setMaxHeight(280);

        inventoryScroll.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;" +
                        "-fx-border-color: transparent;"
        );

        inventoryScroll.getContent().setStyle(
                "-fx-background-color: transparent;"
        );

        inventoryBox.getChildren().addAll(inventoryTitle, inventorySearchField, filterSortRow, inventoryScroll);

        /* =================================================
         * ITEM DETAILS
         * ================================================= */

        itemDetails.setPrefWidth(360);
        itemDetails.setPadding(new Insets(16));
        itemDetails.setStyle(panelSectionStyle());

        itemDetailsTitle.setTextFill(Color.WHITE);
        itemDetailsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        itemDetailsQuality.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        itemDetailsAttack.setTextFill(Color.WHITE);
        itemDetailsCharacteristics.setTextFill(Color.LIGHTGRAY);
        itemDetailsMain.setTextFill(Color.WHITE);
        itemDetailsLore.setTextFill(Color.rgb(170,170,180));

        itemDetailsCharacteristics.setWrapText(true);
        itemDetailsMain.setWrapText(true);
        itemDetailsLore.setWrapText(true);

        itemDetails.getChildren().addAll(
                itemDetailsTitle,
                itemDetailsQuality,
                new Separator(),
                itemDetailsAttack,
                itemDetailsCharacteristics,
                itemDetailsMain,
                itemDetailsLore
        );

        clearItemDetails();

        HBox content = new HBox(12, stats, equipment, inventoryBox, itemDetails);
        content.setAlignment(Pos.TOP_LEFT);

        Button close = createMenuButton("Закрыть");
        close.setPrefWidth(160);
        close.setOnAction(e -> setCharacterPanelVisible(false));

        HBox footer = new HBox(close);
        footer.setAlignment(Pos.CENTER_RIGHT);

        characterPanel.getChildren().addAll(
                header,
                new Separator(),
                content,
                footer
        );

        characterPanel.setVisible(false);
        characterPanel.setManaged(false);

        root.getChildren().add(characterPanel);

        characterPanel.layoutXProperty().bind(
                root.widthProperty()
                        .subtract(characterPanel.widthProperty())
                        .divide(2)
        );

        characterPanel.layoutYProperty().bind(
                root.heightProperty()
                        .subtract(characterPanel.heightProperty())
                        .divide(2)
        );

        updateCharacterPanel();
        rebuildInventoryGrid();
    }

    private StackPane createArtifactSlot(int slotIndex) {
        StackPane slot = new StackPane();
        slot.setPrefSize(66, 66);
        slot.setMinSize(66, 66);
        slot.setMaxSize(66, 66);
        slot.setOnDragOver(event -> {
            if (event.getDragboard().hasString() && isArtifactDrag(event.getDragboard().getString()))
                event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });
        slot.setOnDragDropped(event -> {
            boolean success = handleArtifactDropOnSlot(slotIndex, event.getDragboard().getString());
            event.setDropCompleted(success);
            event.consume();
        });
        slot.setOnDragDetected(event -> {
            if (inventory.getEquippedArtifact(slotIndex) == null) return;
            Dragboard dragboard = slot.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("equipped-artifact:" + slotIndex);
            dragboard.setContent(content);
            event.consume();
        });
        slot.setOnMouseClicked(event -> {
            Artifact artifact = inventory.getEquippedArtifact(slotIndex);
            if (artifact != null) showArtifactDetails(artifact);
        });
        refreshArtifactSlot(slotIndex);
        return slot;
    }

    private boolean isArtifactDrag(String value) {
        return value != null && (value.startsWith("artifact:") || value.startsWith("equipped-artifact:"));
    }

    private boolean handleArtifactDropOnSlot(int targetSlot, String dragValue) {
        if (dragValue == null) return false;
        boolean success = false;
        if (dragValue.startsWith("artifact:")) {
            Artifact artifact = inventory.findArtifactById(dragValue.substring("artifact:".length()));
            success = artifact != null && inventory.equipArtifact(targetSlot, artifact);
        } else if (dragValue.startsWith("equipped-artifact:")) {
            try {
                int sourceSlot = Integer.parseInt(dragValue.substring("equipped-artifact:".length()));
                Artifact moving = inventory.getEquippedArtifact(sourceSlot);
                if (moving != null && sourceSlot != targetSlot) {
                    Artifact displaced = inventory.getEquippedArtifact(targetSlot);
                    inventory.unequipArtifact(sourceSlot);
                    inventory.unequipArtifact(targetSlot);
                    if (displaced != null) inventory.equipArtifact(sourceSlot, displaced);
                    success = inventory.equipArtifact(targetSlot, moving);
                }
            } catch (NumberFormatException ignored) { }
        }
        if (success) {
            saveCurrentLoadout();
            refreshCharacterPanel();
        }
        return success;
    }

    private void refreshArtifactSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= artifactSlotNodes.length || artifactSlotNodes[slotIndex] == null) return;
        StackPane slot = artifactSlotNodes[slotIndex];
        slot.getChildren().clear();
        Artifact artifact = inventory.getEquippedArtifact(slotIndex);
        Label icon = new Label(artifact == null ? "+" : artifactIcon(artifact));
        icon.setTextFill(artifact == null ? Color.GRAY : artifactColor(artifact));
        icon.setStyle("-fx-font-size: 25px; -fx-font-weight: bold;");
        slot.getChildren().add(icon);
        if (artifact != null) {
            Label number = new Label(String.valueOf(slotIndex + 1));
            number.setTextFill(Color.GRAY);
            number.setStyle("-fx-font-size: 9px;");
            StackPane.setAlignment(number, Pos.TOP_LEFT);
            StackPane.setMargin(number, new Insets(4));
            slot.getChildren().add(number);
            javafx.scene.control.Tooltip.install(slot, new javafx.scene.control.Tooltip(artifact.getDisplayName() + "\n" + artifact.getRarity() + "\n" + artifact.getDescription()));
        } else {
            javafx.scene.control.Tooltip.install(slot, new javafx.scene.control.Tooltip("Слот артефакта " + (slotIndex + 1) + ". Перетащите сюда артефакт из инвентаря."));
        }
        slot.setStyle(artifactSlotStyle(artifact));
    }

    private String artifactIcon(Artifact artifact) {
        switch (artifact) {
            case WOODEN_BUCKLER: return "▰";
            case LUCKY_CLOVER: return "♣";
            case SHARP_MOTIVATIONAL_AMULET: return "♦";
            case RELIABLE_SHIELD: return "⬟";
            case WORN_ETHEREAL_DOLL: return "♙";
            case WORN_BLOOD_CHALICE: return "♜";
            case WORN_LIGHTNING_BRACELET: return "ϟ";
            case TIN_WALL: return "◖";
            default: return "◆";
        }
    }

    /**
     * Единая палитра качества для оружия и артефактов.
     * У артефактов названия редкостей отличаются, поэтому приводим их
     * к тем же шести цветовым уровням, что используются в qualityColor().
     */
    private Color artifactColor(Artifact artifact) {
        if (artifact == null) return Color.GRAY;
        return switch (artifact.getRarity()) {
            case "Слабый" -> Color.web("#9E9E9E");       // уровень 0 — серый
            case "Классический" -> Color.web("#7FFFC4"); // уровень 1 — мятный
            case "Редкий" -> Color.web("#6EC6FF");       // уровень 2 — голубой
            case "Элитный" -> Color.web("#4B5DFF");      // уровень 3 — синий
            case "Могущественный" -> Color.web("#A855F7"); // уровень 4 — фиолетовый
            case "Божественный" -> Color.web("#E53935"); // уровень 5 — красный
            default -> Color.WHITE;
        };
    }

    private String artifactSlotStyle(Artifact artifact) {
        String border = artifact == null
                ? "rgba(255,255,255,0.10)"
                : toCss(artifactColor(artifact));
        return "-fx-background-color: rgba(20,20,27,0.95);"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: 2;"
                + "-fx-border-radius: 8;";
    }

    private void showArtifactDetails(Artifact artifact) {
        itemDetailsTitle.setText(artifact.getDisplayName());
        itemDetailsQuality.setText(artifact.getRarity() + " • стоимость " + artifact.getCost());
        itemDetailsQuality.setTextFill(artifactColor(artifact));
        itemDetailsAttack.setText("Защита: " + artifact.getDefense() + " | baseDamage: +"
                + formatNumber(artifact.getBaseDamageBonus()) + " | ATK: " + formatNumber(artifact.getAttackBonus()));
        itemDetailsCharacteristics.setText(artifact.getDescription());
        itemDetailsMain.setText("Перетащите предмет в квадратный слот артефакта, чтобы экипировать.");
        itemDetailsLore.setText("");
    }

    private String panelSectionStyle() {
        return """
        -fx-background-color: rgba(28,28,36,0.92);
        -fx-background-radius: 10;
        -fx-border-color: rgba(255,255,255,0.10);
        -fx-border-radius: 10;
        -fx-border-width: 1;
        """;
    }
    private String equipmentSlotStyle(
            Weapon weapon
    ) {
        String border =
                weapon == null
                        ? "rgba(255,255,255,0.15)"
                        : toCss(
                        qualityColor(
                                weapon.getQuality()
                        )
                );

        return "-fx-background-color: rgba(20,20,27,0.95);"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: 2;"
                + "-fx-border-radius: 8;"
                + "-fx-padding: 8;";
    }
    private void setupEquipmentDropTarget(
            Label slot,
            WeaponType acceptedType
    ) {
        slot.setOnDragDetected(event -> {
            Weapon equipped = acceptedType == WeaponType.MELEE
                    ? inventory.getEquippedMeleeWeapon()
                    : inventory.getEquippedRangedWeapon();
            if (equipped == null) return;
            Dragboard dragboard = slot.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(acceptedType == WeaponType.MELEE ? "equipped-melee:" + equipped.getId()
                    : "equipped-ranged:" + equipped.getId());
            dragboard.setContent(content);
            event.consume();
        });

        slot.setOnDragOver(event -> {

            if (event.getGestureSource() != slot
                    && event.getDragboard().hasString()) {

                try {
                    int index = Integer.parseInt(
                            event.getDragboard().getString()
                    );

                    Weapon weapon =
                            inventory.getWeapon(index);

                    if (weapon != null
                            && weapon.getType() == acceptedType) {

                        event.acceptTransferModes(
                                TransferMode.MOVE
                        );
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            event.consume();
        });

        slot.setOnDragDropped(event -> {

            boolean success = false;

            if (event.getDragboard().hasString()) {

                try {
                    int index = Integer.parseInt(
                            event.getDragboard().getString()
                    );

                    Weapon weapon =
                            inventory.getWeapon(index);

                    if (weapon != null
                            && weapon.getType() == acceptedType) {

                        if (acceptedType == WeaponType.MELEE) {

                            success =
                                    inventory.equipMeleeWeapon(
                                            weapon
                                    );

                        } else if (
                                acceptedType == WeaponType.RANGED
                        ) {

                            success =
                                    inventory.equipRangedWeapon(
                                            weapon
                                    );
                        }

                        if (success) {
                            saveCurrentLoadout();
                            refreshCharacterPanel();
                        }
                    }

                } catch (NumberFormatException ignored) {
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });

        slot.setOnMouseEntered(event ->
                slot.setStyle(
                        equipmentSlotStyle(
                                acceptedType == WeaponType.MELEE
                                        ? inventory.getEquippedMeleeWeapon()
                                        : inventory.getEquippedRangedWeapon()
                        )
                                + "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.25), 10, 0.3, 0, 0);"
                )
        );

        slot.setOnMouseExited(event ->
                slot.setStyle(
                        equipmentSlotStyle(
                                acceptedType == WeaponType.MELEE
                                        ? inventory.getEquippedMeleeWeapon()
                                        : inventory.getEquippedRangedWeapon()
                        )
                )
        );
    }

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.LIGHTGRAY);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        return label;
    }

    private void updateCharacterPanel() {
        characterHpValue.setText(
                "HP: " + (int) playerHp + " / " + (int) playerMaxHp
        );
        characterAttackValue.setText(
                "Атака: " + String.format(
                        java.util.Locale.ROOT,
                        "%.1f",
                        getEffectivePlayerAttack()
                ) + " (артефакты)"
        );
        characterSpeedValue.setText(
                "Скорость: " + (int) LOCAL_PLAYER_SPEED
        );

        Weapon melee = inventory.getEquippedMeleeWeapon();
        Weapon ranged = inventory.getEquippedRangedWeapon();

        equippedMeleeValue.setText(
                melee == null ? "+" : "⚔\n" + melee.getName()
        );
        equippedRangedValue.setText(
                ranged == null ? "+" : "➤\n" + ranged.getName()
        );
        equippedMeleeValue.setTextFill(melee == null ? Color.GRAY : qualityColor(melee.getQuality()));
        equippedRangedValue.setTextFill(ranged == null ? Color.GRAY : qualityColor(ranged.getQuality()));
        equippedMeleeValue.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; " + equipmentSlotStyle(melee));
        equippedRangedValue.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; " + equipmentSlotStyle(ranged));
    }

    private void rebuildInventoryGrid() {
        inventoryGrid.getChildren().clear();
        List<Object> visibleItems = new ArrayList<>();
        String search = inventorySearch == null ? "" : inventorySearch;

        // Собираем только отображаемые предметы; экипированные остаются в модели,
        // но не показываются второй раз в сетке.
        for (int index = 0; index < inventory.getWeaponCount(); index++) {
            Weapon weapon = inventory.getWeapon(index);
            if (weapon == null
                    || weapon == inventory.getEquippedMeleeWeapon()
                    || weapon == inventory.getEquippedRangedWeapon()) continue;
            if (!matchesInventoryFilter(weapon) || !getInventoryItemName(weapon).toLowerCase(Locale.ROOT).contains(search)) continue;
            visibleItems.add(weapon);
        }

        for (Artifact artifact : inventory.getArtifacts()) {
            if (artifact == null || isArtifactEquipped(artifact)) continue;
            if (!matchesInventoryFilter(artifact) || !getInventoryItemName(artifact).toLowerCase(Locale.ROOT).contains(search)) continue;
            visibleItems.add(artifact);
        }

        Comparator<Object> comparator = switch (inventorySort) {
            case "Название А–Я" -> Comparator.comparing(this::getInventoryItemName, String.CASE_INSENSITIVE_ORDER);
            case "Название Я–А" -> Comparator.comparing(this::getInventoryItemName, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Сила ↓" -> Comparator.comparingDouble(this::getInventoryItemScore).reversed();
            case "Сила ↑" -> Comparator.comparingDouble(this::getInventoryItemScore);
            default -> null;
        };
        if (comparator != null) visibleItems.sort(comparator);

        int displayIndex = 0;
        for (Object item : visibleItems) {
            StackPane cell;
            if (item instanceof Weapon weapon) {
                int originalIndex = inventory.getWeapons().indexOf(weapon);
                cell = createInventoryCell(weapon, originalIndex);
            } else {
                Artifact artifact = (Artifact) item;
                int originalIndex = inventory.getArtifacts().indexOf(artifact);
                cell = createArtifactInventoryCell(artifact, originalIndex);
            }
            installInventoryDropTarget(cell);
            inventoryGrid.add(cell, displayIndex % 3, displayIndex / 3);
            displayIndex++;
        }

        if (visibleItems.isEmpty()) {
            Label empty = new Label(search.isEmpty() ? "Нет предметов в этой категории" : "Ничего не найдено");
            empty.setTextFill(Color.GRAY);
            empty.setStyle("-fx-font-size: 11px;");
            inventoryGrid.add(empty, 0, 0, 3, 1);
        }
    }

    private boolean matchesInventoryFilter(Object item) {
        return switch (inventoryFilter) {
            case "Оружие" -> item instanceof Weapon;
            case "Ближний бой" -> item instanceof Weapon weapon && weapon.getType() == WeaponType.MELEE;
            case "Дальний бой" -> item instanceof Weapon weapon && weapon.getType() == WeaponType.RANGED;
            case "Артефакты" -> item instanceof Artifact;
            default -> true;
        };
    }

    private String getInventoryItemName(Object item) {
        if (item instanceof Weapon weapon) return weapon.getName();
        if (item instanceof Artifact artifact) return artifact.getDisplayName();
        return "";
    }

    // Для оружия используется атака, для артефактов — стоимость как стабильный
    // числовой показатель, поскольку у артефактов нет единого параметра силы.
    private double getInventoryItemScore(Object item) {
        if (item instanceof Weapon weapon) return weapon.getAttack();
        if (item instanceof Artifact artifact) return artifact.getCost();
        return 0.0;
    }

    private void installInventoryDropTarget(StackPane cell) {
        cell.setOnDragOver(event -> {
            String value = event.getDragboard().getString();
            if (value != null && (value.startsWith("equipped-artifact:")
                    || value.startsWith("equipped-melee:")
                    || value.startsWith("equipped-ranged:"))) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        cell.setOnDragDropped(event -> {
            boolean success = unequipDraggedItemToInventory(event.getDragboard().getString());
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private boolean unequipDraggedItemToInventory(String value) {
        if (value == null) return false;
        boolean success = false;
        try {
            if (value.startsWith("equipped-artifact:")) {
                int slot = Integer.parseInt(value.substring("equipped-artifact:".length()));
                if (inventory.getEquippedArtifact(slot) != null) {
                    inventory.unequipArtifact(slot);
                    success = true;
                }
            } else if (value.startsWith("equipped-melee:")) {
                inventory.unequipMeleeWeapon();
                success = true;
            } else if (value.startsWith("equipped-ranged:")) {
                inventory.unequipRangedWeapon();
                success = true;
            }
        } catch (NumberFormatException ignored) { }
        if (success) {
            saveCurrentLoadout();
            refreshCharacterPanel();
        }
        return success;
    }

    private StackPane createArtifactInventoryCell(Artifact artifact, int index) {
        StackPane cell = new StackPane();
        cell.setPrefSize(66, 66);
        cell.setMinSize(66, 66);
        cell.setMaxSize(66, 66);
        Label icon = new Label(artifactIcon(artifact));
        icon.setTextFill(artifactColor(artifact));
        icon.setStyle("-fx-font-size: 25px; -fx-font-weight: bold;");

        Label number = new Label(String.valueOf(index + 1));
        number.setTextFill(Color.GRAY);
        number.setStyle("-fx-font-size: 9px;");
        StackPane.setAlignment(number, Pos.TOP_LEFT);
        StackPane.setMargin(number, new Insets(5));

        // Название артефакта оформлено так же, как название оружия в сетке.
        Label name = new Label(artifact.getDisplayName());
        name.setTextFill(Color.WHITE);
        name.setStyle("-fx-font-size: 9px; -fx-font-weight: bold;");
        name.setWrapText(true);
        name.setMaxWidth(58);
        StackPane.setAlignment(name, Pos.BOTTOM_CENTER);
        StackPane.setMargin(name, new Insets(4));

        cell.getChildren().addAll(icon, number, name);
        cell.setStyle(artifactSlotStyle(artifact));
        javafx.scene.control.Tooltip.install(cell, new javafx.scene.control.Tooltip(artifact.getDisplayName() + "\n" + artifact.getRarity()
                + " • стоимость " + artifact.getCost() + "\n" + artifact.getDescription()));
        cell.setOnMouseClicked(event -> showArtifactDetails(artifact));
        cell.setOnDragDetected(event -> {
            Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("artifact:" + artifact.getId());
            dragboard.setContent(content);
            event.consume();
        });
        cell.setOnDragOver(event -> {
            String value = event.getDragboard().getString();
            if (value != null && value.startsWith("equipped-artifact:")) event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });
        cell.setOnDragDropped(event -> {
            boolean success = false;
            String value = event.getDragboard().getString();
            if (value != null && value.startsWith("equipped-artifact:")) {
                try {
                    int slot = Integer.parseInt(value.substring("equipped-artifact:".length()));
                    if (inventory.getEquippedArtifact(slot) != null) {
                        inventory.unequipArtifact(slot);
                        success = true;
                        saveCurrentLoadout();
                        refreshCharacterPanel();
                    }
                } catch (NumberFormatException ignored) { }
            }
            event.setDropCompleted(success);
            event.consume();
        });
        return cell;
    }

    private StackPane createInventoryCell(Weapon weapon, int index) {
        StackPane cell = new StackPane();
        cell.setPrefSize(66, 66);
        cell.setMinSize(66, 66);
        cell.setMaxSize(66, 66);

        if (weapon == null) {
            cell.setStyle(inventoryCellStyle(null));
            return cell;
        }

        Label icon = new Label(
                weapon.getType() == WeaponType.MELEE ? "⚔" : "➤"
        );
        icon.setTextFill(qualityColor(weapon.getQuality()));
        icon.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label number = new Label(String.valueOf(index + 1));
        number.setTextFill(Color.GRAY);
        number.setStyle("-fx-font-size: 10px;");
        StackPane.setAlignment(number, Pos.TOP_LEFT);
        StackPane.setMargin(number, new Insets(5));

        Label name = new Label(weapon.getName());
        name.setTextFill(Color.WHITE);
        name.setStyle("-fx-font-size: 9px; -fx-font-weight: bold;");
        name.setWrapText(true);
        name.setMaxWidth(58);
        StackPane.setAlignment(name, Pos.BOTTOM_CENTER);
        StackPane.setMargin(name, new Insets(4));

        cell.getChildren().addAll(icon, number, name);
        cell.setStyle(inventoryCellStyle(weapon));

        cell.setOnMouseClicked(
                event -> showItemDetails(weapon)
        );

        cell.setOnDragDetected(event -> {

            Dragboard dragboard =
                    cell.startDragAndDrop(
                            TransferMode.MOVE
                    );

            ClipboardContent content =
                    new ClipboardContent();

            content.putString(
                    String.valueOf(index)
            );

            dragboard.setContent(content);

            event.consume();
        });

        return cell;
    }

    private String inventoryCellStyle(Weapon weapon) {
        String border = weapon == null
                ? "rgba(255,255,255,0.10)"
                : toCss(qualityColor(weapon.getQuality()));

        return "-fx-background-color: rgba(20,20,27,0.95);"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: 2;"
                + "-fx-border-radius: 8;";
    }

    private void showItemDetails(Weapon weapon) {
        itemDetailsTitle.setText(weapon.getName());

        ItemQuality quality = weapon.getQuality();
        itemDetailsQuality.setText(
                quality.getDisplayName()
                        + " (" + quality.getLevel() + ")"
        );
        itemDetailsQuality.setTextFill(qualityColor(quality));

        itemDetailsAttack.setText(
                formatNumber(weapon.getAttack()) + " ATK"
        );

        StringBuilder characteristics = new StringBuilder();
        characteristics
                .append(weapon.getType() == WeaponType.MELEE
                        ? "Ближний бой" : "Дальний бой")
                .append('\n');
        characteristics.append("Скорость атаки: ")
                .append(formatNumber(weapon.getAttackSpeed()))
                .append("/сек ");
        characteristics.append("(интервал ")
                .append(formatNumber(weapon.getAttackInterval()))
                .append(" сек)\n");


        if (weapon.getPrimaryAttack().isPiercing()) {
            characteristics.append("Прошив\n");
        }

        for (DamagePart part : weapon.getPrimaryAttack().getDamageParts()) {
            if (weapon.getPrimaryAttack().getProjectileCount() > 0) {characteristics.append("Серия выстрелов: ");} else {characteristics.append(weapon.getType() == WeaponType.MELEE
                    ? "Атака: " : "Выстрел: ");}
            characteristics.append(formatNumber(part.getBaseDamage()))
                    .append(" + ")
                    .append(formatNumber(part.getAttackPercent() * 100f))
                    .append("%").append('\n')
                    .append("Наносит ").append(part.getType().name()).append(" урон")
                    .append('\n');
        }

        itemDetailsCharacteristics.setText(characteristics.toString());

        String mainDescription = getWeaponMainDescription(weapon);
        itemDetailsMain.setText(mainDescription);

        itemDetailsLore.setText(
                getWeaponLore(weapon)
        );
    }

    private String getWeaponMainDescription(Weapon weapon) {
        if (weapon.hasPassiveEffect()) {
            Object passive = weapon.getPassiveEffect();
            if (passive instanceof AbilityDefinition ability) {
                return ability.getDescription();
            }
            return String.valueOf(passive);
        }

        return switch (weapon.getId()) {
            case "weighted_claymore" ->
                    "Отнимает слот артефакта, если экипирован";
            case "angelic_doom" ->
                    "Выпускает Солнечные, либо Лунные снаряды (ЛКМ/ПКМ)\n" +
                            "Солнечные: Наносит Огненный урон, при попадании снарядом выпускает круговую площадь, наносящую дополнительно 4 + 15% урона, снаряды прошивают все цели\n" +
                            "Лунные: Наносит Ледяной урон, снижает Скорость цели при каждом попадании на -2 (лимит до -48), либо снижает Защиту на -1 (лимит до -20) (шанс эффектов 50/50)";
            case "mourning" -> "Дополнительно выпускает 4 погребальных снаряда (Пылевым уроном в 50%) по углам квадрата вокруг основного снаряда, при крит ударе выпускает параллельно ещё 4, формируя восьмиугольник\n" +
                    "При падении Пылевых снарядов между всеми снарядами формируются разряды, наносящие 25% Эл урона";
            case "gungnir_launcher" -> "Для использования этим оружием необходимо экипировать копьё в слот ведущего оружия Ближнего боя\n" +
                    "Оружие получает АТК, эквивалентный АТК копья\n" + "Если вышеуказанное копьё - Гунгнир, тогда увеличивает урон на +50%, также значительно увеличит скорость копья в полёте и дальность";
            case "ice_shooter" -> "На формирование снарядов количеством в 30 потребуется 10 секунд\n";
            case "doom_star_shooter" -> "Начало боя: Заряжает звезду\n" +
                    "До первого выстрела: Следующий выстрел будет \"Заряженной звездой\"\n" +
                    "После выстрела \"Заряженной звездой\" переходит на Обычный выстрел\n" +
                    "Можно снова зарядить звезду, удерживая ПКМ в течение 16 секунд (нужно будет над игроком в таком случае показать таймер, сколько секунд осталось) \n";

            default ->
                    " ";
        };
    }

    private String getWeaponLore(Weapon weapon) {
        return switch (weapon.getId()) {
            case "weighted_claymore" ->
                    "Уга-буга";
            case "angelic_doom" ->
                    "Ха-ха, прикольная пушка, правда, автор её пока такой не сделал...";
            case "mourning" -> "Да не нейрослоп моя игра!";
            default ->
                    "...";
        };
    }

    private void clearItemDetails() {
        itemDetailsTitle.setText("Выберите предмет");
        itemDetailsQuality.setText("Нажмите на клетку инвентаря");
        itemDetailsQuality.setTextFill(Color.GRAY);
        itemDetailsAttack.setText("");
        itemDetailsCharacteristics.setText("");
        itemDetailsMain.setText("");
        itemDetailsLore.setText("");
    }

    private Color qualityColor(ItemQuality quality) {
        return switch (quality.getLevel()) {
            case 0 -> Color.web("#9E9E9E");
            case 1 -> Color.web("#7FFFC4");
            case 2 -> Color.web("#6EC6FF");
            case 3 -> Color.web("#4B5DFF");
            case 4 -> Color.web("#A855F7");
            case 5 -> Color.web("#E53935");
            default -> Color.WHITE;
        };
    }

    private String toCss(Color color) {
        return String.format(
                "#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255)
        );
    }

    private String formatNumber(double value) {
        if (Math.abs(value - Math.round(value)) < 0.0001) {
            return String.valueOf((int) Math.round(value));
        }
        return String.format(Locale.US, "%.2f", value);
    }

    /** Восстановить ведущие слоты из локального сохранения. */
    private void restoreSavedLoadout() {
        String savedMeleeId = weaponLoadoutStorage.loadMeleeId();
        String savedRangedId = weaponLoadoutStorage.loadRangedId();

        if (savedMeleeId != null) {
            Weapon weapon = findWeaponById(savedMeleeId);
            if (weapon != null && weapon.getType() == WeaponType.MELEE) {
                inventory.equipMeleeWeapon(weapon);
            }
        }

        if (savedRangedId != null) {
            Weapon weapon = findWeaponById(savedRangedId);
            if (weapon != null && weapon.getType() == WeaponType.RANGED) {
                inventory.equipRangedWeapon(weapon);
            }
        }
    }

    /** Сохранить текущие ведущие слоты. */
    private void saveCurrentLoadout() {
        Weapon melee = inventory.getEquippedMeleeWeapon();
        Weapon ranged = inventory.getEquippedRangedWeapon();

        weaponLoadoutStorage.save(
                melee == null ? null : melee.getId(),
                ranged == null ? null : ranged.getId()
        );
    }

    private Weapon findWeaponById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        for (int i = 0; i < inventory.getWeaponCount(); i++) {
            Weapon weapon = inventory.getWeapon(i);
            if (weapon != null && id.equals(weapon.getId())) {
                return weapon;
            }
        }

        return null;
    }

    private int getEquippedArtifactHpBonus() {
        int bonus = 0;
        for (int slot = 0; slot < inventory.getArtifactSlotCount(); slot++) {
            Artifact artifact = inventory.getEquippedArtifact(slot);
            if (artifact != null) bonus += artifact.getMaxHpBonus();
        }
        return bonus;
    }

    private void applyEquippedArtifactHpBonuses() {
        // В режиме 1 HP здоровье фиксировано: артефакты не меняют ни HP,
        // ни MaxHP, даже если у них есть положительный/отрицательный бонус.
        if (oneHpMode) {
            playerMaxHp = 1f;
            // Не восстанавливаем HP автоматически после получения урона:
            // 0 HP должно по-прежнему запускать обычную смерть/респаун.
            playerHp = Math.max(0f, Math.min(1f, playerHp));
            appliedArtifactHpBonus = 0;
            return;
        }

        int newBonus = getEquippedArtifactHpBonus();
        int difference = newBonus - appliedArtifactHpBonus;
        if (difference != 0) {
            float oldMaxHp = playerMaxHp;
            boolean wasAtFullHp = playerHp >= oldMaxHp;
            playerMaxHp = Math.max(1f, playerMaxHp + difference);

            // При полном здоровье сохраняем состояние «полное HP».
            // Иначе сохраняем абсолютное текущее HP, ограничивая новым максимумом.
            if (wasAtFullHp) {
                playerHp = playerMaxHp;
            } else {
                playerHp = Math.max(0f, Math.min(playerMaxHp, playerHp));
            }
            appliedArtifactHpBonus = newBonus;
        }
    }

    private void refreshCharacterPanel() {
        applyEquippedArtifactHpBonuses();
        // Экипировка/снятие артефакта может менять MaxHP без получения урона.
        // Синхронизируем верхний HUD сразу, независимо от видимости панели персонажа.
        refreshPlayerHpDisplay();
        updateCharacterPanel();
        rebuildInventoryGrid();
        for (int slot = 0; slot < artifactSlotNodes.length; slot++) refreshArtifactSlot(slot);
    }

    private void setCharacterPanelVisible(
            boolean visible
    ) {
        characterPanelVisible = visible;

        characterPanel.setVisible(
                visible
        );

        characterPanel.setManaged(
                visible
        );

        if (visible) {
            rebuildInventoryGrid();
        }
    }


    private boolean isArtifactEquipped(Artifact wanted) {
        for (int slot = 0; slot < inventory.getArtifactSlotCount(); slot++) {
            if (inventory.getEquippedArtifact(slot) == wanted) return true;
        }
        return false;
    }

    private void handleLocalCombatHit(LocalCombatHit hit) {
        showDamagePopup(
                hit.getX(),
                hit.getY(),
                hit.getDamage(),
                hit.getDamageType()
        );

        LocalCombatTarget target = combatSystem.getTarget(hit.getTargetId());

        // Браслет молнии: +30 п.п. критического шанса.
        // Критический удар добавляет ещё 100% исходного урона.
        if (target != null && target.isAlive()
                && isArtifactEquipped(Artifact.WORN_LIGHTNING_BRACELET)
                && Math.random() < Artifact.WORN_LIGHTNING_BRACELET.getCriticalChanceBonus()) {
            float criticalBonusDamage = hit.getDamage();
            target.damage(criticalBonusDamage);
            showDamagePopup(
                    target.getX() + target.getWidth() / 2f,
                    target.getY() + target.getHeight() / 2f,
                    criticalBonusDamage,
                    hit.getDamageType()
            );
        }

        // Ветхая Эфирная кукла: шанс 10%, перезарядка 20 секунд.
        // Фантом появляется у цели, визуально остаётся 6 секунд и повторяет попадание.
        if (target != null && target.isAlive()
                && isArtifactEquipped(Artifact.WORN_ETHEREAL_DOLL)
                && System.nanoTime() >= etherealDollReadyAtNanos
                && Math.random() < 0.10) {
            float phantomDamage = hit.getDamage() * 0.50f;
            target.damage(phantomDamage);
            showDamagePopup(
                    target.getX() + target.getWidth() / 2f,
                    target.getY() + target.getHeight() / 2f,
                    phantomDamage,
                    hit.getDamageType()
            );
            etherealPhantomX = target.getX() + target.getWidth() / 2f;
            etherealPhantomY = target.getY() + target.getHeight() / 2f;
            etherealPhantomUntilNanos = System.nanoTime() + 6_000_000_000L;
            etherealDollReadyAtNanos = System.nanoTime() + 20_000_000_000L;
        }

        System.out.println(
                "LOCAL DAMAGE: "
                        + hit.getTargetId()
                        + " -> "
                        + hit.getDamage()
                        + " (" + hit.getDamageType() + ")"
        );
    }

    /*
     * =====================================================
     * INPUT
     * =====================================================
     */

    private void setupInput() {

        canvas.setFocusTraversable(
                true
        );

        root.setFocusTraversable(
                true
        );

        /*
         * Нажатие клавиш на Canvas.
         */
        canvas.setOnKeyPressed(e -> {

            if (e.getCode() == KeyCode.W) {
                up = true;
            }

            if (e.getCode() == KeyCode.S) {
                down = true;
            }

            if (e.getCode() == KeyCode.A) {
                left = true;
            }

            if (e.getCode() == KeyCode.D) {
                right = true;
            }
        });

        /*
         * Отпускание клавиш.
         */
        canvas.setOnKeyReleased(e -> {

            if (e.getCode() == KeyCode.W) {
                up = false;
            }

            if (e.getCode() == KeyCode.S) {
                down = false;
            }

            if (e.getCode() == KeyCode.A) {
                left = false;
            }

            if (e.getCode() == KeyCode.D) {
                right = false;
            }
        });

        /*
         * Если Canvas потерял фокус,
         * прекращаем движение.
         */
        canvas.focusedProperty()
                .addListener(
                        (obs, oldValue, focused) -> {

                            if (!focused) {

                                up = false;
                                down = false;
                                left = false;
                                right = false;
                            }
                        }
                );

        /*
         * Дополнительная обработка клавиш
         * на root.
         */
        root.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W -> up = true;
                case S -> down = true;
                case A -> left = true;
                case D -> right = true;

                case F -> interactWithNearbyPortal();
                case Q -> activateTinWallRam();

                case R -> switchActiveWeapon();
                case B -> setCharacterPanelVisible(!characterPanelVisible);

                case DIGIT1 -> useCurrentWeaponTrick();

                case DIGIT2 -> useSkill(0);
                case DIGIT3 -> useSkill(1);
                case DIGIT4 -> useSkill(2);
                case DIGIT5 -> useSkill(3);
                case DIGIT6 -> useSkill(4);
                case DIGIT7 -> useSkill(5);
                case DIGIT8 -> useSkill(6);
                case DIGIT9 -> useSkill(7);
            }
        });

        root.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case W -> up = false;
                case S -> down = false;
                case A -> left = false;
                case D -> right = false;
            }
        });

        /*
         * Движение мыши.
         *
         * Координаты находятся в системе Canvas.
         */
        canvas.setOnMouseMoved(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
        });

        canvas.setOnMouseDragged(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
        });

        canvas.setOnMousePressed(event -> {

            if (event.getButton() == MouseButton.PRIMARY) {

                attackHeld = true;
                attackAnimationTimer = ATTACK_ANIMATION_DURATION;
                attackTimer = 0.0;

                Weapon weapon = getCurrentWeapon();

                if (isAngelicDoom(weapon)) {

                    sendAttack(
                            mouseX,
                            mouseY,
                            AngelicDoomMode.SOLAR
                    );

                } else {

                    sendAttack(
                            mouseX,
                            mouseY
                    );
                }
            }

            if (event.getButton() == MouseButton.SECONDARY) {

                Weapon weapon = getCurrentWeapon();

                if (weapon != null
                        && weapon.getType() == WeaponType.MELEE
                        && (weapon.getAttackStyle() == AttackStyle.THRUST
                        || weapon.getAttackStyle() == AttackStyle.STAB)) {

                    // ПКМ во время активного выпада запрашивает второй удар.
                    if (!spearFollowupRequested) {
                        for (SwordSwing swing : combatSystem.getActiveSwings()) {
                            if (swing.getStyle() == AttackStyle.THRUST
                                    || swing.getStyle() == AttackStyle.STAB) {

                                spearComboThrust = swing;
                                spearFollowupRequested = true;
                                spearFollowupTimer = 0.0;
                                break;
                            }
                        }
                    }

                    return;
                }

                if (isDoomStarShooter(weapon)) {
                    if (!doomStarCharged) {
                        doomStarCharging = true;
                        doomStarChargeTimer = 0.0;
                    }
                } else if (isAngelicDoom(weapon)) {
                    secondaryAttackHeld = true;
                    secondaryAttackTimer = 0.0;
                    sendAttack(mouseX, mouseY, AngelicDoomMode.LUNAR);
                }
            }
        });

        canvas.setOnMouseReleased(event -> {

            if (event.getButton() == MouseButton.PRIMARY) {

                attackHeld = false;
                attackTimer = 0.0;

                if (isDawn(getCurrentWeapon())) {
                    dawnComboIndex = 0;
                }
            }

            if (event.getButton() == MouseButton.SECONDARY) {

                secondaryAttackHeld = false;
                secondaryAttackTimer = 0.0;

                if (isDoomStarShooter(getCurrentWeapon()) && !doomStarCharged) {
                    doomStarCharging = false;
                    doomStarChargeTimer = 0.0;
                }
            }
        });
    }

    private void useCurrentWeaponTrick() {


        if (localPlayer == null) {
            return;
        }

        if (client.isConnected()) {
            return;
        }

        if (gungnirDrillActive) {
            return;
        }

        if (gungnirDrillTimer > 0.0) {
            return;
        }

        Weapon weapon =
                getCurrentWeapon();
        if (isDawn(weapon)) {
            performDawnFinisher();
            return;
        }

        if (!weaponTrickSystem.isGungnirDrill(weapon)) {
            return;
        }

        float[] direction =
                getAimDirection(
                        mouseX,
                        mouseY
                );

        float length =
                (float) Math.sqrt(
                        direction[0] * direction[0]
                                + direction[1] * direction[1]
                );

        if (length < 0.0001f) {
            return;
        }

        gungnirDrillDirectionX =
                direction[0] / length;

        gungnirDrillDirectionY =
                direction[1] / length;

        gungnirDrillActive = true;
        gungnirDrillActiveDuration =
                WeaponTrickSystem.GUNGNIR_DRILL_DURATION;

        gungnirDrillTimer =
                WeaponTrickSystem.GUNGNIR_DRILL_COOLDOWN;

        gungnirDrillTickTimer = 0.0;

        /*
         * Во время бура снаряды не могут
         * наносить игроку урон.
         */
        projectileInvulnerability = true;
    }
    private void useSkill(int slot) {

        if (slot < 0 || slot >= 8) {
            return;
        }

        System.out.println(
                "SKILL SLOT "
                        + (slot + 1)
                        + " USED"
        );

        /*
         * Здесь позже будет:
         *
         * playerSkills[slot].activate(...)
         */
    }
    private void updateCombatDash(
            double deltaSeconds
    ) {

        if (activeCombatDash == null) {
            return;
        }

        if (activeCombatDash.isFinished()) {
            activeCombatDash = null;
            return;
        }

        float movement =
                activeCombatDash.update(
                        (float) deltaSeconds
                );

        if (movement <= 0f) {

            if (activeCombatDash.isFinished()) {
                activeCombatDash = null;
            }

            return;
        }

        float dx =
                activeCombatDash.getDirectionX()
                        * movement;

        float dy =
                activeCombatDash.getDirectionY()
                        * movement;

        /*
         * Останавливаем обычное движение
         * на время рывка.
         */
        localPlayer.serverX += dx;
        localPlayer.serverY += dy;

        /*
         * Не позволяем рывку выйти
         * за границы мира.
         */
        localPlayer.serverX =
                (float) Math.max(
                        0,
                        Math.min(
                                worldWidth - PLAYER_SIZE,
                                localPlayer.serverX
                        )
                );

        localPlayer.serverY =
                (float) Math.max(
                        0,
                        Math.min(
                                worldHeight - PLAYER_SIZE,
                                localPlayer.serverY
                        )
                );

        localPlayer.renderX =
                localPlayer.serverX;

        localPlayer.renderY =
                localPlayer.serverY;

        /*
         * Направление персонажа соответствует
         * направлению рывка.
         */
        localPlayer.direction =
                getDirection(
                        activeCombatDash.getDirectionX(),
                        activeCombatDash.getDirectionY()
                );

        if (activeCombatDash.isFinished()) {
            activeCombatDash = null;
        }
    }

    /*
     * =====================================================
     * GAME LOOP
     * =====================================================
     */

    private void startLoop() {

        gameTimer =
                new AnimationTimer() {

                    @Override
                    public void handle(
                            long now
                    ) {

                        if (lastUpdateTime == 0) {
                            lastUpdateTime = now;
                            return;
                        }

                        double deltaSeconds =
                                (now - lastUpdateTime)
                                        / 1_000_000_000.0;

                        lastUpdateTime = now;

                        update(
                                deltaSeconds
                        );

                        updateDamagePopups(
                                deltaSeconds
                        );

                        render();
                    }
                };

        gameTimer.start();
    }
    private void stopGameLoop() {

        if (gameTimer != null) {
            gameTimer.stop();
            gameTimer = null;
        }

        lastUpdateTime = 0;
    }

    private void updateDamagePopups(
            double deltaSeconds
    ) {
        for (DamagePopup popup : damagePopups) {
            popup.age += deltaSeconds;
        }

        damagePopups.removeIf(
                popup ->
                        popup.age >= DAMAGE_POPUP_DURATION
        );
    }

    /*
     * =====================================================
     * UPDATE
     * =====================================================
     */

    private void update(
            double deltaSeconds
    ) {

        playerAnimationTime += deltaSeconds;
        glitchAnimationTime += deltaSeconds;

        if (attackAnimationTimer > 0.0) {
            attackAnimationTimer = Math.max(
                    0.0,
                    attackAnimationTimer - deltaSeconds
            );
        }

        if (localPlayer != null) {

            /*
             * =================================================
             * SINGLEPLAYER
             * =================================================
             */

            if (!client.isConnected()) {

                moveLocalPlayer(
                        deltaSeconds
                );
                updateGungnirDrill(
                        deltaSeconds
                );
            }


            /*
             * =================================================
             * MULTIPLAYER
             * =================================================
             */

            else {

                /*
                 * Позицию получает сервер.
                 */
                localPlayer.renderX =
                        localPlayer.serverX;

                localPlayer.renderY =
                        localPlayer.serverY;

                /*
                 * Направление определяем локально
                 * по текущим клавишам WASD.
                 */
                double dx = 0;
                double dy = 0;

                if (up) {
                    dy -= 1;
                }

                if (down) {
                    dy += 1;
                }

                if (left) {
                    dx -= 1;
                }

                if (right) {
                    dx += 1;
                }

                if (dx != 0 || dy != 0) {

                    localPlayer.direction =
                            getDirection(dx, dy);
                }
            }

            updatePortalInteraction();

            if (!client.isConnected()) {

                float playerCenterX =
                        localPlayer.renderX +
                                (float) PLAYER_SIZE / 2f;

                float playerCenterY =
                        localPlayer.renderY +
                                (float) PLAYER_SIZE / 2f;

                if (attackCooldown > 0.0) {
                    attackCooldown -= deltaSeconds;

                    if (attackCooldown < 0.0) {
                        attackCooldown = 0.0;
                    }
                }

                updateSpecialWeaponStates(deltaSeconds);

                if (attackHeld) {
                    attackTimer += deltaSeconds;

                    if (attackTimer >= getAttackInputInterval()) {
                        attackTimer = 0.0;

                        Weapon weapon = getCurrentWeapon();

                        if (isAngelicDoom(weapon)) {

                            sendAttack(
                                    mouseX,
                                    mouseY,
                                    AngelicDoomMode.SOLAR
                            );

                        } else {

                            sendAttack(
                                    mouseX,
                                    mouseY
                            );
                        }
                    }
                }
                if (secondaryAttackHeld && !isDoomStarShooter(getCurrentWeapon())) {
                    secondaryAttackTimer += deltaSeconds;

                    if (secondaryAttackTimer >= getAttackInterval()) {
                        secondaryAttackTimer = 0.0;
                        sendAttack(mouseX, mouseY, AngelicDoomMode.LUNAR);
                    }
                }

                if (tinWallRamCooldown > 0.0) {
                    tinWallRamCooldown = Math.max(0.0, tinWallRamCooldown - deltaSeconds);
                }
                if (tinWallRamTimer > 0.0) {
                    tinWallRamTimer = Math.max(0.0, tinWallRamTimer - deltaSeconds);
                    tinWallArcAngle += 9.0 * deltaSeconds;
                    applyTinWallRamDamage(playerCenterX, playerCenterY);
                } else if (isArtifactEquipped(Artifact.TIN_WALL)) {
                    float[] wallAim = getAimDirection(mouseX, mouseY);
                    double aimAngle = Math.atan2(wallAim[1], wallAim[0]) + Math.PI;
                    double angleDelta = Double.isNaN(tinWallPreviousAimAngle)
                            ? 0.0
                            : Math.atan2(Math.sin(aimAngle - tinWallPreviousAimAngle),
                            Math.cos(aimAngle - tinWallPreviousAimAngle));
                    tinWallArcAngle = aimAngle;
                    if (Math.abs(angleDelta) >= 0.015) {
                        applyTinWallContactDamage(playerCenterX, playerCenterY);
                    }
                    tinWallPreviousAimAngle = aimAngle;
                } else {
                    tinWallPreviousAimAngle = Double.NaN;
                }

                combatSystem.update(
                        playerCenterX,
                        playerCenterY,
                        (float) deltaSeconds
                );

                if (spearFollowupRequested
                        && spearComboThrust != null
                        && spearComboThrust.isFinished()) {

                    float dirX = spearComboThrust.getDirectionX();
                    float dirY = spearComboThrust.getDirectionY();

                    // Полная длина выпада: 20 + 90 = 110 единиц.
                    float thrustEndX =
                            spearComboThrust.getOriginX() + dirX * 110f;

                    float thrustEndY =
                            spearComboThrust.getOriginY() + dirY * 110f;

                    // Второй удар наносит ровно половину урона выпада.
                    float followupDamage =
                            spearComboThrust.getDamage() * 0.5f;

                    combatSystem.attackWithStyleAtOrigin(
                            getCurrentWeapon(),
                            followupDamage,
                            thrustEndX,
                            thrustEndY,
                            dirX,
                            dirY,
                            AttackStyle.QUICK_SWING,
                            true
                    );

                    spearComboThrust = null;
                    spearFollowupRequested = false;
                    spearFollowupTimer = 0.0;
                }

                rangedProjectileSystem.update(
                        (float) deltaSeconds
                );
                rangedProjectileSystem.updateSolarExplosions(
                        (float) deltaSeconds
                );
                rangedProjectileSystem.updateStarExplosions(
                        (float) deltaSeconds
                );
                rangedProjectileSystem.updateMourningLightningArcs(
                        (float) deltaSeconds
                );

                if (currentLocation != null
                        && "dungeon".equals(currentLocation.getId())) {

                    bossManager.update(
                            (float) deltaSeconds,
                            localPlayer.renderX,
                            localPlayer.renderY
                    );
                }
            }
        }

        /*
         * Обновляем остальных игроков.
         */
        for (RemotePlayer player :
                players.values()) {

            if (player == localPlayer) {
                continue;
            }

            player.update();
        }

        /*
         * Отправляем INPUT серверу.
         */
        if (client.isConnected()) {

            sendInput();
        }

    }

    private void updateGungnirDrill(
            double deltaSeconds
    ) {

        /*
         * =====================================================
         * COOLDOWN
         * =====================================================
         */

        if (gungnirDrillTimer > 0.0) {

            gungnirDrillTimer -=
                    deltaSeconds;

            if (gungnirDrillTimer < 0.0) {
                gungnirDrillTimer = 0.0;
            }
        }

        if (!gungnirDrillActive) {
            return;
        }
        gungnirDrillActiveDuration -=
                deltaSeconds;

        /*
         * =====================================================
         * DURATION
         * =====================================================
         */

        gungnirDrillTimer =
                Math.max(
                        gungnirDrillTimer,
                        0.0
                );

        gungnirDrillTickTimer +=
                deltaSeconds;

        /*
         * =====================================================
         * MOVEMENT
         * =====================================================
         */

        float movement =
                WeaponTrickSystem.GUNGNIR_DRILL_SPEED
                        * (float) deltaSeconds;

        localPlayer.serverX +=
                gungnirDrillDirectionX
                        * movement;

        localPlayer.serverY +=
                gungnirDrillDirectionY
                        * movement;

        localPlayer.serverX =
                (float) Math.max(
                        0,
                        Math.min(
                                worldWidth - PLAYER_SIZE,
                                localPlayer.serverX
                        )
                );

        localPlayer.serverY =
                (float) Math.max(
                        0,
                        Math.min(
                                worldHeight - PLAYER_SIZE,
                                localPlayer.serverY
                        )
                );

        localPlayer.renderX =
                localPlayer.serverX;

        localPlayer.renderY =
                localPlayer.serverY;

        /*
         * =====================================================
         * DAMAGE TICKS
         * =====================================================
         */

        while (
                gungnirDrillTickTimer
                        >= WeaponTrickSystem.GUNGNIR_DRILL_TICK_INTERVAL
        ) {

            gungnirDrillTickTimer -=
                    WeaponTrickSystem.GUNGNIR_DRILL_TICK_INTERVAL;

            applyGungnirDrillTick();
        }

        gungnirDrillVisualAngle +=
                22.0 * deltaSeconds;

        if (gungnirDrillVisualAngle >= Math.PI * 2.0) {
            gungnirDrillVisualAngle -= Math.PI * 2.0;
        }

        /*
         * =====================================================
         * END
         * =====================================================
         */

        /*
         * Отдельно храним длительность способности.
         */
        if (gungnirDrillActiveDuration <= 0.0) {
            finishGungnirDrill();
        }
    }
    private void applyGungnirDrillTick() {

        Weapon weapon =
                getCurrentWeapon();

        if (!weaponTrickSystem.isGungnirDrill(weapon)) {
            return;
        }

        /*
         * Получаем обычный урон одного попадания Гунгира.
         *
         * Сейчас это:
         *
         * baseDamage 10
         * + attackPercent * (playerAttack + weaponAttack)
         *
         * Затем берём 22%.
         */
        float normalDamage =
                DamageCalculator.calculateTickDamage(
                        weapon,
                        PLAYER_ATTACK
                );

        float drillDamage =
                normalDamage
                        * WeaponTrickSystem
                        .GUNGNIR_DRILL_DAMAGE_PERCENT;

        float centerX =
                localPlayer.renderX
                        + (float) PLAYER_SIZE / 2f;

        float centerY =
                localPlayer.renderY
                        + (float) PLAYER_SIZE / 2f;

        combatSystem.damageLine(
                centerX,
                centerY,
                gungnirDrillDirectionX,
                gungnirDrillDirectionY,
                WeaponTrickSystem.GUNGNIR_DRILL_LENGTH,
                WeaponTrickSystem.GUNGNIR_DRILL_WIDTH,
                drillDamage
        );
    }
    private void finishGungnirDrill() {

        gungnirDrillActive = false;

        gungnirDrillActiveDuration = 0.0;
        gungnirDrillTickTimer = 0.0;

        /*
         * Снимаем иммунитет к снарядам.
         */
        projectileInvulnerability = false;
    }

    /**
     * Итоговая атака игрока с учётом экипированных артефактов.
     *
     * Сначала применяется плоский бонус, затем процентный.
     */
    private float getEffectivePlayerAttack() {
        float baseAttack =
                PLAYER_ATTACK + inventory.getArtifactAttackBonus();

        float percentBonus =
                inventory.getArtifactAttackPercentBonus();

        return Math.max(0f, baseAttack * (1f + percentBonus));
    }

    private double getAttackInterval() {

        Weapon weapon =
                getCurrentWeapon();

        if (weapon == null) {
            return 0.25;
        }

        double attackSpeed =
                weapon.getAttackSpeed();

        if (attackSpeed <= 0.01) {
            return 0.25;
        }

        return 1.0 / attackSpeed;
    }

    private boolean isDawn(Weapon weapon) {
        return weapon != null && "dawn".equals(weapon.getId());
    }

    private double getAttackInputInterval() {
        Weapon weapon = getCurrentWeapon();

        if (isDawn(weapon)) {
            return dawnComboIndex == 4
                    ? DAWN_FINISHER_DELAY
                    : DAWN_COMBO_INTERVAL;
        }

        return getAttackInterval();
    }


    private void damageLocalPlayer(float damage) {
        if (damage <= 0f || playerHp <= 0f) {
            return;
        }

        // Браслет молнии: 30 п.п. критического шанса конвертируются
        // в 15 п.п. уклонения (соотношение 2:1).
        float dodgeChance = isArtifactEquipped(Artifact.WORN_LIGHTNING_BRACELET)
                ? Artifact.WORN_LIGHTNING_BRACELET.getCriticalChanceBonus()
                * Artifact.WORN_LIGHTNING_BRACELET.getDodgeConversionRatio()
                : 0f;
        if (dodgeChance > 0f && Math.random() < dodgeChance) {
            showDamagePopup(
                    localPlayer.renderX + (float) PLAYER_SIZE / 2f,
                    localPlayer.renderY,
                    0f,
                    DamageType.PHYSICAL
            );
            return;
        }

        // Сначала применяем модификатор сложности.
        damage = DifficultySettings.enemyDamage(
                difficulty,
                damage
        );

        // Затем вычитаем плоскую защиту артефактов.
        damage = Math.max(
                0f,
                damage - inventory.getArtifactDefenseBonus()
        );

        // После этого применяем процентную защиту.
        float defensePercent =
                inventory.getArtifactDefensePercentBonus();

        damage *= Math.max(0f, 1f + defensePercent);

        playerHp = Math.max(0f, playerHp - damage);

        updatePlayerHud();

        if (playerHp <= 0f) {
            respawnPlayerAtStart();
        }
    }

    private void respawnPlayerAtStart() {

        /*
         * Полностью восстанавливаем здоровье.
         */
        playerHp = playerMaxHp;

        /*
         * Останавливаем движение.
         */
        up = false;
        down = false;
        left = false;
        right = false;

        /*
         * Очищаем обычные снаряды Ancient Angel.
         */
        bossManager.resetBoss(DungeonBossRegistry.ANCIENT_ANGEL_ID);

        /*
         * Очищаем синие вращающиеся потоки
         * и все выпущенные ими снаряды.
         */

        /*
         * Очищаем боевые объекты игрока.
         */
        rangedProjectileSystem.clear();
        combatSystem.clear();

        /*
         * Возвращаем игрока в основной мир.
         */
        Location mainWorld =
                locations.get("main_world");



        if (mainWorld != null) {

            switchLocation(
                    mainWorld,
                    5000f,
                    5000f
            );
        }

        /*
         * На всякий случай синхронизируем
         * серверные и визуальные координаты.
         */
        if (localPlayer != null) {

            localPlayer.serverX = 5000f;
            localPlayer.serverY = 5000f;

            localPlayer.renderX = 5000f;
            localPlayer.renderY = 5000f;
        }

        updatePlayerHud();

        System.out.println(
                "PLAYER RESPAWNED AT WORLD CENTER"
        );
    }

    /*
     * =====================================================
     * LOCAL PLAYER MOVEMENT
     * =====================================================
     */

    private void moveLocalPlayer(
            double deltaSeconds
    ) {

        if (localPlayer == null) {
            return;
        }
        if (activeCombatDash != null
                && !activeCombatDash.isFinished()) {
            return;
        }

        double dx = 0;
        double dy = 0;

        /*
         * WASD.
         */
        if (up) {
            dy -= 1;
        }

        if (down) {
            dy += 1;
        }

        if (left) {
            dx -= 1;
        }

        if (right) {
            dx += 1;
        }

        /*
         * Определяем направление взгляда.
         */
        if (dx != 0 || dy != 0) {

            localPlayer.direction =
                    getDirection(dx, dy);
        }

        /*
         * Нет движения.
         */
        if (dx == 0 && dy == 0) {
            return;
        }

        /*
         * Нормализация диагонального движения.
         */
        double length =
                Math.sqrt(
                        dx * dx
                                +
                                dy * dy
                );

        dx /= length;
        dy /= length;

        /*
         * Перемещение.
         */

        double movementSpeed =
                DifficultySettings.playerSpeed(
                        difficulty,
                        LOCAL_PLAYER_SPEED
                );

        if (isArtifactEquipped(Artifact.TIN_WALL)) {
            movementSpeed *= Artifact.TIN_WALL.getMovementSpeedMultiplier();
        }

        localPlayer.serverX +=
                dx * movementSpeed * deltaSeconds;

        localPlayer.serverY +=
                dy * movementSpeed * deltaSeconds;



        /*
         * Ограничиваем игрока границами
         * текущего мира.
         */
        localPlayer.serverX =
                (float) Math.max(
                        0,
                        Math.min(
                                worldWidth - PLAYER_SIZE,
                                localPlayer.serverX
                        )
                );

        localPlayer.serverY =
                (float) Math.max(
                        0,
                        Math.min(
                                worldHeight - PLAYER_SIZE,
                                localPlayer.serverY
                        )
                );

        /*
         * В singleplayer render-позиция
         * совпадает с фактической.
         */
        localPlayer.renderX =
                localPlayer.serverX;

        localPlayer.renderY =
                localPlayer.serverY;
    }

    /*
     * =====================================================
     * NETWORK INPUT
     * =====================================================
     */

    private void sendInput() {

        client.send(
                "INPUT:"
                        + up
                        + ":"
                        + down
                        + ":"
                        + left
                        + ":"
                        + right
        );
    }

    /*
     * =====================================================
     * RENDER
     * =====================================================
     */

    private void render() {

        double screenWidth =
                canvas.getWidth();

        double screenHeight =
                canvas.getHeight();

        gc.clearRect(
                0,
                0,
                screenWidth,
                screenHeight
        );

        if (localPlayer == null) {
            return;
        }

        /*
         * =================================================
         * CAMERA POSITION
         * =================================================
         *
         * Камера находится в центре игрока.
         */

        double cameraX =
                localPlayer.renderX
                        + PLAYER_SIZE / 2.0;

        double cameraY =
                localPlayer.renderY
                        + PLAYER_SIZE / 2.0;

        /*
         * =================================================
         * CAMERA TRANSFORM
         * =================================================
         *
         * Центр Canvas становится центром камеры.
         */

        gc.save();

        gc.translate(
                screenWidth / 2.0,
                screenHeight / 2.0
        );

        gc.scale(
                CAMERA_ZOOM,
                CAMERA_ZOOM
        );

        gc.translate(
                -cameraX,
                -cameraY
        );

        /*
         * Фон.
         */
        renderBackground(
                cameraX,
                cameraY,
                screenWidth,
                screenHeight
        );

        renderWorldObjects(
                cameraX,
                cameraY,
                screenWidth,
                screenHeight
        );

        renderPortals();

        if (currentLocation != null
                && "dungeon".equals(currentLocation.getId())) {
            bossManager.render(gc);
        }

        renderTrainingDummy();
        renderTrainingDummy2();

        renderRangedProjectiles();
        renderSpecialWeaponHud();

        renderSwordSwings();

        // Сначала оружие позади персонажа.
        renderEquippedWeapon(false);

        renderTinWallArc();
        renderEtherealPhantom();

        // Затем сам персонаж.
        renderPlayers();

        // Оружие поверх персонажа — только при атаке вниз.
        renderEquippedWeapon(true);

        if (gungnirDrillActive) {
            renderGungnirDrill(gc);
        }

        renderDamagePopups();

        renderTargetDebuffTooltip();

        renderPortalInteraction();

        renderWorldBorder();

        gc.restore();

        /*
         * UI поверх мира.
         */
        updateBossHud();
        updateWeaponHud();
        renderInfo();
    }


    private double getDirectionAngle(Direction direction) {

        if (direction == null) {
            return 0;
        }

        return switch (direction) {
            case DOWN -> 0;
            case DOWN_RIGHT -> -45;
            case RIGHT -> -90;
            case UP_RIGHT -> -135;
            case UP -> 180;
            case UP_LEFT -> 135;
            case LEFT -> 90;
            case DOWN_LEFT -> 45;
        };
    }

    private void addAllWeaponsToInventory() {
        String[] allWeaponIds = {
                "weighted_claymore",
                "iron_sword",
                "angelic_doom",
                "gungnir",
                "gungnir_launcher",
                "reliable_spear",
                "mourning",
                "ice_shooter",
                "doom_star_shooter",

                // Новое оружие
                "material_curse_blade",
                "wooden_sword",
                "magmatic_claymore",
                "dawn",
                "zenith",
                "twilight",
                "tomorrow"
        };

        for (String weaponId : allWeaponIds) {
            if (inventory.findWeaponById(weaponId) != null) {
                continue;
            }

            Weapon weapon = createWeaponById(weaponId);

            if (weapon != null) {
                if (inventory.addWeapon(weapon)) {
                    System.out.println(
                            "[Inventory] Добавлено оружие: "
                                    + weapon.getName()
                    );
                } else {
                    System.err.println(
                            "[Inventory] Не удалось добавить оружие: "
                                    + weapon.getName()
                    );
                }
            }
        }
    }

    private void renderGungnirDrill(GraphicsContext gc) {

        if (localPlayer == null) {
            return;
        }

        float centerX =
                localPlayer.renderX
                        + (float) PLAYER_SIZE / 2f;

        float centerY =
                localPlayer.renderY
                        + (float) PLAYER_SIZE / 2f;

        float dirX =
                gungnirDrillDirectionX;

        float dirY =
                gungnirDrillDirectionY;

        double angle =
                Math.atan2(dirY, dirX);

        /*
         * Центр бура находится перед игроком.
         */
        double drillDistance = 52.0;

        double drillX =
                centerX
                        + dirX * drillDistance;

        double drillY =
                centerY
                        + dirY * drillDistance;

        /*
         * Размер бура.
         */
        double radius = 30.0;

        gc.save();

        /*
         * Поворачиваем систему координат
         * по направлению движения.
         */
        gc.translate(
                drillX,
                drillY
        );

        gc.rotate(
                Math.toDegrees(angle)
        );

        /*
         * -------------------------------------------------
         * ВНЕШНИЙ КОНУС
         * -------------------------------------------------
         */

        gc.setFill(
                Color.color(
                        0.75,
                        0.75,
                        0.80,
                        0.85
                )
        );

        gc.beginPath();

        gc.moveTo(
                radius,
                0
        );

        gc.lineTo(
                -radius * 0.75,
                -radius * 0.75
        );

        gc.lineTo(
                -radius * 0.75,
                radius * 0.75
        );

        gc.closePath();

        gc.fill();

        /*
         * -------------------------------------------------
         * СПИРАЛЬ БУРА
         * -------------------------------------------------
         */

        gc.setStroke(
                Color.color(
                        0.95,
                        0.95,
                        1.0,
                        0.95
                )
        );

        gc.setLineWidth(5.0);

        gc.beginPath();

        int points = 32;

        for (int i = 0; i <= points; i++) {

            double t =
                    (double) i / points;

            /*
             * От задней части к острию.
             */
            double x =
                    -radius * 0.75
                            + radius * 1.75 * t;

            /*
             * Спираль.
             */
            double wave =
                    Math.sin(
                            t * Math.PI * 4.0
                                    + gungnirDrillVisualAngle
                    );

            double y =
                    wave
                            * radius
                            * (1.0 - t);

            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                gc.lineTo(x, y);
            }
        }

        gc.stroke();

        /*
         * -------------------------------------------------
         * ЦЕНТРАЛЬНЫЙ НАКОНЕЧНИК
         * -------------------------------------------------
         */

        gc.setFill(
                Color.color(
                        0.95,
                        0.95,
                        1.0,
                        1.0
                )
        );

        gc.beginPath();

        gc.moveTo(
                radius * 1.15,
                0
        );

        gc.lineTo(
                radius * 0.25,
                -radius * 0.30
        );

        gc.lineTo(
                radius * 0.25,
                radius * 0.30
        );

        gc.closePath();

        gc.fill();

        /*
         * -------------------------------------------------
         * ВНУТРЕННИЙ ВИХРЬ
         * -------------------------------------------------
         */

        gc.setStroke(
                Color.color(
                        1.0,
                        1.0,
                        1.0,
                        0.75
                )
        );

        gc.setLineWidth(2.0);

        gc.strokeOval(
                -radius * 0.45,
                -radius * 0.45,
                radius * 0.9,
                radius * 0.9
        );

        gc.restore();
    }
    private void renderPortals() {

        if (currentLocation == null) {
            return;
        }

        for (Portal portal :
                currentLocation.getPortals()) {

            double x =
                    portal.getX();

            double y =
                    portal.getY();

            double width =
                    portal.getWidth();

            double height =
                    portal.getHeight();

            /*
             * Основная рамка портала.
             */
            gc.save();

            gc.setLineWidth(4.0);

            if (portal == nearbyPortal) {

                /*
                 * Когда игрок рядом —
                 * рамка становится заметнее.
                 */
                gc.setStroke(
                        Color.GOLD
                );

                gc.setGlobalAlpha(1.0);

            } else {

                gc.setStroke(
                        Color.WHITE
                );

                gc.setGlobalAlpha(0.75);
            }

            gc.strokeRect(
                    x - PORTAL_FRAME_PADDING,
                    y - PORTAL_FRAME_PADDING,
                    width + PORTAL_FRAME_PADDING * 2,
                    height + PORTAL_FRAME_PADDING * 2
            );

            /*
             * Внутренняя часть.
             * Это НЕ спрайт, а просто обозначение
             * области портала.
             */
            gc.setGlobalAlpha(
                    portal == nearbyPortal
                            ? 0.16
                            : 0.07
            );

            gc.setFill(
                    portal == nearbyPortal
                            ? Color.GOLD
                            : Color.WHITE
            );

            gc.fillRect(
                    x,
                    y,
                    width,
                    height
            );

            gc.setGlobalAlpha(1.0);

            gc.restore();
        }
    }


    private void renderDamagePopups() {

        gc.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        18
                )
        );

        for (DamagePopup popup : damagePopups) {

            double progress =
                    popup.age / DAMAGE_POPUP_DURATION;

            double drawX = popup.x;

            double drawY =
                    popup.y
                            - progress * 45.0;

            double alpha;

            if (progress < 0.55) {
                alpha = 1.0;
            } else {
                alpha =
                        1.0
                                - (
                                (progress - 0.55)
                                        / 0.45
                        );
            }

            alpha =
                    Math.max(
                            0,
                            Math.min(
                                    1,
                                    alpha
                            )
                    );

            String text =
                    "-" + formatDamage(popup.damage);

            gc.setFill(
                    getDamageColor(popup.damageType)
            );
            gc.setGlobalAlpha(alpha);

            gc.setStroke(
                    Color.rgb(
                            0,
                            0,
                            0,
                            alpha
                    )
            );

            gc.setLineWidth(2);

            gc.strokeText(
                    text,
                    drawX,
                    drawY
            );

            gc.fillText(
                    text,
                    drawX,
                    drawY
            );
            gc.setGlobalAlpha(1.0);
        }
    }


    private String formatDamage(
            float damage
    ) {
        if (Math.abs(damage - Math.round(damage)) < 0.001f) {
            return String.valueOf(
                    Math.round(damage)
            );
        }

        return String.format(
                Locale.US,
                "%.1f",
                damage
        );
    }



    private double estimateTextWidth(
            String text
    ) {

        return text.length() * 8.0;
    }

    //temp
    private double measureTextWidth(
            String text
    ) {
        return text.length() * 6.5;
    }
    private void renderTrainingDummy() {

        if (!showTrainingDummy) {
            return;
        }
        if (currentLocation == null
                || !"main_world".equals(
                currentLocation.getId()
        )) {
            return;
        }

        if (trainingDummy == null) {
            return;
        }

        float x = trainingDummy.getX();
        float y = trainingDummy.getY();

        float width =
                trainingDummy.getWidth();

        float height =
                trainingDummy.getHeight();

        float hp =
                trainingDummy.getHp();

        float maxHp =
                trainingDummy.getMaxHp();

        /*
         * Тело манекена
         */
        gc.setFill(
                Color.SADDLEBROWN
        );

        gc.fillRoundRect(
                x,
                y,
                width,
                height,
                8,
                8
        );

        /*
         * Голова
         */
        float headSize = 20f;

        gc.setFill(
                Color.BURLYWOOD
        );

        gc.fillOval(
                x + width / 2f - headSize / 2f,
                y - headSize + 5f,
                headSize,
                headSize
        );

        /*
         * HP bar
         */
        float hpBarWidth = 60f;
        float hpBarHeight = 7f;

        float hpRatio =
                maxHp <= 0f
                        ? 0f
                        : hp / maxHp;

        hpRatio =
                Math.max(
                        0f,
                        Math.min(
                                1f,
                                hpRatio
                        )
                );

        float hpBarX =
                x + width / 2f -
                        hpBarWidth / 2f;

        float hpBarY =
                y - 30f;

        gc.setFill(
                Color.DARKRED
        );

        gc.fillRoundRect(
                hpBarX,
                hpBarY,
                hpBarWidth,
                hpBarHeight,
                4,
                4
        );

        gc.setFill(
                Color.LIMEGREEN
        );

        gc.fillRoundRect(
                hpBarX,
                hpBarY,
                hpBarWidth * hpRatio,
                hpBarHeight,
                4,
                4
        );

        /*
         * Имя
         */
        gc.setFill(
                Color.WHITE
        );

        gc.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        String hpText =

                (int) hp +
                        " / " +
                        (int) maxHp;

        gc.fillText(
                hpText,
                x + width / 2f -
                        hpText.length() * 3.2,
                y - 36f
        );
    }
    private void renderTrainingDummy2() {

        if (!showTrainingDummy) {
            return;
        }

        if (currentLocation == null
                || !"main_world".equals(
                currentLocation.getId()
        )) {
            return;
        }

        if (trainingDummy2 == null) {
            return;
        }

        float x = trainingDummy2.getX();
        float y = trainingDummy2.getY();

        float width =
                trainingDummy2.getWidth();

        float height =
                trainingDummy2.getHeight();

        float hp =
                trainingDummy2.getHp();

        float maxHp =
                trainingDummy2.getMaxHp();

        /*
         * Тело манекена
         */
        gc.setFill(
                Color.SADDLEBROWN
        );

        gc.fillRoundRect(
                x,
                y,
                width,
                height,
                8,
                8
        );

        /*
         * Голова
         */
        float headSize = 20f;

        gc.setFill(
                Color.BURLYWOOD
        );

        gc.fillOval(
                x + width / 2f - headSize / 2f,
                y - headSize + 5f,
                headSize,
                headSize
        );

        /*
         * HP bar
         */
        float hpBarWidth = 60f;
        float hpBarHeight = 7f;

        float hpRatio =
                maxHp <= 0f
                        ? 0f
                        : hp / maxHp;

        hpRatio =
                Math.max(
                        0f,
                        Math.min(
                                1f,
                                hpRatio
                        )
                );

        float hpBarX =
                x + width / 2f
                        - hpBarWidth / 2f;

        float hpBarY =
                y - 30f;

        gc.setFill(
                Color.DARKRED
        );

        gc.fillRoundRect(
                hpBarX,
                hpBarY,
                hpBarWidth,
                hpBarHeight,
                4,
                4
        );

        gc.setFill(
                Color.LIMEGREEN
        );

        gc.fillRoundRect(
                hpBarX,
                hpBarY,
                hpBarWidth * hpRatio,
                hpBarHeight,
                4,
                4
        );

        /*
         * Имя
         */
        gc.setFill(
                Color.WHITE
        );

        gc.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        12
                )
        );

        String hpText =

                (int) hp +
                        " / " +
                        (int) maxHp;

        gc.fillText(
                hpText,
                x + width / 2f
                        - hpText.length() * 3.2,
                y - 36f
        );
        /*
         * =====================================================
         * DEBUFF PANEL
         * =====================================================
         *
         * Показываем только при наведении курсора
         * на манекен.
         */

        boolean mouseOver =
                mouseX >= x
                        && mouseX <= x + width
                        && mouseY >= y
                        && mouseY <= y + height;

        if (!mouseOver) {
            return;
        }

        int speedDebuff =
                trainingDummy.getSpeedDebuff();

        int defenseDebuff =
                trainingDummy.getDefenseDebuff();

        if (speedDebuff == 0
                && defenseDebuff == 0) {
            return;
        }

        List<String> debuffs =
                new ArrayList<>();

        if (speedDebuff < 0) {
            debuffs.add(
                    "⚡ Замедление: "
                            + Math.abs(speedDebuff)
            );
        }

        if (defenseDebuff < 0) {
            debuffs.add(
                    "🛡 Ослабление защиты: "
                            + Math.abs(defenseDebuff)
            );
        }

        if (debuffs.isEmpty()) {
            return;
        }

        gc.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        /*
         * Размер плашки.
         */
        double paddingX = 10;
        double paddingY = 7;
        double lineHeight = 18;

        double panelWidth = 0;

        for (String text : debuffs) {
            panelWidth =
                    Math.max(
                            panelWidth,
                            measureTextWidth(text)
                    );
        }

        panelWidth += paddingX * 2;

        double panelHeight =
                debuffs.size() * lineHeight
                        + paddingY * 2;

        /*
         * Центрируем плашку над именем.
         */
        double panelX =
                x + width / 2.0
                        - panelWidth / 2.0;

        double panelY =
                y - 36.0
                        - panelHeight
                        - 8.0;

        /*
         * Фон.
         */
        gc.setFill(
                Color.rgb(
                        15,
                        15,
                        20,
                        0.90
                )
        );

        gc.fillRoundRect(
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                8,
                8
        );

        /*
         * Рамка.
         */
        gc.setStroke(
                Color.rgb(
                        120,
                        120,
                        130,
                        0.9
                )
        );

        gc.setLineWidth(1.5);

        gc.strokeRoundRect(
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                8,
                8
        );

        /*
         * Текст дебаффов.
         */
        gc.setFill(
                Color.WHITE
        );

        double textY =
                panelY + paddingY + 13;

        for (String text : debuffs) {

            gc.fillText(
                    text,
                    panelX + paddingX,
                    textY
            );

            textY += lineHeight;
        }
    }

    private void renderTargetDebuffTooltip() {

        LocalCombatTarget hoveredTarget = null;

        /*
         * Ищем цель под курсором.
         *
         * mouseX / mouseY находятся в координатах Canvas,
         * поэтому переводим их обратно в координаты мира.
         */
        if (localPlayer == null) {
            return;
        }

        double cameraX =
                localPlayer.renderX
                        + PLAYER_SIZE / 2.0;

        double cameraY =
                localPlayer.renderY
                        + PLAYER_SIZE / 2.0;

        double worldMouseX =
                cameraX
                        + (
                        mouseX
                                - canvas.getWidth() / 2.0
                ) / CAMERA_ZOOM;

        double worldMouseY =
                cameraY
                        + (
                        mouseY
                                - canvas.getHeight() / 2.0
                ) / CAMERA_ZOOM;

        for (LocalCombatTarget target :
                combatSystem.getTargets()) {

            if (target == null) {
                continue;
            }

            boolean inside =
                    worldMouseX >= target.getX()
                            && worldMouseX <=
                            target.getX()
                                    + target.getWidth()
                            && worldMouseY >= target.getY()
                            && worldMouseY <=
                            target.getY()
                                    + target.getHeight();

            if (inside) {
                hoveredTarget = target;
                break;
            }
        }

        if (hoveredTarget == null) {
            return;
        }

        /*
         * Позиция плашки над целью.
         */
        float targetCenterX =
                hoveredTarget.getX()
                        + hoveredTarget.getWidth() / 2f;

        float tooltipY =
                hoveredTarget.getY()
                        - 65f;

        /*
         * Формируем содержимое.
         */
        int speedDebuff =
                hoveredTarget.getSpeedDebuff();

        int defenseDebuff =
                hoveredTarget.getDefenseDebuff();

        String speedText =
                speedDebuff < 0
                        ? "Замедление: " + Math.abs(speedDebuff)
                        : null;

        String defenseText =
                defenseDebuff < 0
                        ? "Ослабление защиты: "
                        + Math.abs(defenseDebuff)
                        : null;

        /*
         * Даже если дебаффов нет,
         * плашка всё равно показывается.
         */
        List<String> lines =
                new ArrayList<>();

        lines.add(
                "Цель: " + hoveredTarget.getId()
        );

        if (speedText != null) {
            lines.add("⚡ " + speedText);
        }

        if (defenseText != null) {
            lines.add("🛡 " + defenseText);
        }

        if (speedText == null && defenseText == null) {
            lines.add("Дебаффов нет");
        }

        /*
         * Размер плашки.
         */
        gc.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        13
                )
        );

        double lineHeight = 18;
        double padding = 8;

        double maxTextWidth = 0;

        for (String line : lines) {
            maxTextWidth =
                    Math.max(
                            maxTextWidth,
                            measureTextWidth(line)
                    );
        }

        double tooltipWidth =
                maxTextWidth + padding * 2;

        double tooltipHeight =
                lines.size() * lineHeight
                        + padding * 2;

        double tooltipX =
                targetCenterX
                        - tooltipWidth / 2.0;

        double tooltipTop =
                tooltipY
                        - tooltipHeight;

        /*
         * Фон.
         */
        gc.setFill(
                Color.rgb(
                        10,
                        10,
                        15,
                        0.90
                )
        );

        gc.fillRoundRect(
                tooltipX,
                tooltipTop,
                tooltipWidth,
                tooltipHeight,
                8,
                8
        );

        /*
         * Рамка.
         */
        gc.setStroke(
                Color.rgb(
                        255,
                        255,
                        255,
                        0.35
                )
        );

        gc.setLineWidth(1);

        gc.strokeRoundRect(
                tooltipX,
                tooltipTop,
                tooltipWidth,
                tooltipHeight,
                8,
                8
        );

        /*
         * Текст.
         */
        gc.setFill(Color.WHITE);

        for (int i = 0; i < lines.size(); i++) {

            gc.fillText(
                    lines.get(i),
                    tooltipX + padding,
                    tooltipTop
                            + padding
                            + lineHeight
                            * (i + 1)
                            - 4
            );
        }
    }

    /*
     * =====================================================
     * BACKGROUND
     * =====================================================
     */

    private void renderBackground(
            double cameraX,
            double cameraY,
            double screenWidth,
            double screenHeight
    ) {

        gc.setFill(
                Color.web("#444444")
        );

        /*
         * Видимая область мира.
         */
        double left =
                Math.max(
                        0,
                        cameraX
                                - screenWidth / 2
                                - 100
                );

        double top =
                Math.max(
                        0,
                        cameraY
                                - screenHeight / 2
                                - 100
                );

        double right =
                Math.min(
                        worldWidth,
                        cameraX
                                + screenWidth / 2
                                + 100
                );

        double bottom =
                Math.min(
                        worldHeight,
                        cameraY
                                + screenHeight / 2
                                + 100
                );

        /*
         * Фон.
         */
        gc.fillRect(
                left,
                top,
                Math.max(
                        0,
                        right - left
                ),
                Math.max(
                        0,
                        bottom - top
                )
        );

        /*
         * Сетка.
         */
        gc.setStroke(
                Color.rgb(
                        255,
                        255,
                        255,
                        0.05
                )
        );

        int gridSize = 100;

        int startX =
                ((int) Math.max(
                        0,
                        left
                ) / gridSize)
                        * gridSize;

        int startY =
                ((int) Math.max(
                        0,
                        top
                ) / gridSize)
                        * gridSize;

        for (
                int x = startX;
                x <= right;
                x += gridSize
        ) {

            gc.strokeLine(
                    x,
                    top,
                    x,
                    bottom
            );
        }

        for (
                int y = startY;
                y <= bottom;
                y += gridSize
        ) {

            gc.strokeLine(
                    left,
                    y,
                    right,
                    y
            );
        }
    }

    /*
     * =====================================================
     * WORLD OBJECTS
     * =====================================================
     */

    private void renderWorldObjects(
            double cameraX,
            double cameraY,
            double screenWidth,
            double screenHeight
    ) {

        /*
         * Видимая область.
         */
        double visibleLeft =
                cameraX
                        - screenWidth / 2;

        double visibleRight =
                cameraX
                        + screenWidth / 2;

        double visibleTop =
                cameraY
                        - screenHeight / 2;

        double visibleBottom =
                cameraY
                        + screenHeight / 2;

        for (WorldObject object :
                objects) {

            /*
             * Frustum culling.
             */
            if (
                    object.x + object.width
                            < visibleLeft
                            ||
                            object.x
                                    > visibleRight
                            ||
                            object.y + object.height
                                    < visibleTop
                            ||
                            object.y
                                    > visibleBottom
            ) {

                continue;
            }

            gc.drawImage(
                    object.image,
                    object.x,
                    object.y,
                    object.width,
                    object.height
            );
        }
    }

    /*
     * =====================================================
     * PLAYERS
     * =====================================================
     */


    private void activateTinWallRam() {
        if (!isArtifactEquipped(Artifact.TIN_WALL)
                || tinWallRamCooldown > 0.0) {
            return;
        }
        float[] aim = getAimDirection(mouseX, mouseY);
        tinWallArcAngle = Math.atan2(aim[1], aim[0]) + Math.PI;
        tinWallRamTimer = 0.65;
        tinWallRamCooldown = 1.5;
        tinWallRamHitTargets.clear();
    }

    private void applyTinWallRamDamage(float playerCenterX, float playerCenterY) {
        Weapon weapon = getCurrentWeapon();
        float physicalDamage = weapon == null
                ? 0f
                : DamageCalculator.calculateTickDamage(weapon, PLAYER_ATTACK);
        float ramDamage = 10f + physicalDamage * 0.25f;
        double halfArc = Math.PI / 5.0; // дуга 1/5 окружности = 72 градуса
        double radius = PLAYER_SIZE * 2.2;

        for (LocalCombatTarget target : combatSystem.getTargets()) {
            if (target == null || !target.isAlive()
                    || tinWallRamHitTargets.contains(target.getId())) {
                continue;
            }
            double tx = target.getX() + target.getWidth() / 2.0 - playerCenterX;
            double ty = target.getY() + target.getHeight() / 2.0 - playerCenterY;
            double distance = Math.hypot(tx, ty);
            if (distance > radius) continue;
            double targetAngle = Math.atan2(ty, tx);
            double angleDiff = Math.atan2(
                    Math.sin(targetAngle - tinWallArcAngle),
                    Math.cos(targetAngle - tinWallArcAngle));
            if (Math.abs(angleDiff) <= halfArc) {
                target.damage(ramDamage);
                tinWallRamHitTargets.add(target.getId());
                showDamagePopup(
                        target.getX() + target.getWidth() / 2f,
                        target.getY() + target.getHeight() / 2f,
                        ramDamage,
                        DamageType.PHYSICAL
                );
            }
        }
    }

    private void applyTinWallContactDamage(float playerCenterX, float playerCenterY) {
        Weapon weapon = getCurrentWeapon();
        float physicalDamage = weapon == null
                ? PLAYER_ATTACK
                : DamageCalculator.calculateTickDamage(weapon, PLAYER_ATTACK);
        float damage = 10f + physicalDamage * 0.25f;
        double halfArc = Math.PI / 5.0;
        double maxDistance = PLAYER_SIZE * 2.8;
        long now = System.nanoTime();

        for (LocalCombatTarget target : combatSystem.getTargets()) {
            if (target == null || !target.isAlive()) continue;
            double tx = target.getX() + target.getWidth() / 2.0 - playerCenterX;
            double ty = target.getY() + target.getHeight() / 2.0 - playerCenterY;
            double distance = Math.hypot(tx, ty);
            if (distance > maxDistance || distance < PLAYER_SIZE * 0.8) continue;

            double targetAngle = Math.atan2(ty, tx);
            double angleDiff = Math.atan2(Math.sin(targetAngle - tinWallArcAngle),
                    Math.cos(targetAngle - tinWallArcAngle));
            if (Math.abs(angleDiff) > halfArc) continue;

            long lastHit = tinWallLastContactHitNanos.getOrDefault(target.getId(), 0L);
            if (now - lastHit < 300_000_000L) continue;

            target.damage(damage);
            tinWallLastContactHitNanos.put(target.getId(), now);
            showDamagePopup(target.getX() + target.getWidth() / 2f,
                    target.getY() + target.getHeight() / 2f, damage, DamageType.PHYSICAL);
        }
    }

    private void renderTinWallArc() {
        if (!isArtifactEquipped(Artifact.TIN_WALL) || localPlayer == null) return;

        float[] aim = getAimDirection(mouseX, mouseY);
        double baseAngle = Math.atan2(aim[1], aim[0]) + Math.PI;
        if (tinWallRamTimer <= 0.0) tinWallArcAngle = baseAngle;

        double cx = localPlayer.renderX + PLAYER_SIZE / 2.0;
        double cy = localPlayer.renderY + PLAYER_SIZE / 2.0;
        double radius = PLAYER_SIZE * 1.9;
        double halfArc = Math.PI / 5.0;

        // Рисуем дугу через мировые координаты (x=cos, y=sin),
        // чтобы центр дуги точно смотрел на 180 градусов от курсора.
        gc.save();
        gc.setStroke(Color.web("#66717D", 0.98));
        gc.setLineWidth(17.0);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.beginPath();
        for (int i = 0; i <= 24; i++) {
            double angle = tinWallArcAngle - halfArc + (halfArc * 2.0) * i / 24.0;
            double x = cx + Math.cos(angle) * radius;
            double y = cy + Math.sin(angle) * radius;
            if (i == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.stroke();
        gc.setStroke(Color.web("#E2E8F0", 0.98));
        gc.setLineWidth(3.0);
        gc.stroke();
        gc.restore();
    }

    private void renderEtherealPhantom() {
        if (System.nanoTime() >= etherealPhantomUntilNanos) return;
        double remaining = (etherealPhantomUntilNanos - System.nanoTime()) / 6_000_000_000.0;
        gc.save();
        gc.setGlobalAlpha(Math.max(0.15, Math.min(0.65, remaining * 0.65)));
        gc.setFill(Color.web("#8DEBFF"));
        gc.fillOval(etherealPhantomX - 9, etherealPhantomY - 15, 18, 18);
        gc.fillRoundRect(etherealPhantomX - 7, etherealPhantomY + 2, 14, 23, 5, 5);
        gc.setStroke(Color.web("#D9FAFF"));
        gc.setLineWidth(2.0);
        gc.strokeOval(etherealPhantomX - 12, etherealPhantomY - 18, 24, 45);
        gc.restore();
    }

    private void renderPlayers() {
        for (RemotePlayer player : players.values()) {
            double centerX = player.renderX + PLAYER_SIZE / 2.0;
            double centerY = player.renderY + PLAYER_SIZE / 2.0;
            double size = PLAYER_VISUAL_SIZE;

            boolean isLocal = player == localPlayer;

            boolean moving = isLocal && (up || down || left || right);

            double stride = moving
                    ? Math.sin(playerAnimationTime * 10.0)
                    : 0.0;

// При ходьбе движение заметнее, в покое — едва заметно.
            double bounce = moving
                    ? Math.abs(stride) * 3.0
                    : Math.sin(playerAnimationTime * 2.0) * 0.5;

            boolean attacking = isLocal
                    && (attackAnimationTimer > 0.0
                    || attackHeld
                    || secondaryAttackHeld);

            double attackProgress = attackAnimationTimer > 0.0
                    ? 1.0 - attackAnimationTimer / ATTACK_ANIMATION_DURATION
                    : 0.0;

// Сила замаха: сначала замах, затем удар.
            double attackSwing = attacking
                    ? Math.sin(attackProgress * Math.PI) * 55.0
                    : 0.0;

// Направление текущей атаки.
            float[] aim = getAimDirection(mouseX, mouseY);
            double aimAngle = Math.toDegrees(
                    Math.atan2(aim[1], aim[0])
            );

// При атаке корпус наклоняется в сторону цели.
            double bodyLean = attacking
                    ? Math.sin(attackProgress * Math.PI) * 5.0
                    : 0.0;

// Рука направляется к цели.
// У drawPlayerLimb нулевой угол означает движение вниз,
// поэтому вычитаем 90 градусов.
            double attackArmAngle = aimAngle - 90.0;

// Размах ног при ходьбе.
            double legSwing = stride * 22.0;

// Движение рук при ходьбе.
            double armSwing = stride * 18.0;

            gc.save();
            gc.translate(centerX, centerY + bounce);

// Наклон вперёд во время атаки.
            gc.rotate(bodyLean);

            boolean isLocalPlayer = player == localPlayer;


            if (hardcoreMode) {
                if (isLocalPlayer && oneHpMode) {
                    drawStickman(
                            stride,
                            attacking,
                            attackArmAngle,
                            legSwing,
                            armSwing
                    );
                } else {
                    drawPlayerSilhouette(
                            stride,
                            attacking,
                            attackSwing,
                            attackArmAngle,
                            legSwing,
                            armSwing
                    );
                }
            } else {

                drawGlitchCharacter(
                        isLocalPlayer && oneHpMode,
                        stride,
                        attacking,
                        attackSwing,
                        attackArmAngle,
                        legSwing,
                        armSwing
                );

            }


            gc.restore();

            // Имя над персонажем.
            gc.setFill(Color.WHITE);
            gc.fillText(
                    player.username,
                    centerX - size / 2.0,
                    centerY - size / 2.0 - 8
            );
        }
    }

    private void drawStickman(
            double stride,
            boolean attacking,
            double attackArmAngle,
            double legSwing,
            double armSwing
    ) {
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setLineWidth(6.0);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        // Голова.
        gc.fillOval(-12, -46, 24, 24);

        // Туловище.
        gc.strokeLine(0, -20, 0, 19);



        if (attacking) {
            // attackArmAngle — угол конечности, направленной вниз
            // при значении 0. Восстанавливаем угол направления к цели.
            double aimAngle = Math.toRadians(attackArmAngle + 90.0);

            double dirX = Math.cos(aimAngle);
            double dirY = Math.sin(aimAngle);

            // Выбираем плечо со стороны цели.
            double shoulderX = dirX >= 0.0 ? 5.0 : -5.0;

            // Атакующая рука идёт от плеча строго к цели.
            gc.strokeLine(
                    shoulderX, -12,
                    shoulderX + dirX * 31.0,
                    -12 + dirY * 31.0
            );

            // Вторая рука короткая и прижата к туловищу.
            // Не отбрасываем её в противоположную сторону.
            // Вспомогательная рука видна только при атаке почти строго вниз.
            boolean keepSupportArm =
                    dirY > 0.8 && Math.abs(dirX) < 0.35;

            if (keepSupportArm) {
                // При атаке вниз оставляем короткую вспомогательную руку.
                double supportX = -shoulderX;

                gc.strokeLine(
                        supportX, -12,
                        supportX, 0
                );
            } else if (dirY < -0.35) {
                // При атаке вверх показываем только плечи,
                // без вытянутой вспомогательной руки.
                gc.strokeLine(-5, -12, -2, -9);
                gc.strokeLine(5, -12, 2, -9);
            }
        } else {
            // Руки движутся в противофазе ногам.
            gc.strokeLine(
                    -5, -12,
                    -22 - armSwing * 0.4,
                    8 + armSwing * 0.3
            );

            gc.strokeLine(
                    5, -12,
                    22 + armSwing * 0.4,
                    8 - armSwing * 0.3
            );
        }


        // Ноги.
        gc.strokeLine(
                0, 19,
                -17 + legSwing, 43
        );

        gc.strokeLine(
                0, 19,
                17 - legSwing, 43
        );
    }


    private void drawPlayerSilhouette(
            double stride,
            boolean attacking,
            double attackSwing,
            double attackArmAngle,
            double legSwing,
            double armSwing
    ) {
        gc.setFill(Color.BLACK);

        // Ноги.
        drawPlayerLimb(
                -8, 18,
                -legSwing,
                31, 11
        );

        drawPlayerLimb(
                8, 18,
                legSwing,
                31, 11
        );

        // Ступни.
        gc.fillRoundRect(
                -19 + stride * 5,
                43,
                19, 8, 5, 5
        );

        gc.fillRoundRect(
                -stride * 5,
                43,
                19, 8, 5, 5
        );

        // Корпус.
        gc.fillPolygon(
                new double[]{
                        -13, -27, -27, -17,
                        -12, 12, 17, 27,
                        27, 13, 10, -10
                },
                new double[]{
                        -24, -19, -10, -3,
                        19, 19, -3, -10,
                        -19, -24, 14, 14
                },
                12
        );

        // Шея и голова.
        gc.fillRoundRect(-6, -36, 12, 15, 4, 4);
        gc.fillOval(-14, -55, 28, 27);

        gc.fillPolygon(
                new double[]{-11, 11, 8, -8},
                new double[]{-35, -35, -27, -27},
                4
        );




        if (attacking) {
            double aimAngle = Math.toRadians(attackArmAngle + 90.0);
            double dirX = Math.cos(aimAngle);
            double dirY = Math.sin(aimAngle);

            // При атаке вверх полностью скрываем обе руки.
            // Корпус, голова и ноги продолжают рисоваться.
            if (dirY >= -0.35) {
                double attackShoulderX = dirX >= 0.0 ? 22.0 : -22.0;

                drawPlayerLimb(
                        attackShoulderX, -19,
                        attackArmAngle, 31, 10
                );

                // Вспомогательная рука — только при ударе почти строго вниз.
                boolean keepSupportArm =
                        dirY > 0.8 && Math.abs(dirX) < 0.35;

                if (keepSupportArm) {
                    double supportShoulderX = -attackShoulderX;

                    drawPlayerLimb(
                            supportShoulderX, -19,
                            0.0, 12, 9
                    );
                }
            }
        } else {
            drawPlayerLimb(
                    -22, -19,
                    -armSwing,
                    31, 10
            );

            drawPlayerLimb(
                    22, -19,
                    armSwing,
                    31, 10
            );
        }



    }



    private void drawGlitchCharacter(
            boolean stickman,
            double stride,
            boolean attacking,
            double attackSwing,
            double attackArmAngle,
            double legSwing,
            double armSwing
    ) {
        final double cell = 6.0;

        final Color[] palette = {
                Color.rgb(0, 220, 255),
                Color.rgb(35, 100, 255),
                Color.rgb(130, 55, 255),
                Color.rgb(255, 35, 170),
                Color.rgb(255, 55, 65),
                Color.rgb(255, 125, 30),
                Color.rgb(255, 220, 45),
                Color.rgb(60, 255, 100),
                Color.rgb(235, 245, 255),
                Color.rgb(55, 60, 75),
                Color.rgb(100, 110, 130)
        };

        for (double y = -60; y < 55; y += cell) {
            for (double x = -35; x < 35; x += cell) {
                double cx = x + cell / 2.0;
                double cy = y + cell / 2.0;

                if (!isInsideCharacter(
                        cx, cy, stickman,
                        stride, attacking, attackArmAngle,
                        legSwing, armSwing
                )) {
                    continue;
                }

                double phase = glitchAnimationTime * 5.3
                        + cx * 0.173
                        + cy * 0.317;

                double noise = Math.sin(phase * 1.71)
                        * Math.sin(phase * 0.83 + cx * 0.21)
                        * Math.cos(
                        cy * 0.39
                                - glitchAnimationTime * 2.7
                );

                if (noise > 0.91) {
                    continue;
                }

                double burst = Math.sin(
                        cx * 0.41 + glitchAnimationTime * 3.7
                ) * Math.cos(
                        cy * 0.27 - glitchAnimationTime * 2.9
                );

                boolean displaced = burst > 0.78
                        && Math.sin(phase * 2.3) > -0.1;

                double width = cell;
                double height = cell;

                double sizeNoise = Math.sin(
                        phase * 2.91 + cx * 0.13
                );

                if (sizeNoise > 0.55) {
                    width = cell * 1.6;
                }

                if (sizeNoise < -0.58) {
                    height = cell * 1.5;
                }

                double offsetX = Math.sin(phase * 1.9) * 1.2;
                double offsetY = Math.cos(phase * 1.4) * 0.8;

                if (displaced) {
                    offsetX += 5.0
                            + Math.abs(Math.sin(phase)) * 7.0;

                    height = Math.max(2.0, cell * 0.65);
                    width = cell * (
                            1.8 + Math.abs(Math.sin(phase))
                    );
                }

                double colorNoise = Math.sin(
                        phase * 2.17 + cx * 0.17
                ) * Math.cos(
                        phase * 1.37 - cy * 0.23
                );

                int colorIndex = (int) Math.floor(
                        Math.abs(Math.sin(
                                cx * 0.31
                                        + cy * 0.47
                                        + glitchAnimationTime * 1.9
                        )) * palette.length
                );

                colorIndex = Math.min(
                        palette.length - 1,
                        Math.max(0, colorIndex)
                );

                Color color;

                if (colorNoise > -0.05) {
                    color = palette[colorIndex];
                } else if (colorNoise < -0.64) {
                    color = palette[8];
                } else {
                    double shade = 0.035 + Math.abs(noise) * 0.17;
                    color = Color.gray(shade);
                }

                gc.setFill(color);
                gc.fillRect(
                        x + offsetX,
                        y + offsetY,
                        width,
                        height
                );

                if (displaced && Math.sin(phase * 3.13) > 0.38) {
                    gc.setFill(
                            palette[(colorIndex + 3) % palette.length]
                    );

                    gc.fillRect(
                            x + offsetX + width,
                            y + offsetY + cell * 0.25,
                            cell * 0.7,
                            cell * 0.55
                    );
                }
            }
        }
    }




    private boolean isInsideCharacter(
            double x,
            double y,
            boolean stickman,
            double stride,
            boolean attacking,
            double attackArmAngle,
            double legSwing,
            double armSwing
    ) {
        if (stickman) {
            // Голова.
            if (insideEllipse(x, y, 0, -34, 12, 12)) {
                return true;
            }

            // Туловище.
            if (nearSegment(x, y, 0, -20, 0, 19, 4)) {
                return true;
            }

            // Руки — маска повторяет drawStickman().

            if (attacking) {
                // Точно повторяем направление руки из drawStickman().
                double aimAngle = Math.toRadians(attackArmAngle + 90.0);
                double dirX = Math.cos(aimAngle);
                double dirY = Math.sin(aimAngle);

                double shoulderX = dirX >= 0.0 ? 5.0 : -5.0;
                double handX = shoulderX + dirX * 31.0;
                double handY = -12.0 + dirY * 31.0;

                if (nearSegment(
                        x, y,
                        shoulderX, -12,
                        handX, handY,
                        4
                )) {
                    return true;
                }

                boolean keepSupportArm =
                        dirY > 0.8 && Math.abs(dirX) < 0.35;

                if (keepSupportArm) {
                    double supportX = -shoulderX;

                    if (nearSegment(
                            x, y,
                            supportX, -12,
                            supportX, 0,
                            4
                    )) {
                        return true;
                    }
                }
            } else {
                if (nearSegment(
                        x, y,
                        -5, -12,
                        -22 - armSwing * 0.4,
                        8 + armSwing * 0.3,
                        4
                )) {
                    return true;
                }

                if (nearSegment(
                        x, y,
                        5, -12,
                        22 + armSwing * 0.4,
                        8 - armSwing * 0.3,
                        4
                )) {
                    return true;
                }
            }


            // Ноги двигаются в соответствии с шагом.
            return nearSegment(
                    x, y, 0, 19,
                    -17 + legSwing, 43, 4
            )
                    || nearSegment(
                    x, y, 0, 19,
                    17 - legSwing, 43, 4
            );
        }

        // Голова и шея гуманоидного силуэта.
        if (insideEllipse(x, y, 0, -41.5, 14, 13.5)
                || insideEllipse(x, y, 0, -28.5, 6, 7.5)) {
            return true;
        }

        // Корпус.
        double halfWidth;

        if (y >= -24 && y <= -19) {
            halfWidth = 13 + (y + 24) * 2.8;
        } else if (y > -19 && y <= -10) {
            halfWidth = 27;
        } else if (y > -10 && y <= -3) {
            halfWidth = 27 - (y + 10) * 1.4;
        } else if (y > -3 && y <= 19) {
            halfWidth = 17 - (y + 3) * 0.2;
        } else {
            halfWidth = 0;
        }

        if (Math.abs(x) <= halfWidth) {
            return true;
        }

        // Анимация ног: угол drawPlayerLimb задаёт направление
        // относительно вертикального положения вниз.
        double leftLegAngle = -legSwing;
        double rightLegAngle = legSwing;
        double legLength = 31;

        double leftLegEndX =
                -8 - Math.sin(Math.toRadians(leftLegAngle)) * legLength;
        double leftLegEndY =
                18 + Math.cos(Math.toRadians(leftLegAngle)) * legLength;

        double rightLegEndX =
                8 - Math.sin(Math.toRadians(rightLegAngle)) * legLength;
        double rightLegEndY =
                18 + Math.cos(Math.toRadians(rightLegAngle)) * legLength;

        if (nearSegment(
                x, y, -8, 18, leftLegEndX, leftLegEndY, 6
        ) || nearSegment(
                x, y, 8, 18, rightLegEndX, rightLegEndY, 6
        )) {
            return true;
        }

        // Анимация рук.

        // Маска рук должна повторять drawPlayerSilhouette().
        if (attacking) {
            double aimAngle = Math.toRadians(attackArmAngle + 90.0);
            double dirX = Math.cos(aimAngle);
            double dirY = Math.sin(aimAngle);

            double attackShoulderX = dirX >= 0.0 ? 22.0 : -22.0;

            // Маска атакующей руки.
            double attackEndX = attackShoulderX
                    - Math.sin(Math.toRadians(attackArmAngle)) * 31.0;
            double attackEndY = -19.0
                    + Math.cos(Math.toRadians(attackArmAngle)) * 31.0;

            if (nearSegment(
                    x, y,
                    attackShoulderX, -19,
                    attackEndX, attackEndY,
                    6
            )) {
                return true;
            }

            // Вспомогательная рука видна только при ударе строго вниз.
            boolean keepSupportArm =
                    dirY > 0.8 && Math.abs(dirX) < 0.35;

            if (keepSupportArm) {
                double supportShoulderX = -attackShoulderX;

                return nearSegment(
                        x, y,
                        supportShoulderX, -19,
                        supportShoulderX, -7,
                        5
                );
            }

            return false;
        }

        // Руки при ходьбе — углы совпадают с отрисовкой.
        double leftArmEndX = -22
                - Math.sin(Math.toRadians(-armSwing)) * 31.0;
        double leftArmEndY = -19
                + Math.cos(Math.toRadians(-armSwing)) * 31.0;

        double rightArmEndX = 22
                - Math.sin(Math.toRadians(armSwing)) * 31.0;
        double rightArmEndY = -19
                + Math.cos(Math.toRadians(armSwing)) * 31.0;

        return nearSegment(
                x, y, -22, -19,
                leftArmEndX, leftArmEndY, 6
        ) || nearSegment(
                x, y, 22, -19,
                rightArmEndX, rightArmEndY, 6
        );

    }


    private boolean insideEllipse(
            double x,
            double y,
            double centerX,
            double centerY,
            double radiusX,
            double radiusY
    ) {
        double dx = (x - centerX) / radiusX;
        double dy = (y - centerY) / radiusY;

        return dx * dx + dy * dy <= 1.0;
    }

    private boolean nearSegment(
            double px,
            double py,
            double x1,
            double y1,
            double x2,
            double y2,
            double radius
    ) {
        double dx = x2 - x1;
        double dy = y2 - y1;

        double lengthSquared = dx * dx + dy * dy;

        double t = lengthSquared == 0
                ? 0
                : ((px - x1) * dx + (py - y1) * dy)
                / lengthSquared;

        t = Math.max(0, Math.min(1, t));

        double nearestX = x1 + t * dx;
        double nearestY = y1 + t * dy;

        double distanceX = px - nearestX;
        double distanceY = py - nearestY;

        return distanceX * distanceX + distanceY * distanceY
                <= radius * radius;
    }

    /**
     * Рисует конечность от заданного сустава.
     * Угол задаётся в градусах.
     */
    private void drawPlayerLimb(
            double pivotX,
            double pivotY,
            double angle,
            double length,
            double thickness
    ) {
        gc.save();

        gc.translate(pivotX, pivotY);
        gc.rotate(angle);

        gc.fillRoundRect(
                -thickness / 2.0,
                0,
                thickness,
                length,
                thickness,
                thickness
        );

        gc.restore();
    }


    /**
     * Отрисовывает спрайт экипированного копья
     * поверх персонажа, направляя его к курсору.
     */

    /**
     * Отрисовывает экипированное оружие поверх персонажа.
     *
     * Дальнобойное оружие отображается постоянно и поворачивается
     * вслед за прицелом. Оружие ближнего боя отображается во время
     * соответствующей анимации атаки.
     */

    /**
     * Рисует экипированное оружие с учётом положения руки
     * и направления атаки.
     *
     * foreground == false — оружие позади персонажа.
     * foreground == true  — оружие перед персонажем,
     *                       только при атаке вниз.
     */
    private void renderEquippedWeapon(boolean foreground) {
        if (localPlayer == null) {
            return;
        }

        Weapon weapon = meleeActive
                ? inventory.getEquippedMeleeWeapon()
                : inventory.getEquippedRangedWeapon();

        if (weapon == null) {
            return;
        }

        Image sprite = weaponSpriteManager.getSprite(weapon.getId());

        if (sprite == null) {
            return;
        }

        double playerX =
                localPlayer.renderX + PLAYER_SIZE / 2.0;

        double playerY =
                localPlayer.renderY + PLAYER_SIZE / 2.0;

        double angle;
        double distance;
        double size;

        /*
         * ДАЛЬНИЙ БОЙ
         */
        if (weapon.getType() == WeaponType.RANGED) {
            boolean attacking =
                    attackHeld
                            || secondaryAttackHeld
                            || doomStarCharging;

            if (!attacking) {
                return;
            }

            float[] direction = getAimDirection(mouseX, mouseY);

            angle = Math.atan2(direction[1], direction[0]);

            // Стреляем вниз — оружие будет перед персонажем.
            boolean attackDown = direction[1] > 0.35f;

            if (foreground != attackDown) {
                return;
            }

            size = 96.0;

            // Переносим оружие из центра тела к руке.
            distance = 12.0;

        } else {
            /*
             * БЛИЖНИЙ БОЙ
             */
            if (!meleeActive) {
                return;
            }

            SwordSwing activeSwing = null;

            for (SwordSwing swing : combatSystem.getActiveSwings()) {
                activeSwing = swing;
                break;
            }

            if (activeSwing == null) {
                return;
            }

            angle = activeSwing.getWorldAngleRadians();


            // Вниз направлены атаки с положительной составляющей Y.
            boolean attackDown = Math.sin(angle) > 0.35;

            if (foreground != attackDown) {
                return;
            }

            double progress = activeSwing.getProgress();

            size = 112.0;

            if (activeSwing.getStyle() == AttackStyle.THRUST
                    || activeSwing.getStyle() == AttackStyle.STAB) {

                // При уколе оружие выдвигается из руки вперёд.
                double extension;

                if (progress < 0.65) {
                    extension = progress / 0.65;
                } else {
                    extension = 1.0
                            - (progress - 0.65) / 0.35 * 0.25;
                }

                extension = Math.max(0.0, Math.min(1.0, extension));

                distance = 24.0 + 38.0 * extension;

            } else {
                // Точка удержания меча остаётся около руки.
                distance = 24.0
                        + Math.sin(progress * Math.PI) * 8.0;
            }
        }



        double dirX = Math.cos(angle);
        double dirY = Math.sin(angle);
        boolean attackUp = dirY < -0.35;

// Плечо выбирается со стороны атаки.
        double shoulderX = dirX >= 0.0 ? 22.0 : -22.0;
        double shoulderY = -19.0;

// Запястье ведущей руки.
        double wristX = playerX + shoulderX + dirX * 31.0;
        double wristY = playerY + shoulderY + dirY * 31.0;

        // Позиция спрайта относительно кисти — исходный расчёт.
        double handX = wristX + dirX * distance;
        double handY = wristY + dirY * distance;

        // При атаке вверх немного поднимаем оружие к голове.
        // Смещение не меняет положение запястья и направление удара.
        if (meleeActive && attackUp) {
            handY -= 20.0;
        }

// Небольшие визуальные поправки для читаемости позы.
        if (dirY > 0.65) {
            // Атака вниз: опускаем оружие ниже плеч.
            handY += 18.0;
        } else if (Math.abs(dirX) > 0.65) {
            // Атака влево/вправо: чуть ниже и в сторону цели.
            handX += Math.signum(dirX) * 7.0;
            handY += 8.0;
        }


        gc.save();

        gc.translate(handX, handY);

// Спрайты дальнего боя изначально направлены слева направо.
// Спрайты оружия ближнего боя сохраняют прежний угол +45°.
        double spriteAngle =
                Math.toDegrees(angle)
                        + (weapon.getType() == WeaponType.RANGED ? 0.0 : 45.0);

        gc.rotate(spriteAngle);

        gc.drawImage(
                sprite,
                -size / 2.0,
                -size / 2.0,
                size,
                size
        );

        gc.restore();

    }



    /*
     * =====================================================
     * WORLD BORDER
     * =====================================================
     */

    private void renderWorldBorder() {

        gc.setStroke(
                Color.WHITE
        );

        gc.setLineWidth(
                4
        );

        gc.strokeRect(
                0,
                0,
                worldWidth,
                worldHeight
        );
    }

    /*
     * =====================================================
     * UI
     * =====================================================
     */

    private void renderInfo() {

        if (localPlayer == null) {
            return;
        }

        infoLabel.setText(
                "WORLD "
                        + (int) worldWidth
                        + " × "
                        + (int) worldHeight

                        + "\n"
                        + "X: "
                        + (int) localPlayer.renderX

                        + "    Y: "
                        + (int) localPlayer.renderY

                        + "\n"
                        + "PLAYERS: "
                        + players.size()

                        + "\n"
                        + "PING: "
                        + client.getPing()
                        + " ms"
        );
    }

    /*
     * =====================================================
     * WORLD DATA
     * =====================================================
     */

    public void loadWorld(
            WorldData data
    ) {

        objects.clear();

        if (data == null) {
            return;
        }

        /*
         * =================================================
         * WORLD SIZE
         * =================================================
         */

        if (data.width > 0) {
            worldWidth =
                    data.width;
        }

        if (data.height > 0) {
            worldHeight =
                    data.height;
        }

        /*
         * =================================================
         * LOCAL PLAYER SPAWN
         * =================================================
         */

        if (localPlayer != null) {

            float spawnX =
                    (float) (
                            worldWidth
                                    / 2.0
                                    - PLAYER_SIZE / 2.0
                    );

            float spawnY =
                    (float) (
                            worldHeight
                                    / 2.0
                                    - PLAYER_SIZE / 2.0
                    );

            localPlayer.serverX =
                    spawnX;

            localPlayer.serverY =
                    spawnY;

            localPlayer.renderX =
                    spawnX;

            localPlayer.renderY =
                    spawnY;
        }

        /*
         * =================================================
         * WORLD OBJECTS
         * =================================================
         */

        if (data.objects == null) {
            return;
        }

        for (PlacedObjectData placed :
                data.objects) {

            if (placed == null) {
                continue;
            }

            ObjectDefinition definition =
                    objectRegistry.getById(
                            placed.objectId
                    );

            if (definition == null) {

                System.out.println(
                        "OBJECT NOT FOUND: "
                                + placed.objectId
                );

                continue;
            }

            if (definition.getTexture() == null) {
                continue;
            }

            try {

                InputStream stream =
                        getClass()
                                .getClassLoader()
                                .getResourceAsStream(
                                        definition.getTexture()
                                );

                if (stream == null) {

                    System.out.println(
                            "MISSING TEXTURE: "
                                    + definition.getTexture()
                    );

                    continue;
                }

                Image image =
                        new Image(stream);

                objects.add(
                        new WorldObject(
                                placed.x,
                                placed.y,
                                definition.getWidth(),
                                definition.getHeight(),
                                image
                        )
                );

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        System.out.println(
                "WORLD SIZE = "
                        + (int) worldWidth
                        + "x"
                        + (int) worldHeight
        );

        if (trainingDummy != null) {

            trainingDummy.setPosition(
                    (float) (
                            worldWidth / 2.0 -
                                    TRAINING_DUMMY_SIZE / 2.0
                    ),
                    (float) (
                            worldHeight / 2.0 +
                                    40.0
                    )
            );
        }

        System.out.println(
                "WORLD OBJECTS LOADED = "
                        + objects.size()
        );
    }

    /*
     * =====================================================
     * NETWORK WORLD STATE
     * =====================================================
     */

    public void updateWorld(
            WorldStatePacket packet
    ) {

        if (packet == null) {
            return;
        }

        for (PlayerState state : packet.players) {

            if (state == null
                    || state.username == null) {
                continue;
            }

            /*
             * =================================================
             * LOCAL PLAYER
             * =================================================
             */

            if (state.username.equals(username)) {

                if (localPlayer == null) {

                    localPlayer =
                            new RemotePlayer(
                                    username,
                                    state.x,
                                    state.y
                            );

                    players.put(
                            username,
                            localPlayer
                    );
                }

                localPlayer.serverX =
                        state.x;

                localPlayer.serverY =
                        state.y;

                localPlayer.renderX =
                        state.x;

                localPlayer.renderY =
                        state.y;

                continue;
            }

            /*
             * =================================================
             * REMOTE PLAYER
             * =================================================
             */

            RemotePlayer player =
                    players.get(
                            state.username
                    );

            if (player == null) {

                player =
                        new RemotePlayer(
                                state.username,
                                state.x,
                                state.y
                        );

                players.put(
                        state.username,
                        player
                );
            }

            player.serverX =
                    state.x;

            player.serverY =
                    state.y;
        }
    }

    /*
     * =====================================================
     * PLAYER SPRITESHEET
     * =====================================================
     */

    private Image loadPlayerSpriteSheet() {

        InputStream stream =
                WorldScene.class.getResourceAsStream(
                        PLAYER_SPRITE_PATH
                );

        if (stream == null) {

            throw new IllegalStateException(
                    "Не найден spritesheet игрока: "
                            + PLAYER_SPRITE_PATH
            );
        }

        return new Image(stream);
    }

    private Image loadRequiredImage(String resourcePath) {

        InputStream stream =
                WorldScene.class.getResourceAsStream(
                        resourcePath
                );

        if (stream == null) {

            throw new IllegalStateException(
                    "Не найден ресурс: " +
                            resourcePath
            );
        }

        return new Image(stream);
    }

    /*
     * =====================================================
     * DIRECTION
     * =====================================================
     */

    private Direction getDirection(
            double dx,
            double dy
    ) {

        if (dx < 0 && dy < 0) {
            return Direction.UP_LEFT;
        }

        if (dx > 0 && dy < 0) {
            return Direction.UP_RIGHT;
        }

        if (dx < 0 && dy > 0) {
            return Direction.DOWN_LEFT;
        }

        if (dx > 0 && dy > 0) {
            return Direction.DOWN_RIGHT;
        }

        if (dx < 0) {
            return Direction.LEFT;
        }

        if (dx > 0) {
            return Direction.RIGHT;
        }

        if (dy < 0) {
            return Direction.UP;
        }

        return Direction.DOWN;
    }

    private Color getDamageColor(DamageType damageType) {
        if (damageType == null) {
            return Color.web("#E8E8E8");
        }

        return switch (damageType) {
            case PHYSICAL -> Color.web("#E8E8E8");
            case FIRE     -> Color.web("#FF5733");
            case WIND     -> Color.web("#64E8B7");
            case DUST     -> Color.web("#C9A477");
            case ICE      -> Color.web("#65CFFF");
            case ELECTRIC -> Color.web("#F5E642");
        };
    }


    private void performDawnAttack(float directionX, float directionY) {
        Weapon weapon = getCurrentWeapon();

        if (!isDawn(weapon) || localPlayer == null) {
            return;
        }

        int hitIndex = dawnComboIndex;
        float multiplier = DAWN_COMBO_MULTIPLIERS[hitIndex];

        float centerX = localPlayer.renderX + (float) PLAYER_SIZE / 2f;
        float centerY = localPlayer.renderY + (float) PLAYER_SIZE / 2f;

        DamageType damageType = hitIndex == 4
                ? DamageType.FIRE
                : DamageType.PHYSICAL;

        boolean attacked = combatSystem.attack(
                weapon,
                PLAYER_ATTACK,
                centerX,
                centerY,
                directionX,
                directionY,
                multiplier,
                damageType
        );

        if (!attacked) {
            return;
        }

        dawnComboIndex = (dawnComboIndex + 1) % DAWN_COMBO_MULTIPLIERS.length;

        if (dawnComboIndex == 4) {
            attackCooldown = DAWN_FINISHER_DELAY;
        } else if (dawnComboIndex == 0) {
            attackCooldown = getAttackInterval();
        } else {
            attackCooldown = DAWN_COMBO_INTERVAL;
        }

        System.out.println(
                "DAWN COMBO: hit " + (hitIndex + 1)
                        + ", multiplier x" + multiplier
        );
    }

    private void performDawnFinisher() {
        Weapon weapon = getCurrentWeapon();

        if (!isDawn(weapon) || localPlayer == null) {
            return;
        }

        float[] direction = getAimDirection(mouseX, mouseY);

        float length = (float) Math.sqrt(
                direction[0] * direction[0]
                        + direction[1] * direction[1]
        );

        if (length < 0.0001f) {
            return;
        }

        direction[0] /= length;
        direction[1] /= length;

        float centerX = localPlayer.renderX + (float) PLAYER_SIZE / 2f;
        float centerY = localPlayer.renderY + (float) PLAYER_SIZE / 2f;

        boolean attacked = combatSystem.attack(
                weapon,
                PLAYER_ATTACK,
                centerX,
                centerY,
                direction[0],
                direction[1],
                2.0f,
                DamageType.FIRE
        );

        if (attacked) {
            dawnComboIndex = 0;
            System.out.println("DAWN TRICK: finisher");
        }
    }


    private void sendAttack(
            double cursorX,
            double cursorY,
            AngelicDoomMode mode
    ) {

        /*
         * Multiplayer combat пока заморожен.
         */
        if (client.isConnected()) {

            System.out.println(
                    "COMBAT: multiplayer combat is frozen"
            );

            return;
        }

        if (localPlayer == null) {
            return;
        }

        Weapon weapon = getCurrentWeapon();
        if (isGungnirLauncher(weapon)) {

            Weapon source = getGungnirLauncherSource();

            if (source == null) {
                System.out.println(
                        "GUNGNIR LAUNCHER: no spear equipped"
                );
                return;
            }

            System.out.println(
                    "GUNGNIR LAUNCHER SOURCE: "
                            + source.getName()
            );
        }

        if (weapon == null) {

            System.out.println(
                    "COMBAT: no active weapon"
            );

            return;
        }

        /*
         * Lunar доступен только для
         * Ангельской погибели.
         */
        if (mode == AngelicDoomMode.LUNAR
                && !isAngelicDoom(weapon)) {

            return;
        }



        if (attackCooldown > 0.0) {
            return;
        }

        float centerX =
                localPlayer.renderX
                        + (float) PLAYER_SIZE / 2f;

        float centerY =
                localPlayer.renderY
                        + (float) PLAYER_SIZE / 2f;

        float[] direction =
                getAimDirection(
                        cursorX,
                        cursorY
                );

        /*
         * Для дальнобойного оружия переносим начало снаряда
         * из центра персонажа к кончику отображаемого спрайта.
         *
         * Спрайт повёрнут на +45°, поэтому предполагаем,
         * что его кончик изначально находится вверху справа.
         */
        if (weapon.getType() == WeaponType.RANGED) {
            double weaponSize = 96.0;
            double weaponCenterOffset = 12.0;

            double muzzleOffset =
                    weaponCenterOffset + weaponSize / 2.0;

            centerX += (float) (direction[0] * muzzleOffset);
            centerY += (float) (direction[1] * muzzleOffset);
        }


        /*
         * =====================================================
         * RANGED
         * =====================================================
         */
        if (weapon.getType()
                == WeaponType.RANGED) {

            if (isIceShooter(weapon) && iceAmmo < 6) {
                return;
            }

            if (isMourning(weapon)) {
                mourningCombatActive = true;
                boolean fired = rangedProjectileSystem.fireMourning(
                        centerX, centerY, direction[0], direction[1],
                        weapon, PLAYER_ATTACK, false, getMourningAttack()
                );
                if (!fired) return;
                attackCooldown = getAttackInterval();
                return;
            }

            if (isIceShooter(weapon)) {
                boolean fired = rangedProjectileSystem.fire(
                        centerX, centerY, direction[0], direction[1],
                        weapon, PLAYER_ATTACK
                );
                if (!fired) return;
                iceAmmo -= 6;
                iceReloadTimer = 0.0;
                attackCooldown = getAttackInterval();
                return;
            }

            if (isDoomStarShooter(weapon)) {
                AttackDefinition attack = doomStarCharged
                        ? (Math.random() < 0.5 ? weapon.getChargedFireAttack() : weapon.getChargedElectricAttack())
                        : weapon.getPrimaryAttack();
                boolean fired = rangedProjectileSystem.fire(
                        centerX, centerY, direction[0], direction[1],
                        weapon, attack, PLAYER_ATTACK
                );
                if (!fired) return;
                if (doomStarCharged) {
                    doomStarCharged = false;
                    doomStarChargeTimer = 0.0;
                }
                attackCooldown = getAttackInterval();
                return;
            }

            // Выстрел из пускового устройства «Гунгнир»
            if (isGungnirLauncher(weapon)) {
                Weapon source = getGungnirLauncherSource();

                // Без экипированного копья выстрел невозможен.
                if (source == null) {
                    return;
                }

                boolean fired = rangedProjectileSystem.fireSpear(
                        centerX,
                        centerY,
                        direction[0],
                        direction[1],
                        source,
                        PLAYER_ATTACK,
                        isGungnir(source)
                );

                if (!fired) {
                    return;
                }

                attackCooldown = getAttackInterval();
                return;
            }

// Обычное дальнобойное оружие — прежняя логика.
            Weapon projectileWeapon = weapon;
            boolean fired;

            if (isAngelicDoom(weapon)) {
                fired = rangedProjectileSystem.fire(
                        centerX,
                        centerY,
                        direction[0],
                        direction[1],
                        projectileWeapon,
                        PLAYER_ATTACK,
                        mode
                );
            } else {
                fired = rangedProjectileSystem.fire(
                        centerX,
                        centerY,
                        direction[0],
                        direction[1],
                        projectileWeapon,
                        PLAYER_ATTACK
                );
            }

            if (!fired) {
                return;
            }

            attackCooldown = getAttackInterval();

            System.out.println(
                    "RANGED ATTACK: "
                            + weapon.getName()
                            + " / "
                            + mode
            );

            return;
        }

        /*
         * =====================================================
         * MELEE
         * =====================================================
         */
        if (weapon.getType()
                == WeaponType.MELEE) {

            boolean attacked =
                    combatSystem.attack(
                            weapon,
                            PLAYER_ATTACK,
                            centerX,
                            centerY,
                            direction[0],
                            direction[1]
                    );

            if (!attacked) {
                return;
            }

            attackCooldown =
                    getAttackInterval();

            System.out.println(
                    "MELEE ATTACK: "
                            + weapon.getName()
            );
        }
    }

    private void sendAttack(
            double cursorX,
            double cursorY
    ) {

        /*
         * Multiplayer combat пока заморожен.
         */
        if (client.isConnected()) {

            System.out.println(
                    "COMBAT: multiplayer combat is frozen"
            );

            return;
        }

        if (localPlayer == null) {
            return;
        }

        Weapon weapon =
                getCurrentWeapon();

        if (weapon == null) {

            System.out.println(
                    "COMBAT: no active weapon"
            );

            return;
        }

        if (attackCooldown > 0.0) {
            return;
        }

        float centerX =
                localPlayer.renderX
                        + (float) PLAYER_SIZE / 2f;

        float centerY =
                localPlayer.renderY
                        + (float) PLAYER_SIZE / 2f;

        float[] direction =
                getAimDirection(
                        cursorX,
                        cursorY
                );

        /*
         * Для дальнобойного оружия переносим начало снаряда
         * из центра персонажа к кончику отображаемого спрайта.
         *
         * Спрайт повёрнут на +45°, поэтому предполагаем,
         * что его кончик изначально находится вверху справа.
         */
        if (weapon.getType() == WeaponType.RANGED) {
            double weaponSize = 96.0;
            double weaponCenterOffset = 12.0;

            double muzzleOffset =
                    weaponCenterOffset + weaponSize / 2.0;

            centerX += (float) (direction[0] * muzzleOffset);
            centerY += (float) (direction[1] * muzzleOffset);
        }


        /*
         * =====================================================
         * RANGED
         * =====================================================
         */

        if (weapon.getType()
                == WeaponType.RANGED) {

            if (isIceShooter(weapon) && iceAmmo < 6) {
                return;
            }

            if (isMourning(weapon)) {
                mourningCombatActive = true;
                boolean fired = rangedProjectileSystem.fireMourning(
                        centerX, centerY, direction[0], direction[1],
                        weapon, PLAYER_ATTACK, false, getMourningAttack()
                );
                if (!fired) return;
                attackCooldown = getAttackInterval();
                return;
            }

            if (isIceShooter(weapon)) {
                boolean fired = rangedProjectileSystem.fire(
                        centerX, centerY, direction[0], direction[1],
                        weapon, PLAYER_ATTACK
                );
                if (!fired) return;
                iceAmmo -= 6;
                iceReloadTimer = 0.0;
                attackCooldown = getAttackInterval();
                return;
            }

            if (isDoomStarShooter(weapon)) {
                AttackDefinition attack = doomStarCharged
                        ? (Math.random() < 0.5 ? weapon.getChargedFireAttack() : weapon.getChargedElectricAttack())
                        : weapon.getPrimaryAttack();
                boolean fired = rangedProjectileSystem.fire(
                        centerX, centerY, direction[0], direction[1],
                        weapon, attack, PLAYER_ATTACK
                );
                if (!fired) return;
                if (doomStarCharged) {
                    doomStarCharged = false;
                    doomStarChargeTimer = 0.0;
                }
                attackCooldown = getAttackInterval();
                return;
            }


            if (isGungnirLauncher(weapon)) {
                Weapon source = getGungnirLauncherSource();

                if (source == null) {
                    System.out.println(
                            "GUNGNIR LAUNCHER: no spear equipped"
                    );
                    return;
                }

                System.out.println(
                        "GUNGNIR LAUNCHER SOURCE: "
                                + source.getName()
                                + ", id=" + source.getId()
                                + ", weaponAttack=" + source.getAttack()
                );

                boolean fired = rangedProjectileSystem.fireSpear(
                        centerX,
                        centerY,
                        direction[0],
                        direction[1],
                        source,
                        PLAYER_ATTACK,
                        isGungnir(source)
                );

                if (!fired) {
                    System.out.println(
                            "GUNGNIR LAUNCHER: fireSpear returned false"
                    );
                    return;
                }

                attackCooldown = getAttackInterval();
                return;
            }

            boolean fired = rangedProjectileSystem.fire(
                    centerX,
                    centerY,
                    direction[0],
                    direction[1],
                    weapon,
                    PLAYER_ATTACK
            );

            if (!fired) {
                return;
            }

            attackCooldown = getAttackInterval();

            System.out.println(
                    "RANGED ATTACK: " + weapon.getName()
            );
            return;

        }

        /*
         * =====================================================
         * MELEE
         * =====================================================
         */

        if (weapon.getType()
                == WeaponType.MELEE) {

            if (isDawn(weapon)) {
                performDawnAttack(direction[0], direction[1]);
                return;
            }

            boolean attacked =
                    combatSystem.attack(
                            weapon,
                            PLAYER_ATTACK,
                            centerX,
                            centerY,
                            direction[0],
                            direction[1]
                    );

            if (!attacked) {
                return;
            }

            attackCooldown =
                    getAttackInterval();

            System.out.println(
                    "MELEE ATTACK: "
                            + weapon.getName()
            );
        }
    }


    /**
     * Возвращает нормализованное направление
     * от центра игрока к курсору.
     *
     * Координаты мыши находятся в Canvas,
     * а игрок находится в мировых координатах.
     *
     * Поскольку камера всегда центрирована на игроке,
     * преобразование:
     *
     * screen -> world
     *
     * выполняется относительно центра Canvas.
     */
    private float[] getAimDirection(
            double cursorX,
            double cursorY
    ) {
        double screenWidth =
                canvas.getWidth();

        double screenHeight =
                canvas.getHeight();

        double dx =
                (cursorX - screenWidth / 2.0)
                        / CAMERA_ZOOM;

        double dy =
                (cursorY - screenHeight / 2.0)
                        / CAMERA_ZOOM;

        double length =
                Math.sqrt(
                        dx * dx +
                                dy * dy
                );

        if (length < 0.0001) {
            return new float[]{0f, 1f};
        }

        return new float[]{
                (float) (dx / length),
                (float) (dy / length)
        };
    }

    private void renderRangedProjectiles() {

        for (RangedProjectile projectile :
                rangedProjectileSystem.getProjectiles()) {

            if (projectile == null ||
                    projectile.isFinished()) {
                continue;
            }

            float x = projectile.getX();
            float y = projectile.getY();

            float dx = projectile.getDirectionX();
            float dy = projectile.getDirectionY();


            if (rangedProjectileSystem.isSpearProjectile(projectile)) {
                if (gungnirProjectileImage == null) {
                    gungnirProjectileImage = loadRequiredImage(
                            "/assets/weapons/spears/gungnir.png"
                    );
                }

                // Исходный спрайт копья направлен примерно на 45° вверх-вправо.
                // Компенсируем этот наклон, чтобы кончик совпадал с направлением полёта.
                double angle = Math.toDegrees(Math.atan2(dy, dx)) + 45.0;

                // Используем размер оружия, сопоставимый с размером в руках.
                double spearSize = 96.0;

                gc.save();
                gc.translate(x, y);
                gc.rotate(angle);

                gc.drawImage(
                        gungnirProjectileImage,
                        -spearSize / 2.0,
                        -spearSize / 2.0,
                        spearSize,
                        spearSize
                );

                gc.restore();
                continue;
            }


            float trail = 18f;



            gc.save();

            gc.setGlobalAlpha(0.30);
            gc.setStroke(
                    Color.rgb(255, 220, 100)
            );
            gc.setLineWidth(9);

            gc.strokeLine(
                    x,
                    y,
                    x - dx * trail,
                    y - dy * trail
            );

            gc.setGlobalAlpha(1.0);
            gc.setStroke(
                    Color.rgb(255, 245, 190)
            );
            gc.setLineWidth(4);

            gc.strokeLine(
                    x,
                    y,
                    x - dx * trail,
                    y - dy * trail
            );

            gc.setFill(
                    Color.rgb(255, 245, 190)
            );

            gc.fillOval(
                    x - 4,
                    y - 4,
                    8,
                    8
            );

            gc.restore();
        }

        for (SolarExplosion explosion :
                rangedProjectileSystem.getSolarExplosions()) {

            if (explosion == null) {
                continue;
            }

            float x = explosion.getX();
            float y = explosion.getY();

            float radius = explosion.getCurrentRadius();
            float alpha = explosion.getAlpha();

            gc.save();

            gc.setGlobalAlpha(alpha * 0.35);

            gc.setFill(
                    Color.rgb(255, 190, 60)
            );

            gc.fillOval(
                    x - radius,
                    y - radius,
                    radius * 2f,
                    radius * 2f
            );

            gc.setGlobalAlpha(alpha);

            gc.setStroke(
                    Color.rgb(255, 235, 150)
            );

            gc.setLineWidth(4);

            gc.strokeOval(
                    x - radius,
                    y - radius,
                    radius * 2f,
                    radius * 2f
            );

            gc.restore();
        }
    }

    private void renderSpecialWeaponHud() {
        Weapon weapon = getCurrentWeapon();
        if (localPlayer == null || weapon == null) return;

        float x = localPlayer.renderX + (float) PLAYER_SIZE / 2f;
        float y = localPlayer.renderY - 34f;

        if (isDoomStarShooter(weapon) && (doomStarCharging || doomStarCharged)) {
            gc.save();
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.setFont(Font.font("System", FontWeight.BOLD, 15));
            if (doomStarCharged) {
                gc.setFill(Color.GOLD);
                gc.fillText("★ ЗАРЯЖЕНА", x, y);
            } else {
                gc.setFill(Color.WHITE);
                gc.fillText(String.format(Locale.ROOT, "★ %.1f", Math.max(0, DOOM_STAR_CHARGE_TIME - doomStarChargeTimer)), x, y);
            }
            gc.restore();
        }

        if (isMourning(weapon) && mourningCombatActive) {
            gc.save();
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.setFont(Font.font("System", FontWeight.BOLD, 13));
            gc.setFill(Color.LIGHTGRAY);
            gc.fillText("+" + (mourningStacks * 4), x, y);
            gc.restore();
        }

        if (isIceShooter(weapon)) {
            gc.save();
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.setFont(Font.font("System", FontWeight.BOLD, 13));
            gc.setFill(Color.LIGHTBLUE);
            if (iceAmmo < ICE_MAX_AMMO) {
                gc.fillText(String.format(Locale.ROOT, "❄ %d/30  %.1fs", iceAmmo, Math.max(0, ICE_RELOAD_TIME - iceReloadTimer)), x, y);
            } else {
                gc.fillText("❄ 30/30", x, y);
            }
            gc.restore();
        }

        for (var explosion : rangedProjectileSystem.getStarExplosions()) {
            float ex = explosion.getX();
            float ey = explosion.getY();
            float r = explosion.getCurrentRadius();
            gc.save();
            gc.setGlobalAlpha(explosion.getAlpha() * 0.35);
            gc.setFill(Color.GOLD);
            gc.fillOval(ex - r, ey - r, r * 2f, r * 2f);
            gc.restore();
        }

        for (RangedProjectileSystem.LightningArc arc : rangedProjectileSystem.getMourningLightningArcs()) {
            gc.save();
            gc.setGlobalAlpha(arc.getAlpha());
            gc.setStroke(Color.LIGHTYELLOW);
            gc.setLineWidth(3);
            gc.strokeLine(arc.getX1(), arc.getY1(), arc.getX2(), arc.getY2());
            gc.restore();
        }
    }

    private void renderSwordSwings() {

        for (SwordSwing swing :
                combatSystem.getActiveSwings()) {

            // Если для экипированного оружия есть спрайт,
            // не рисуем его повторно старым способом через линии.
            Weapon equippedWeapon = inventory.getEquippedMeleeWeapon();

            if (equippedWeapon != null
                    && weaponSpriteManager.getSprite(equippedWeapon.getId()) != null) {
                continue;
            }

            // Копья отображаются собственными спрайтами.
            // Не рисуем для них старый процедурный клинок.
            if (swing.getStyle() == AttackStyle.THRUST
                    || swing.getStyle() == AttackStyle.STAB) {
                continue;
            }

            gc.save();

            float startX =
                    swing.getStartX();

            float startY =
                    swing.getStartY();

            float endX =
                    swing.getEndX();

            float endY =
                    swing.getEndY();

            /*
             * Тень / свечение меча.
             */
            gc.setGlobalAlpha(0.25);

            gc.setStroke(
                    Color.rgb(
                            180,
                            190,
                            220
                    )
            );

            gc.setLineWidth(
                    SwordSwing.SWORD_THICKNESS + 8
            );

            gc.strokeLine(
                    startX,
                    startY,
                    endX,
                    endY
            );

            /*
             * Сам меч.
             */
            gc.setGlobalAlpha(1.0);

            gc.setStroke(
                    Color.rgb(
                            225,
                            230,
                            240
                    )
            );

            gc.setLineWidth(
                    6
            );

            gc.strokeLine(
                    startX,
                    startY,
                    endX,
                    endY
            );

            /*
             * Тёмная гарда.
             */
            double angle =
                    swing.getWorldAngleRadians();

            double perpendicularX =
                    -Math.sin(angle);

            double perpendicularY =
                    Math.cos(angle);

            double guardLength = 18.0;

            gc.setStroke(
                    Color.rgb(
                            90,
                            75,
                            55
                    )
            );

            gc.setLineWidth(5);

            gc.strokeLine(
                    swing.getOriginX()
                            - perpendicularX * guardLength,
                    swing.getOriginY()
                            - perpendicularY * guardLength,

                    swing.getOriginX()
                            + perpendicularX * guardLength,
                    swing.getOriginY()
                            + perpendicularY * guardLength
            );

            gc.restore();
        }
    }

    private void showDamagePopup(
            float x,
            float y,
            float damage,
            DamageType damageType
    ) {
        damagePopups.add(
                new DamagePopup(
                        x,
                        y,
                        damage,
                        damageType
                )
        );
    }

    private void setupLocations() {

        /*
         * =====================================================
         * ОСНОВНОЙ МИР
         * =====================================================
         */

        Location mainWorld =
                new Location(
                        "main_world",
                        "Основной мир",
                        10000,
                        10000,
                        "#2f6b3f"
                );

        /*
         * Портал в подземелье.
         *
         * Пока ставим его немного правее
         * центра мира.
         */
        Portal dungeonPortal =
                new Portal(
                        "main_to_dungeon",
                        "dungeon",
                        5200,
                        5000,
                        80,
                        80,

                        /*
                         * Координаты появления
                         * в подземелье.
                         */
                        760,
                        1000,

                        "[F] — войти"
                );

        mainWorld.addPortal(
                dungeonPortal
        );


        /*
         * =====================================================
         * ПОДЗЕМЕЛЬЕ
         * =====================================================
         */

        Location dungeon =
                new Location(
                        "dungeon",
                        "Подземелье",
                        1600,
                        1200,
                        "#25252b"
                );

        DungeonBossRegistry.register(
                bossManager,
                combatSystem,
                this::damageLocalPlayer,
                difficulty
        );

        /*
         * Обратный портал.
         *
         * Игрок появится в основном мире
         * немного левее входного портала.
         */
        Portal exitPortal =
                new Portal(
                        "dungeon_to_main",
                        "main_world",
                        720,
                        100,
                        80,
                        80,

                        5000,
                        5000,

                        "[F] — выйти"
                );

        dungeon.addPortal(
                exitPortal
        );


        /*
         * Пока храним локации прямо в WorldScene.
         */
        locations.clear();

        locations.put(
                mainWorld.getId(),
                mainWorld
        );

        locations.put(
                dungeon.getId(),
                dungeon
        );

        currentLocation =
                mainWorld;
    }
    private void updatePortalInteraction() {

        nearbyPortal = null;


        if (localPlayer == null) {
            return;
        }

        if (currentLocation == null) {
            return;
        }

        double playerCenterX =
                localPlayer.renderX +
                        PLAYER_SIZE / 2.0;

        double playerCenterY =
                localPlayer.renderY +
                        PLAYER_SIZE / 2.0;

        double nearestDistance =
                Double.MAX_VALUE;

        for (Portal portal :
                currentLocation.getPortals()) {

            double dx =
                    playerCenterX -
                            portal.getCenterX();

            double dy =
                    playerCenterY -
                            portal.getCenterY();

            double distance =
                    Math.sqrt(
                            dx * dx +
                                    dy * dy
                    );

            if (
                    distance <= PORTAL_INTERACTION_DISTANCE
                            &&
                            distance < nearestDistance
            ) {
                nearestDistance = distance;

                nearbyPortal = portal;
            }
        }
    }
    private void interactWithNearbyPortal() {

        if (nearbyPortal == null) {
            return;
        }

        String targetId =
                nearbyPortal.getTargetLocationId();

        Location targetLocation =
                locations.get(targetId);

        if (targetLocation == null) {

            System.out.println(
                    "LOCATION NOT FOUND: "
                            + targetId
            );

            return;
        }

        /*
         * Если игрок покидает подземелье,
         * полностью сбрасываем Ancient Angel.
         */
        if ("dungeon".equals(currentLocation.getId())
                && "main_world".equals(targetId)) {

            bossManager.resetBoss(DungeonBossRegistry.ANCIENT_ANGEL_ID);
        }

        switchLocation(
                targetLocation,
                nearbyPortal.getTargetSpawnX(),
                nearbyPortal.getTargetSpawnY()
        );
    }
    private void renderPortalInteraction() {

        if (nearbyPortal == null) {
            return;
        }

        double x =
                nearbyPortal.getCenterX();

        double y =
                nearbyPortal.getY() - 22;

        String text =
                nearbyPortal.getInteractionText();

        gc.save();

        gc.setFont(
                Font.font(
                        "Arial",
                        FontWeight.BOLD,
                        16
                )
        );

        double textWidth =
                measureTextWidth(text);

        /*
         * Фон под подсказкой.
         */
        gc.setFill(
                Color.rgb(
                        0,
                        0,
                        0,
                        0.75
                )
        );

        gc.fillRoundRect(
                x - textWidth / 2.0 - 10,
                y - 22,
                textWidth + 20,
                30,
                8,
                8
        );

        /*
         * Сам текст.
         */
        gc.setFill(
                Color.WHITE
        );

        gc.fillText(
                text,
                x - textWidth / 2.0,
                y
        );

        gc.restore();
    }

    private void switchLocation(
            Location location,
            double spawnX,
            double spawnY
    ) {

        if (location == null) {
            return;
        }

        currentLocation =
                location;

        worldWidth =
                location.getWidth();

        worldHeight =
                location.getHeight();

        /*
         * Перемещаем игрока.
         */
        if (localPlayer != null) {

            localPlayer.serverX =
                    (float) spawnX;

            localPlayer.serverY =
                    (float) spawnY;

            localPlayer.renderX =
                    (float) spawnX;

            localPlayer.renderY =
                    (float) spawnY;
        }

        /*
         * Старый портал больше не считается
         * ближайшим после перехода.
         */
        nearbyPortal = null;

        if ("dungeon".equals(location.getId())) {

            MusicManager.playLoop(
                    "/assets/music/music.wav"
            );

        } else {

            MusicManager.stop();
        }



        System.out.println(
                "LOCATION CHANGED: "
                        + location.getName()
                        + " ("
                        + location.getId()
                        + ")"
        );
    }

    public void setSaveSlot(int slot) {
        if (slot <= 0) {
            throw new IllegalArgumentException(
                    "Номер слота должен быть больше 0"
            );
        }
        this.saveSlot = slot;
    }

    public void saveGame() {
        if (localPlayer == null) {
            System.out.println("Сохранение пропущено: игрок не создан.");
            return;
        }

        SaveData data = SaveManager.load(saveSlot);

        if (data == null) {
            data = new SaveData();
            data.slot = saveSlot;
            data.saveName = "Сохранение " + saveSlot;
        }

        data.player.x = (float) localPlayer.serverX;
        data.player.y = (float) localPlayer.serverY;
        data.player.locationX = locationX;
        data.player.locationY = locationY;
        if (oneHpMode) {
            playerMaxHp = 1f;
            playerHp = Math.max(0f, Math.min(1f, playerHp));
        } else {
            applyEquippedArtifactHpBonuses();
        }
        data.player.hp = playerHp;
        data.player.maxHp = playerMaxHp;
        data.difficulty = this.difficulty;
        data.player.oneHpMode = oneHpMode;
        data.player.hardcoreMode = hardcoreMode;

        data.world.currentLocationId =
                currentLocation != null
                        ? currentLocation.getId()
                        : "main_world";

        // Сохраняем актуальный инвентарь оружия и экипировку, включая пустые слоты.
        data.weaponIds.clear();
        for (Weapon weapon : inventory.getWeapons()) {
            if (weapon != null) {
                data.weaponIds.add(weapon.getId());
            }
        }

        Weapon equippedMelee = inventory.getEquippedMeleeWeapon();
        Weapon equippedRanged = inventory.getEquippedRangedWeapon();
        data.equippedMelee = equippedMelee == null ? null : equippedMelee.getId();
        data.equippedRanged = equippedRanged == null ? null : equippedRanged.getId();
        data.meleeActive = meleeActive;

        // Сохраняем все артефакты в инвентаре и ID в каждом слоте отдельно.
        data.artifactIds.clear();
        for (Artifact artifact : inventory.getArtifacts()) {
            if (artifact != null) {
                data.artifactIds.add(artifact.getId());
            }
        }
        data.equippedArtifactIds.clear();
        for (int artifactSlot = 0;
             artifactSlot < inventory.getArtifactSlotCount();
             artifactSlot++) {
            Artifact artifact = inventory.getEquippedArtifact(artifactSlot);
            data.equippedArtifactIds.add(artifact == null ? null : artifact.getId());
        }

        try {
            SaveManager.save(data);
            System.out.println(
                    "Игра сохранена в слот " + saveSlot
                            + ": x=" + data.player.x
                            + ", y=" + data.player.y
                            + ", location=" + locationX + "," + locationY
            );
        } catch (RuntimeException e) {
            System.err.println("Не удалось сохранить игру:");
            e.printStackTrace();
        }
    }


    private static class DamagePopup {

        private final float x;
        private final float y;
        private final float damage;
        private final DamageType damageType;

        private double age;

        private DamagePopup(
                float x,
                float y,
                float damage,
                DamageType damageType
        ) {
            this.x = x;
            this.y = y;
            this.damage = damage;
            this.damageType = damageType != null
                    ? damageType
                    : DamageType.PHYSICAL;
            this.age = 0;
        }
    }

    private static class AttackHitbox {

        private final AttackStyle style;

        private final float originX;
        private final float originY;

        private final float directionX;
        private final float directionY;

        private double age;

        private static final double DURATION = 0.15;

        private AttackHitbox(
                AttackStyle style,
                float originX,
                float originY,
                float directionX,
                float directionY
        ) {
            this.style = style;

            this.originX = originX;
            this.originY = originY;

            this.directionX = directionX;
            this.directionY = directionY;

            this.age = 0;
        }

        private boolean update(
                double deltaSeconds
        ) {
            age += deltaSeconds;
            return age < DURATION;
        }
    }
}

