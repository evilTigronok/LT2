package game.ui.register;

import game.ui.SceneManager;
import game.ui.SceneType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class RegisterScene {

    private final SceneManager sceneManager;
    private final VBox root = new VBox(10);

    public RegisterScene(SceneManager sceneManager) {
        this.sceneManager = sceneManager;

        build();
    }

    private void build() {

        TextField input = new TextField();

        Button btn = new Button("Finish");

        btn.setOnAction(e -> {

            sceneManager.getClient().send(
                    "REGISTER:" + input.getText()
            );

            sceneManager.show(SceneType.LOGIN);
        });

        root.getChildren().addAll(input, btn);
    }

    public VBox getRoot() {
        return root;
    }
}