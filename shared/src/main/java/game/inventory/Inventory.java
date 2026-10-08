package game.inventory;

import game.weapon.Weapon;
import game.weapon.WeaponType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Inventory {

    /*
     * Максимальное количество оружия,
     * которое можно хранить.
     */
    private static final int MAX_WEAPONS = 9;

    /*
     * Список всего оружия в инвентаре.
     */
    private final List<Weapon> weapons =
            new ArrayList<>();

    /*
     * Оружие, экипированное в слот ближнего боя.
     */
    private Weapon equippedMeleeWeapon;

    /*
     * Оружие, экипированное в слот дальнего боя.
     */
    private Weapon equippedRangedWeapon;


    /*
     * Добавляет оружие в инвентарь.
     *
     * Первое оружие соответствующего типа
     * автоматически становится экипированным
     * в этот тип слота.
     */
    public boolean addWeapon(
            Weapon weapon
    ) {

        if (weapon == null) {
            return false;
        }

        if (weapons.size() >= MAX_WEAPONS) {
            return false;
        }

        weapons.add(weapon);

        /*
         * Если это первое оружие ближнего боя —
         * автоматически экипируем его.
         */
        if (weapon.getType() == WeaponType.MELEE
                && equippedMeleeWeapon == null) {

            equippedMeleeWeapon = weapon;
        }

        /*
         * Если это первое оружие дальнего боя —
         * автоматически экипируем его.
         */
        if (weapon.getType() == WeaponType.RANGED
                && equippedRangedWeapon == null) {

            equippedRangedWeapon = weapon;
        }

        return true;
    }


    /*
     * Удаляет конкретное оружие.
     */
    public boolean removeWeapon(
            Weapon weapon
    ) {

        if (weapon == null) {
            return false;
        }

        int index =
                weapons.indexOf(weapon);

        if (index < 0) {
            return false;
        }

        return removeWeapon(index);
    }


    /*
     * Удаляет оружие по индексу.
     */
    public boolean removeWeapon(
            int index
    ) {

        if (index < 0
                || index >= weapons.size()) {

            return false;
        }

        Weapon removedWeapon =
                weapons.remove(index);

        /*
         * Если удалили оружие,
         * которое было экипировано,
         * снимаем его.
         */
        if (removedWeapon == equippedMeleeWeapon) {
            equippedMeleeWeapon = null;
        }

        if (removedWeapon == equippedRangedWeapon) {
            equippedRangedWeapon = null;
        }

        /*
         * После удаления автоматически
         * ищем другое оружие того же типа.
         *
         * Это удобно для инвентаря:
         * если игрок удалил экипированный меч,
         * слот ближнего боя не остаётся
         * в некорректном состоянии.
         */
        if (removedWeapon.getType() == WeaponType.MELEE
                && equippedMeleeWeapon == null) {

            equippedMeleeWeapon =
                    findFirstWeaponOfType(
                            WeaponType.MELEE
                    );
        }

        if (removedWeapon.getType() == WeaponType.RANGED
                && equippedRangedWeapon == null) {

            equippedRangedWeapon =
                    findFirstWeaponOfType(
                            WeaponType.RANGED
                    );
        }

        return true;
    }


    /*
     * Экипировать оружие ближнего боя.
     *
     * Оружие должно находиться
     * в инвентаре и иметь тип MELEE.
     */
    public boolean equipMeleeWeapon(
            Weapon weapon
    ) {

        if (weapon == null) {
            return false;
        }

        if (!weapons.contains(weapon)) {
            return false;
        }

        if (weapon.getType() != WeaponType.MELEE) {
            return false;
        }

        equippedMeleeWeapon = weapon;

        return true;
    }


    /*
     * Экипировать оружие дальнего боя.
     *
     * Оружие должно находиться
     * в инвентаре и иметь тип RANGED.
     */
    public boolean equipRangedWeapon(
            Weapon weapon
    ) {

        if (weapon == null) {
            return false;
        }

        if (!weapons.contains(weapon)) {
            return false;
        }

        if (weapon.getType() != WeaponType.RANGED) {
            return false;
        }

        equippedRangedWeapon = weapon;

        return true;
    }


    /*
     * Получить экипированное оружие ближнего боя.
     */
    public Weapon getEquippedMeleeWeapon() {
        return equippedMeleeWeapon;
    }


    /*
     * Получить экипированное оружие дальнего боя.
     */
    public Weapon getEquippedRangedWeapon() {
        return equippedRangedWeapon;
    }


    /*
     * Проверка, экипировано ли оружие ближнего боя.
     */
    public boolean hasEquippedMeleeWeapon() {
        return equippedMeleeWeapon != null;
    }


    /*
     * Проверка, экипировано ли оружие дальнего боя.
     */
    public boolean hasEquippedRangedWeapon() {
        return equippedRangedWeapon != null;
    }


    /*
     * Получить оружие по индексу.
     */
    public Weapon getWeapon(
            int index
    ) {

        if (index < 0
                || index >= weapons.size()) {

            return null;
        }

        return weapons.get(index);
    }


    /*
     * Получить количество оружия.
     */
    public int getWeaponCount() {
        return weapons.size();
    }


    /*
     * Получить список оружия.
     *
     * Только для чтения.
     */
    public List<Weapon> getWeapons() {

        return Collections.unmodifiableList(
                weapons
        );
    }


    /*
     * Проверка наличия оружия.
     */
    public boolean containsWeapon(
            Weapon weapon
    ) {

        return weapon != null
                && weapons.contains(weapon);
    }


    /*
     * Снять оружие ближнего боя.
     */
    public void unequipMeleeWeapon() {
        equippedMeleeWeapon = null;
    }


    /*
     * Снять оружие дальнего боя.
     */
    public void unequipRangedWeapon() {
        equippedRangedWeapon = null;
    }


    /*
     * Полностью очистить оружие.
     */
    public void clearWeapons() {

        weapons.clear();

        equippedMeleeWeapon = null;
        equippedRangedWeapon = null;
    }


    /*
     * Найти первое оружие указанного типа.
     */
    private Weapon findFirstWeaponOfType(
            WeaponType type
    ) {

        for (Weapon weapon : weapons) {

            if (weapon != null
                    && weapon.getType() == type) {

                return weapon;
            }
        }

        return null;
    }

    /*
     * Найти оружие по его стабильному ID.
     *
     * Используется системой сохранения экипировки,
     * чтобы восстановить оружие после перезапуска игры.
     */
    public Weapon findWeaponById(String id) {

        if (id == null || id.isBlank()) {
            return null;
        }

        for (Weapon weapon : weapons) {

            if (weapon != null
                    && id.equals(weapon.getId())) {

                return weapon;
            }
        }

        return null;
    }
}