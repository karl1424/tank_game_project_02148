package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Player {
    private InputHandler inputHandler;
    private GameEngine ge;

    private String playerName;
    private int x = 100;
    private int y = 100;

    private int angle = 0;
    private int angleSpeed = 6;
    private int speed = 5;

    private Image playerImage;
    private Rectangle hitbox;

    private int previousX;
    private int previousY;

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
            x += (int) Math.round(Math.cos(angleRadians) * speed);
            y += (int) Math.round(Math.sin(angleRadians) * speed);
        }
        if (inputHandler.downPressed) {
            x -= (int) Math.round(Math.cos(angleRadians) * speed);
            y -= (int) Math.round(Math.sin(angleRadians) * speed);
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
            //angle = previousAngle;
            updateHitbox();
        }
    }

    private void updateHitbox() {
        hitbox.setX(x);
        hitbox.setY(y);
        hitbox.setRotate(angle);
        // System.out.println(hitbox.getX() + ", " + hitbox.getY());
    }

    private boolean checkCollision() {
        Rectangle[][] grid = ge.grid.getGrid();

        int startX = (int) (hitbox.getX() / ge.tileSize - 2);
        int startY = (int) (hitbox.getY() / ge.tileSize - 2);
        int endX = (int) ((hitbox.getX() + 2 * ge.tileSize) / ge.tileSize + 2);
        int endY = (int) ((hitbox.getY() + 2 * ge.tileSize) / ge.tileSize + 2);

        // System.out.println(startX + ", " + startY + ", " + endX + ", " + endY);

        /*
         * for (int i = startX; i <= endX; i++) {
         * for (int j = startY; j <= endY; j++) {
         * if (i < 0 || i >= 46 || j < 0 || j >= 36) {
         * continue;
         * }
         * if (grid[j][i] != null) {
         * //System.out.println(i + ", " + j);
         * Shape tileShape = (Shape) grid[j][i];
         * Shape intersection = Shape.intersect(hitboxShape, tileShape);
         * 
         * if (intersection.getBoundsInLocal().getWidth() != -1) {
         * return true;
         * }
         * }
         * }
         * }
         */

        for (int i = startX; i <= endX; i++) {
            for (int j = startY; j <= endY; j++) {
                if (i < 0 || i >= 46 || j < 0 || j >= 36) {
                    continue;
                }
                Rectangle rect = grid[j][i];
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
