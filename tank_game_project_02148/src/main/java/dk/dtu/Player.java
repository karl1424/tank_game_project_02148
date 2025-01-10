package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Player {
    private InputHandler inputHandler;
    private GameEngine ge;

    private String playerName;
    private double x = 100.0;
    private double y = 100.0;

    private double angle = 0.0;
    private double angleSpeed = 6.0;
    private double speed = 5.0;

    private Image playerImage;
    private Rectangle hitbox;

    private double previousX;
    private double previousY;

    public Player(GameEngine ge, InputHandler inputHandler, String playerName) {
        this.ge = ge;
        this.playerName = playerName;
        this.inputHandler = inputHandler;
        this.previousX = x;
        this.previousY = y;

        init();
    }

    private void init() {
        playerImage = new Image("file:res/tank1.png");

        hitbox = new Rectangle(x, y, ge.tileSize * 2, ge.tileSize * 2);
        hitbox.setFill(Color.TRANSPARENT);
        hitbox.setStroke(Color.RED);
        hitbox.setStrokeWidth(2);
    }

    public void update() {
        previousX = x;
        previousY = y;

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

        updateHitbox();

        if (checkCollision()) {
            x = previousX;
            y = previousY;
            updateHitbox();
        }
    }

    private void updateHitbox() {
        hitbox.setX(x);
        hitbox.setY(y);
        hitbox.setRotate(angle);
    }

    private boolean checkCollision() {
        boolean check = false;
        int[][] grid = ge.grid.getGrid();
        double x = hitbox.getX();
        double y = hitbox.getY();
        double width = hitbox.getWidth();
        double height = hitbox.getHeight();

        int y1 = (int) (x / ge.tileSize);
        int y2 = (int) ((x + width) / ge.tileSize);
        int x1 = (int) (y / ge.tileSize);
        int x2 = (int) ((y + height) / ge.tileSize);

        if (grid[x1][y1] == 1 || grid[x1][y2] == 1 || grid[x2][y1] == 1 || grid[x2][y2] == 1) {
            check = true;
        }

        return check;
    }

    public void repaint(GraphicsContext gc) {
        gc.save();
        gc.translate(x + ge.tileSize, y + ge.tileSize);
        gc.rotate(angle);

        gc.setFill(Color.BLUE);
        gc.drawImage(playerImage, -ge.tileSize, -ge.tileSize, ge.tileSize * 2, ge.tileSize * 2);

        gc.restore();
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
