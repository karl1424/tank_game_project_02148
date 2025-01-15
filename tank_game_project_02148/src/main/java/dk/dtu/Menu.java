package dk.dtu;

import java.io.IOException;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Menu {
    private GameEngine ge;
    private MouseHandler mouseHandler;
    private Client client;

    private boolean hostHover = false;
    private boolean joinHover = false;
    private boolean quitHover = false;

    private int buttonScale = 10;
    private int spacing = 10;
    private int buttonWidth;
    private int buttonHeight;
    private int buttonX;
    private int hostButtonY;
    private int joinButtonY;
    private int quitButtonY;

    public Menu(GameEngine ge, MouseHandler mouseHandler, Client client) {
        this.ge = ge;
        this.mouseHandler = mouseHandler;
        this.client = client;

        buttonWidth = buttonScale * ge.tileSize;
        buttonHeight = buttonScale / 2 * ge.tileSize;
        buttonX = (ge.screenWidth - buttonWidth) / 2;
        hostButtonY = (ge.screenHeight - buttonHeight) / 2;
        joinButtonY = hostButtonY + buttonHeight + spacing;
        quitButtonY = joinButtonY + buttonHeight + spacing;
    }

    public void update() {

        hostHover = checkHover(hostButtonY);
        joinHover = checkHover(joinButtonY);
        quitHover = checkHover(quitButtonY);

        if (mouseHandler.wasMouseClicked()) {
            if (hostHover) {
                ge.isHost = true;
                client.setPlayerNames();
                client.connectToServerHost();
                try {
                    client.connectToServer();
                } catch (InterruptedException | IOException e) {
                    System.out.println("SOMETHING WENT WRONG!");
                }
                
                client.initLobby();
                Gamestate.state = Gamestate.LOBBY;
            } else if (joinHover) {
                Gamestate.state = Gamestate.JOIN;
            } else if (quitHover) {
                System.exit(0);
            }
        }

    }

    private boolean checkHover(int buttonY) {
        return mouseHandler.getMouseX() >= buttonX
                && mouseHandler.getMouseX() <= buttonX + buttonWidth
                && mouseHandler.getMouseY() >= buttonY
                && mouseHandler.getMouseY() <= buttonY + buttonHeight;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.GREEN);
        gc.setFont(new javafx.scene.text.Font("Arial", 100));

        javafx.scene.text.Text tempText = new javafx.scene.text.Text("TANK GAME");
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getBoundsInLocal().getWidth();

        int x = (int) (ge.screenWidth - textWidth) / 2;
        int y = 150;

        gc.fillText("TANK GAME", x, y);

        drawButton(gc, hostButtonY, hostHover, "HOST");
        drawButton(gc, joinButtonY, joinHover, "JOIN");
        drawButton(gc, quitButtonY, quitHover, "QUIT");
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
