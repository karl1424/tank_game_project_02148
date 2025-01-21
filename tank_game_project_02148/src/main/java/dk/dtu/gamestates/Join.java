package dk.dtu.gamestates;

import java.io.IOException;

import dk.dtu.connection.Client;
import dk.dtu.helper.Help;
import dk.dtu.inputs.MouseHandler;
import dk.dtu.main.GameEngine;
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

    private String errorMessage = "";

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
        joinHover = Help.checkHover(joinButtonY, mouseHandler, buttonX, buttonWidth, buttonHeight);
        goBackHover = Help.checkHover(goBackButtonY, mouseHandler, buttonX, buttonWidth, buttonHeight);

        if (mouseHandler.wasMouseClicked()) {
            if (mouseHandler.getMouseX() >= textBoxX &&
                    mouseHandler.getMouseX() <= textBoxX + textBoxWidth &&
                    mouseHandler.getMouseY() >= textBoxY &&
                    mouseHandler.getMouseY() <= textBoxY + textBoxHeight) {
                textBoxFocused = true;
            } else {
                textBoxFocused = false;
            }

            if (joinHover) {
                if (textBoxContent.isEmpty()) {
                    return;
                }

                client.setPlayerNames();

                client.setLobbyID(Integer.parseInt(textBoxContent));

                client.initLobby();
                try {
                    client.connectToLobby(); // Join lobby
                    errorMessage = "";
                } catch (InterruptedException | IOException e) {
                    errorMessage = "CANNOT FIND THE LOBBY";
                    textBoxContent = "";
                    return;
                } catch (NullPointerException e) {
                    errorMessage = "LOBBY IS FULL";
                    textBoxContent = "";
                    return;
                }

                new Thread(() -> client.recieveGameStart()).start();
                new Thread(() -> client.recieveLeft()).start();

                Gamestate.state = Gamestate.LOBBY;
                textBoxContent = "";

            }

            if (goBackHover) {
                errorMessage = "";
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
            } else if (Character.isDigit(character.charAt(0))) {
                textBoxContent += character;
            }
        }
    }

    public void draw(GraphicsContext gc) {
        Help.drawText(gc, Color.RED, ge.screenWidth, errorMessage, 50, 150);

        drawTextBox(gc);

        Help.drawButton(gc, joinButtonY, joinHover, "JOIN LOBBY", buttonX, buttonWidth, buttonHeight);
        Help.drawButton(gc, goBackButtonY, goBackHover, "GO BACK", buttonX, buttonWidth, buttonHeight);
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
