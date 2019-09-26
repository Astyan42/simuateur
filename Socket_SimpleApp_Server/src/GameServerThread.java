
import Actions.Action;
import Actions.ActionFactory;
import GameEngine.GameEngine;
import GameEngine.Cell;
import utils.Pair;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.HashMap;

public class GameServerThread extends Thread {

	private Socket socketPlayer1 = null;
	private Socket socketPlayer2 = null;
	private Server server = null;
	private GameEngine engine;
	private HashMap<Integer, Pair<ObjectInputStream, ObjectOutputStream>> playerStreams;

	public GameServerThread(Server _server) {
		server = _server;
		engine = new GameEngine();
		this.playerStreams = new HashMap<>();
	}

	public int addPlayer(Socket s) {
		int newPlayerId = engine.addNewPlayer();
		Pair<ObjectInputStream, ObjectOutputStream> streams = null;
		try {
			streams = new Pair<>(new ObjectInputStream(s.getInputStream()), new ObjectOutputStream(s.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.playerStreams.put(newPlayerId, streams);
		return newPlayerId;
	}

	public boolean waitingForPlayers() {
		return !engine.isGameFull();
	}

	public ObjectInputStream getPlayerInputStream(int player) {
		return this.playerStreams.get(player).getLeft();
	}

	public ObjectOutputStream getPlayerOutputStream(int player) {
		return this.playerStreams.get(player).getRight();
	}

	@Override
	public void run() {
		try {
			getPlayerOutputStream(1).writeUTF(""+GameEngine.MAP_WIDTH+ " "+GameEngine.MAP_HEIGHT);
			getPlayerOutputStream(2).writeUTF(""+GameEngine.MAP_WIDTH+ " "+GameEngine.MAP_HEIGHT);
			while (true) {
				try {
					//init round
					getPlayerOutputStream(1).writeUTF(""+GameEngine.scores[0]+ " "+GameEngine.scores[1]);
					getPlayerOutputStream(2).writeUTF(""+GameEngine.scores[1]+ " "+GameEngine.scores[0]);
					Cell[][] player1Map = engine.mapForPlayer(1);
					Cell[][] player2Map = engine.mapForPlayer(2);
					for (int i = 0; i < GameEngine.MAP_HEIGHT; i++) {
						for (int j = 0; j < GameEngine.MAP_WIDTH; j++) {
							Cell c1 = player1Map[i][j];
							String oreP1 = "?";
							Cell c2 = player2Map[i][j];
							String oreP2 = "?";
							if(c1.known){
								oreP1 = Integer.toString(c1.ore);
							}
							if(c2.known){
								oreP2 = Integer.toString(c2.ore);
							}

							getPlayerOutputStream(1).writeUTF(""+oreP1);
							getPlayerOutputStream(2).writeUTF(""+oreP2);

							getPlayerOutputStream(1).writeUTF(c1.hole?"1":"0");
							getPlayerOutputStream(2).writeUTF(c2.hole?"1":"0");
						}
					}

					// wait for actions
					String actionStringP1 = getPlayerInputStream(1).readUTF();
					String actionStringP2 = getPlayerInputStream(2).readUTF();
					// cast to the good type of object
					Action[] actions = concatenate(ActionFactory.parse(actionStringP1, 1),
						ActionFactory.parse(actionStringP2,	2));

					engine.executeActions(actions);

					// All actions have been executed move to nextRound
					boolean gameIsOver = engine.nextRound();
					// TODO end at 200 rounds

					if(gameIsOver){
						break;
					}
				} catch (IOException e) {
					break;
				}
			}

			System.out.println("scores : "+GameEngine.scores[0]+" : "+GameEngine.scores[1]);

			this.closeStreams();
			socketPlayer1.close();
			socketPlayer2.close();
		} catch (Exception e) {
			System.out.println("Error : " + e);
		}
	}

	public void closeStreams() {
		for (Pair<ObjectInputStream, ObjectOutputStream> streams :
			this.playerStreams.values()) {
			try {
				streams.getLeft().close();
				streams.getRight().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public <T> T[] concatenate(T[] a, T[] b) {
		int aLen = a.length;
		int bLen = b.length;

		@SuppressWarnings("unchecked")
		T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
		System.arraycopy(a, 0, c, 0, aLen);
		System.arraycopy(b, 0, c, aLen, bLen);

		return c;
	}
}
