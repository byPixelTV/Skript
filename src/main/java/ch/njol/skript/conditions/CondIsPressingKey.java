package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInputEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.InputKey;

@Name("Is Pressing Key")
@Description("Checks if a player is pressing a certain input key.")
@Examples({
	"on player input:",
	"\tif player is pressing forward movement key:",
		"\t\tsend \"You are moving forward!\""
})
@Since("INSERT VERSION")
@Keywords({"press", "input"})
public class CondIsPressingKey extends Condition {

	static {
		if (Skript.classExists("org.bukkit.Input")) {
			Skript.registerCondition(CondIsPressingKey.class,
				"%players% (is|are) pressing %inputkeys%",
				"%players% (isn't|is not|aren't|are not) pressing %inputkeys%",
				"%players% (was|were) pressing %inputkeys%",
				"%players% (wasn't|was not|weren't|were not) pressing %inputkeys%"
			);
		}
	}

	private Expression<Player> players;
	private Expression<InputKey> inputKeys;
	private boolean past;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		players = (Expression<Player>) expressions[0];
		//noinspection unchecked
		inputKeys = (Expression<InputKey>) expressions[1];
		past = matchedPattern > 1;
		if (past && !getParser().isCurrentEvent(PlayerInputEvent.class))
			Skript.warning("Checking for the past state outside of a 'player input' event is useless.");
		setNegated(matchedPattern == 1 || matchedPattern == 3);
		return true;
	}

	@Override
	public boolean check(Event event) {
		Player eventPlayer = !past && event instanceof PlayerInputEvent inputEvent ? inputEvent.getPlayer() : null;
		InputKey[] inputKeys = this.inputKeys.getAll(event);
		boolean and = this.inputKeys.getAnd();
		return players.check(event, player -> {
			Input input;
			if (player.equals(eventPlayer)) {
				input = ((PlayerInputEvent) event).getInput();
			} else {
				input = player.getCurrentInput();
			}
			return CollectionUtils.check(inputKeys, inputKey -> inputKey.check(input), and);
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		StringBuilder builder = new StringBuilder();
		builder.append(players.toString(event, debug));
		if (past) {
			builder.append(players.isSingle() ? " was " : " were ");
		} else {
			builder.append(players.isSingle() ? " is " : " are ");
		}
		builder.append(isNegated() ? "not " : "");
		builder.append("pressing ");
		builder.append(inputKeys.toString(event, debug));
		return builder.toString();
	}

}
