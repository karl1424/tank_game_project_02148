package dk.dtu;

import org.jspace.ActualField;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Lobby {
    private GameEngine ge;
    private MouseHandler mouseHandler;
    private Client client;

    private boolean startHover = false;
    private boolean goBackHover = false;

    private int buttonScale = 10;
    private int spacing = 10;
    private int buttonWidth;
    private int buttonHeight;
    private int buttonX;
    private int startButtonY;
    private int goBackButtonY;

    public Lobby(GameEngine ge, MouseHandler mouseHandler, Client client) {
        this.ge = ge;
        this.mouseHandler = mouseHandler;
        this.client = client;

        buttonWidth = buttonScale * ge.tileSize;
        buttonHeight = buttonScale / 2 * ge.tileSize;
        buttonX = (ge.screenWidth - buttonWidth) / 2;
        startButtonY = (ge.screenHeight - buttonHeight) / 2 + buttonHeight + spacing;
        goBackButtonY = startButtonY + buttonHeight + spacing;
    }

    public void update() {
        if (ge.isHost) {
            startHover = checkHover(startButtonY);
        }
        goBackHover = checkHover(goBackButtonY);

        if (mouseHandler.wasMouseClicked()) {
            if (ge.isHost) {
                if (startHover) {
                    if (ge.online) {
                        new Thread(() -> client.recieveShots()).start();
                        new Thread(() -> client.recieveGameOver()).start();
                    }
                    client.startPos = true;
                    client.startGame = true;
                    client.sendStart();
                    Gamestate.state = Gamestate.PLAYING;
                    
                }
            }
            if (goBackHover) {
                // RESET LOBBY!!!
                if (ge.isHost) {
                    client.sendLeft();
                    client.closeLobby();
                } else {
                    try {
                        client.getOccupied();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }

                ge.isHost = false;
                Gamestate.state = Gamestate.MENU;
            }
        }
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.GREEN);
        gc.setFont(new javafx.scene.text.Font("Arial", 50));

        javafx.scene.text.Text tempText = new javafx.scene.text.Text(
                "LOBBY ID: " + (ge.online ? client.getLobbyID() : ""));
        tempText.setFont(gc.getFont());
        double textWidth = tempText.getBoundsInLocal().getWidth();

        int x = (int) (ge.screenWidth - textWidth) / 2;
        int y = 150;

        gc.fillText("LOBBY ID: " + (ge.online ? client.getLobbyID() : ""), x, y);

        drawButton(gc, startButtonY, startHover, "START");
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
