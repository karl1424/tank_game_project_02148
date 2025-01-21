package dk.dtu.helper;

import dk.dtu.inputs.MouseHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Help {

    public static boolean checkHover(int buttonY, MouseHandler mouseHandler, int buttonX, int buttonWidth,
            int buttonHeight) {
        return mouseHandler.getMouseX() >= buttonX
                && mouseHandler.getMouseX() <= buttonX + buttonWidth
                && mouseHandler.getMouseY() >= buttonY
                && mouseHandler.getMouseY() <= buttonY + buttonHeight;
    }

    public static void drawButton(GraphicsContext gc, int buttonY, boolean hover, String text, int buttonX,
            int buttonWidth, int buttonHeight) {
        if (hover) {
            gc.setFill(Color.DIMGRAY);
        } else {
            gc.setFill(Color.GRAY);
        }
        gc.fillRect(buttonX, buttonY, buttonWidth, buttonHeight);

        gc.setFill(Color.BLACK);
        gc.setFont(new javafx.scene.text.Font("Arial", 24));

        javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getBoundsInLocal().getWidth();
        double textHeight = tempText.getBoundsInLocal().getHeight();

        double textX = buttonX + (buttonWidth - textWidth) / 2;
        double textY = buttonY + (buttonHeight + textHeight / 4) / 2;

        gc.fillText(text, textX, textY);
    }

    public static void drawText(GraphicsContext gc, Color color, int screenWidth, String text, int size, int y) {
        gc.setFill(color);
        gc.setFont(new javafx.scene.text.Font("Arial", size));

        javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getBoundsInLocal().getWidth();

        int x = (int) (screenWidth - textWidth) / 2;

        gc.fillText(text, x, y);
    }
}
