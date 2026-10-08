package game.avatar;

public class PixelPoseTransform {

    private final PixelCharacterTemplate baseTemplate;

    public PixelPoseTransform() {
        this.baseTemplate = new PixelCharacterTemplate();
    }

    public PixelPoseTransform(PixelCharacterTemplate template) {
        this.baseTemplate = template;
    }

    public TransformedTemplate transform(PixelPose pose) {

        TransformedTemplate result = copyBaseTemplate();

        if (pose == null) {
            return result;
        }

        Direction direction = pose.getDirection();

        result.setDirection(direction);
        result.setOrientation(
                direction.getBodyOrientation()
        );

        switch (direction) {

            case DOWN ->
                    applyDown(result);

            case DOWN_RIGHT ->
                    applyDownRight(result);

            case RIGHT ->
                    applyRight(result);

            case UP_RIGHT ->
                    applyUpRight(result);

            case UP ->
                    applyUp(result);

            case UP_LEFT -> {
                applyUpRight(result);
                mirrorHorizontal(result);
            }

            case LEFT -> {
                applyRight(result);
                mirrorHorizontal(result);
            }

            case DOWN_LEFT -> {
                applyDownRight(result);
                mirrorHorizontal(result);
            }
        }

        /*
         * Пока проверяем только IDLE.
         * WALK/DASH подключим после того,
         * как все 8 направлений будут выглядеть правильно.
         */
        if (pose.getType() != PoseType.IDLE) {
            applyAnimation(result, pose);
        }

        return result;
    }


    // =========================================================
    // BASE
    // =========================================================

    private TransformedTemplate copyBaseTemplate() {

        TransformedTemplate result =
                new TransformedTemplate();

        for (var entry :
                baseTemplate.getParts().entrySet()) {

            result.putPart(
                    entry.getKey(),
                    entry.getValue(),
                    false,
                    getLayer(entry.getKey())
            );
        }

        for (var entry :
                baseTemplate.getAnchors().entrySet()) {

            result.putAnchor(
                    entry.getKey(),
                    entry.getValue()
            );
        }

        return result;
    }


    // =========================================================
    // DOWN
    // =========================================================

    private void applyDown(
            TransformedTemplate result
    ) {

        /*
         * Базовая поза уже является фронтальной.
         *
         * Ничего не меняем.
         */
    }


    // =========================================================
    // DOWN RIGHT
    // =========================================================

    private void applyDownRight(
            TransformedTemplate result
    ) {

        /*
         * 3/4 спереди.
         *
         * Очень небольшие изменения относительно FRONT.
         * Главное — сохранить соединение всех частей.
         */

        // Голова
        move(
                result,
                PixelPart.HEAD,
                1,
                0
        );

        move(
                result,
                PixelPart.FACE,
                1,
                0
        );

        move(
                result,
                PixelPart.HAIR,
                1,
                0
        );

        move(
                result,
                PixelPart.HAIR_BACK,
                1,
                0
        );

        move(
                result,
                PixelPart.NECK,
                1,
                0
        );


        // Торс
        move(
                result,
                PixelPart.TORSO,
                1,
                0
        );

        move(
                result,
                PixelPart.WAIST,
                1,
                0
        );

        move(
                result,
                PixelPart.PELVIS,
                1,
                0
        );


        // Дальняя левая рука немного ближе к корпусу
        moveArmChain(
                result,
                true,
                2,
                0
        );

        // Правая рука — немного наружу
        moveArmChain(
                result,
                false,
                1,
                0
        );


        // Ноги
        moveLegChain(
                result,
                true,
                1,
                0
        );

        moveLegChain(
                result,
                false,
                1,
                0
        );
    }


    // =========================================================
    // RIGHT
    // =========================================================

