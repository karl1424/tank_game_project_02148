package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class Join {
    private GameEngine ge;
    private MouseHandler mouseHandler;
    private Client client;

    private boolean joinHover = false;
    private boolean goBackHover = false;

    private boolean textBoxFocused = false;
    private String textBoxContent = "";

    private int buttonScale = 10;
    private int spacing = 10;
    private int buttonWidth;
    private int buttonHeight;
    private int buttonX;
    private int joinButtonY;
    private int goBackButtonY;

    private int textBoxX;
    private int textBoxY;
    private int textBoxWidth;
    private int textBoxHeight;

    public Join(GameEngine ge, MouseHandler mouseHandler, Client client) {
        this.ge = ge;
        this.mouseHandler = mouseHandler;
        this.client = client;

        buttonWidth = buttonScale * ge.tileSize;
        buttonHeight = buttonScale / 2 * ge.tileSize;
        buttonX = (ge.screenWidth - buttonWidth) / 2;
        joinButtonY = (ge.screenHeight - buttonHeight) / 2 + buttonHeight + spacing;
        goBackButtonY = joinButtonY + buttonHeight + spacing;

        textBoxWidth = buttonWidth;
        textBoxHeight = buttonHeight / 2;
        textBoxX = buttonX;
        textBoxY = joinButtonY - textBoxHeight - spacing;
    }

    public void update() {
        // Opdater musens hover til knapperne
        joinHover = checkHover(joinButtonY);
        goBackHover = checkHover(goBackButtonY);

        if (mouseHandler.isClicked()) {
            if (mouseHandler.mouseX >= textBoxX &&
                mouseHandler.mouseX <= textBoxX + textBoxWidth &&
                mouseHandler.mouseY >= textBoxY &&
                mouseHandler.mouseY <= textBoxY + textBoxHeight) {
                textBoxFocused = true;
            } else {
                textBoxFocused = false;
            }

            if (joinHover) {
                client.setPlayerNames();
                client.setLobbyID(Integer.parseInt(textBoxContent));
                
                if (ge.online) {
                    client.connectToServer();
                }
                textBoxContent = "";
                if (ge.online) {
                    new Thread(() -> {
                        client.recieveShots();
                    }).start();
                }
                if (ge.online) {
                    new Thread(() -> {
                        client.recieveGameOver();
                    }).start();
                }
                Gamestate.state = Gamestate.PLAYING;
            }

            if (goBackHover) {
                textBoxContent = "";
                Gamestate.state = Gamestate.MENU;
            }
        }
    }

    public void handleInput(KeyEvent event) {
        if (textBoxFocused) {
            String character = event.getCharacter();
            if ("\b".equals(character) && textBoxContent.length() > 0) {
                textBoxContent = textBoxContent.substring(0, textBoxContent.length() - 1);
            } else if (Character.isLetter(character.charAt(0)) || Character.isDigit(character.charAt(0))) {
                textBoxContent += character;
            }
        }
    }

    public void draw(GraphicsContext gc) {
        drawTextBox(gc);
        drawButton(gc, joinButtonY, joinHover, "JOIN LOBBY");
        drawButton(gc, goBackButtonY, goBackHover, "GO BACK");
    }

    private boolean checkHover(int buttonY) {
        return mouseHandler.mouseX >= buttonX &&
               mouseHandler.mouseX <= buttonX + buttonWidth &&
               mouseHandler.mouseY >= buttonY &&
               mouseHandler.mouseY <= buttonY + buttonHeight;
    }

    private void drawButton(GraphicsContext gc, int buttonY, boolean hover, String text) {
        gc.setFill(hover ? Color.DIMGRAY : Color.GRAY);
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

    private void drawTextBox(GraphicsContext gc) {
        gc.setFill(textBoxFocused ? Color.LIGHTGREY : Color.WHITE);
        gc.fillRect(textBoxX, textBoxY, textBoxWidth, textBoxHeight);

        gc.setStroke(textBoxFocused ? Color.BLACK : Color.GRAY);
        gc.strokeRect(textBoxX, textBoxY, textBoxWidth, textBoxHeight);

        gc.setFill(Color.BLACK);
        gc.setFont(new javafx.scene.text.Font("Arial", 18));
        gc.fillText(textBoxContent, textBoxX + 5, textBoxY + textBoxHeight / 2 + 5);
    }
}
