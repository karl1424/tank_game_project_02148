package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Player {
    private String playerName;
    private int x;
    private int y;

    public Player(String playerName, int x, int y) {
        this.playerName = playerName;
        this.x = x;
        this.y = y;
    }

    public void update() {
    }

    public void repaint(GraphicsContext gc) {
        gc.setFill(Color.BLUE);
        gc.fillRect(x, y, 20, 20);
    }
}
