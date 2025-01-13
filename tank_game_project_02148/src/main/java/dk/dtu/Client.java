package dk.dtu;

import java.util.ArrayList;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Client {
    private Player player;
    private String playername;
    private InputHandler inputHandler;
    private Space server;
    private Space lobbySend;
    private Space lobbyGet;
    private Space lobbyShots;
    private GameEngine ge;

    private int prevX, prevY, prevAngle;

    Object[] coordinates;
    private Image opponentImage;
    private int opponentPrevX, opponentPrevY, opponentPrevAngle, opponentStartPosX, opponentStartPosY,
            opponentStartAngle;

    ArrayList<Projectile> projectileList = new ArrayList<>();

    private boolean startPos = true;

    public Client(GameEngine ge, InputHandler inputHandler) {
        this.ge = ge;
        this.inputHandler = inputHandler;
        if (!ge.online) {
            ge.isHost = true;
            startPos = false;
        } else {
            startPos = true;
        }
        playername = ge.isHost ? "player1" : "player2";

        int port = 31145;
        String host = ge.IP;
        int lobbyID = 0;

        opponentImage = new Image("file:res/tank2.png");

        if (ge.online) {
            // Connect to server
            if (ge.isHost) {
                try {
                    String uri = "tcp://" + host + ":" + port + "/lobbyRequests?conn";
                    server = new RemoteSpace(uri);
                    server.put("host", 1);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            // Connect to lobby
            try {
                String uri1 = "tcp://" + host + ":" + port + "/" + lobbyID + "player1" + "?conn";
                String uri2 = "tcp://" + host + ":" + port + "/" + lobbyID + "player2" + "?conn";
                String uriS = "tcp://" + host + ":" + port + "/" + lobbyID + "shots" + "?conn";
                lobbySend = ge.isHost ? new RemoteSpace(uri1) : new RemoteSpace(uri2);
                lobbyGet = ge.isHost ? new RemoteSpace(uri2) : new RemoteSpace(uri1);
                lobbyShots = new RemoteSpace(uriS);
            } catch (Exception e) {
                System.out.println(e);
            }

        }
        createPlayer();

        // Player 2 starting possitions:
        if (playername == "player1") {
            opponentStartPosX = 200;
            opponentStartPosY = 200;
            opponentStartAngle = 180;
        } else {
            opponentStartPosX = 100;
            opponentStartPosY = 100;
            opponentStartAngle = 0;
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

    public void sendCoordinate() throws InterruptedException {
        if (player.getShot()) {
            spawnProjectile(player.getX(), player.getY(), player.getAngle());
            if (ge.online) {
                lobbyShots.put(playername);
            }
            player.shot = false;
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
                spawnProjectile((int) coordinates[1], (int) coordinates[2], (int) coordinates[3]);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void spawnProjectile(int x, int y, int angle) {
        double angleRadians = Math.toRadians(angle);
        double offset = 1.6 * ge.tileSize;
        int projectileX = (int) Math.round(x + Math.cos(angleRadians) * offset + ge.tileSize);
        int projectileY = (int) Math.round(y + Math.sin(angleRadians) * offset + ge.tileSize);

        projectileList.add(new Projectile(ge, projectileX, projectileY, angle));
    }

    public void drawOpponent(GraphicsContext gc) {
        if (projectileList != null) {
            if (projectileList.size() != 0) {
                for (int i = 0; i < projectileList.size(); i++) {
                    projectileList.get(i).repaint(gc);
                    projectileList.get(i).update(player.getHitbox(), ge.grid.getGrid());
                    if (!projectileList.get(i).isActive()) {
                        projectileList.remove(i);
                    }
                }
            }
        }

        
        if (coordinates != null) {
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
                }

                gc.setFill(Color.BLUE);
                gc.drawImage(opponentImage, -ge.tileSize, -ge.tileSize, ge.tileSize * 2, ge.tileSize * 2);

                gc.restore();

        }

    }
}
