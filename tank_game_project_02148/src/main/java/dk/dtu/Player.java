package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Player {
    private InputHandler inputHandler;
    private String playerName;
    private double x;
    private double y;

    private double angle = 0.0;
    private double angleSpeed = 6.0;
    private double speed = 5.0;

    public Player(InputHandler inputHandler, String playerName, double x, double y) {
        this.playerName = playerName;
        this.x = x;
        this.y = y;
        this.inputHandler = inputHandler;
    }

    public void update() {
        if (inputHandler.leftPressed) {
            angle -= angleSpeed;
        }
        if (inputHandler.rightPressed) {
            angle += angleSpeed;
        }

        double angleRadians = Math.toRadians(angle);

        if (inputHandler.upPressed) {
            x += Math.cos(angleRadians) * speed;
            y += Math.sin(angleRadians) * speed;
        }
        if (inputHandler.downPressed) {
            x -= Math.cos(angleRadians) * speed;
            y -= Math.sin(angleRadians) * speed;
        }
    }

    public void repaint(GraphicsContext gc) {
        gc.save();
        gc.translate(x+10, y+10);
        gc.rotate(angle);

        gc.setFill(Color.BLUE);
        gc.fillRect(-10, -10, 20, 20);

        gc.restore();
    }
}
