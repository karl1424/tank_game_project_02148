package dk.dtu;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jspace.*;

public class Server {
    public static void main(String[] args) throws UnknownHostException, IOException {
        final String IP = "10.141.156.47";
        final String port = "31145";
        int lobbyID = 0;
        SpaceRepository serverSpace = new SpaceRepository();
        serverSpace.addGate("tcp://" + IP + ":" + port + "/?conn");
        serverSpace.add("lobbyRequests", new SequentialSpace());
        System.out.println("server up");

        RemoteSpace lobbyRequests = new RemoteSpace("tcp://" + IP + ":" + port + "/lobbyRequests?conn");

        while (true) {
            try {
                lobbyRequests.get(new ActualField("host"));
                new Thread(new lobbyHandeler(IP, port, lobbyID, serverSpace)).start();
                System.out.println("lobby: " + (lobbyID) + " started");
                lobbyRequests.put("lobby", lobbyID);
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
        serverSpace.add(lobbyID + "player1", new StackSpace());
        serverSpace.add(lobbyID + "player2", new StackSpace());
        serverSpace.add(lobbyID + "shots", new SequentialSpace());

        Boolean running = true;

        String uri1 = "tcp://" + IP + ":" + port + "/" + lobbyID + "player1" + "?conn";
        String uri2 = "tcp://" + IP + ":" + port + "/" + lobbyID + "player2" + "?conn";

        int player1X = 0;
        int player1Y = 0;
        int player1Angle = 0;
        int player2X = 0;
        int player2Y = 0;
        int player2Angle = 0;
        /* 
        try {
            Space lobbyPlayer1 = new RemoteSpace(uri1);
            Space lobbyPlayer2 = new RemoteSpace(uri2);

            Thread.sleep(120000);
            while (running) {
                Object[] player1coord = lobbyPlayer1.queryp();
                Object[] player2coord = lobbyPlayer2.queryp();

                if (player1X == (int) player1coord[1] && player1Y == (int) player1coord[2]
                        && player1Angle == (int) player1coord[3]
                        && player2X == (int) player2coord[1] && player2Y == (int) player2coord[2]
                        && player2Angle == (int) player2coord[3]) {
                    serverSpace.remove(lobbyID + "player1");
                    serverSpace.remove(lobbyID + "player2");
                    serverSpace.remove(lobbyID + "shots");
                    running = false;
                }
            }
        } catch (Exception e) {
            serverSpace.remove(lobbyID + "player1");
            serverSpace.remove(lobbyID + "player2");
            serverSpace.remove(lobbyID + "shots");
            running = false;
            System.out.println("Lobby: "+lobbyID+" closed");
            e.printStackTrace();
        }*/
    }
}