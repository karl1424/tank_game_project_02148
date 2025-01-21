package dk.dtu.entities;

import dk.dtu.main.GameEngine;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Projectile {
    private GameEngine ge;

    private int x, y, angle;
    private int previousX, previousY;
    private int speed = 5;

    private int numberOfHits = 0;

    private boolean hitVerticalWall = false;
    private boolean hitHorizontalWall = false;

    private Rectangle projectileHitbox;

    private long creationTime;
    private long LIFETIME;

    private boolean isActive = true;

    public Projectile(GameEngine ge, int x, int y, int angle, int numberOfHits) {
        this.numberOfHits = numberOfHits;
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

    public void update(Circle tankHitbox, Rectangle[][] grid) {
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

        if (x != previousX || y != previousY) {
            bounceOffWall();
        }

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
                ge.stopGame();
            }
        }

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

    }

    private void updateAngle() {
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
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void deactive() {
        this.isActive = false;
    }

}
