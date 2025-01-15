package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameOver {
    private GameEngine ge;
    private MouseHandler mouseHandler;
    private Client client;

    private boolean goBackHover = false;

    private int buttonScale = 10;
    private int spacing = 10;
    private int buttonWidth;
    private int buttonHeight;
    private int buttonX;
    private int goBackButtonY;

    public GameOver(GameEngine ge, MouseHandler mouseHandler, Client client) {
        this.ge = ge;
        this.mouseHandler = mouseHandler;
        this.client = client;

        buttonWidth = buttonScale * ge.tileSize;
        buttonHeight = buttonScale / 2 * ge.tileSize;
        buttonX = (ge.screenWidth - buttonWidth) / 2;
        goBackButtonY = (ge.screenHeight - buttonHeight) / 2 + 2 * buttonHeight + 2 * spacing;
    }

    
    public void update() {
        goBackHover = checkHover(goBackButtonY);

        if(mouseHandler.wasMouseClicked()) {
            if(goBackHover) {
                ge.isHost = false;
                client.winner = false;
                Gamestate.state = Gamestate.MENU;
            }
        }
    }


    public void draw(GraphicsContext gc) {

        gc.setFill(Color.DIMGRAY);
        gc.setGlobalAlpha(0.5);

        // gc.fillRect(0, 0, ge.screenWidth, ge.screenHeight);

        gc.setGlobalAlpha(1.0);

        gc.setFill(client.winner ? Color.GREEN : Color.RED);
        gc.setFont(new javafx.scene.text.Font("Arial", 100));

        javafx.scene.text.Text tempText = new javafx.scene.text.Text(client.winner ? "VICTORY" : "GAME OVER");
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getBoundsInLocal().getWidth();
        double textHeight = tempText.getBoundsInLocal().getHeight();

        int x = (int) (ge.screenWidth - textWidth) / 2;
        int y = (int) (ge.screenHeight - textHeight) / 2;

        gc.fillText(client.winner ? "VICTORY" : "GAME OVER", x, y);
        drawButton(gc, goBackButtonY, goBackHover, "GO BACK");
    }

    private boolean checkHover(int buttonY) {
        return mouseHandler.getMouseX() >= buttonX
                && mouseHandler.getMouseX() <= buttonX + buttonWidth
                && mouseHandler.getMouseY() >= buttonY
                && mouseHandler.getMouseY() <= buttonY + buttonHeight;
    }

    private void drawButton(GraphicsContext gc, int buttonY, boolean hover, String text) {
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

}
