package game.player;

import java.util.Arrays;

public class PlayerSkillLoadout {

    public static final int SKILL_SLOTS = 8;

    private final PlayerSkill[] skills =
            new PlayerSkill[SKILL_SLOTS];

    public PlayerSkillLoadout() {
    }

    public PlayerSkill getSkill(int slot) {

        checkSlot(slot);

        return skills[slot];
    }

    public void setSkill(
            int slot,
            PlayerSkill skill
    ) {

        checkSlot(slot);

        skills[slot] = skill;
    }

    public void clearSkill(int slot) {

        checkSlot(slot);

        skills[slot] = null;
    }

    public boolean hasSkill(int slot) {

        checkSlot(slot);

        return skills[slot] != null;
    }

    public PlayerSkill[] getSkills() {
        return Arrays.copyOf(
                skills,
                skills.length
        );
    }

    private void checkSlot(int slot) {

        if (slot < 0 || slot >= SKILL_SLOTS) {
            throw new IllegalArgumentException(
                    "Invalid skill slot: " + slot
            );
        }
    }
}