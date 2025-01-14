package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Projectile {

    private int x, y, angle;
    private int previousX;
    private int previousY;
    private int speed = 5;

    private int numberOfHits = 0;

    private boolean hitVerticalWall = false;
    private boolean hitHorizontalWall = false;

    private Rectangle projectileHitbox;

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
        this.previousX = x;
        this.previousY = y;
        this.projectileHitbox = new Rectangle(x - 4, y - 4, 8, 8);
    }

    public void update(Rectangle tankHitbox, Rectangle[][] grid) {
        if (!isActive) {
            return;
        }

        previousX = x;
        previousY = y;

        double angleRadians = Math.toRadians(angle);

        if (System.currentTimeMillis() - creationTime >= LIFETIME) {
            isActive = false;
            return;
        }

        x += (int) Math.round(Math.cos(angleRadians) * speed);
        y += (int) Math.round(Math.sin(angleRadians) * speed);

        updateHitbox();
        bounceOffWall();

        if (hitHorizontalWall || hitVerticalWall) {
            x = previousX;
            y = previousY;
            updateHitbox();
            updateAngle();
            hitHorizontalWall = false;
            hitVerticalWall = false;
        }

        if (numberOfHits > 0) {
            if (projectileHitbox.intersects(tankHitbox.getBoundsInLocal())) {
                isActive = false;
            }
        }

        //System.out.println(x + ", " + y + ", " + numberOfHits);

    }

    private void updateHitbox() {
        projectileHitbox.setX(x - 4);
        projectileHitbox.setY(y - 4);

    }

    private void bounceOffWall() {

        Rectangle[][] grid = ge.grid.getGrid();

        int startX = (int) (projectileHitbox.getX() / ge.tileSize - 1);
        int startY = (int) (projectileHitbox.getY() / ge.tileSize - 1);
        int endX = (int) ((projectileHitbox.getX() + 8) / ge.tileSize + 1);
        int endY = (int) ((projectileHitbox.getY() + 8) / ge.tileSize + 1);

        double maxHeight = -1.;
        double maxWidth = -1.;

        for (int i = startX; i <= endX; i++) {
            for (int j = startY; j <= endY; j++) {
                if (i < 0 || i >= 46 || j < 0 || j >= 36) {
                    continue;
                }
                Rectangle rect = grid[j][i];
                if (rect != null) {
                    Shape intersection = Shape.intersect(rect, projectileHitbox);
                    double width = intersection.getBoundsInParent().getWidth();
                    double height = intersection.getBoundsInParent().getHeight();

                    if (intersection.getBoundsInLocal().getWidth() != -1) {
                        maxHeight = maxHeight < height ? height : maxHeight;
                        maxWidth = maxWidth < width ? width : maxWidth;
                    }

                    /*
                     * if (intersection.getBoundsInLocal().getWidth() != -1) {
                     * if (intersection.getBoundsInParent().getWidth() <
                     * intersection.getBoundsInParent()
                     * .getHeight()) {
                     * hitVerticalWall = true;
                     * } else if (intersection.getBoundsInParent().getWidth() >
                     * intersection.getBoundsInParent()
                     * .getHeight()) {
                     * hitHorizontalWall = true;
                     * } else {
                     * hitHorizontalWall = true;
                     * hitVerticalWall = true;
                     * }
                     * }
                     */

                }
            }
        }

        if (maxHeight != -1 && maxWidth != -1) {
            if (maxHeight >= maxWidth) {
                hitVerticalWall = true;
            } else if (maxHeight < maxWidth) {
                hitHorizontalWall = true;
            }
            numberOfHits++;
        }

        /*
         * if (gridY >= 0 && gridY < grid.length && gridX - 1 >= 0 && gridX + 1 <
         * grid[gridY].length) {
         * if (grid[gridY][gridX - 1] != null || grid[gridY][gridX + 1] != null) {
         * hitVerticalWall = true;
         * }
         * }
         * 
         * if (gridY >= 0 && gridY < grid.length && gridX >= 0 && gridX <
         * grid[gridY].length) {
         * if (grid[gridY - 1][gridX] != null || grid[gridY + 1][gridX] != null) {
         * hitHorizontalWall = true;
         * }
         * }
         * 
         * if (gridX < 0 || gridX >= grid[0].length || gridY < 0 || gridY >=
         * grid.length) {
         * isActive = false;
         * return;
         * }
         */

        // Handle corner collisions (both vertical and horizontal)

    }

    private void updateAngle() {
        /*
         * if (hitVerticalWall && hitHorizontalWall) {
         * angle = (angle + 180) % 360; // Reverse completely
         * } else
         */
        if (hitVerticalWall) {
            angle = 180 - angle;
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
            // gc.setStroke(Color.RED);
            // gc.strokeLine(x, y, x + Math.cos(Math.toRadians(angle)) * 20, y +
            // Math.sin(Math.toRadians(angle)) * 20);

        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void deactive() {
        this.isActive = false;
    }

}
