package game.avatar.asset;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class PlaceholderWeapons {

    private PlaceholderWeapons() {
    }

    public static WeaponAsset sword() {

        BufferedImage image =
                new BufferedImage(
                        12,
                        20,
                        BufferedImage.TYPE_INT_ARGB
                );

        Graphics2D g = image.createGraphics();

        // Лезвие
        g.setColor(Color.LIGHT_GRAY);

        for (int y = 0; y < 13; y++) {
            int width = Math.max(1, 5 - y / 3);

            int x = 6 - width / 2;

            g.fillRect(
                    x,
                    y,
                    width,
                    1
            );
        }

        // Гарда
        g.setColor(new Color(180, 150, 70));

        g.fillRect(
                2,
                13,
                8,
                2
        );

        // Рукоять
        g.setColor(new Color(100, 60, 30));

        g.fillRect(
                5,
                15,
                2,
                4
        );

        // Навершие
        g.setColor(new Color(180, 150, 70));

        g.fillRect(
                4,
                19,
                4,
                1
        );

        g.dispose();

        return new WeaponAsset(
                "sword_01",
                "Sword",
                image,

                // Точка крепления
                6,
                17,

                0,

                WeaponAsset.Layer.IN_FRONT_OF_CHARACTER
        );
    }
}