
import Actions.Action;
import Actions.ActionFactory;
import Actions.RequestAction;
import GameEngine.GameEngine;
import GameEngine.Robot;
import GameEngine.Cell;
import enumerations.Items;
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
			getPlayerOutputStream(1).writeInt(GameEngine.MAP_WIDTH);
			getPlayerOutputStream(1).writeInt(GameEngine.MAP_HEIGHT);
			getPlayerOutputStream(2).writeInt(GameEngine.MAP_WIDTH);
			getPlayerOutputStream(2).writeInt(GameEngine.MAP_HEIGHT);
			while (true) {
				try {
					//init round
					getPlayerOutputStream(1).writeInt(GameEngine.scores[0]);
					getPlayerOutputStream(1).writeInt(GameEngine.scores[1]);
					getPlayerOutputStream(2).writeInt(GameEngine.scores[1]);
					getPlayerOutputStream(2).writeInt(GameEngine.scores[0]);
					Cell[][] player1Map = engine.mapForPlayer(1);
					Cell[][] player2Map = engine.mapForPlayer(2);
					int count =0;
					for (int i = 0; i < GameEngine.MAP_HEIGHT; i++) {
						for (int j = 0; j < GameEngine.MAP_WIDTH; j++) {
							count++;
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

							getPlayerOutputStream(1).writeChar((""+oreP1).charAt(0));
							getPlayerOutputStream(2).writeChar((""+oreP2).charAt(0));
							getPlayerOutputStream(1).writeInt(c1.hole?1:0);
							getPlayerOutputStream(2).writeInt(c2.hole?1:0);
						}
					}

					int entityCountP1 = engine.getEntityCount(1);
					getPlayerOutputStream(1).writeInt(entityCountP1);
					int entityCountP2 = engine.getEntityCount(2);
					getPlayerOutputStream(2).writeInt(entityCountP2);
					System.out.println("entityCountP2 : "+entityCountP2);

					getPlayerOutputStream(1).writeInt(RequestAction.getCooldownForPlayer(1).get(Items.RADAR));
					getPlayerOutputStream(1).writeInt(RequestAction.getCooldownForPlayer(1).get(Items.TRAP));

					getPlayerOutputStream(2).writeInt(RequestAction.getCooldownForPlayer(2).get(Items.RADAR));
					getPlayerOutputStream(2).writeInt(RequestAction.getCooldownForPlayer(2).get(Items.TRAP));

					for (int i = 0; i < engine.playersRobots.length; i++) {
						Robot entity = engine.playersRobots[i];
						getPlayerOutputStream(1).writeInt(entity.robotId);
						getPlayerOutputStream(2).writeInt(entity.robotId);

						getPlayerOutputStream(1).writeInt((entity.owner == 1)?0:1);
						getPlayerOutputStream(2).writeInt((entity.owner == 2)?0:1);

						getPlayerOutputStream(1).writeInt(entity.coordinates.x);
						getPlayerOutputStream(1).writeInt(entity.coordinates.y);

						getPlayerOutputStream(2).writeInt(entity.coordinates.x);
						getPlayerOutputStream(2).writeInt(entity.coordinates.y);

						getPlayerOutputStream(1).writeInt(entity.carriedItem.getKey());
						getPlayerOutputStream(2).writeInt(entity.carriedItem.getKey());
					}

					for (int i = 0; i < GameEngine.MAP_HEIGHT; i++) {
						for (int j = 0; j < GameEngine.MAP_WIDTH; j++) {
							Cell c1 = player1Map[i][j];
							Cell c2 = player2Map[i][j];
							if(c1.item.type == Items.RADAR){
								getPlayerOutputStream(1).writeInt(Items.RADAR.getKey());
								getPlayerOutputStream(1).writeInt(c1.coordinates.x);
								getPlayerOutputStream(1).writeInt(c1.coordinates.y);
							}
							if(c2.item.type == Items.RADAR){
								getPlayerOutputStream(2).writeInt(Items.RADAR.getKey());
								getPlayerOutputStream(2).writeInt(c2.coordinates.x);
								getPlayerOutputStream(2).writeInt(c2.coordinates.y);
							}
							getPlayerOutputStream(1).flush();
							getPlayerOutputStream(2).flush();
							if(c1.item.type == Items.TRAP){
								getPlayerOutputStream(1).writeInt(Items.TRAP.getKey());
								getPlayerOutputStream(1).writeInt(c1.coordinates.x);
								getPlayerOutputStream(1).writeInt(c1.coordinates.y);
							}
							if(c2.item.type == Items.TRAP){
								getPlayerOutputStream(2).writeInt(Items.TRAP.getKey());
								getPlayerOutputStream(2).writeInt(c2.coordinates.x);
								getPlayerOutputStream(2).writeInt(c2.coordinates.y);
							}
						}
					}

					// wait for actions
					System.out.println("waiting for p1 input");
					String actionStringP1 = getPlayerInputStream(1).readUTF();
					System.out.println(actionStringP1);
					System.out.println("waiting for p2 input");
					String actionStringP2 = getPlayerInputStream(2).readUTF();
					System.out.println(actionStringP2);
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
					Thread.sleep(500);
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}

			System.out.println("scores : "+GameEngine.scores[0]+" : "+GameEngine.scores[1]);

			this.closeStreams();
			socketPlayer1.close();
			socketPlayer2.close();
		} catch (Exception e) {
			e.printStackTrace();
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
