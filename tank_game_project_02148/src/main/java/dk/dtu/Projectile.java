package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Projectile {

    private int x, y, angle;

    private int speed = 5;

    private long creationTime;
    private long LIFETIME = 3000;

    private boolean isActive = true;
    private GameEngine ge;

    public Projectile(GameEngine ge, int x, int y, int angle) {
        this.ge = ge;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.LIFETIME = ge.projectileLifespan;
        this.creationTime = System.currentTimeMillis();

    }

    public void update(Rectangle tankHitbox, Rectangle[][] grid) {
        double angleRadians = Math.toRadians(angle);

        if (!isActive) {
            return;
        }

        if (System.currentTimeMillis() - creationTime >= LIFETIME) {
            isActive = false;
            return;
        }

        x += Math.cos(angleRadians) * speed;
        y += Math.sin(angleRadians) * speed;

        int gridX = Math.floorDiv((int) x, ge.tileSize);
        int gridY = Math.floorDiv((int) y, ge.tileSize);

        bounceOffWall(grid, gridX, gridY);

        Circle projectileHitbox = new Circle(x, y, 5);
        if (projectileHitbox.intersects(tankHitbox.getBoundsInLocal())) {
            isActive = false;
        }

    }

    private void bounceOffWall(Rectangle[][] grid, int gridX, int gridY) {
        boolean hitVerticalWall = false;
        boolean hitHorizontalWall = false;

        if (gridY >= 0 && gridY < grid.length && gridX - 1 >= 0 && gridX + 1 < grid[gridY].length) {
            if (grid[gridY][gridX - 1] != null || grid[gridY][gridX + 1] != null) {
                hitVerticalWall = true;
            }
        }

        if (gridY >= 0 && gridY < grid.length && gridX >= 0 && gridX < grid[gridY].length) {
            if (grid[gridY - 1][gridX] != null || grid[gridY + 1][gridX] != null) {
                hitHorizontalWall = true;
            }
        }

        if (gridX < 0 || gridX >= grid[0].length || gridY < 0 || gridY >= grid.length) {
            isActive = false;
            return;
        }

        // Handle corner collisions (both vertical and horizontal)
        if (hitVerticalWall && hitHorizontalWall) {
            angle = (angle + 180) % 360; // Reverse completely
        } else if (hitVerticalWall) {
            angle = (angle > 90 && angle < 270) ? 180 - angle : 180 - angle;
        } else if (hitHorizontalWall) {
            angle = -angle;
        }

        angle = (angle + 360) % 360;

    }

    public void repaint(GraphicsContext gc) {
        if (!isActive) {
            return;
        } else {
            gc.setFill(Color.BLACK);
            gc.fillOval(x - 5, y - 5, 10, 10);
            //gc.setStroke(Color.RED);
            //gc.strokeLine(x, y, x + Math.cos(Math.toRadians(angle)) * 20, y + Math.sin(Math.toRadians(angle)) * 20);

        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void deactive() {
        this.isActive = false;
    }

}
