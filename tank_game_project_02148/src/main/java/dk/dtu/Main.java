package dk.dtu;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        GameEngine gameLoop = new GameEngine();
        Scene scene = new Scene(gameLoop);

        primaryStage.setTitle("Tank Game");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.setOnCloseRequest((WindowEvent _) -> {
            System.exit(0);
        });
        primaryStage.show();
        gameLoop.requestFocus();
    }
}