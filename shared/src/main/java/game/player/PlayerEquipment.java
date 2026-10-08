package game.player;

import game.weapon.Weapon;

public class PlayerEquipment {

    private Weapon weapon;

    public Weapon getWeapon() {
        return weapon;
    }

    public void equipWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public void unequipWeapon() {
        this.weapon = null;
    }

    public boolean hasWeapon() {
        return weapon != null;
    }
}