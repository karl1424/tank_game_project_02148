package dk.dtu;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameOver {
    private GameEngine ge;
    private InputHandler inputHandler;
    private Client client;

    public GameOver(GameEngine ge, InputHandler inputHandler, Client client) {
        this.ge = ge;
        this.inputHandler = inputHandler;
        this.client = client;
    }

    public void update() {

    }

    public void draw(GraphicsContext gc) {
        
        gc.setFill(Color.LIGHTGRAY);
        gc.setGlobalAlpha(0.3);

        //gc.fillRect(0, 0, ge.screenWidth, ge.screenHeight);

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
    }
}
