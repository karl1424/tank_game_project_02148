package dk.dtu;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jspace.*;

public class Server {
    public static void main(String[] args) throws UnknownHostException, IOException {
        final String IP = "10.134.17.47";
        final String port = "31145";
        int lobbyID = 0;
        SpaceRepository serverSpace = new SpaceRepository();
        serverSpace.add("lobbyRequests", new SequentialSpace());
        serverSpace.addGate("tcp://" + IP + ":" + port + "/?conn");
        System.out.println("server up");

        RemoteSpace lobbyRequests = new RemoteSpace("tcp://" + IP + ":" + port + "/lobbyRequests?conn");
        
        while (true) {
            try {
                Object[] request = lobbyRequests.get(new ActualField("host"), new FormalField(Integer.class));
                serverSpace.add(lobbyID+"", new SequentialSpace());
                new Thread(new lobbyHandeler(lobbyID++,(int) request[1])).start();
                System.out.println("lobby: "+(lobbyID-1)+" started");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}

class lobbyHandeler implements Runnable {
    private int lobbyID;
    private int players;
    
    final String IP = "10.134.17.47";
    final String port = "31145";

    public lobbyHandeler(int lobbyID, int players) {
        this.lobbyID = lobbyID;
        this.players = players;
    }

    public void run() {
        try {
            RemoteSpace lobbySpace = new RemoteSpace("tcp://" + IP + ":" + port + "/"+lobbyID+"?conn");
            while (true) {
                Object [] position = lobbySpace.getp(new FormalField(String.class),new FormalField(Double.class),new FormalField(Double.class));
                if (position != null) {
                    System.out.println(position[0].toString()+": x:"+(double) position[1]+" y:"+(double) position[2]);
                    lobbySpace.put("Server",position[0],position[1],position[2]);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}