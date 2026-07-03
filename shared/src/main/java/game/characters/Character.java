package game.characters;

import game.battle.DamageType;
import game.skills.Skill;

import java.util.ArrayList;
import java.util.List;

public abstract class Character {

    protected String name;

    protected int hp;
    protected int maxHp;

    protected int attack;
    protected int defense;
    protected int speed;

    protected int aggro;

    protected List<Skill> skills = new ArrayList<>();

    public Character(String name) {
        this.name = name;

        this.maxHp = 100;
        this.hp = 100;
        this.attack = 10;
        this.defense = 5;
        this.speed = 5;
        this.aggro = 0;
    }

    public abstract boolean isAlive();

    public void takeDamage(int dmg, DamageType type) {
        int real = Math.max(1, dmg - defense);
        hp -= real;
        if (hp < 0) hp = 0;
    }

    // getters
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public int getAggro() { return aggro; }

    public List<Skill> getSkills() { return skills; }
}