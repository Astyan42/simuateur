package GameEngine;

import enumerations.Items;

import java.awt.*;

public class Robot {
	public Point coordinates=new Point();
	public Items carriedItem= Items.NOTHING;
	public int owner=-1;
	public boolean dead = false;
	public Robot(Point coordinates, int owner) {
		this.coordinates = coordinates;
		this.owner = owner;
	}

	public boolean isInHQ(){
		return this.coordinates.x == 1;
	}

	public boolean isDead(){
		return this.dead;
	}

	public void die(){
		this.dead = true;
		this.coordinates = new Point(-1,-1);
	}
}
