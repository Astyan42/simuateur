package enumerations;

import java.util.Arrays;
import java.util.Optional;

public enum Items {
	NOTHING(-1),
	RADAR(2),
	TRAP(3),
	ORE(4);
	private int key;

	Items(int key) {
		this.key = key;
	}

	public int getKey() {
		return key;
	}

	public static Items valueOf(int key) {
		return Arrays.stream(values())
			.filter(item -> item.key == key)
			.findFirst().get();
	}
}
