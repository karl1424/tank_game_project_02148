package dk.dtu;

import java.io.IOException;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

public class Client {
    private Player player;
    private String playername = "Player1";
    private InputHandler inputHandler;
    private Boolean offlineTest = true;
    private Space server;
    private Space lobby;
    private GameEngine ge;

    private double prevX;
    private double prevY;

    public Client(GameEngine ge, InputHandler inputHandler) {
        this.ge = ge;
        this.inputHandler = inputHandler;

        int port = 31145;
        String host = "10.134.17.47";
        int lobbyID = 0;

        // Connect to server
        if (!offlineTest) {
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
                lobby.put(playername, player.getX(), player.getY());
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        prevX = player.getX();
        prevY = player.getY();
    }

    public void recieveCoordinates() {

        try {
            Object[] coordinates = lobby.getp(new FormalField(String.class), new ActualField("Player1"),
                    new FormalField(Double.class), new FormalField(Double.class));
            if (coordinates != null) {
                System.out.println(coordinates[1] + ": " + "x = " + coordinates[2].toString() + ", y = "
                        + coordinates[3].toString());
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
