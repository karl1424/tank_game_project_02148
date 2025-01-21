package dk.dtu.gamestates;

import java.io.IOException;

import dk.dtu.connection.Client;
import dk.dtu.helper.Help;
import dk.dtu.inputs.MouseHandler;
import dk.dtu.main.GameEngine;
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

        hostHover = Help.checkHover(hostButtonY, mouseHandler, buttonX, buttonWidth, buttonHeight);
        joinHover = Help.checkHover(joinButtonY, mouseHandler, buttonX, buttonWidth, buttonHeight);
        quitHover = Help.checkHover(quitButtonY, mouseHandler, buttonX, buttonWidth, buttonHeight);

        if (mouseHandler.wasMouseClicked()) {
            if (hostHover) {
                ge.isHost = true;
                client.setPlayerNames();
                client.connectToLobbyHost(); // Host server
                try {
                    client.connectToLobby(); // Host connect to lobby
                } catch (InterruptedException | IOException e) {
                   e.printStackTrace();
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

    public void draw(GraphicsContext gc) {
        Help.drawText(gc, Color.GREEN, ge.screenWidth, "TANK GAME", 100, 150);

        Help.drawButton(gc, hostButtonY, hostHover, "HOST", buttonX, buttonWidth, buttonHeight);
        Help.drawButton(gc, joinButtonY, joinHover, "JOIN", buttonX, buttonWidth, buttonHeight);
        Help.drawButton(gc, quitButtonY, quitHover, "QUIT", buttonX, buttonWidth, buttonHeight);
    }

}
