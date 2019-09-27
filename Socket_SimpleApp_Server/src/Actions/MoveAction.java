package Actions;

import GameEngine.Cell;
import GameEngine.GameEngine;
import GameEngine.Robot;
import enumerations.Items;

import java.awt.*;

public class MoveAction implements Action {
	int  actingRobot;
	private Point actionCoordinates;
	public MoveAction(int x, int y, int actingRobot) {

		this.actionCoordinates= new Point(x,y);
		this.actingRobot=actingRobot;
	}

	public Point getNextLocation(int currentX, int currentY, Point targetPoint) {
		for (int i = 0; i < 4; i++) {
			int dx = Math.abs(targetPoint.x - currentX);
			int dy = Math.abs(targetPoint.y - currentY);
			if (dy > dx) currentY += (targetPoint.y > currentY) ? 1 : -1;
			else currentX += (targetPoint.x > currentX) ? 1 : -1;
		}
		return new Point(currentX, currentY);
	}

	@Override
	public void execute(Cell[][] map, Robot[] robots) {
		// TODO Do nothing
		Robot robot = robots[actingRobot];
		if(robot.isDead()) return;
		int xCoordinate = robot.coordinates.x;
		int yCoordinate = robot.coordinates.y;
		robot.coordinates = getNextLocation(xCoordinate, yCoordinate, this.actionCoordinates);
		if(robot.coordinates.x == 1 && robot.carriedItem == Items.ORE){
			robot.carriedItem = Items.NOTHING;
			System.out.println("robot "+robot.robotId+" scores");
			GameEngine.scores[robot.owner-1] += 1;
		}
	}

	@Override
	public int getRobotExecutingAction() {
		return actingRobot;
	}
}
