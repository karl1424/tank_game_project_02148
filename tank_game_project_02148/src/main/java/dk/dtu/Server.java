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
                lobbyRequests.get(new ActualField("host"), new FormalField(Integer.class));
                serverSpace.add(lobbyID+"player1", new StackSpace());
                serverSpace.add(lobbyID+"player2", new StackSpace());
                //new Thread(new lobbyHandeler(IP, port, lobbyID++)).start();
                System.out.println("lobby: "+(lobbyID-1)+" started");
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


    public lobbyHandeler(String IP, String port, int lobbyID) {
        this.IP = IP;
        this.port = port;
        this.lobbyID = lobbyID;
    }

    public void run() {
        try {
            RemoteSpace player1Coords = new RemoteSpace("tcp://" + IP + ":" + port + "/"+lobbyID+"player1"+"?conn");
            RemoteSpace player2Coords = new RemoteSpace("tcp://" + IP + ":" + port + "/"+lobbyID+"player2"+"?conn");
            /* 
            while (true) {
                Object [] position = lobbySpace.getp(new FormalField(String.class),new FormalField(Double.class),new FormalField(Double.class), new FormalField(Double.class));
                if (position != null) {
                    System.out.println(position[0].toString()+": x:"+(double) position[1]+" y:"+(double) position[2]+" angle:"+(double) position[3]);
                    lobbySpace.put("Server",position[0],position[1],position[2], position[3]);
                }
            }
            */
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 
}