package game.item;

import java.util.Objects;

public class AbilityDefinition {

    private final String id;
    private final String name;
    private final String description;

    public AbilityDefinition(
            String id,
            String name,
            String description
    ) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Ability id cannot be empty"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Ability name cannot be empty"
            );
        }

        this.id = id;
        this.name = name;
        this.description =
                description == null ? "" : description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof AbilityDefinition other)) {
            return false;
        }

        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}