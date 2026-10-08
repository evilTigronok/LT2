package game.avatar.asset;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AvatarAssetRegistry {

    private final Map<String, BodyAsset> bodies =
            new HashMap<>();

    private final Map<String, HairAsset> hairs =
            new HashMap<>();

    private final Map<String, ClothingAsset> clothing =
            new HashMap<>();

    private final Map<String, AccessoryAsset> accessories =
            new HashMap<>();

    private final Map<String, WeaponAsset> weapons =
            new HashMap<>();

    public void registerDefaultAssets() {

        // =========================
        // BODY
        // =========================

        registerBody(
                new BodyAsset(
                        "skin_light",
                        "Light Skin",
                        "/assets/avatar/body/skin_light.png"
                )
        );

        registerBody(
                new BodyAsset(
                        "skin_medium",
                        "Medium Skin",
                        "/assets/avatar/body/skin_medium.png"
                )
        );

        registerBody(
                new BodyAsset(
                        "skin_dark",
                        "Dark Skin",
                        "/assets/avatar/body/skin_dark.png"
                )
        );

        // =========================
        // HAIR
        // =========================

        registerHair(
                new HairAsset(
                        "hair_01",
                        "Hair 01",
                        "/assets/avatar/hair/hair_01.png"
                )
        );

        registerHair(
                new HairAsset(
                        "hair_02",
                        "Hair 02",
                        "/assets/avatar/hair/hair_02.png"
                )
        );

        registerHair(
                new HairAsset(
                        "hair_03",
                        "Hair 03",
                        "/assets/avatar/hair/hair_03.png"
                )
        );

        // =========================
        // CLOTHING
        // =========================

        registerClothing(
                new ClothingAsset(
                        "shirt_black",
                        "Black Shirt",
                        "/assets/avatar/clothes/shirt_black.png",
                        ClothingAsset.Type.SHIRT
                )
        );

        registerClothing(
                new ClothingAsset(
                        "shirt_white",
                        "White Shirt",
                        "/assets/avatar/clothes/shirt_white.png",
                        ClothingAsset.Type.SHIRT
                )
        );

        registerClothing(
                new ClothingAsset(
                        "pants_blue",
                        "Blue Pants",
                        "/assets/avatar/clothes/pants_blue.png",
                        ClothingAsset.Type.PANTS
                )
        );

        registerClothing(
                new ClothingAsset(
                        "pants_black",
                        "Black Pants",
                        "/assets/avatar/clothes/pants_black.png",
                        ClothingAsset.Type.PANTS
                )
        );

        registerClothing(
                new ClothingAsset(
                        "shoes_black",
                        "Black Shoes",
                        "/assets/avatar/clothes/shoes_black.png",
                        ClothingAsset.Type.SHOES
                )
        );

        // =========================
        // ACCESSORIES
        // =========================

        registerAccessory(
                new AccessoryAsset(
                        "necklace_01",
                        "Necklace 01",
                        "/assets/avatar/accessories/necklace_01.png",
                        AccessoryAsset.Type.NECKLACE
                )
        );

        registerAccessory(
                new AccessoryAsset(
                        "earrings_01",
                        "Earrings 01",
                        "/assets/avatar/accessories/earrings_01.png",
                        AccessoryAsset.Type.EARRING
                )
        );

        // =========================
        // WEAPONS
        // =========================

        registerWeapon(
                PlaceholderWeapons.sword()
        );
    }

    // =========================
    // REGISTER
    // =========================

    public void registerBody(
            BodyAsset asset
    ) {

        if (asset == null) {
            throw new IllegalArgumentException(
                    "Body asset cannot be null"
            );
        }

        bodies.put(
                asset.getId(),
                asset
        );
    }

    public void registerHair(
            HairAsset asset
    ) {

        if (asset == null) {
            throw new IllegalArgumentException(
                    "Hair asset cannot be null"
            );
        }

        hairs.put(
                asset.getId(),
                asset
        );
    }

    public void registerClothing(
            ClothingAsset asset
    ) {

        if (asset == null) {
            throw new IllegalArgumentException(
                    "Clothing asset cannot be null"
            );
        }

        clothing.put(
                asset.getId(),
                asset
        );
    }

    public void registerAccessory(
            AccessoryAsset asset
    ) {

        if (asset == null) {
            throw new IllegalArgumentException(
                    "Accessory asset cannot be null"
            );
        }

        accessories.put(
                asset.getId(),
                asset
        );
    }

    public void registerWeapon(
            WeaponAsset asset
    ) {

        if (asset == null) {
            throw new IllegalArgumentException(
                    "Weapon asset cannot be null"
            );
        }

        weapons.put(
                asset.getId(),
                asset
        );
    }

    // =========================
    // GET
    // =========================

    public BodyAsset getBody(
            String id
    ) {

        return bodies.get(id);
    }

    public HairAsset getHair(
            String id
    ) {

        return hairs.get(id);
    }

    public ClothingAsset getClothing(
            String id
    ) {

        return clothing.get(id);
    }

    public AccessoryAsset getAccessory(
            String id
    ) {

        return accessories.get(id);
    }

    public WeaponAsset getWeapon(
            String id
    ) {

        return weapons.get(id);
    }

    // =========================
    // COLLECTIONS
    // =========================

    public Map<String, BodyAsset> getBodies() {

        return Collections.unmodifiableMap(
                bodies
        );
    }

    public Map<String, HairAsset> getHairs() {

        return Collections.unmodifiableMap(
                hairs
        );
    }

    public Map<String, ClothingAsset> getClothingAssets() {

        return Collections.unmodifiableMap(
                clothing
        );
    }

    public Map<String, AccessoryAsset> getAccessories() {

        return Collections.unmodifiableMap(
                accessories
        );
    }

    public Map<String, WeaponAsset> getWeapons() {

        return Collections.unmodifiableMap(
                weapons
        );
    }
}