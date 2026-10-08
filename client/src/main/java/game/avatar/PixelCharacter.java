package game.avatar;

import game.avatar.asset.AccessoryAsset;
import game.avatar.asset.BodyAsset;
import game.avatar.asset.ClothingAsset;
import game.avatar.asset.HairAsset;
import game.avatar.asset.WeaponAsset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PixelCharacter {

    private BodyAsset body;
    private HairAsset hair;

    private ClothingAsset shirt;
    private ClothingAsset outerwear;
    private ClothingAsset pants;
    private ClothingAsset shoes;

    private final List<AccessoryAsset> accessories =
            new ArrayList<>();

    private WeaponAsset weapon;

    public BodyAsset getBody() {
        return body;
    }

    public void setBody(
            BodyAsset body
    ) {

        this.body = body;
    }

    public HairAsset getHair() {
        return hair;
    }

    public void setHair(
            HairAsset hair
    ) {

        this.hair = hair;
    }

    public ClothingAsset getShirt() {
        return shirt;
    }

    public void setShirt(
            ClothingAsset shirt
    ) {

        this.shirt = shirt;
    }

    public ClothingAsset getOuterwear() {
        return outerwear;
    }

    public void setOuterwear(
            ClothingAsset outerwear
    ) {

        this.outerwear = outerwear;
    }

    public ClothingAsset getPants() {
        return pants;
    }

    public void setPants(
            ClothingAsset pants
    ) {

        this.pants = pants;
    }

    public ClothingAsset getShoes() {
        return shoes;
    }

    public void setShoes(
            ClothingAsset shoes
    ) {

        this.shoes = shoes;
    }

    public List<AccessoryAsset> getAccessories() {

        return Collections.unmodifiableList(
                accessories
        );
    }

    public void addAccessory(
            AccessoryAsset accessory
    ) {

        if (accessory != null) {
            accessories.add(accessory);
        }
    }

    public void removeAccessory(
            String id
    ) {

        accessories.removeIf(
                accessory ->
                        accessory.getId().equals(id)
        );
    }

    public void clearAccessories() {

        accessories.clear();
    }

    public WeaponAsset getWeapon() {
        return weapon;
    }

    public void setWeapon(
            WeaponAsset weapon
    ) {

        this.weapon = weapon;
    }
}