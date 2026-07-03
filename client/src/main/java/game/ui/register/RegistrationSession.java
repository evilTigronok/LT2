package game.ui.register;

import game.auth.AccountType;

public class RegistrationSession {

    private AccountType accountType;

    private String token;

    private String login;
    private String password;

    private String fullName;
    private String eyeColor;

    private String starterItem;

    private boolean acceptedRules;

    private int step;

    private boolean agreementAccepted;

    private boolean spectator;

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }


    // =====================
    // GETTERS / SETTERS
    // =====================

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAgreementAccepted(boolean value) {
        this.agreementAccepted = value;
    }

    public boolean isAgreementAccepted() {
        return agreementAccepted;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
    }

    public String getStarterItem() {
        return starterItem;
    }

    public void setStarterItem(String starterItem) {
        this.starterItem = starterItem;
    }

    public boolean isAcceptedRules() {
        return acceptedRules;
    }

    public void setAcceptedRules(boolean acceptedRules) {
        this.acceptedRules = acceptedRules;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
}