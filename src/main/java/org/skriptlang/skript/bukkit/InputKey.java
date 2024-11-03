package org.skriptlang.skript.bukkit;

import org.bukkit.Input;

import java.util.EnumSet;
import java.util.Set;

public enum InputKey {

	FORWARD("forward movement key"),
	BACKWARD("backward movement key"),
	RIGHT("right movement key"),
	LEFT("left movement key"),
	JUMP("jumping key"),
	SNEAK("sneaking key"),
	SPRINT("sprinting key");

	private final String toString;

	InputKey(String toString) {
		this.toString = toString;
	}

	public boolean check(Input input) {
		return switch (this) {
			case FORWARD -> input.isForward();
			case BACKWARD -> input.isBackward();
			case RIGHT -> input.isRight();
			case LEFT -> input.isLeft();
			case JUMP -> input.isJump();
			case SNEAK -> input.isSneak();
			case SPRINT -> input.isSprint();
		};
	}

	@Override
	public String toString() {
		return toString;
	}

	public static Set<InputKey> fromInput(Input input) {
		Set<InputKey> keys = EnumSet.noneOf(InputKey.class);
		for (InputKey key : values()) {
			if (key.check(input))
				keys.add(key);
		}
		return keys;
	}

}
