package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Projectile {

    private double x;
    private double y;
    private double angle;

    private double speed = 10.0;

    private boolean isActive = true;

    private int wallHits = 0;

    private static final int MAX_WALL_HITS = 7;

    private int tileSize;


    public Projectile(double x, double y,double angle, int tileSize) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.tileSize = tileSize;
    }

    public void update(Rectangle tankHitbox, int[][] grid) {
        double angleRadians = Math.toRadians(angle);

        if(!isActive) {
            return;
        } 

            x += Math.cos(angleRadians)*speed;
            y += Math.sin(angleRadians)*speed;

            int gridX = (int)(x/tileSize);
            int gridY = (int)(y/tileSize);

            if(grid[gridY][gridX] == 1) {
                wallHits++;
            } 
            if (wallHits >= MAX_WALL_HITS) {
                isActive = false;
            } else {
                bounceOffWall(grid, gridX, gridY);
            }
        
        Rectangle projectileHitbox = new Rectangle(x - 5, y - 5, 10, 10);
            if (projectileHitbox.intersects(tankHitbox.getBoundsInLocal())) {
                isActive = false;
            }
        
    }

    
    private void bounceOffWall(int[][] grid, int gridX, int gridY) {
        boolean hitVerticalWall = false;
        boolean hitHorizontalWall = false;
    
        // Check for vertical wall collision
        if (grid[gridY][gridX - 1] == 1 || grid[gridY][gridX + 1] == 1) {
            hitVerticalWall = true;
        }
    
        // Check for horizontal wall collision
        if (grid[gridY - 1][gridX] == 1 || grid[gridY + 1][gridX] == 1) {
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
    }
    


    public void repaint(GraphicsContext gc) {
        if(!isActive) {
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
