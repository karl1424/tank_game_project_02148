package dk.dtu.gamestates;

import dk.dtu.connection.Client;
import dk.dtu.helper.Help;
import dk.dtu.inputs.MouseHandler;
import dk.dtu.main.GameEngine;
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
        goBackHover = Help.checkHover(goBackButtonY, mouseHandler, buttonX, buttonWidth, buttonHeight);

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
        gc.setGlobalAlpha(0.1);

        gc.fillRect(0, 0, ge.screenWidth, ge.screenHeight);

        gc.setGlobalAlpha(1.0);

        Help.drawText(gc, client.winner ? Color.GREEN : Color.RED, ge.screenWidth, client.winner ? "VICTORY" : "GAME OVER", 100);

        Help.drawButton(gc, goBackButtonY, goBackHover, "GO BACK", buttonX, buttonWidth, buttonHeight);
    }

}
