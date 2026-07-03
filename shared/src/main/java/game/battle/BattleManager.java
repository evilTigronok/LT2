package game.battle;

import game.characters.Character;

import java.util.List;

public class BattleManager {

    private final List<Character> characters;

    public BattleManager(List<Character> characters) {
        this.characters = characters;
    }

    // УПРОЩЁННАЯ ЛОГИКА БЕЗ ACTION POINTS

    public void startTurn(Character c) {
        // заглушка (можно расширить позже)
    }

    public boolean canAct(Character c) {
        return c != null && c.isAlive();
    }

    public void endTurn(Character c) {
        // просто переход хода
    }
}