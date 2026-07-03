package game.skills;

import game.battle.DamageType;
import game.characters.Character;

public class PhysicalWave extends Skill {

    public PhysicalWave() {
        super("Physical Wave");
    }

    @Override
    public void use(Character user, Character target) {
        target.takeDamage(user.getAttack(), DamageType.PHYSICAL);
    }
}