package dk.dtu.gamestates;

import dk.dtu.connection.Client;
import dk.dtu.helper.Help;
import dk.dtu.inputs.MouseHandler;
import dk.dtu.main.GameEngine;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Lobby {
    private GameEngine ge;
    private MouseHandler mouseHandler;
    private Client client;

    private boolean startHover = false;
    private boolean goBackHover = false;

    private boolean missingPlayer = false;

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
            startHover = Help.checkHover(startButtonY, mouseHandler, buttonX, buttonWidth, buttonHeight);
        }
        goBackHover = Help.checkHover(goBackButtonY, mouseHandler, buttonX, buttonWidth, buttonHeight);

        if (mouseHandler.wasMouseClicked()) {
            if (ge.isHost) {
                if (startHover) {
                    if (client.readyToStart()) {
                        new Thread(() -> client.recieveShots()).start();
                        new Thread(() -> client.recieveGameOver()).start();
                        client.startPos = true;
                        client.startGame = true;
                        client.sendStart();
                        missingPlayer = false;
                        Gamestate.state = Gamestate.PLAYING;
                    } else {
                        missingPlayer = true;
                        System.out.println("MISSING PLAYER");
                    }
                }
            }
            if (goBackHover) {
                // RESET LOBBY!!!
                if (ge.isHost) {
                    client.sendLeft();
                    client.closeLobby();
                    missingPlayer = false;
                } else {
                    client.sendLeftPlayer2();
                    client.sendNoGameStart();
                }
                ge.isHost = false;
                Gamestate.state = Gamestate.MENU;
            }
        }
    }

    public void draw(GraphicsContext gc) {
        Help.drawText(gc, Color.GREEN, ge.screenWidth, "LOBBY ID: " + (ge.online ? client.getLobbyID() : ""), 50, 150);
        Help.drawText(gc, Color.RED, ge.screenWidth, (missingPlayer ? "MISSING PLAYER 2" : ""), 50, 250);

        Help.drawButton(gc, startButtonY, startHover, "START", buttonX, buttonWidth, buttonHeight);
        Help.drawButton(gc, goBackButtonY, goBackHover, "GO BACK", buttonX, buttonWidth, buttonHeight);
    }

}