    private void applyRight(
            TransformedTemplate result
    ) {

        /*
         * Профиль.
         *
         * Персонаж остаётся вертикально связанным,
         * но ширина основных частей уменьшается.
         */

        // -----------------------------------------------------
        // HEAD
        // -----------------------------------------------------

        resizeAndMove(
                result,
                PixelPart.HEAD,
                11,
                4,
                9,
                13
        );

        resizeAndMove(
                result,
                PixelPart.FACE,
                14,
                7,
                6,
                8
        );

        resizeAndMove(
                result,
                PixelPart.HAIR,
                10,
                3,
                11,
                10
        );

        resizeAndMove(
                result,
                PixelPart.HAIR_BACK,
                10,
                3,
                12,
                16
        );

        resizeAndMove(
                result,
                PixelPart.NECK,
                14,
                16,
                5,
                5
        );


        // -----------------------------------------------------
        // TORSO
        // -----------------------------------------------------

        resizeAndMove(
                result,
                PixelPart.TORSO,
                11,
                20,
                12,
                16
        );

        resizeAndMove(
                result,
                PixelPart.WAIST,
                11,
                34,
                11,
                6
        );

        resizeAndMove(
                result,
                PixelPart.PELVIS,
                11,
                38,
                12,
                8
        );


        // -----------------------------------------------------
        // ARMS
        // -----------------------------------------------------

        /*
         * Дальняя рука скрыта.
         */
        hideArm(
                result,
                true
        );

        /*
         * Видимая рука располагается справа
         * от корпуса.
         */
        moveArmChain(
                result,
                false,
                1,
                0
        );


        // -----------------------------------------------------
        // LEGS
        // -----------------------------------------------------

        /*
         * Обе ноги остаются соединёнными с тазом,
         * просто сближаются.
         */

        resizeAndMove(
                result,
                PixelPart.LEFT_HIP,
                11,
                42,
                6,
                7
        );

        resizeAndMove(
                result,
                PixelPart.LEFT_THIGH,
                11,
                47,
                6,
                9
        );

        resizeAndMove(
                result,
                PixelPart.LEFT_KNEE,
                11,
                55,
                6,
                5
        );

        resizeAndMove(
                result,
                PixelPart.LEFT_SHIN,
                11,
                59,
                6,
                4
        );

        resizeAndMove(
                result,
                PixelPart.LEFT_ANKLE,
                11,
                62,
                6,
                2
        );

        resizeAndMove(
                result,
                PixelPart.LEFT_FOOT,
                10,
                62,
                7,
                2
        );


        resizeAndMove(
                result,
                PixelPart.RIGHT_HIP,
                17,
                42,
                6,
                7
        );

        resizeAndMove(
                result,
                PixelPart.RIGHT_THIGH,
                17,
                47,
                6,
                9
        );

        resizeAndMove(
                result,
                PixelPart.RIGHT_KNEE,
                17,
                55,
                6,
                5
        );

        resizeAndMove(
                result,
                PixelPart.RIGHT_SHIN,
                17,
                59,
                6,
                4
        );

        resizeAndMove(
                result,
                PixelPart.RIGHT_ANKLE,
                17,
                62,
                6,
                2
        );

        resizeAndMove(
                result,
                PixelPart.RIGHT_FOOT,
                16,
                62,
                7,
                2
        );
    }


    // =========================================================
    // UP RIGHT
    // =========================================================

    private void applyUpRight(
            TransformedTemplate result
    ) {

        /*
         * 3/4 со спины.
         */

        result.removePart(
                PixelPart.FACE
        );


        // Голова
        move(
                result,
                PixelPart.HEAD,
                1,
                0
        );

        move(
                result,
                PixelPart.HAIR,
                1,
                0
        );

        move(
                result,
                PixelPart.HAIR_BACK,
                1,
                0
        );

        move(
                result,
                PixelPart.NECK,
                1,
                0
        );


        // Корпус
        move(
                result,
                PixelPart.TORSO,
                1,
                0
        );

        move(
                result,
                PixelPart.WAIST,
                1,
                0
        );

        move(
                result,
                PixelPart.PELVIS,
                1,
                0
        );


        // Руки
        moveArmChain(
                result,
                true,
                2,
                0
        );

        moveArmChain(
                result,
                false,
                1,
                0
        );


        // Ноги
        moveLegChain(
                result,
                true,
                1,
                0
        );

        moveLegChain(
                result,
                false,
                1,
                0
        );
    }


    // =========================================================
    // UP
    // =========================================================

