package dk.dtu;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class GameEngine extends Pane implements Runnable {
    public boolean isHost = true;
    public boolean online = false;
    public String IP = "10.134.17.47";

    private final int rows = 36;
    private final int cols = 46;
    public final int tileSize = 16;
    private final int screenWidth = tileSize * cols;
    private final int screenHeight = tileSize * rows;
    private final int fps = 30;

    private Canvas canvas;
    private Thread gameThread;
    private GraphicsContext gc;
    private InputHandler inputHandler;
    public Grid grid;

    private Client client;
    private Menu menu;

    public long projectileLifespan = 3000;

    public GameEngine() {
        this.setPrefSize(screenWidth, screenHeight);
        this.setStyle("-fx-background-color: lightgrey;");

        canvas = new Canvas(screenWidth, screenHeight);
        gc = canvas.getGraphicsContext2D();
        inputHandler = new InputHandler();

        this.setOnKeyPressed(inputHandler);
        this.setOnKeyReleased(inputHandler);

        this.getChildren().add(canvas);

        menu = new Menu(this, inputHandler);
        client = new Client(this, inputHandler);
        grid = new Grid(this);
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

        if (online) {
            new Thread(() -> {
                client.recieveShots();
            }).start();
        }

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += currentTime - lastTime;
            lastTime = currentTime;

            if (delta >= 1) {
                new Thread(() -> {
                    try {
                        update();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                repaint(gc);
                delta--;
                drawCount++;
            }

            if (timer >= 1000000000) {
                // System.out.println("FPS: " + drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    private void update() throws InterruptedException {
        switch (Gamestate.state) {
            case MENU:
                menu.update();
                break;
            case PLAYING:
                client.getPlayer().update();

                client.sendCoordinate();
                if (online) {
                    client.recieveCoordinates();
                }
                break;
            default:
                break;
        }
    }

    private void repaint(GraphicsContext gc) {
        switch (Gamestate.state) {
            case MENU:
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(0, 0, screenWidth, screenHeight);
                this.getChildren().removeAll();
                menu.draw(gc);
                break;
            case PLAYING:
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(0, 0, screenWidth, screenHeight);
                grid.drawGrid(gc);
                client.getPlayer().repaint(gc);

                client.drawOpponent(gc);
                break;
            default:
                break;
        }

        /*
         * if (!this.getChildren().contains(client.getPlayer().getHitbox())) {
         * this.getChildren().add(client.getPlayer().getHitbox());
         * }
         */
    }
}
