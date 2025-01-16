package dk.dtu.connection;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import dk.dtu.entities.Player;
import dk.dtu.entities.Projectile;
import dk.dtu.gamestates.Gamestate;
import dk.dtu.inputs.InputHandler;
import dk.dtu.main.GameEngine;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Client {
    private Player player;
    private GameEngine ge;
    private InputHandler inputHandler;

    private Space server, lobbySend, lobbyGet, lobbyShots;;

    private String playername;

    private int prevX, prevY, prevAngle, opponentPrevX, opponentPrevY, opponentPrevAngle;
    private int opponentStartPosX, opponentStartPosY, opponentStartAngle;

    public boolean startPos = true;
    public boolean startGame;

    public boolean winner = false;
    public boolean recieveStartBool = false;
    public boolean recieveLeftBool = false;

    private int port, lobbyID;
    private String host;

    Object[] coordinates;
    ArrayList<Projectile> projectileList = new ArrayList<>();
    private Image opponentImage;

    public Client(GameEngine ge, InputHandler inputHandler) {
        this.ge = ge;
        this.inputHandler = inputHandler;

        this.port = 31145;
        this.host = ge.IP;
        // this.lobbyID = 0;

    }

    public void setPlayerNames() {

        if (ge.isHost) {
            playername = "player1";
            opponentImage = new Image("file:res/tank2.png");
        } else {
            playername = "player2";
            opponentImage = new Image("file:res/tank1.png");
        }

        createPlayer();

        // Player 2 starting possitions:
        if (playername == "player1") {
            opponentStartPosX = player.p2X;
            opponentStartPosY = player.p2Y;
            opponentStartAngle = player.p2Angle;
        } else {
            opponentStartPosX = player.p1X;
            opponentStartPosY = player.p1Y;
            opponentStartAngle = player.p1Angle;
        }

    }

    public void connectToServerHost() {
        if (ge.online) {
            // Connect to server
            if (ge.isHost) {
                System.out.println("Attempting to create lobby");
                try {
                    String uri = "tcp://" + host + ":" + port + "/lobbyRequests?conn";
                    server = new RemoteSpace(uri);
                    server.put("host");
                    Object[] lobby = server.get(new ActualField("lobby"), new FormalField(Integer.class));
                    lobbyID = (int) lobby[1];
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    public void initLobby() {
        String uriS = "tcp://" + host + ":" + port + "/" + lobbyID + "shots" + "?conn";
        try {
            lobbyShots = new RemoteSpace(uriS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectToServer() throws InterruptedException, UnknownHostException, IOException {
        // Connect to lobby
        if (ge.online) {
            System.out.println(lobbyID);
            String uri1 = "tcp://" + host + ":" + port + "/" + lobbyID + "player1" + "?conn";
            String uri2 = "tcp://" + host + ":" + port + "/" + lobbyID + "player2" + "?conn";
            lobbySend = ge.isHost ? new RemoteSpace(uri1) : new RemoteSpace(uri2);
            lobbyGet = ge.isHost ? new RemoteSpace(uri2) : new RemoteSpace(uri1);
            lobbySend.put("Connected");
            lobbySend.get(new ActualField("Connected"));
        }
    }

    private void createPlayer() {
        player = new Player(ge, inputHandler, playername);
        prevX = player.getX();
        prevY = player.getY();
        prevAngle = player.getAngle();
    }

    public Player getPlayer() {
        return player;
    }

    public void sendCoordinate() {

        if (player.getShot()) {
            player.shot = false;
            spawnProjectile(player.getX(), player.getY(), player.getAngle(), 0);
            if (ge.online) {
                try {
                    lobbyShots.put(playername);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (prevX != player.getX() || prevY != player.getY() || prevAngle != player.getAngle()) {
            if (ge.online) {
                try {
                    lobbySend.put(playername, player.getX(), player.getY(), player.getAngle());
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

        }
        prevX = player.getX();
        prevY = player.getY();
        prevAngle = player.getAngle();
    }

    public void recieveCoordinates() {
        try {
            coordinates = lobbyGet.queryp(ge.isHost ? new ActualField("player2") : new ActualField("player1"),
                    new FormalField(Integer.class), new FormalField(Integer.class), new FormalField(Integer.class));
            if (coordinates != null) {
                /*
                 * System.out.println(coordinates[0] + ": " + "x = " + coordinates[2].toString()
                 * + ", y = "
                 * + coordinates[2].toString() + ", angle = "
                 * + coordinates[3].toString());
                 */
                opponentPrevX = (int) coordinates[1];
                opponentPrevY = (int) coordinates[2];
                opponentPrevAngle = (int) coordinates[3];
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void recieveShots() {
        while (true) {
            try {
                lobbyShots.get(ge.isHost ? new ActualField("player2") : new ActualField("player1"));
                spawnProjectile((int) coordinates[1], (int) coordinates[2], (int) coordinates[3], 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendStart() {
        try {
            lobbyShots.put("Game Start", ge.isHost ? "player1" : "player2");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void recieveGameStart() {
        recieveStartBool = true;
        while (recieveStartBool) {
            try {
                System.out.println("Looking for Game Start");
                lobbyShots.get(new ActualField("Game Start"),
                        ge.isHost ? new ActualField("player2") : new ActualField("player1"));
                new Thread(() -> recieveShots()).start();
                new Thread(() -> recieveGameOver()).start();
                startPos = true;
                startGame = true;
                Gamestate.state = Gamestate.PLAYING;
                System.out.println("Game Start");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendLeft() {
        try {
            lobbyShots.put("Left", ge.isHost ? "player1" : "player2");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void recieveLeft() {
        recieveLeftBool = true;
        while (recieveLeftBool) {
            try {
                lobbyShots.get(new ActualField("Left"),
                        ge.isHost ? new ActualField("player2") : new ActualField("player1"));
                Gamestate.state = Gamestate.MENU;
                System.out.println("Left");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendGameOver() {
        System.out.println("sending Game Over");
        try {
            lobbyShots.put("Game Over", ge.isHost ? "player1" : "player2");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void recieveGameOver() {
        while (true) {
            try {
                System.out.println("Looking for Game Over");
                lobbyShots.get(new ActualField("Game Over"),
                        ge.isHost ? new ActualField("player2") : new ActualField("player1"));
                winner = true;
                Gamestate.state = Gamestate.GAMEOVER;
                System.out.println("Game Over");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeLobby() {
        try {
            lobbyShots.put("Game Over", "lobbyclose");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getOccupied() {
        try {
            lobbyShots.get(new ActualField("occupied"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Object[] queryOccupied() {
        Object[] query = null;
        try {
            query = lobbyShots.queryp(new ActualField("occupied"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return query;
    }

    public void putOccupied() {
        try {
            lobbyShots.put("occupied");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void spawnProjectile(int x, int y, int angle, int numberOfHits) {
        double angleRadians = Math.toRadians(angle);
        int projectileX = (int) Math.round(x + Math.cos(angleRadians) * ge.tileSize + ge.tileSize);
        int projectileY = (int) Math.round(y + Math.sin(angleRadians) * ge.tileSize + ge.tileSize);

        projectileList.add(new Projectile(ge, projectileX, projectileY, angle, numberOfHits));
    }

    public void drawOpponent(GraphicsContext gc) {
        if (projectileList != null) {
            if (projectileList.size() != 0) {
                for (int i = 0; i < projectileList.size(); i++) {
                    projectileList.get(i).repaint(gc);
                    projectileList.get(i).update(player.getHitbox(), ge.grid.getGrid());
                    // if (!projectileList.get(i).isActive()) {
                    // projectileList.remove(i);
                    // }
                }
            }
        }

        if (coordinates != null && !startGame) {
            startPos = false;
            gc.save();
            gc.translate((double) (int) coordinates[1] + ge.tileSize, (double) (int) coordinates[2] + ge.tileSize);
            gc.rotate((double) (int) coordinates[3]);

            gc.setFill(Color.BLUE);
            gc.drawImage(opponentImage, -ge.tileSize, -ge.tileSize, ge.tileSize * 2, ge.tileSize * 2);

            gc.restore();
        } else {
            // System.out.println("Drawp player1");
            gc.save();

            if (!startPos) {
                gc.translate((double) opponentPrevX + ge.tileSize, (double) (int) opponentPrevY + ge.tileSize);
                gc.rotate((double) opponentPrevAngle);
            } else {
                // Draw player 2 in correct starting possition;
                gc.translate(opponentStartPosX + ge.tileSize, opponentStartPosY + ge.tileSize);
                gc.rotate(opponentStartAngle);
                startGame = false;
            }

            gc.setFill(Color.BLUE);
            gc.drawImage(opponentImage, -ge.tileSize, -ge.tileSize, ge.tileSize * 2, ge.tileSize * 2);

            gc.restore();
        }
    }

    public int getLobbyID() {
        return lobbyID;
    }

    public void setLobbyID(int lobbyID) {
        this.lobbyID = lobbyID;
    }

}
