package GameEngine;

import enumerations.Items;

import java.awt.*;

public class Cell {
	public Item item;
	public int ore;
	public boolean hole;
	public boolean known = true;
	public Point coordinates;
	public Cell(Item item, int ore, boolean hole, int x, int y) {
		this.item = item;
		this.ore = ore;
		this.hole = hole;
		this.coordinates = new Point(x,y);
	}

	public boolean isNeighbour(Robot r){
		if(r.coordinates.x == this.coordinates.x){
			if(r.coordinates.y >= this.coordinates.y-1 && r.coordinates.y <= this.coordinates.y+1){
				return true;
			}
		}
		if(r.coordinates.y == this.coordinates.y){
			if(r.coordinates.x >= this.coordinates.x-1 && r.coordinates.x <= this.coordinates.x+1){
				return true;
			}
		}
		return false;
	}

	public Item itemVisibleFor(int player){
		if(this.item.playerOwner == player){
			return this.item;
		}
		else{
			return new Item(player, Items.NOTHING);
		}
	}
}
