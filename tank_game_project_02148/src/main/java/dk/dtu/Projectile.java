package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Projectile {

    private double x;
    private double y;
    private double angle;

    private double speed = 5.0;

    private boolean isActive = true;

    private int wallHits = 0;

    private static final int MAX_WALL_HITS = 7;

    private int tileSize;

    public Projectile(double x, double y, double angle, int tileSize) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.tileSize = tileSize;
    }

    public void update(Rectangle tankHitbox, Rectangle[][] grid) {
        double angleRadians = Math.toRadians(angle);

        if (!isActive) {
            return;
        }

        x += Math.cos(angleRadians) * speed;
        y += Math.sin(angleRadians) * speed;

        int gridX = (int) (x / tileSize);
        int gridY = (int) (y / tileSize);

        if (wallHits >= MAX_WALL_HITS) {
            isActive = false;
        } else {
            bounceOffWall(grid, gridX, gridY);
        }

        Circle projectileHitbox = new Circle(x - 5, y - 5, 5);

        Shape tankHitboxShape = (Shape) tankHitbox;
        Shape projectileHitboxShape = (Shape) projectileHitbox;
        Shape intersection = Shape.intersect(tankHitboxShape, projectileHitboxShape);

        if (intersection.getBoundsInLocal().getWidth() > 0) {
            isActive = false;
        }

    }

    private void bounceOffWall(Rectangle[][] grid, int gridX, int gridY) {
        boolean hitVerticalWall = false;
        boolean hitHorizontalWall = false;

        // Check for vertical wall collision
        if (grid[gridY][gridX - 1] != null || grid[gridY][gridX + 1] != null) {
            hitVerticalWall = true;
        }

        // Check for horizontal wall collision
        if (grid[gridY - 1][gridX] != null || grid[gridY + 1][gridX] != null) {
            hitHorizontalWall = true;
        }

        // Reverse angle based on wall type
        if (hitVerticalWall && hitHorizontalWall) {
            angle = (angle + 180) % 360; // Reverse completely
        } else if (hitVerticalWall) {
            angle = 180 - angle; // Bounce off vertical wall
        } else if (hitHorizontalWall) {
            angle = -angle; // Bounce off horizontal wall
        }

        if (hitVerticalWall || hitHorizontalWall) {
            wallHits++;
        }
    }

    public void repaint(GraphicsContext gc) {
        if (!isActive) {
            return;
        } else {
            gc.setFill(Color.BLACK);
            gc.fillOval(x - 5, y - 5, 10, 10);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void deactive() {
        this.isActive = false;
    }

}
