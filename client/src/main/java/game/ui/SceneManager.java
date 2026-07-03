package game.ui;

import game.client.NetworkClient;
import game.ui.register.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private final Stage stage;

    private final NetworkClient client;

    private String username;

    private RegistrationSession pendingSession;


    public SceneManager(Stage stage,
                        NetworkClient client) {

        this.stage = stage;
        this.client = client;
        client.setTokenValidHandler(() -> {
            onTokenValid();
        });
        client.setLoginSuccessHandler(() -> {
            show(SceneType.WORLD);
        });
        client.setTokenInvalidHandler(() -> {
            onTokenInvalid();
        });
    }

    public void setPendingSession(RegistrationSession session) {
        this.pendingSession = session;
    }
    public void onTokenValid() {
        showLoginPassword(pendingSession);
    }

    public void onTokenInvalid() {
        System.out.println("Invalid token");
    }

    // =====================================
    // BASIC SCENES
    // =====================================

    public void show(SceneType type) {

        Parent root;

        switch (type) {

            case LOGIN:

                root = new LoginScene(
                        this
                ).getRoot();

                break;

            case WORLD:

                WorldScene worldScene =
                        new WorldScene(
                                this,
                                client,
                                username
                        );

                client.setWorldScene(worldScene);

                root = worldScene.getRoot();

                break;

            case CHARACTER:

                root = new CharacterScene(
                        this
                ).getRoot();

                break;

            case COMBAT:

                root = new CombatScene(
                        this
                ).getRoot();

                break;

            default:
                throw new IllegalStateException(
                        "Unknown scene"
                );
        }

        stage.setScene(
                new Scene(
                        root,
                        1920,
                        1080
                )
        );

        stage.setMaximized(true);
    }

    // =====================================
    // REGISTRATION FLOW
    // =====================================

    public void startRegistration() {

        RegistrationSession session =
                new RegistrationSession();

        showRoleSelect(session);
    }

    public void showRoleSelect(
            RegistrationSession session
    ) {

        RoleSelectScene scene =
                new RoleSelectScene(
                        this,
                        session
                );

        stage.setScene(
                new Scene(
                        scene.getRoot(),
                        900,
                        700
                )
        );
    }

    public void showToken(
            RegistrationSession session
    ) {

        TokenScene scene =
                new TokenScene(
                        this,
                        session
                );

        stage.setScene(
                new Scene(
                        scene.getRoot(),
                        900,
                        700
                )
        );
    }

    public void showLoginPassword(
            RegistrationSession session
    ) {

        LoginPasswordStep scene =
                new LoginPasswordStep(
                        this,
                        session
                );

        stage.setScene(
                new Scene(
                        scene.getRoot(),
                        900,
                        700
                )
        );
    }

    public void showAgreement(RegistrationSession session) {

        AgreementStep scene =
                new AgreementStep(
                        this,
                        session,
                        () -> showCharacter(session)
                );

        stage.setScene(new Scene(scene, 900, 700));
    }

    public void showCharacter(
            RegistrationSession session
    ) {

        CharacterStep scene =
                new CharacterStep(
                        this,
                        session
                );

        stage.setScene(
                new Scene(
                        scene.getRoot(),
                        900,
                        700
                )
        );
    }

    public void showStarterItem(
            RegistrationSession session
    ) {

        StarterItemStep scene =
                new StarterItemStep(
                        this,
                        session
                );

        stage.setScene(
                new Scene(
                        scene.getRoot(),
                        900,
                        700
                )
        );
    }

    public void finishRegistration(
            RegistrationSession session
    ) {

        sendRegister(session);

        show(SceneType.LOGIN);
    }

    // =====================================
    // NETWORK
    // =====================================

    public void sendRegister(RegistrationSession session) {

        client.send(
                "REGISTER:" +
                        session.getLogin() + ":" +
                        session.getPassword() + ":" +
                        session.getFullName() + ":" +
                        session.getEyeColor() + ":" +
                        session.getStarterItem()
        );
    }

    // =====================================
    // USER
    // =====================================

    public void setUsername(
            String username
    ) {

        this.username = username;
    }

    public NetworkClient getClient() {
        return client;
    }
}