package game.avatar.test;

import game.avatar.AvatarRenderer;
import game.avatar.Direction;
import game.avatar.PixelCharacter;
import game.avatar.PixelPose;
import game.avatar.asset.AvatarAssetRegistry;

import java.awt.image.BufferedImage;

public final class TestAvatar {

    private TestAvatar() {
    }

    public static PixelCharacter create() {

        AvatarAssetRegistry registry =
                new AvatarAssetRegistry();

        registry.registerDefaultAssets();

        PixelCharacter character =
                new PixelCharacter();

        character.setBody(
                registry.getBody(
                        "skin_medium"
                )
        );

        character.setHair(
                registry.getHair(
                        "hair_01"
                )
        );

        character.setShirt(
                registry.getClothing(
                        "shirt_black"
                )
        );

        character.setPants(
                registry.getClothing(
                        "pants_blue"
                )
        );

        character.setShoes(
                registry.getClothing(
                        "shoes_black"
                )
        );

        character.addAccessory(
                registry.getAccessory(
                        "necklace_01"
                )
        );

        character.addAccessory(
                registry.getAccessory(
                        "earrings_01"
                )
        );

        character.setWeapon(
                registry.getWeapon(
                        "sword_01"
                )
        );

        return character;
    }

    public static BufferedImage createIdle() {

        AvatarRenderer renderer =
                new AvatarRenderer();

        return renderer.render(
                create(),
                PixelPose.idle(
                        Direction.DOWN
                )
        );
    }

    public static BufferedImage createWalkFrame(
            int frame
    ) {

        AvatarRenderer renderer =
                new AvatarRenderer();

        return renderer.render(
                create(),
                PixelPose.walk(
                        Direction.DOWN,
                        frame
                )
        );
    }

    public static BufferedImage createDashFrame(
            int frame
    ) {

        AvatarRenderer renderer =
                new AvatarRenderer();

        return renderer.render(
                create(),
                PixelPose.dash(
                        Direction.DOWN,
                        frame
                )
        );
    }
}