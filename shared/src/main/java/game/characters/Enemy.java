package game.characters;

public class Enemy extends Character {

    public Enemy(String name) {
        super(name);

        this.hp = 80;
        this.attack = 8;
        this.defense = 3;
        this.speed = 4;
    }

    @Override
    public boolean isAlive() {
        return hp > 0;
    }
}