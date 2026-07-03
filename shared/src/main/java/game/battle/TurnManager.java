package game.battle;

import game.characters.Character;

import java.util.ArrayList;
import java.util.List;

public class TurnManager {

    private final List<Character> queue = new ArrayList<>();

    public TurnManager(List<Character> characters) {
        queue.addAll(characters);
    }

    public Character nextTurn() {
        Character c = queue.remove(0);
        queue.add(c);
        return c;
    }

    public List<Character> getUpcomingTurns() {
        return queue;
    }
}