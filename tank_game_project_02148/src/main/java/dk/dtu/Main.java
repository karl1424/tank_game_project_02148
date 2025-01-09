package dk.dtu;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class Main extends Application {

    private boolean rotatingRight = false;
    private boolean rotatingLeft = false;
    private boolean movingForward = false;
    private boolean movingBackward = false;
    private boolean running = true; // Flag for at kontrollere om trådene skal køre

    @Override
    public void start(Stage primaryStage) {
        // Opret en firkant (tankens hoved)
        Rectangle rectangle = new Rectangle(100, 100, Color.BLUE);

        // Opret en layout container (StackPane) og centrer firkanten
        StackPane root = new StackPane();
        root.getChildren().add(rectangle);

        // Opret scenen
        Scene scene = new Scene(root, 500, 500);

        root.setOnMouseClicked(_ -> root.requestFocus());
        root.requestFocus();

        // Eventfilter for tastetryk
        scene.setOnKeyPressed(event -> handleKeyPress(event, rectangle, root));
        scene.setOnKeyReleased(event -> handleKeyRelease(event));

        // Sæt scenen til primærvinduet
        primaryStage.setTitle("Rotér og Bevæg Firkant");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Konstant rotation og bevægelse, mens tasterne er nede
        new Thread(() -> {
            while (running) { // Tjekker om tråden stadig skal køre
                try {
                    // Rotation
                    if (rotatingRight) {
                        rectangle.setRotate(rectangle.getRotate() + 1);
                    }
                    if (rotatingLeft) {
                        rectangle.setRotate(rectangle.getRotate() - 1);
                    }

                    // Bevægelse fremad eller bagud
                    double angleInRadians = Math.toRadians(rectangle.getRotate());
                    if (movingForward) {
                        rectangle.setTranslateX(rectangle.getTranslateX() + Math.cos(angleInRadians) * 2);
                        rectangle.setTranslateY(rectangle.getTranslateY() + Math.sin(angleInRadians) * 2);
                    }
                    if (movingBackward) {
                        rectangle.setTranslateX(rectangle.getTranslateX() - Math.cos(angleInRadians) * 2);
                        rectangle.setTranslateY(rectangle.getTranslateY() - Math.sin(angleInRadians) * 2);
                    }

                    Thread.sleep(10); // Juster for hastighed
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Når vinduet lukkes, stopper vi trådene
        primaryStage.setOnCloseRequest(_ -> {
            running = false; // Stop trådene
            Platform.exit(); // Luk JavaFX-applikationen korrekt
        });
    }

    // Håndterer tastetryk og starter rotation og bevægelse
    private void handleKeyPress(KeyEvent event, Rectangle rectangle, StackPane root) {
        if (event.getCode() == KeyCode.RIGHT) {
            rotatingRight = true; // Start rotation til højre
        } else if (event.getCode() == KeyCode.LEFT) {
            rotatingLeft = true; // Start rotation til venstre
        } else if (event.getCode() == KeyCode.UP) {
            movingForward = true; // Start bevægelse fremad
        } else if (event.getCode() == KeyCode.DOWN) {
            movingBackward = true; // Start bevægelse bagud
        }
    }

    // Stop rotation og bevægelse, når tasten slippes
    private void handleKeyRelease(KeyEvent event) {
        if (event.getCode() == KeyCode.RIGHT) {
            rotatingRight = false; // Stop rotation til højre
        } else if (event.getCode() == KeyCode.LEFT) {
            rotatingLeft = false; // Stop rotation til venstre
        } else if (event.getCode() == KeyCode.UP) {
            movingForward = false; // Stop bevægelse fremad
        } else if (event.getCode() == KeyCode.DOWN) {
            movingBackward = false; // Stop bevægelse bagud
        }
    }
}
