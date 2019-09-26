
import enumerations.SocketFlags;

import java.io.*;
import java.net.Socket;

public class GameServerThread extends Thread {

    private Socket socketPlayer1 = null;
    private Socket socketPlayer2 = null;
    private Server server = null;
    private ObjectInputStream  currentPlayerInputStream = null;
    private ObjectOutputStream currentPlayerOutputStream = null;
    private int currentPlayer = 1;
    private GameEngine engine;

    public GameServerThread(Server _server) {
        server = _server;
        engine = new GameEngine();
    }

    public int addPlayer(Socket s){
        return engine.addNewPlayer(s);
    }

    public boolean waitingForPlayers(){
        return !engine.isGameFull();
    }

    @Override
    public void run() {
        try {
            currentPlayerInputStream = engine.getPlayerInputStream(currentPlayer);
            currentPlayerOutputStream = engine.getPlayerOutputStream(currentPlayer);
            
            while (true) {
                try {
                    // wait for an action
                    Object object = currentPlayerInputStream.readObject(); // TODO Change Object type
                    // cast to the good type of object

                    // Send notif your turn to currentplayer
                    currentPlayerOutputStream.writeUTF(SocketFlags.YOUR_TURN.getKey());

                    if(!engine.executeAction(object)){
                        currentPlayerOutputStream.writeUTF(SocketFlags.LOOSE.getKey());
                    }
                    // change the current player and change the currentplayer streams
                    currentPlayer = nextRound();

                } catch (IOException e) {
                    break;
                }
            }
            engine.closeStreams();
            socketPlayer1.close();
            socketPlayer2.close();
        } catch (Exception e) {
            System.out.println("Error : " + e);
        }
    }

    private int nextRound() {
        currentPlayer = GameEngine.getNextPlayer(currentPlayer);
        currentPlayerInputStream = engine.getPlayerInputStream(currentPlayer);
        currentPlayerOutputStream = engine.getPlayerOutputStream(currentPlayer);
        return currentPlayer;
    }
}