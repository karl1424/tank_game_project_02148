package dk.dtu;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jspace.*;

public class Server {
    public static void main(String[] args) throws UnknownHostException, IOException {
        final String IP = "localhost";
        final String port = "31145";
        int lobbyID = 0;
        SpaceRepository serverSpace = new SpaceRepository();
        serverSpace.addGate("tcp://" + IP + ":" + port + "/?conn");
        serverSpace.add("lobbyRequests", new SequentialSpace());
        System.out.println("server up");

        RemoteSpace lobbyRequests = new RemoteSpace("tcp://" + IP + ":" + port + "/lobbyRequests?conn");
        
        while (true) {
            try {
                lobbyRequests.get(new ActualField("host"), new FormalField(Integer.class));
                new Thread(new lobbyHandeler(IP, port, lobbyID,serverSpace)).start();
                System.out.println("lobby: "+(lobbyID)+" started");
                lobbyRequests.put("lobby",lobbyID);
                lobbyID++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

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
        serverSpace.add(lobbyID+"player1", new StackSpace());
        serverSpace.add(lobbyID+"player2", new StackSpace());
        serverSpace.add(lobbyID+"shots", new SequentialSpace());
    } 
}