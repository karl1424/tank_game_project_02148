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

    private boolean canShoot = true;
    private long startCooldownTime;
    private long COOLDOWN; // Eaqul to projectile LIFETIME

    public boolean shot = false;

    public Player(GameEngine ge, InputHandler inputHandler, String playerName) {
        this.ge = ge;
        this.playerName = playerName;
        this.inputHandler = inputHandler;
        this.previousX = x;
        this.previousY = y;
        this.COOLDOWN = ge.projectileLifespan;
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

        if (inputHandler.shootPressed && canShoot) {
            canShoot = false;
            shot = true;
            startCooldown();
            //Start a timer to reset canShoot;
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
    }

    public void startCooldown(){
        startCooldownTime = System.currentTimeMillis();
        while(true){
            if (System.currentTimeMillis() - startCooldownTime >= COOLDOWN) {
                canShoot = true;
                return;
            }
        }
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getAngle() {
        return angle;
    }

    public boolean getShot(){
        return shot;
    }
}
