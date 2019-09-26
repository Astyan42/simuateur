import utils.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameEngine {
    private static final int MAX_NUMBER_OF_PLAYER = 2;
    private int currentNumberOfPlayer = 0;
    private HashMap<Integer, Pair<ObjectInputStream, ObjectOutputStream>> playerStreams;
    private List<Rule> rules;


    public GameEngine() {
        this.playerStreams = new HashMap<>();
        this.rules = new ArrayList<>();
        // TODO insert rules here
    }

    public int addNewPlayer(Socket s){
        if(currentNumberOfPlayer >= MAX_NUMBER_OF_PLAYER) return -1;
        try {
            Pair<ObjectInputStream, ObjectOutputStream> streams = new Pair<>(new ObjectInputStream(s.getInputStream()), new ObjectOutputStream(s.getOutputStream()));
            currentNumberOfPlayer += 1;
            this.playerStreams.put(currentNumberOfPlayer, streams);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return currentNumberOfPlayer;

    }

    public static int getNextPlayer(int currentPlayer){
        if(currentPlayer == MAX_NUMBER_OF_PLAYER){
            return 1;
        }
        return ++currentPlayer;
    }

    public ObjectInputStream getPlayerInputStream(int player){
        return this.playerStreams.get(player).getLeft();
    }

    public ObjectOutputStream getPlayerOutputStream(int player){
        return this.playerStreams.get(player).getRight();
    }

    public boolean isValid(Object action){
        for (Rule r :
                rules) {
            if(!r.isMoveValid(action)){
                return false;
            }
        }
        return true;
    }

    public boolean executeAction(Object action){
        if(this.isValid(action)){
            // TODO Update current game status
            return true;
        }
        return false;
    }

    public void closeStreams() {
        for (Pair<ObjectInputStream, ObjectOutputStream> streams:
             this.playerStreams.values()) {
            try {
                streams.getLeft().close();
                streams.getRight().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isGameFull() {
        return this.currentNumberOfPlayer == MAX_NUMBER_OF_PLAYER;
    }
}
