package Actions;

import GameEngine.GameEngine;

public class ActionFactory {

	public static Action[] parse(String actionString, int player) throws Exception {
		String[] robotAction = actionString.split("\n");
		Action[] actionList = new Action[robotAction.length];
		String[] split = actionString.split(" ");

		for (int i = 0; i < actionList.length; i++) {
			int actingRobot = (player-1)* GameEngine.NUMBER_OF_ROBOTS;
			if(split[0].equals(Actions.WAIT.getKey())){
				actionList[i] = new WaitAction(actingRobot);
			}
			else if(split[0].equals(Actions.DIG.getKey())){
				actionList[i] = new DigAction( Integer.parseInt(split[1]), Integer.parseInt(split[2]), actingRobot);
			}
			else if(split[0].equals(Actions.MOVE.getKey())){
				actionList[i] =new MoveAction( Integer.parseInt(split[1]), Integer.parseInt(split[2]), actingRobot);
			}
			else if(split[0].equals(Actions.REQUEST.getKey())){
				actionList[i] = new RequestAction( split[1], actingRobot);
			}
		}

		return actionList;
	}
}

enum Actions{
	MOVE("MOVE"),
	DIG("DIG"),
	WAIT("WAIT"),
	REQUEST("REQUEST");
	private String key;

	Actions(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
