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
    final static String IP = "10.209.242.14";
    final static String port = "31145";
    public static int lobbyID;

    public static void main(String[] args) throws UnknownHostException, IOException {
        lobbyID = 0; 
        serverSpace = new SpaceRepository();
        serverSpace.addGate("tcp://" + IP + ":" + port + "/?conn");
        serverSpace.add("lobbyRequests", new SequentialSpace());
        System.out.println("server up");
        
//START PETRINET

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
//END PETRINET
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
        serverSpace.add(lobbyID + "player1", new StackSpace());
        serverSpace.add(lobbyID + "player2", new StackSpace());
        serverSpace.add(lobbyID + "shots", new SequentialSpace());

        String uri = "tcp://" + IP + ":" + port + "/" + lobbyID + "shots" + "?conn";

        try {
            Space lobbyShots = new RemoteSpace(uri);
            lobbyShots.query(new ActualField("Game Over"), new FormalField(String.class));
            Thread.sleep(200);
            serverSpace.remove(lobbyID + "player1");
            serverSpace.remove(lobbyID + "player2");
            serverSpace.remove(lobbyID + "shots");
            System.out.println("Closed lobby: "+lobbyID);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
