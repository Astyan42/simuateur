package GameEngine;

import Actions.*;
import enumerations.Items;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class GameEngine {
	public static final AtomicInteger idGenerator = new AtomicInteger(10);
	public static final int MAP_HEIGHT = 15;
	public static final int MAP_WIDTH = 30;
	public int entityCount = 10;
	Cell[][] map;

	public Robot[] playersRobots;
	public static int[] scores = new int[2];
	private static final int MAX_NUMBER_OF_PLAYER = 2;
	private static final int RADAR_RANGE = 4;
	public static final int NUMBER_OF_ROBOTS = 5;
	private int currentNumberOfPlayer = 0;
	private int currentRound=0;
	private List<Rule> rules;

	public GameEngine() {
		this.rules = new ArrayList<>();
		this.map = generateMap();
		this.playersRobots = generateRobots();
		scores[0] = 0;
		scores[1] = 0;
	}

	public int addNewPlayer() {
		if (currentNumberOfPlayer >= MAX_NUMBER_OF_PLAYER) return -1;
		currentNumberOfPlayer += 1;
		return currentNumberOfPlayer;

	}

	public void executeActions(Action[] action) {
		// handle explosions
		for (int i = 0; i < action.length; i++) {
			if(action[i] instanceof DigAction) {
				((DigAction)action[i]).shouldExplode(this.map, this.playersRobots);
			}
		}
		// handle digs
		for (int i = 0; i < action.length; i++) {
			if(action[i] instanceof DigAction) {
				action[i].execute(this.map, this.playersRobots);
			}
		}
		// request and decrease cd
		for (int i = 0; i < action.length; i++) {
			if(action[i] instanceof RequestAction) {
				action[i].execute(this.map, this.playersRobots);
			}
		}
		RequestAction.HandleItemCD();


		// move wait
		for (int i = 0; i < action.length; i++) {
			if(action[i] instanceof MoveAction || action[i] instanceof WaitAction) {
				action[i].execute(this.map, this.playersRobots);
			}
		}
	}

	private Robot[] generateRobots(){
		Robot[] robotList = new Robot[GameEngine.NUMBER_OF_ROBOTS*2];
		int[] yValues = new int[NUMBER_OF_ROBOTS];
		Random r = new Random();
		for (int i = 0; i < yValues.length; i++) {
			int yCoord;
			boolean shouldContinue;
			do{
				shouldContinue = false;
				yCoord = r.nextInt(GameEngine.MAP_HEIGHT);
				for (int j = 0; j < yValues.length; j++) {
					if (yValues[j] == yCoord){
						shouldContinue = true;
						break;
					}
				}
			}while(shouldContinue);
			yValues[i] = yCoord;
		}

		for (int i = 0; i < yValues.length; i++) {
			Point coordinates = new Point(1, yValues[i]);
			robotList[i] = new Robot(coordinates,1, i);
			robotList[i+GameEngine.NUMBER_OF_ROBOTS] = new Robot(coordinates,2, i+GameEngine.NUMBER_OF_ROBOTS);
		}
		return robotList;
	}

	private Cell[][] generateMap() {
		int threshold = 15;
		Cell[][] mapToReturn = new Cell[MAP_HEIGHT][MAP_WIDTH];
		for (int i = 0; i < MAP_HEIGHT; i++) {
			for (int j = 0; j < MAP_WIDTH; j++) {
				int oreProba = 5;
				int numberOfOre = 0;
				if (j >= threshold) {
					oreProba = 75;
				}
				if (Math.random() * 100 > oreProba) {
					numberOfOre = Math.round((float)Math.random() * 4);
				}
				mapToReturn[i][j] = new Cell(new Item(-1, Items.NOTHING), numberOfOre, false,i,j);
			}
		}
		return mapToReturn;
	}

	public Cell[][] mapForPlayer(int player) {
		Cell[][] mapToReturn = new Cell[MAP_HEIGHT][MAP_WIDTH];
		for (int i = 0; i < MAP_HEIGHT; i++) {
			for (int j = 0; j < MAP_WIDTH; j++) {
				Cell mapCell = this.map[i][j];
				mapToReturn[i][j] = new Cell(mapCell.itemVisibleFor(player), mapCell.ore, mapCell.hole,i,j);
				if (!cellIsKnown(player, i, j)) {
					mapToReturn[i][j].known = false;
				}
			}
		}
		return mapToReturn;
	}

	public boolean cellIsKnown(int player, int cellX, int cellY) {
		for (int i = cellY-RADAR_RANGE, iteration=0; i <=cellY+RADAR_RANGE ; i++) {
			for (int j = cellX-iteration; j <= cellX+iteration; j++) {
				if(i > 0 && i < MAP_HEIGHT  && j > 0 && j < MAP_WIDTH
					&& map[i][j].item.playerOwner == player && map[i][j].item.type == Items.RADAR){
					return true;
				}
			}
			if( i <= cellX) {
				iteration++;
			}
			else{
				iteration--;
			}
		}
		return false;
	}

	public boolean isGameFull() {
		return this.currentNumberOfPlayer == MAX_NUMBER_OF_PLAYER;
	}

	public boolean nextRound() {
		this.currentRound++;
		System.out.println( "========================= Round "+ this.currentRound +" ==========================="  );
		return this.currentRound >= 200;
	}

	public int getEntityCount(int player) {
		int count = NUMBER_OF_ROBOTS * MAX_NUMBER_OF_PLAYER;
		Cell[][] mapPlayer = this.mapForPlayer(player);
		for (int i = 0; i < MAP_HEIGHT; i++) {
			for (int j = 0; j < MAP_WIDTH; j++) {
				if(mapPlayer[i][j].item.type != Items.NOTHING){
					count++;
				}
			}
		}
		return count;
	}
}
