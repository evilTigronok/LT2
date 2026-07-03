package game.ui;

import game.battle.DamageType;
import game.battle.TurnManager;
import game.characters.Character;
import game.characters.Player;
import game.skills.PhysicalWave;
import game.skills.Skill;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class CombatScene {

    private final SceneManager sceneManager;

    private final BorderPane root;

    private final List<Character> playerTeam = new ArrayList<>();
    private final List<Character> enemyTeam = new ArrayList<>();

    private final TextArea log = new TextArea();
    private final TextArea turnOrder = new TextArea();

    private final VBox skillBox = new VBox(10);

    private TurnManager turnManager;
    private Character current;

    private Skill selectedSkill;

    public CombatScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;

        root = new BorderPane();
        root.setPadding(new Insets(10));

        initBattle();
        initUI();
        updateUI();
    }

    private void initBattle() {

        Player player = new Player("Hero");
        player.getSkills().add(new PhysicalWave());

        playerTeam.add(player);

        enemyTeam.add(new Character("Goblin") {
            @Override
            public boolean isAlive() {
                return getHp() > 0;
            }
        });

        enemyTeam.add(new Character("Orc") {
            @Override
            public boolean isAlive() {
                return getHp() > 0;
            }
        });

        enemyTeam.add(new Character("Slime") {
            @Override
            public boolean isAlive() {
                return getHp() > 0;
            }
        });

        List<Character> all = new ArrayList<>();
        all.addAll(playerTeam);
        all.addAll(enemyTeam);

        turnManager = new TurnManager(all);
        current = turnManager.nextTurn();
    }

    private void initUI() {

        log.setEditable(false);
        turnOrder.setEditable(false);

        root.setCenter(log);
        root.setRight(turnOrder);
        root.setBottom(skillBox);

        rebuildSkills();
    }

    private void rebuildSkills() {

        skillBox.getChildren().clear();

        if (!(current instanceof Player)) return;

        for (Skill s : current.getSkills()) {

            Button b = new Button(s.getName());

            b.setOnAction(e -> {
                selectedSkill = s;
                log.appendText("\nSelected: " + s.getName());
            });

            skillBox.getChildren().add(b);
        }
    }

    private void nextTurn() {
        current = turnManager.nextTurn();
        rebuildSkills();
        updateUI();
    }

    private void updateUI() {

        StringBuilder sb = new StringBuilder();

        sb.append("PLAYERS:\n");
        for (Character c : playerTeam) {
            sb.append(c.getName()).append(" HP: ").append(c.getHp()).append("\n");
        }

        sb.append("\nENEMIES:\n");
        for (Character c : enemyTeam) {
            sb.append(c.getName()).append(" HP: ").append(c.getHp()).append("\n");
        }

        log.setText(sb.toString());

        StringBuilder t = new StringBuilder("TURN ORDER:\n");
        for (Character c : turnManager.getUpcomingTurns()) {
            t.append(c.getName()).append("\n");
        }

        turnOrder.setText(t.toString());
    }

    public Parent getRoot() {
        return root;
    }
}