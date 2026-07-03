package game.ui.register;

import game.ui.SceneManager;

public class RegisterStepController {

    private final SceneManager sceneManager;
    private final RegistrationSession session;

    public RegisterStepController(SceneManager sceneManager,
                                  RegistrationSession session) {
        this.sceneManager = sceneManager;
        this.session = session;
    }

    // =========================
    // FLOW CONTROL
    // =========================

    public void start() {
        goToRoleSelect();
    }

    public void goToRoleSelect() {
        session.setStep(1);
        sceneManager.showRoleSelect(session);
    }

    public void goToToken() {
        session.setStep(2);
        sceneManager.showToken(session);
    }

    public void goToLoginPassword() {
        session.setStep(3);
        sceneManager.showLoginPassword(session);
    }

    public void goToAgreement() {
        session.setStep(4);
        sceneManager.showAgreement(session);
    }

    public void goToCharacter() {
        session.setStep(5);
        sceneManager.showCharacter(session);
    }

    public void goToStarterItem() {
        session.setStep(6);
        sceneManager.showStarterItem(session);
    }

    public void finish() {
        session.setStep(7);
        sceneManager.finishRegistration(session);
    }
}