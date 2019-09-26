package Actions;

import GameEngine.Cell;
import GameEngine.Robot;

public class WaitAction implements Action {
	int actingRobot;

	public WaitAction(int actingRobot) {
		this.actingRobot = actingRobot;
	}

	@Override
	public void execute(Cell[][] map, Robot[] robots) {
		// TODO Do nothing
	}

	@Override
	public int getRobotExecutingAction() {
		return actingRobot;
	}
}
