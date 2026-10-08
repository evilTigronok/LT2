package game.avatar.test;

import game.avatar.AvatarAssetComposer;
import game.avatar.Direction;
import game.avatar.PixelCharacter;
import game.avatar.PixelCharacterTemplate;
import game.avatar.PixelPose;
import game.avatar.PixelPoseTransform;
import game.avatar.PoseType;
import game.avatar.TransformedTemplate;
import game.avatar.asset.AvatarAssetRegistry;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AvatarPreview {

    private static final int SCALE = 2;

    private static final int CELL_WIDTH =
            PixelCharacterTemplate.WIDTH * SCALE;

    private static final int CELL_HEIGHT =
            PixelCharacterTemplate.HEIGHT * SCALE;

    public static void main(String[] args)
            throws IOException {

        AvatarAssetRegistry registry =
                new AvatarAssetRegistry();

        registry.registerDefaultAssets();


        PixelCharacter character =
                new PixelCharacter();

        character.setBody(
                registry.getBody("skin_medium")
        );

        character.setHair(
                registry.getHair("hair_01")
        );

        character.setShirt(
                registry.getClothing("shirt_black")
        );

        character.setPants(
                registry.getClothing("pants_blue")
        );

        character.setShoes(
                registry.getClothing("shoes_black")
        );

        character.addAccessory(
                registry.getAccessory("necklace_01")
        );

        character.addAccessory(
                registry.getAccessory("earrings_01")
        );

        character.setWeapon(
                registry.getWeapon("sword_01")
        );


        PixelPoseTransform poseTransform =
                new PixelPoseTransform();

        AvatarAssetComposer composer =
                new AvatarAssetComposer();


        Direction[] directions = {

                Direction.DOWN,
                Direction.DOWN_RIGHT,
                Direction.RIGHT,
                Direction.UP_RIGHT,

                Direction.UP,
                Direction.UP_LEFT,
                Direction.LEFT,
                Direction.DOWN_LEFT
        };


        BufferedImage result =
                new BufferedImage(
                        CELL_WIDTH * 4,
                        CELL_HEIGHT * 2,
                        BufferedImage.TYPE_INT_ARGB
                );

        Graphics2D graphics =
                result.createGraphics();

        configureGraphics(graphics);


        for (int i = 0; i < directions.length; i++) {

            Direction direction =
                    directions[i];

            PixelPose pose =
                    new PixelPose(
                            PoseType.IDLE,
                            direction,
                            0
                    );


            TransformedTemplate transformed =
                    poseTransform.transform(pose);


            BufferedImage avatar =
                    composer.compose(
                            character,
                            pose,
                            transformed
                    );


            int column =
                    i % 4;

            int row =
                    i / 4;


            int x =
                    column * CELL_WIDTH;

            int y =
                    row * CELL_HEIGHT;


            graphics.drawImage(
                    avatar,
                    x,
                    y,
                    CELL_WIDTH,
                    CELL_HEIGHT,
                    null
            );
        }


        graphics.dispose();


        File output =
                new File(
                        "avatar_preview_directions.png"
                );

        ImageIO.write(
                result,
                "png",
                output
        );


        System.out.println(
                "Preview saved to: "
                        + output.getAbsolutePath()
        );
    }


    private static void configureGraphics(
            Graphics2D graphics
    ) {

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED
        );
    }
}