    private void applyUp(
            TransformedTemplate result
    ) {

        /*
         * Вид со спины.
         *
         * Основная форма почти такая же,
         * но лицо отсутствует.
         */

        result.removePart(
                PixelPart.FACE
        );


        /*
         * Волосы становятся немного важнее,
         * чем в FRONT.
         */
        resizeAndMove(
                result,
                PixelPart.HAIR_BACK,
                9,
                2,
                14,
                16
        );

        resizeAndMove(
                result,
                PixelPart.HAIR,
                9,
                3,
                14,
                10
        );


        /*
         * Голова остаётся связанной с шеей.
         */
        resizeAndMove(
                result,
                PixelPart.HEAD,
                10,
                4,
                12,
                13
        );


        /*
         * Корпус оставляем практически базовым.
         */
        move(
                result,
                PixelPart.NECK,
                0,
                0
        );

        move(
                result,
                PixelPart.TORSO,
                0,
                0
        );

        move(
                result,
                PixelPart.WAIST,
                0,
                0
        );

        move(
                result,
                PixelPart.PELVIS,
                0,
                0
        );
    }


    // =========================================================
    // MIRROR
    // =========================================================

    private void mirrorHorizontal(
            TransformedTemplate result
    ) {

        for (PixelPart part :
                PixelPart.values()) {

            TransformedPart current =
                    result.getParts().get(part);

            if (current == null) {
                continue;
            }

            PixelRect bounds =
                    current.bounds();

            double mirroredX =
                    PixelCharacterTemplate.WIDTH
                            - bounds.x()
                            - bounds.width();

            result.putPart(
                    part,
                    new PixelRect(
                            mirroredX,
                            bounds.y(),
                            bounds.width(),
                            bounds.height()
                    ),
                    !current.mirrored(),
                    current.layer()
            );
        }
    }


    // =========================================================
    // ARM
    // =========================================================

    private void moveArmChain(
            TransformedTemplate result,
            boolean left,
            double dx,
            double dy
    ) {

        if (left) {

            move(result,
                    PixelPart.LEFT_UPPER_ARM,
                    dx, dy);

            move(result,
                    PixelPart.LEFT_ELBOW,
                    dx, dy);

            move(result,
                    PixelPart.LEFT_FOREARM,
                    dx, dy);

            move(result,
                    PixelPart.LEFT_HAND,
                    dx, dy);

        } else {

            move(result,
                    PixelPart.RIGHT_UPPER_ARM,
                    dx, dy);

            move(result,
                    PixelPart.RIGHT_ELBOW,
                    dx, dy);

            move(result,
                    PixelPart.RIGHT_FOREARM,
                    dx, dy);

            move(result,
                    PixelPart.RIGHT_HAND,
                    dx, dy);
        }
    }


    private void hideArm(
            TransformedTemplate result,
            boolean left
    ) {

        if (left) {

            result.removePart(
                    PixelPart.LEFT_UPPER_ARM
            );

            result.removePart(
                    PixelPart.LEFT_ELBOW
            );

            result.removePart(
                    PixelPart.LEFT_FOREARM
            );

            result.removePart(
                    PixelPart.LEFT_HAND
            );

        } else {

            result.removePart(
                    PixelPart.RIGHT_UPPER_ARM
            );

            result.removePart(
                    PixelPart.RIGHT_ELBOW
            );

            result.removePart(
                    PixelPart.RIGHT_FOREARM
            );

            result.removePart(
                    PixelPart.RIGHT_HAND
            );
        }
    }


    // =========================================================
    // LEGS
    // =========================================================

    private void moveLegChain(
            TransformedTemplate result,
            boolean left,
            double dx,
            double dy
    ) {

        if (left) {

            move(result,
                    PixelPart.LEFT_HIP,
                    dx, dy);

            move(result,
                    PixelPart.LEFT_THIGH,
                    dx, dy);

            move(result,
                    PixelPart.LEFT_KNEE,
                    dx, dy);

            move(result,
                    PixelPart.LEFT_SHIN,
                    dx, dy);

            move(result,
                    PixelPart.LEFT_ANKLE,
                    dx, dy);

            move(result,
                    PixelPart.LEFT_FOOT,
                    dx, dy);

        } else {

            move(result,
                    PixelPart.RIGHT_HIP,
                    dx, dy);

            move(result,
                    PixelPart.RIGHT_THIGH,
                    dx, dy);

            move(result,
                    PixelPart.RIGHT_KNEE,
                    dx, dy);

            move(result,
                    PixelPart.RIGHT_SHIN,
                    dx, dy);

            move(result,
                    PixelPart.RIGHT_ANKLE,
                    dx, dy);

            move(result,
                    PixelPart.RIGHT_FOOT,
                    dx, dy);
        }
    }


