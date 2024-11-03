package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInputEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.InputKey;

import java.util.Set;

public class EvtPlayerInput extends SkriptEvent {

	static {
		if (Skript.classExists("org.bukkit.Input")) {
			Skript.registerEvent("Player Input", EvtPlayerInput.class, PlayerInputEvent.class,
					"player input",
					"[player] press[ing] %inputkeys%")
				.description("Called when a player sends an updated input to the server.")
				.examples("on player input:",
					"\tsend \"You are pressing: %event-inputs%\" to player")
				.since("INSERT VERSION")
				.requiredPlugins("Minecraft 1.21.3+");
		}
	}

	private Literal<InputKey> keysToCheck;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
		if (matchedPattern == 1)
			//noinspection unchecked
			keysToCheck = (Literal<InputKey>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		PlayerInputEvent inputEvent = (PlayerInputEvent) event;
		if (keysToCheck != null) {
			Set<InputKey> givenKeys = InputKey.fromInput(inputEvent.getInput());
			keysToCheck.check(event, givenKeys::contains);
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return keysToCheck == null ? "player input" : "player pressing " + keysToCheck.toString(event, debug);
	}

}
