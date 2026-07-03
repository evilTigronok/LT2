package game.skills;

import game.battle.DamageType;
import game.characters.Character;

public abstract class Skill {

    protected String name;

    public Skill(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void use(Character user, Character target);
}