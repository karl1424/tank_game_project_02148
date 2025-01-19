package dk.dtu.connection;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jspace.*;

public class Server {
    public static Space p0 = new SequentialSpace();
    public static Space p1 = new SequentialSpace();
    public static Space p2 = new SequentialSpace();
    public static Space p3 = new SequentialSpace();
    public static Space p4 = new SequentialSpace();
    public static SpaceRepository serverSpace;
    public static RemoteSpace lobbyRequests;
    final static String IP = "localhost";
    final static String port = "31145";
    public static int lobbyID;

    public static void main(String[] args) throws UnknownHostException, IOException {
        lobbyID = 0;
        serverSpace = new SpaceRepository();
        serverSpace.addGate("tcp://" + IP + ":" + port + "/?conn");
        serverSpace.add("lobbyRequests", new SequentialSpace());
        System.out.println("server up");

        // START PETRINET

        new Thread(() -> connectToRequests()).start();
        new Thread(() -> lobbyReq()).start();
        new Thread(() -> createLobbyHandler()).start();
        new Thread(() -> sendLobbyID()).start();
        new Thread(() -> increaseLobbyID()).start();
        try {
            p0.put("token");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void connectToRequests() {
        while (true) {
            try {
                p0.get(new ActualField("token"));
                lobbyRequests = new RemoteSpace("tcp://" + IP + ":" + port + "/lobbyRequests?conn");
                p1.put("token");
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void lobbyReq() {
        while (true) {
            try {
                p1.get(new ActualField("token"));
                lobbyRequests.get(new ActualField("host"));
                p2.put("token");
                p3.put("token");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createLobbyHandler() {
        while (true) {
            try {
                p2.get(new ActualField("token"));
                new Thread(new lobbyHandeler(IP, port, lobbyID, serverSpace)).start();
                System.out.println("lobby: " + (lobbyID) + " started");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendLobbyID() {
        while (true) {
            try {
                p3.get(new ActualField("token"));
                lobbyRequests.put("lobby", lobbyID);
                p4.put("token");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void increaseLobbyID() {
        while (true) {
            try {
                p4.get(new ActualField("token"));
                lobbyID++;
                p1.put("token");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// END PETRINET
class lobbyHandeler implements Runnable {
    private String IP;
    private String port;
    private int lobbyID;
    private SpaceRepository serverSpace;

    public lobbyHandeler(String IP, String port, int lobbyID, SpaceRepository serverSpace) {
        this.IP = IP;
        this.port = port;
        this.lobbyID = lobbyID;
        this.serverSpace = serverSpace;
    }

    public void run() {
        serverSpace.add(lobbyID + "player1", new PileSpace());
        serverSpace.add(lobbyID + "player2", new PileSpace());
        serverSpace.add(lobbyID + "events", new SequentialSpace());

        String uri = "tcp://" + IP + ":" + port + "/" + lobbyID + "events" + "?conn";

        new Thread(() -> getConnect()).start();

        try {
            Space lobbyEvents = new RemoteSpace(uri);
            lobbyEvents.query(new ActualField("Game Over"), new FormalField(String.class));
            Thread.sleep(200);
            serverSpace.remove(lobbyID + "player1");
            serverSpace.remove(lobbyID + "player2");
            serverSpace.remove(lobbyID + "events");
            System.out.println("Closed lobby: " + lobbyID);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getConnect() {
        String uri1 = "tcp://" + IP + ":" + port + "/" + lobbyID + "player1" + "?conn";
        String uri2 = "tcp://" + IP + ":" + port + "/" + lobbyID + "player2" + "?conn";

        try {
            Space player1Send = new RemoteSpace(uri1);
            Space player2Send = new RemoteSpace(uri2);
            player1Send.get(new ActualField("join/leave"), new ActualField("try to connect"));
            player1Send.put("connection","Connected");
            System.out.println("Outside");
            
            while (true) {
                Object [] player2status = player2Send.get(new ActualField("join/leave"),new FormalField(String.class));
                if (((String)player2status[1]).equals("try to connect")) {
                    if(!checkOccupied(player2Send)){
                        System.out.println("Not occupied");
                        player2Send.put("occupied");
                        player2Send.put("connection","Connected");
                        System.out.println("Inside");
                        // boolean ready to start
                        player1Send.put(true);
                    } else {
                        System.out.println("Occupied");
                        player2Send.put("connection","Not connected");
                    }
                
                } else {
                    player2Send.get(new ActualField("occupied"));
                    System.out.println("PLAYER 2 LEFT");
                    // boolean not ready to start
                    player1Send.put(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkOccupied(Space space) {
        Object [] isOccupied = null;
        try {
             isOccupied = space.queryp(new ActualField("occupied"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (isOccupied != null);
    }
}
