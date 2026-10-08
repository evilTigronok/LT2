package game.avatar;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PixelCharacterTemplate {

    public static final int WIDTH = 32;
    public static final int HEIGHT = 40;

    private final EnumMap<PixelPart, PixelRect> parts =
            new EnumMap<>(PixelPart.class);

    private final HashMap<String, PixelPoint> anchors =
            new HashMap<>();

    public PixelCharacterTemplate() {
        build();
    }

    private void build() {

        /*
         * =========================
         * HEAD
         * =========================
         */

        parts.put(
                PixelPart.HEAD,
                new PixelRect(10, 2, 12, 10)
        );

        parts.put(
                PixelPart.FACE,
                new PixelRect(11, 5, 10, 7)
        );

        parts.put(
                PixelPart.HAIR_BACK,
                new PixelRect(9, 1, 14, 13)
        );

        parts.put(
                PixelPart.HAIR,
                new PixelRect(9, 1, 14, 8)
        );

        parts.put(
                PixelPart.LEFT_EAR,
                new PixelRect(9, 6, 3, 5)
        );

        parts.put(
                PixelPart.RIGHT_EAR,
                new PixelRect(20, 6, 3, 5)
        );


        /*
         * =========================
         * NECK
         * =========================
         */

        parts.put(
                PixelPart.NECK,
                new PixelRect(13, 11, 6, 3)
        );


        /*
         * =========================
         * TORSO
         * =========================
         */

        parts.put(
                PixelPart.TORSO,
                new PixelRect(8, 13, 16, 10)
        );

        parts.put(
                PixelPart.WAIST,
                new PixelRect(9, 22, 14, 4)
        );

        parts.put(
                PixelPart.PELVIS,
                new PixelRect(9, 25, 14, 6)
        );


        /*
         * =========================
         * ARMS
         * =========================
         */

        parts.put(
                PixelPart.LEFT_UPPER_ARM,
                new PixelRect(4, 14, 5, 6)
        );

        parts.put(
                PixelPart.LEFT_ELBOW,
                new PixelRect(3, 19, 5, 4)
        );

        parts.put(
                PixelPart.LEFT_FOREARM,
                new PixelRect(3, 22, 5, 7)
        );

        parts.put(
                PixelPart.LEFT_HAND,
                new PixelRect(2, 28, 6, 4)
        );


        parts.put(
                PixelPart.RIGHT_UPPER_ARM,
                new PixelRect(23, 14, 5, 6)
        );

        parts.put(
                PixelPart.RIGHT_ELBOW,
                new PixelRect(24, 19, 5, 4)
        );

        parts.put(
                PixelPart.RIGHT_FOREARM,
                new PixelRect(24, 22, 5, 7)
        );

        parts.put(
                PixelPart.RIGHT_HAND,
                new PixelRect(24, 28, 6, 4)
        );


        /*
         * =========================
         * LEFT LEG
         * =========================
         */

        parts.put(
                PixelPart.LEFT_HIP,
                new PixelRect(9, 25, 7, 4)
        );

        parts.put(
                PixelPart.LEFT_THIGH,
                new PixelRect(8, 28, 8, 6)
        );

        parts.put(
                PixelPart.LEFT_KNEE,
                new PixelRect(8, 33, 8, 3)
        );

        parts.put(
                PixelPart.LEFT_SHIN,
                new PixelRect(8, 35, 7, 4)
        );

        parts.put(
                PixelPart.LEFT_ANKLE,
                new PixelRect(7, 38, 8, 2)
        );

        parts.put(
                PixelPart.LEFT_FOOT,
                new PixelRect(5, 37, 10, 3)
        );


        /*
         * =========================
         * RIGHT LEG
         * =========================
         */

        parts.put(
                PixelPart.RIGHT_HIP,
                new PixelRect(16, 25, 7, 4)
        );

        parts.put(
                PixelPart.RIGHT_THIGH,
                new PixelRect(16, 28, 8, 6)
        );

        parts.put(
                PixelPart.RIGHT_KNEE,
                new PixelRect(16, 33, 8, 3)
        );

        parts.put(
                PixelPart.RIGHT_SHIN,
                new PixelRect(17, 35, 7, 4)
        );

        parts.put(
                PixelPart.RIGHT_ANKLE,
                new PixelRect(17, 38, 8, 2)
        );

        parts.put(
                PixelPart.RIGHT_FOOT,
                new PixelRect(17, 37, 10, 3)
        );


        /*
         * =========================
         * ANCHORS
         * =========================
         */

        anchors.put(
                "head_center",
                new PixelPoint(16, 7)
        );

        anchors.put(
                "neck",
                new PixelPoint(16, 13)
        );

        anchors.put(
                "left_shoulder",
                new PixelPoint(8, 15)
        );

        anchors.put(
                "right_shoulder",
                new PixelPoint(24, 15)
        );

        anchors.put(
                "left_elbow",
                new PixelPoint(5, 21)
        );

        anchors.put(
                "right_elbow",
                new PixelPoint(27, 21)
        );

        anchors.put(
                "left_hand",
                new PixelPoint(5, 30)
        );

        anchors.put(
                "right_hand",
                new PixelPoint(27, 30)
        );

        anchors.put(
                "torso_center",
                new PixelPoint(16, 18)
        );

        anchors.put(
                "waist",
                new PixelPoint(16, 24)
        );

        anchors.put(
                "pelvis",
                new PixelPoint(16, 28)
        );

        anchors.put(
                "left_knee",
                new PixelPoint(12, 34)
        );

        anchors.put(
                "right_knee",
                new PixelPoint(20, 34)
        );

        anchors.put(
                "left_foot",
                new PixelPoint(10, 39)
        );

        anchors.put(
                "right_foot",
                new PixelPoint(22, 39)
        );

        anchors.put(
                "weapon_left",
                new PixelPoint(5, 30)
        );

        anchors.put(
                "weapon_right",
                new PixelPoint(27, 30)
        );

        anchors.put(
                "center",
                new PixelPoint(16, 20)
        );
    }


    public PixelRect getPart(PixelPart part) {
        return parts.get(part);
    }

    public PixelPoint getAnchor(String name) {
        return anchors.get(name);
    }

    public Map<PixelPart, PixelRect> getParts() {
        return Map.copyOf(parts);
    }

    public Map<String, PixelPoint> getAnchors() {
        return Map.copyOf(anchors);
    }

    public static int getWidth() {
        return WIDTH;
    }

    public static int getHeight() {
        return HEIGHT;
    }
}