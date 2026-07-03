package game.characters;

import game.inventory.Inventory;

public class Player extends Character {

    private Inventory inventory = new Inventory();

    public Player(String name) {
        super(name);

        this.hp = 100;
        this.maxHp = 100;
        this.attack = 10;
        this.defense = 5;
        this.speed = 5;
    }

    @Override
    public boolean isAlive() {
        return hp > 0;
    }

    public Inventory getInventory() {
        return inventory;
    }
}