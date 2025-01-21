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

    private Space server, lobbySend, lobbyGet, lobbyEvents;;

    private String playername;

    private int prevX, prevY, prevAngle, opponentPrevX, opponentPrevY, opponentPrevAngle;
    private int opponentStartPosX, opponentStartPosY, opponentStartAngle;

    public boolean startPos = true;
    public boolean startGame;

    public boolean winner = false;

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

        // Player starting positions:
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

    // Host Connect to server
    public void connectToLobbyHost() {
        if (ge.isHost) {
            try {
                String uri = "tcp://" + host + ":" + port + "/lobbyRequests?conn";
                server = new RemoteSpace(uri);
                server.put("host");
                Object[] lobby = server.get(new ActualField("lobby"), new FormalField(Integer.class));
                lobbyID = (int) lobby[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Connect to lobby
    public void connectToLobby() throws InterruptedException, UnknownHostException, IOException {
        establishConnectionToLobby(lobbyID);
        if (!lobbyHandShake()) {
            throw new NullPointerException();
        }
    }

    public void establishConnectionToLobby(int lobbyID) throws InterruptedException, UnknownHostException, IOException {
        String uri1 = getLobbyUri(String.valueOf(lobbyID) + "player1");
        String uri2 = getLobbyUri(String.valueOf(lobbyID) + "player2");
        lobbySend = ge.isHost ? new RemoteSpace(uri1) : new RemoteSpace(uri2);
        lobbyGet = ge.isHost ? new RemoteSpace(uri2) : new RemoteSpace(uri1);
    }

    public String getLobbyUri(String name) {
        String Uri = "tcp://" + host + ":" + port + "/" + name + "?conn";
        return Uri;
    }

    public boolean lobbyHandShake() throws InterruptedException, UnknownHostException, IOException {
        lobbySend.put("join/leave", "try to connect");
        Object[] connection = lobbySend.get(new ActualField("connection"), new FormalField(String.class));
        return (((String) connection[1]).equals("Connected"));
    }

    public void initLobby() {
        String uriS = "tcp://" + host + ":" + port + "/" + lobbyID + "events" + "?conn";
        try {
            lobbyEvents = new RemoteSpace(uriS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean readyToStart() {
        boolean bool = false;
        try {
            Object[] startBool = lobbySend.queryp(new FormalField(Boolean.class));
            if (startBool != null) {
                bool = (boolean) startBool[0];
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return bool;
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
                try {
                    lobbyEvents.put(playername);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }

        if (prevX != player.getX() || prevY != player.getY() || prevAngle != player.getAngle()) {
                try {
                    lobbySend.put(playername, player.getX(), player.getY(), player.getAngle());
                } catch (Exception e) {
                    e.printStackTrace();
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
                opponentPrevX = (int) coordinates[1];
                opponentPrevY = (int) coordinates[2];
                opponentPrevAngle = (int) coordinates[3];
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recieveShots() {
        while (true) {
            try {
                lobbyEvents.get(ge.isHost ? new ActualField("player2") : new ActualField("player1"));
                spawnProjectile((int) coordinates[1], (int) coordinates[2], (int) coordinates[3], 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendStart() {
        try {
            lobbyEvents.put("Game Start", ge.isHost ? "player1" : "player2", true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void recieveGameStart() {
        while (true) {
            try {
                Object[] gameStart = lobbyEvents.get(new ActualField("Game Start"),
                        ge.isHost ? new ActualField("player2") : new ActualField("player1"),
                        new FormalField(Boolean.class));
                if ((boolean) gameStart[2]) {
                    new Thread(() -> recieveShots()).start();
                    new Thread(() -> recieveGameOver()).start();
                    startPos = true;
                    startGame = true;
                    Gamestate.state = Gamestate.PLAYING;
                } else {
                    return;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendLeftPlayer2() {
        try {
            lobbySend.put("join/leave", "Left");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendLeft() {
        try {
            lobbyEvents.put("Left", ge.isHost ? "player1" : "player2");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendNoGameStart() {
        try {
            lobbyEvents.put("Game Start", ge.isHost ? "player2" : "player1", false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void recieveLeft() {
        while (true) {
            try {
                lobbyEvents.get(new ActualField("Left"),
                        ge.isHost ? new ActualField("player2") : new ActualField("player1"));
                Gamestate.state = Gamestate.MENU;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendGameOver() {
        projectileList.clear();
        try {
            lobbyEvents.put("Game Over", ge.isHost ? "player1" : "player2");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void recieveGameOver() {
        boolean checkGameOver = true;
        while (checkGameOver) {
            try {
                lobbyEvents.query(new ActualField("Game Over"),
                        ge.isHost ? new ActualField("player2") : new ActualField("player1"));
                winner = true;
                checkGameOver = false;
                projectileList.clear();
                Gamestate.state = Gamestate.GAMEOVER;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeLobby() {
        try {
            lobbyEvents.put("Game Over", "lobbyclose");
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
            gc.save();

            if (!startPos) {
                gc.translate((double) opponentPrevX + ge.tileSize, (double) (int) opponentPrevY + ge.tileSize);
                gc.rotate((double) opponentPrevAngle);
            } else {
                // Draw player 2 in correct starting position;
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