    // =========================================================
    // ANIMATION
    // =========================================================

    private void applyAnimation(
            TransformedTemplate result,
            PixelPose pose
    ) {

        int frame =
                pose.getFrame() % 4;

        switch (pose.getType()) {

            case WALK ->
                    applyWalk(
                            result,
                            pose.getDirection(),
                            frame
                    );

            case DASH ->
                    applyDash(
                            result,
                            pose.getDirection()
                    );

            case IDLE -> {
                // Ничего.
            }
        }
    }


    private void applyWalk(
            TransformedTemplate result,
            Direction direction,
            int frame
    ) {

        int movement =
                switch (frame) {
                    case 1 -> 2;
                    case 3 -> -2;
                    default -> 0;
                };

        moveLegChain(
                result,
                true,
                0,
                movement
        );

        moveLegChain(
                result,
                false,
                0,
                -movement
        );

        if (frame == 1 ||
                frame == 3) {

            move(
                    result,
                    PixelPart.HEAD,
                    0,
                    1
            );

            move(
                    result,
                    PixelPart.HAIR,
                    0,
                    1
            );

            move(
                    result,
                    PixelPart.HAIR_BACK,
                    0,
                    1
            );
        }
    }


    private void applyDash(
            TransformedTemplate result,
            Direction direction
    ) {

        int dx =
                direction.horizontal() * -2;

        int dy =
                direction.vertical() * -2;

        move(
                result,
                PixelPart.HEAD,
                dx,
                dy
        );

        move(
                result,
                PixelPart.HAIR,
                dx,
                dy
        );

        move(
                result,
                PixelPart.HAIR_BACK,
                dx,
                dy
        );

        move(
                result,
                PixelPart.TORSO,
                dx,
                dy
        );

        move(
                result,
                PixelPart.WAIST,
                dx,
                dy
        );

        move(
                result,
                PixelPart.PELVIS,
                dx,
                dy
        );
    }


    // =========================================================
    // GEOMETRY
    // =========================================================

    private void move(
            TransformedTemplate result,
            PixelPart part,
            double dx,
            double dy
    ) {

        TransformedPart current =
                result.getParts().get(part);

        if (current == null) {
            return;
        }

        PixelRect bounds =
                current.bounds();

        result.putPart(
                part,
                new PixelRect(
                        bounds.x() + dx,
                        bounds.y() + dy,
                        bounds.width(),
                        bounds.height()
                ),
                current.mirrored(),
                current.layer()
        );
    }


    private void resizeAndMove(
            TransformedTemplate result,
            PixelPart part,
            double x,
            double y,
            double width,
            double height
    ) {

        TransformedPart current =
                result.getParts().get(part);

        if (current == null) {
            return;
        }

        result.putPart(
                part,
                new PixelRect(
                        x,
                        y,
                        width,
                        height
                ),
                current.mirrored(),
                current.layer()
        );
    }


    // =========================================================
    // LAYERS
    // =========================================================

    private int getLayer(
            PixelPart part
    ) {

        return switch (part) {

            case HAIR_BACK ->
                    10;

            case LEFT_EAR,
                 RIGHT_EAR ->
                    20;

            case LEFT_UPPER_ARM,
                 LEFT_ELBOW,
                 LEFT_FOREARM,
                 LEFT_HAND,
                 RIGHT_UPPER_ARM,
                 RIGHT_ELBOW,
                 RIGHT_FOREARM,
                 RIGHT_HAND ->
                    30;

            case TORSO,
                 WAIST,
                 PELVIS ->
                    40;

            case LEFT_HIP,
                 LEFT_THIGH,
                 LEFT_KNEE,
                 LEFT_SHIN,
                 LEFT_ANKLE,
                 LEFT_FOOT,
                 RIGHT_HIP,
                 RIGHT_THIGH,
                 RIGHT_KNEE,
                 RIGHT_SHIN,
                 RIGHT_ANKLE,
                 RIGHT_FOOT ->
                    50;

            case NECK ->
                    60;

            case HEAD,
                 FACE ->
                    70;

            case HAIR ->
                    80;
        };
    }
}