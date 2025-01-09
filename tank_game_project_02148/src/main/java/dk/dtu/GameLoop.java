package dk.dtu;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class GameLoop extends Pane implements Runnable {
    private final int rows = 12;
    private final int cols = 16;
    private final int originalTileSize = 16;
    private final int scale = 3;
    private final int tileSize = originalTileSize * scale;
    private final int screenWidth = tileSize * cols;
    private final int screenHeight = tileSize * rows;
    private final int fps = 30;

    private Canvas canvas;
    private Thread gameThread;
    private Player player;
    private GraphicsContext gc;

    public GameLoop() {
        this.setPrefSize(screenWidth, screenHeight);
        this.setStyle("-fx-background-color: lightgrey;");

        canvas = new Canvas(screenWidth, screenHeight);
        gc = canvas.getGraphicsContext2D();

        this.getChildren().add(canvas);
        player = new Player("Player1", 100, 100);
        startGameThread();
    }

    private void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / fps;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += currentTime - lastTime;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint(gc);
                delta--;
                drawCount++;
            }

            if (timer >= 1000000000) {
                System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    private void update() {
        player.update();
    }

    private void repaint(GraphicsContext gc) {
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, screenWidth, screenHeight);
        player.repaint(gc);
    }
}
