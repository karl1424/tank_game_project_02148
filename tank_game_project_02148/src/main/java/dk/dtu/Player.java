package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Player {
    private InputHandler inputHandler;
    private GameEngine ge;

    private String playerName;

    private int x, y, angle, previousX, previousY;
    private int angleSpeed = 6;
    private int speed = 4;

    //Player 1 Start positions:
    public int p1X = 100;
    public int p1Y = 100;
    public int p1Angle = 0;
    //Player 2 Start positions:
    public int p2X = 200;
    public int p2Y = 200; 
    public int p2Angle = 180;

    private Image playerImage;
    private Circle hitbox;

    private long startCooldownTime, COOLDOWN;
    private boolean canShoot = true;
    public boolean shot = false;

    public boolean isActive = false;


    public Player(GameEngine ge, InputHandler inputHandler, String playerName) {
        this.ge = ge;
        this.playerName = playerName;
        this.inputHandler = inputHandler;
        if(playerName == "player1"){
            x = p1X;
            y = p1Y;
            angle = p1Angle;
        } else {
            x = p2X;
            y = p2Y;
            angle = p1Angle;
        }
        this.previousX = x;
        this.previousY = y;
        this.COOLDOWN = ge.projectileLifespan;
        init();
    }

    private void init() {
        playerImage = new Image("file:res/tank1.png");
        hitbox = new Circle(x + ge.tileSize, y + ge.tileSize, ge.tileSize - 2);
        hitbox.setFill(Color.TRANSPARENT);
        hitbox.setStroke(Color.RED);
        hitbox.setStrokeWidth(2);
    }

    public void update() {
        previousX = x;
        previousY = y;

        if (inputHandler.escapePressed) {
            Gamestate.state = Gamestate.MENU;
        }

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

        updateHitbox();

        if (inputHandler.shootPressed && canShoot) {
            canShoot = false;
            shot = true;
            startCooldown();
            // Start a timer to reset canShoot;
        }

        if (checkCollision()) {
            x = previousX;
            y = previousY;
            // angle = previousAngle;
            updateHitbox();
        }

        if(!isActive()) {
            return;
        }
    }

    private void updateHitbox() {
        hitbox.setCenterX(x + ge.tileSize);
        hitbox.setCenterY(y + ge.tileSize);
        // hitbox.setRotate(angle);
        // System.out.println(hitbox.getX() + ", " + hitbox.getY());
    }

    private boolean checkCollision() {
        Rectangle[][] grid = ge.grid.getGrid();

        int startX = (int) ((hitbox.getCenterX() - ge.tileSize) / ge.tileSize - 2);
        int startY = (int) ((hitbox.getCenterY() - ge.tileSize) / ge.tileSize - 2);
        int endX = (int) ((hitbox.getCenterX() + ge.tileSize) / ge.tileSize + 2);
        int endY = (int) ((hitbox.getCenterY() + ge.tileSize) / ge.tileSize + 2);

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
                if (rect != null && !Shape.intersect(hitbox, grid[j][i]).getBoundsInLocal().isEmpty()) {
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

        /*gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        gc.strokeOval(
                hitbox.getCenterX() - hitbox.getRadius(),
                hitbox.getCenterY() - hitbox.getRadius(),
                hitbox.getRadius() * 2,
                hitbox.getRadius() * 2);*/
    }

    public void startCooldown() {
        startCooldownTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startCooldownTime >= COOLDOWN) {
                canShoot = true;
                return;
            }
        }
    }

    public Circle getHitbox() {
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

    public boolean getShot() {
        return shot;
    }


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean b) {
        b = isActive;
    }
    
}
