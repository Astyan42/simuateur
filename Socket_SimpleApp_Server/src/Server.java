import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// SERVER : Multi Server
// TIPE : Two-Way Communication (Client to Server, Server to Client)
// Description :
// Example of Server that receive data from client and save it to List
// and also send info total of Data to client
public class Server implements Runnable {

    private int portP1 = 8081;
    private ServerSocket serverP1Socket = null;
    private int portP2 = 8082;
    private ServerSocket serverP2Socket = null;
    private Thread thread = null;
    private GameServerThread client = null;

    public Server() {
        try {
            serverP1Socket = new ServerSocket(portP1);
            serverP2Socket = new ServerSocket(portP2);
            serverP1Socket.setReceiveBufferSize(99262144);
            serverP2Socket.setReceiveBufferSize(99262144);
            System.out.println("Server started on port " + serverP1Socket.getLocalPort() + "...");
            System.out.println("Server started on port " + serverP2Socket.getLocalPort() + "...");
            System.out.println("Waiting for clients...");
            thread = new Thread(this);
            thread.start();
        } catch (IOException e) {
            System.out.println("Error : " + e);
        }
    }

    @Override
    public void run() {
        client = new GameServerThread(this);
        try {
					client.addPlayer(serverP1Socket.accept());
					System.out.println("player 1 connected");
					client.addPlayer(serverP2Socket.accept());
					System.out.println("player 2 connected");
					System.out.println("new player");
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.start();
    }


    public static void main(String args[]) {
        Server server = new Server();
    }
}
