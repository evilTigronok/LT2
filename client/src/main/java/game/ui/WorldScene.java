
package game.ui;

import game.audio.MusicManager;
import game.boss.BossManager;
import game.boss.Boss;
import game.boss.DungeonBossRegistry;
import game.combat.*;

import game.player.PlayerSkillLoadout;
import game.weapon.WeaponFactory;
import game.world.location.Location;
import game.world.location.Portal;
import game.avatar.Direction;
import game.client.NetworkClient;
import game.network.dto.PlayerState;
import game.network.packets.WorldStatePacket;
import game.ui.world.RemotePlayer;
import game.ui.world.WorldObject;
import game.weapon.AttackStyle;
import game.weapon.Weapon;
import game.weapon.WeaponType;
import game.weapon.WeaponSpecial;
import game.combat.AttackDefinition;
import game.inventory.Inventory;
import game.item.AbilityDefinition;
import game.item.ItemQuality;
import game.world.data.PlacedObjectData;
import game.world.data.WorldData;
import game.world.objects.ObjectDefinition;
import game.world.objects.ObjectRegistry;
import javafx.animation.AnimationTimer;
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

    private final LocalCombatSystem combatSystem =
            new LocalCombatSystem();

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

    private final RangedProjectileSystem rangedProjectileSystem =
            new RangedProjectileSystem(combatSystem);

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

    private final WeaponTrickSystem weaponTrickSystem =
            new WeaponTrickSystem();

    private double gungnirDrillTimer = 0.0;
    private double gungnirDrillTickTimer = 0.0;
    private double gungnirDrillActiveDuration = 0.0;

    private float gungnirDrillDirectionX = 0f;
    private float gungnirDrillDirectionY = 0f;

    private boolean gungnirDrillActive = false;

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

    private static final double PLAYER_SIZE =
            40;

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

    /*
     * Один spritesheet содержит 8 направлений.
     */
    private final Image playerSpriteSheet =
            loadPlayerSpriteSheet();

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


    /*
     * =====================================================
     * CONSTRUCTOR
     * =====================================================
     */

    public WorldScene(
            SceneManager sceneManager,
            NetworkClient client,
            String username
    ) {

        this.client = client;
        this.username = username;
        this.weaponLoadoutStorage = new WeaponLoadoutStorage(username);

        this.sceneManager = sceneManager;

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

        //temp
        float dummyX =
                (float) (
                        worldWidth / 2.0 -
                                TRAINING_DUMMY_SIZE / 2.0
                );

        float dummyY =
                (float) (
                        worldHeight / 2.0 +
                                40.0
                );

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
         * Начальное оружие.
         */
        Weapon claymore =
                WeaponFactory.createWeightedClaymore();

        Weapon angelicDoom =
                WeaponFactory.createAngelicDoom();

        inventory.addWeapon(claymore);
        inventory.addWeapon(angelicDoom);
        inventory.addWeapon(WeaponFactory.createGungnir());
        inventory.addWeapon(WeaponFactory.createReliableSpear());
        inventory.addWeapon(WeaponFactory.createGungnirLauncher());
        inventory.addWeapon(WeaponFactory.createMourning());
        inventory.addWeapon(WeaponFactory.createIceShooter());
        inventory.addWeapon(WeaponFactory.createDoomStarShooter());

        doomStarCharged = true;

        /*
         * Восстанавливаем ведущие слоты ПОСЛЕ добавления всего оружия.
         * Inventory автоматически экипирует первые claymore/angelicDoom,
         * поэтому загрузка должна выполняться именно здесь и явно заменять
         * эти значения сохранёнными оружиями.
         */
        restoreSavedLoadout();

        meleeActive = true;

        // setupCharacterPanel() вызывается раньше добавления оружия,
        // поэтому UI необходимо обновить после восстановления слотов.
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

        double ratio =
                playerMaxHp <= 0
                        ? 0
                        : playerHp / playerMaxHp;

        ratio = Math.max(0, Math.min(1, ratio));

        playerHpBar.setProgress(ratio);

        playerHpLabel.setText(
                "HP   " +
                        (int) playerHp +
                        " / " +
                        (int) playerMaxHp
        );

        if (characterPanelVisible) {
            refreshCharacterPanel();
        }
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

        Button returnButton =
                createMenuButton("Вернуться");

        Button settingsButton =
                createMenuButton("Настройки");

        Button mainMenuButton =
                createMenuButton(
                        "Выйти в главное меню"
                );

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
            sceneManager.show(
                    SceneType.MAIN_MENU
            );
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

        equippedMeleeValue.setPrefHeight(70);
        equippedRangedValue.setPrefHeight(70);

        equippedMeleeValue.setMaxWidth(Double.MAX_VALUE);
        equippedRangedValue.setMaxWidth(Double.MAX_VALUE);

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

        equipment.getChildren().addAll(
                equipmentTitle,
                equippedMeleeValue,
                equippedRangedValue
        );

        /* =================================================
         * INVENTORY
         * ================================================= */

        VBox inventoryBox = new VBox(10);
        inventoryBox.setPrefWidth(300);
        inventoryBox.setPadding(new Insets(14));
        inventoryBox.setStyle(panelSectionStyle());

        Label inventoryTitle = createSectionTitle("ИНВЕНТАРЬ");

        inventoryGrid.setHgap(7);
        inventoryGrid.setVgap(7);
        inventoryGrid.setPadding(new Insets(4));

        inventoryBox.getChildren().addAll(
                inventoryTitle,
                inventoryGrid
        );

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
                "Атака: " + (int) PLAYER_ATTACK
        );
        characterSpeedValue.setText(
                "Скорость: " + (int) LOCAL_PLAYER_SPEED
        );

        Weapon melee = inventory.getEquippedMeleeWeapon();
        Weapon ranged = inventory.getEquippedRangedWeapon();

        equippedMeleeValue.setText(
                "Ближний бой: " +
                        (melee == null ? "—" : melee.getName())
        );
        equippedRangedValue.setText(
                "Дальний бой: " +
                        (ranged == null ? "—" : ranged.getName())
        );
        equippedMeleeValue.setStyle(
                equipmentSlotStyle(melee)
        );

        equippedRangedValue.setStyle(
                equipmentSlotStyle(ranged)
        );
    }

    private void rebuildInventoryGrid() {
        inventoryGrid.getChildren().clear();

        for (int index = 0; index < 9; index++) {
            Weapon weapon = inventory.getWeapon(index);
            StackPane cell = createInventoryCell(weapon, index);

            inventoryGrid.add(cell, index % 3, index / 3);
        }
    }

    private StackPane createInventoryCell(Weapon weapon, int index) {
        StackPane cell = new StackPane();
        cell.setPrefSize(78, 78);
        cell.setMinSize(78, 78);
        cell.setMaxSize(78, 78);

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
        name.setMaxWidth(68);
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

    private void refreshCharacterPanel() {
        updateCharacterPanel();
        rebuildInventoryGrid();
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


    private void handleLocalCombatHit(
            LocalCombatHit hit
    ) {

        showDamagePopup(
                hit.getX(),
                hit.getY(),
                hit.getDamage()
        );

        /*
         * LocalCombatSystem уже применил урон к цели.
         * Боссы используют BossCombatTarget, который
         * автоматически передаёт урон своему Boss.
         */

        System.out.println(
                "LOCAL DAMAGE: "
                        + hit.getTargetId()
                        + " -> "
                        + hit.getDamage()
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

                    if (attackTimer >= getAttackInterval()) {
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

                combatSystem.update(
                        playerCenterX,
                        playerCenterY,
                        (float) deltaSeconds
                );

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

    private void damageLocalPlayer(
            float damage
    ) {

        if (damage <= 0) {
            return;
        }

        /*
         * Если игрок уже мёртв,
         * дополнительный урон не принимаем.
         */
        if (playerHp <= 0) {
            return;
        }

        playerHp =
                Math.max(
                        0,
                        playerHp - damage
                );

        updatePlayerHud();

        System.out.println(
                "ANCIENT ANGEL HIT PLAYER: -"
                        + damage
        );

        /*
         * Игрок погиб.
         */
        if (playerHp <= 0) {

            System.out.println(
                    "PLAYER DIED"
            );

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
        localPlayer.serverX +=
                dx
                        * LOCAL_PLAYER_SPEED
                        * deltaSeconds;

        localPlayer.serverY +=
                dy
                        * LOCAL_PLAYER_SPEED
                        * deltaSeconds;

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

        renderPlayers();

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
                    Color.rgb(
                            255,
                            235,
                            80,
                            alpha
                    )
            );

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

    private void renderPlayers() {

        for (RemotePlayer player :
                players.values()) {

            /*
             * Direction enum:
             *
             * 0 DOWN
             * 1 DOWN_RIGHT
             * 2 RIGHT
             * 3 UP_RIGHT
             * 4 UP
             * 5 UP_LEFT
             * 6 LEFT
             * 7 DOWN_LEFT
             */

            int directionIndex =
                    player.direction.ordinal();

            double sourceX =
                    directionIndex
                            * PLAYER_SPRITE_WIDTH;

            /*
             * Центрируем 32px sprite
             * внутри игровой области 40px.
             */
            double spriteX =
                    player.renderX
                            + (
                            PLAYER_SIZE
                                    - PLAYER_SPRITE_WIDTH
                    ) / 2.0;

            double spriteY =
                    player.renderY;

            gc.drawImage(
                    playerSpriteSheet,

                    sourceX,
                    0,

                    PLAYER_SPRITE_WIDTH,
                    PLAYER_SPRITE_HEIGHT,

                    spriteX,
                    spriteY,

                    PLAYER_SPRITE_WIDTH,
                    PLAYER_SPRITE_HEIGHT
            );

            /*
             * Имя игрока.
             */
            gc.setFill(
                    Color.WHITE
            );

            gc.fillText(
                    player.username,
                    player.renderX,
                    player.renderY - 8
            );
        }
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

            Weapon projectileWeapon = weapon;

            if (isGungnirLauncher(weapon)) {

                Weapon source = getGungnirLauncherSource();

                if (source == null) {
                    return;
                }

                projectileWeapon = source;
            }

            boolean fired;

            if (isAngelicDoom(weapon)) {

                fired =
                        rangedProjectileSystem.fire(
                                centerX,
                                centerY,
                                direction[0],
                                direction[1],
                                projectileWeapon,
                                PLAYER_ATTACK,
                                mode
                        );

            } else {

                fired =
                        rangedProjectileSystem.fire(
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

            attackCooldown =
                    getAttackInterval();

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

            boolean fired = rangedProjectileSystem.fire(
                    centerX, centerY, direction[0], direction[1], weapon, PLAYER_ATTACK
            );
            if (!fired) return;
            attackCooldown = getAttackInterval();
            System.out.println("RANGED ATTACK: " + weapon.getName());
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
            float damage
    ) {
        damagePopups.add(
                new DamagePopup(
                        x,
                        y,
                        damage
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
                this::damageLocalPlayer
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

    private static class DamagePopup {

        private final float x;
        private final float y;
        private final float damage;

        private double age;

        private DamagePopup(
                float x,
                float y,
                float damage
        ) {
            this.x = x;
            this.y = y;
            this.damage = damage;
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

