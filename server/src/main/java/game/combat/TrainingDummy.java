package game.combat;

public class TrainingDummy extends CombatTarget {

    public TrainingDummy(
            float x,
            float y
    ) {

        super(
                "training_dummy",
                x,
                y,
                40f,
                40f,
                100f
        );
    }
}