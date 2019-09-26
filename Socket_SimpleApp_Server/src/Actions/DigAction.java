package Actions;

import GameEngine.Cell;
import GameEngine.Item;
import GameEngine.Robot;
import enumerations.Items;

import java.awt.*;
import java.util.Stack;

public class DigAction implements Action {
	private int actingRobot;
	private Point actionCoordinates;
	public DigAction(int x, int y, int actingRobot) {
		this.actionCoordinates = new Point(x,y);
		this.actingRobot = actingRobot;
	}

	@Override
	public void execute(Cell[][] map, Robot[] robots) {
		Cell dugCell = map[actionCoordinates.x][actionCoordinates.y];
		Robot robot = robots[actingRobot];
		if(robot.isDead()) return;
		if(!dugCell.isNeighbour(robot)){
			new MoveAction(this.actionCoordinates.x, this.actionCoordinates.y, this.actingRobot).execute(map,robots);
		}
		else{
			dugCell.hole = true;
			if(robot.carriedItem != Items.NOTHING){
				dugCell.item = new Item(robot.owner, robot.carriedItem);
			}
			if(dugCell.ore > 0){
				robot.carriedItem = Items.ORE;
				dugCell.ore --;
			}
		}
	}

	@Override
	public int getRobotExecutingAction() {
		return actingRobot;
	}

	public void shouldExplode(Cell[][] map, Robot[] robots){
		Cell dugCell = map[actionCoordinates.x][actionCoordinates.y];
		Robot robot = robots[actingRobot];
		Stack<Cell> explosionChain = new Stack<>();
		if(dugCell.item.type == Items.TRAP){
			explosionChain.push(dugCell);
		}
		while(!explosionChain.empty()){
			Cell explodingCell = explosionChain.pop();
			if(map[explodingCell.coordinates.x][explodingCell.coordinates.y-1].item.type == Items.TRAP){
				explosionChain.push(map[explodingCell.coordinates.x][explodingCell.coordinates.y-1]);
			}
			if(map[explodingCell.coordinates.x][explodingCell.coordinates.y+1].item.type == Items.TRAP){
				explosionChain.push(map[explodingCell.coordinates.x][explodingCell.coordinates.y+1]);
			}
			if(map[explodingCell.coordinates.x-1][explodingCell.coordinates.y].item.type == Items.TRAP){
				explosionChain.push(map[explodingCell.coordinates.x-1][explodingCell.coordinates.y]);
			}
			if(map[explodingCell.coordinates.x+1][explodingCell.coordinates.y].item.type == Items.TRAP){
				explosionChain.push(map[explodingCell.coordinates.x+1][explodingCell.coordinates.y]);
			}

			// Kill robots
			for (int i = 0; i < robots.length; i++) {
				Robot currentRobot = robots[i];
				if(currentRobot.coordinates.x>=explodingCell.coordinates.x-1 && currentRobot.coordinates.x<=explodingCell.coordinates.x+1
					&&currentRobot.coordinates.y>=explodingCell.coordinates.y-1 && currentRobot.coordinates.y<=explodingCell.coordinates.y+1
				){
					robot.die();
				}
			}
		}
	}
}
