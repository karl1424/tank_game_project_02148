package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Menu {
    private GameEngine ge;
    private InputHandler inputHandler;

    public Menu(GameEngine ge, InputHandler inputHandler) {
        this.ge = ge;
        this.inputHandler = inputHandler;
    }

    public void update() {
        if (inputHandler.enterPressed) {
            Gamestate.state = Gamestate.PLAYING;
        }
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.fillRect(0, 0, ge.tileSize, ge.tileSize);
    }
}
