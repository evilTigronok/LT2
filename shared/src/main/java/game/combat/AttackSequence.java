package game.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttackSequence {

    private final List<AttackStep> steps;

    public AttackSequence(
            List<AttackStep> steps
    ) {

        if (steps == null
                || steps.isEmpty()) {

            throw new IllegalArgumentException(
                    "Attack sequence cannot be empty"
            );
        }

        this.steps =
                Collections.unmodifiableList(
                        new ArrayList<>(
                                steps
                        )
                );
    }

    public List<AttackStep> getSteps() {
        return steps;
    }

    public int getStepCount() {
        return steps.size();
    }
}