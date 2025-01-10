package dk.dtu;

import java.io.IOException;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Client {
    private Player player;
    private String playername = "Player2";
    private InputHandler inputHandler;
    private Boolean offlineTest = false;
    private Space server;
    private Space lobby;
    private GameEngine ge;

    private double prevX;
    private double prevY;

    Object[] coordinates;
    private Image opponentImage;
    private double opponentPrevX;
    private double opponentPrevY;
    private double opponentPrevAngle;


    public Client(GameEngine ge, InputHandler inputHandler) {
        this.ge = ge;
        this.inputHandler = inputHandler;

        int port = 31145;
        String host = "localhost";
        int lobbyID = 0;

        opponentImage = new Image("file:res/tank1.png");

        // Connect to server
        if (false) {
            try {
                String uri = "tcp://" + host + ":" + port + "/lobbyRequests?conn";
                server = new RemoteSpace(uri);
                server.put("host", 1);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        // Connect to lobby
        if (!offlineTest) {
            try {
                String uri = "tcp://" + host + ":" + port + "/" + lobbyID + "?conn";
                lobby = new RemoteSpace(uri);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        createPlayer();

    }

    private void createPlayer() {
        player = new Player(ge, inputHandler, playername);
        prevX = player.getX();
        prevY = player.getY();
    }

    public Player getPlayer() {
        return player;
    }

    public void sendCoordinate() {
        if (prevX != player.getX() || prevY != player.getY()) {
            try {
                lobby.put(playername, player.getX(), player.getY(), player.getAngle());
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        prevX = player.getX();
        prevY = player.getY();
    }

    public void recieveCoordinates() {
        try {
            coordinates = lobby.getp(new FormalField(String.class), new ActualField("Player1"),
                    new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class));
            if (coordinates != null) {
                System.out.println(coordinates[1] + ": " + "x = " + coordinates[2].toString() + ", y = "
                        + coordinates[3].toString() + ", angle = "
                        + coordinates[4].toString());
                opponentPrevX = (double) coordinates[2];
                opponentPrevY = (double) coordinates[3];
                opponentPrevAngle = (double) coordinates[4];
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void drawOpponent(GraphicsContext gc) {
        if (coordinates != null) {
            gc.save();
            gc.translate((double) coordinates[2] + ge.tileSize, (double) coordinates[3] + ge.tileSize);
            gc.rotate((double) coordinates[4]);

            gc.setFill(Color.BLUE);
            gc.drawImage(opponentImage, -ge.tileSize, -ge.tileSize, ge.tileSize * 2, ge.tileSize * 2);

            gc.restore();
        } else {
            gc.save();
            gc.translate(opponentPrevX + ge.tileSize, (double) opponentPrevY + ge.tileSize);
            gc.rotate(opponentPrevAngle);

            gc.setFill(Color.BLUE);
            gc.drawImage(opponentImage, -ge.tileSize, -ge.tileSize, ge.tileSize * 2, ge.tileSize * 2);

            gc.restore();
        }

    }
}
