package dk.dtu.main;

import dk.dtu.connection.Client;
import dk.dtu.gamestates.GameOver;
import dk.dtu.gamestates.Gamestate;
import dk.dtu.gamestates.Join;
import dk.dtu.gamestates.Lobby;
import dk.dtu.gamestates.Menu;
import dk.dtu.inputs.InputHandler;
import dk.dtu.inputs.MouseHandler;
import dk.dtu.map.Grid;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class GameEngine extends Pane implements Runnable {
    public boolean isHost = false; 
    public boolean online = true;
    public String IP = "localhost";

    private final int rows = 36;
    private final int cols = 46;
    public final int tileSize = 16;
    public final int screenWidth = tileSize * cols;
    public final int screenHeight = tileSize * rows;
    private final int fps = 30;

    private Canvas canvas;
    private Thread gameThread;
    private GraphicsContext gc;
    private InputHandler inputHandler;
    private MouseHandler mouseHandler;
    public Grid grid;

    public Client client;
    private Menu menu;
    private GameOver gameOver;
    private Lobby lobby;
    private Join join;

    public long projectileLifespan = 9000;

    public GameEngine() {
        this.setPrefSize(screenWidth, screenHeight);
        this.setStyle("-fx-background-color: lightgrey;");

        canvas = new Canvas(screenWidth, screenHeight);
        gc = canvas.getGraphicsContext2D();
        inputHandler = new InputHandler();
        mouseHandler = new MouseHandler();

        this.setOnMousePressed(mouseHandler);
        this.setOnMouseReleased(mouseHandler);
        this.setOnMouseMoved(mouseHandler);

        this.setOnKeyPressed(inputHandler);
        this.setOnKeyReleased(inputHandler);

        this.setOnKeyTyped(event -> join.handleInput(event));

        this.getChildren().add(canvas);

        client = new Client(this, inputHandler);
        menu = new Menu(this, mouseHandler, client);
        lobby = new Lobby(this, mouseHandler, client);
        join = new Join(this, mouseHandler, client);
        gameOver = new GameOver(this, mouseHandler, client);
        grid = new Grid(this);

        // client.connectToServerHost();
        // client.connectToServer();

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
        //int drawCount = 0;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += currentTime - lastTime;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint(gc);
                delta--;
                //drawCount++;
            }

            if (timer >= 1000000000) {
                // System.out.println("FPS: " + drawCount);
                //drawCount = 0;
                timer = 0;
            }
        }
    }

    private void update() {
        switch (Gamestate.state) {
            case MENU:
                menu.update();
                break;
            case LOBBY:
                lobby.update();
                break;
            case JOIN:
                join.update();
                break;
            case PLAYING:
                client.getPlayer().update();
                client.sendCoordinate();
                if (online) {
                    client.recieveCoordinates();
                }
                break;
            case GAMEOVER:
                gameOver.update();
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
            case LOBBY:
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(0, 0, screenWidth, screenHeight);
                lobby.draw(gc);
                break;
            case JOIN:
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(0, 0, screenWidth, screenHeight);
                join.draw(gc);
                break;
            case PLAYING:
                gc.setFill(Color.LIGHTGRAY);
                gc.fillRect(0, 0, screenWidth, screenHeight);
                grid.drawGrid(gc);
                client.getPlayer().repaint(gc);

                client.drawOpponent(gc);
                break;
            case GAMEOVER:
                gameOver.draw(gc);
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

    public void stopGame() {
        client.sendGameOver();
        Gamestate.state = Gamestate.GAMEOVER;
    }


}
