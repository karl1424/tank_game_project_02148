package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Grid {
    private GameEngine ge;

    private int[][] grid;

    public Grid(GameEngine ge) {
        this.ge = ge;

        grid = new int[36][48];
        initGrid();
    }

    private void initGrid() {
        int rows = 36;
        int cols = 48;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i == 0 || i == rows - 1 || j == 0 || j == cols - 1 || i == 4) {
                    grid[i][j] = 1;
                } else {
                    grid[i][j] = 0;
                }
            }
        }
    }

    public void drawGrid(GraphicsContext gc) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == 1) {
                    gc.setFill(Color.GRAY);
                    gc.fillRect(j * ge.tileSize, i * ge.tileSize, ge.tileSize, ge.tileSize);
                }
            }
        }
    }

    public int[][] getGrid() {
        return grid;
    }
}
