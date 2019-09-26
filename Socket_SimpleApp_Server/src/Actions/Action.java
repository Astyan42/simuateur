package Actions;

import GameEngine.Cell;
import GameEngine.Robot;

public interface Action {
	void execute(Cell[][] map, Robot[] robots);
	int getRobotExecutingAction();
}
