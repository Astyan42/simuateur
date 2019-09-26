package GameEngine;

import enumerations.Items;

public class Item {
	public int playerOwner;
	public Items type;

	public Item(int playerOwner, Items type) {
		this.playerOwner = playerOwner;
		this.type = type;
	}
}
