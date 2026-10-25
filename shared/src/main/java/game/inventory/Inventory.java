package game.inventory;

import game.weapon.Weapon;
import game.weapon.WeaponType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Inventory {

    private static final int MAX_WEAPONS = 100;

    private final List<Weapon> weapons = new ArrayList<>();

    private Weapon equippedMeleeWeapon;
    private Weapon equippedRangedWeapon;

    private static final int ARTIFACT_SLOT_COUNT = 4;

    private final List<Artifact> artifacts = new ArrayList<>();
    private final Artifact[] equippedArtifacts =
            new Artifact[ARTIFACT_SLOT_COUNT];

    private boolean cursedArtifactSlotsLocked;
    private Artifact[] artifactsBeforeCurse;

    /*
     * ОРУЖИЕ
     */

    public boolean addWeapon(Weapon weapon) {
        if (weapon == null || weapons.size() >= MAX_WEAPONS) {
            return false;
        }

        weapons.add(weapon);
        return true;
    }

    public boolean removeWeapon(Weapon weapon) {
        if (weapon == null) {
            return false;
        }

        int index = weapons.indexOf(weapon);
        if (index < 0) {
            return false;
        }

        return removeWeapon(index);
    }

    public boolean removeWeapon(int index) {
        if (index < 0 || index >= weapons.size()) {
            return false;
        }

        Weapon removedWeapon = weapons.remove(index);

        if (removedWeapon == equippedMeleeWeapon) {
            equippedMeleeWeapon = null;
        }

        if (removedWeapon == equippedRangedWeapon) {
            equippedRangedWeapon = null;
        }

        updateCursedArtifactSlots();
        return true;
    }

    private boolean hasMaterialCurseBladeEquipped() {
        return equippedMeleeWeapon != null
                && "material_curse_blade".equals(
                equippedMeleeWeapon.getId()
        );
    }

    private void updateCursedArtifactSlots() {
        boolean shouldLock = hasMaterialCurseBladeEquipped();

        if (shouldLock == cursedArtifactSlotsLocked) {
            return;
        }

        Artifact cursedArtifact = Artifact.CURSED_SOUL_IMITATION;

        if (shouldLock) {
            artifactsBeforeCurse = equippedArtifacts.clone();
            cursedArtifactSlotsLocked = true;

            if (!artifacts.contains(cursedArtifact)) {
                artifacts.add(cursedArtifact);
            }

            for (int i = 0; i < equippedArtifacts.length; i++) {
                equippedArtifacts[i] = cursedArtifact;
            }
        } else {
            cursedArtifactSlotsLocked = false;

            if (artifactsBeforeCurse != null) {
                System.arraycopy(
                        artifactsBeforeCurse,
                        0,
                        equippedArtifacts,
                        0,
                        equippedArtifacts.length
                );
            } else {
                java.util.Arrays.fill(equippedArtifacts, null);
            }

            artifactsBeforeCurse = null;
        }
    }

    public boolean areCursedArtifactSlotsLocked() {
        return cursedArtifactSlotsLocked;
    }

    public boolean equipMeleeWeapon(Weapon weapon) {
        if (weapon == null
                || !weapons.contains(weapon)
                || weapon.getType() != WeaponType.MELEE) {
            return false;
        }

        equippedMeleeWeapon = weapon;
        updateCursedArtifactSlots();

        return true;
    }

    public boolean equipRangedWeapon(Weapon weapon) {
        if (weapon == null
                || !weapons.contains(weapon)
                || weapon.getType() != WeaponType.RANGED) {
            return false;
        }

        equippedRangedWeapon = weapon;
        return true;
    }

    public Weapon getEquippedMeleeWeapon() {
        return equippedMeleeWeapon;
    }

    public Weapon getEquippedRangedWeapon() {
        return equippedRangedWeapon;
    }

    public boolean hasEquippedMeleeWeapon() {
        return equippedMeleeWeapon != null;
    }

    public boolean hasEquippedRangedWeapon() {
        return equippedRangedWeapon != null;
    }

    public Weapon getWeapon(int index) {
        if (index < 0 || index >= weapons.size()) {
            return null;
        }

        return weapons.get(index);
    }

    public int getWeaponCount() {
        return weapons.size();
    }

    public List<Weapon> getWeapons() {
        return Collections.unmodifiableList(weapons);
    }

    public boolean containsWeapon(Weapon weapon) {
        return weapon != null && weapons.contains(weapon);
    }

    public void unequipMeleeWeapon() {
        equippedMeleeWeapon = null;
        updateCursedArtifactSlots();
    }

    public void unequipRangedWeapon() {
        equippedRangedWeapon = null;
    }

    public void clearWeapons() {
        weapons.clear();
        equippedMeleeWeapon = null;
        equippedRangedWeapon = null;
        updateCursedArtifactSlots();
    }

    private Weapon findFirstWeaponOfType(WeaponType type) {
        for (Weapon weapon : weapons) {
            if (weapon != null && weapon.getType() == type) {
                return weapon;
            }
        }

        return null;
    }

    public Weapon findWeaponById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        for (Weapon weapon : weapons) {
            if (weapon != null && id.equals(weapon.getId())) {
                return weapon;
            }
        }

        return null;
    }

    /*
     * АРТЕФАКТЫ
     */

    public boolean addArtifact(Artifact artifact) {
        if (artifact == null || artifacts.contains(artifact)) {
            return false;
        }

        artifacts.add(artifact);
        return true;
    }

    public boolean removeArtifact(Artifact artifact) {
        if (cursedArtifactSlotsLocked
                || artifact == null
                || !artifacts.remove(artifact)) {
            return false;
        }

        for (int i = 0; i < equippedArtifacts.length; i++) {
            if (equippedArtifacts[i] == artifact) {
                equippedArtifacts[i] = null;
            }
        }

        return true;
    }

    public boolean equipArtifact(int slot, Artifact artifact) {
        if (cursedArtifactSlotsLocked
                || slot < 0
                || slot >= ARTIFACT_SLOT_COUNT
                || artifact == null
                || !artifacts.contains(artifact)) {
            return false;
        }

        // Один экземпляр артефакта нельзя надеть в несколько слотов.
        for (int i = 0; i < equippedArtifacts.length; i++) {
            if (equippedArtifacts[i] == artifact) {
                equippedArtifacts[i] = null;
            }
        }

        equippedArtifacts[slot] = artifact;
        return true;
    }

    public void unequipArtifact(int slot) {
        if (cursedArtifactSlotsLocked) {
            return;
        }

        if (slot >= 0 && slot < ARTIFACT_SLOT_COUNT) {
            equippedArtifacts[slot] = null;
        }
    }

    public Artifact getEquippedArtifact(int slot) {
        if (slot < 0 || slot >= ARTIFACT_SLOT_COUNT) {
            return null;
        }

        return equippedArtifacts[slot];
    }

    public List<Artifact> getArtifacts() {
        return Collections.unmodifiableList(artifacts);
    }

    public List<Artifact> getEquippedArtifacts() {
        List<Artifact> result = new ArrayList<>();

        for (Artifact artifact : equippedArtifacts) {
            if (artifact != null) {
                result.add(artifact);
            }
        }

        return Collections.unmodifiableList(result);
    }

    public int getArtifactSlotCount() {
        return ARTIFACT_SLOT_COUNT;
    }

    public Artifact findArtifactById(String id) {
        Artifact artifact = Artifact.byId(id);

        return artifact != null && artifacts.contains(artifact)
                ? artifact
                : null;
    }

    public void clearArtifacts() {
        if (cursedArtifactSlotsLocked) {
            artifacts.clear();
            artifacts.add(Artifact.CURSED_SOUL_IMITATION);

            for (int i = 0; i < equippedArtifacts.length; i++) {
                equippedArtifacts[i] =
                        Artifact.CURSED_SOUL_IMITATION;
            }

            return;
        }

        artifacts.clear();
        java.util.Arrays.fill(equippedArtifacts, null);
    }

    /*
     * СУММАРНЫЕ БОНУСЫ АРТЕФАКТОВ
     */

    public int getArtifactDefenseBonus() {
        int total = 0;

        for (Artifact artifact : equippedArtifacts) {
            if (artifact != null) {
                total += artifact.getDefense();
            }
        }

        return total;
    }

    public float getArtifactDefensePercentBonus() {
        float total = 0f;

        for (Artifact artifact : equippedArtifacts) {
            if (artifact != null) {
                total += artifact.getDefensePercentBonus();
            }
        }

        return total;
    }

    public float getArtifactBaseDamageBonus() {
        float total = 0f;

        for (Artifact artifact : equippedArtifacts) {
            if (artifact != null) {
                total += artifact.getBaseDamageBonus();
            }
        }

        return total;
    }

    public float getArtifactAttackBonus() {
        float total = 0f;

        for (Artifact artifact : equippedArtifacts) {
            if (artifact != null) {
                total += artifact.getAttackBonus();
            }
        }

        return total;
    }

    public float getArtifactAttackPercentBonus() {
        float total = 0f;

        for (Artifact artifact : equippedArtifacts) {
            if (artifact != null) {
                total += artifact.getAttackPercentBonus();
            }
        }

        return total;
    }
}