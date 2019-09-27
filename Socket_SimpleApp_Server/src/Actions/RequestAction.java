package Actions;

import GameEngine.Item;
import enumerations.Items;
import GameEngine.Cell;
import GameEngine.Robot;

import java.util.HashMap;
import java.util.Map;

public class RequestAction implements Action {
	private static final int GLOBAL_CD = 5;

	private int actingRobot;
	private Items requestedItem;
	private static HashMap<Integer, HashMap<Items, Integer>> itemsCooldown;
	public RequestAction(String s, int actingRobot) {
		requestedItem = Items.valueOf(Integer.parseInt(s));
		this.actingRobot = actingRobot;
	}

	 static {
		 itemsCooldown = new HashMap<>();
		 HashMap<Items, Integer> player1Hashmap = new HashMap<>();
		 player1Hashmap.put(Items.RADAR, -1);
		 player1Hashmap.put(Items.TRAP, -1);
		 HashMap<Items, Integer> player2Hashmap = new HashMap<>();
		 player2Hashmap.put(Items.RADAR, -1);
		 player2Hashmap.put(Items.TRAP, -1);
		 itemsCooldown.put(1, player1Hashmap);
		 itemsCooldown.put(2, player2Hashmap);
	}

	@Override
	public void execute(Cell[][] map, Robot[] robots) {
		// TODO Do nothing
		Robot r = robots[actingRobot];
		if(r.isDead()) return;
		if(r.coordinates.x != 1){
			new MoveAction(1, r.coordinates.y, this.actingRobot).execute(map,robots);
		}
		else if(itemsCooldown.get(r.owner).get(requestedItem) == 0){
			r.carriedItem = requestedItem;
			itemsCooldown.get(r.owner).put(requestedItem,GLOBAL_CD);
		}
	}

	@Override
	public int getRobotExecutingAction() {
		return actingRobot;
	}

	public static HashMap<Items,Integer> getCooldownForPlayer(int player){
		return itemsCooldown.get(player);
	}

	public static void HandleItemCD(){
		for (HashMap<Items, Integer> itemCD:
				 itemsCooldown.values()) {
			for (Map.Entry<Items, Integer> entry:
					 itemCD.entrySet()) {
				if(entry.getValue() != 0){
					entry.setValue(entry.getValue()-1);
				}
			}
		}
	}
}
