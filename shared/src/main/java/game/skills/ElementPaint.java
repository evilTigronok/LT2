package game.skills;

import game.battle.DamageType;
import game.characters.Character;

public class ElementPaint extends Skill {

    public ElementPaint() {
        super("Element Paint");
    }

    @Override
    public void use(Character user, Character target) {
        target.takeDamage(user.getAttack(), DamageType.MAGIC);
    }
}