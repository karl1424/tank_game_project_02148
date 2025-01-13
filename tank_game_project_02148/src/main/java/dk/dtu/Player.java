package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

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

    private Projectile projectile;

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

        if (inputHandler.shootPressed && projectile == null) {
            double offset = 1.6 * ge.tileSize;

            double projectileX = x + Math.cos(angleRadians) * offset + ge.tileSize;
            double projectileY = y + Math.sin(angleRadians) * offset + ge.tileSize;

            projectile = new Projectile(projectileX, projectileY, angle, ge.tileSize);
        }

        if (projectile != null) {
            projectile.update(hitbox, ge.grid.getGrid());
            if (!projectile.isActive()) {
                projectile = null;
            }
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
        Rectangle[][] grid = ge.grid.getGrid();

        int buffer = 30;
        int startX = Math.max(0, (int) ((hitbox.getX() - buffer) / ge.tileSize));
        int endX = Math.min(grid[0].length, (int) ((hitbox.getX() + hitbox.getWidth()
                + buffer) / ge.tileSize));
        int startY = Math.max(0, (int) ((hitbox.getY() - buffer) / ge.tileSize));
        int endY = Math.min(grid.length, (int) ((hitbox.getY() + hitbox.getHeight() +
                buffer) / ge.tileSize));
        for (int row = startY; row < endY; row++) {
            for (int col = startX; col < endX; col++) {
                Rectangle rect = grid[row][col];
                if (rect != null && hitbox.intersects(rect.getBoundsInLocal())) {
                    return true;
                }
            }
        }
        return false;

    }

    public void repaint(GraphicsContext gc) {
        gc.save();
        gc.translate(x + ge.tileSize, y + ge.tileSize);
        gc.rotate(angle);

        gc.setFill(Color.BLUE);
        gc.drawImage(playerImage, -ge.tileSize, -ge.tileSize, ge.tileSize * 2, ge.tileSize * 2);

        gc.restore();

        if (projectile != null) {
            projectile.repaint(gc);
        }
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

    public double getAngle() {
        return angle;
    }
}
