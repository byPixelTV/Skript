package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Name("Spread Across")
@Description("A general effect to spread a list of objects across a number of settable expressions")
@Examples({
	"spread 1, 2, 3, 4 and 5 across {_one}, {_two}, {_three and four::*} and {_five}",
	"spread (shuffled all players) across {_winner} and {_losers::*}",
})
@Since("INSERT VERSION")
public class EffSpread extends Effect {

	static {
		Skript.registerEffect(EffSpread.class, "spread %objects% across %objects%");
	}

	private static final ClassInfoReference SINGLE_OBJECT_REFERENCE = new ClassInfoReference(DefaultClasses.OBJECT);

	private Expression<?> objectsToSpread;
	private ExpressionList<?> spreadTarget;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		objectsToSpread = expressions[0];
		if (objectsToSpread.isSingle()) {
			Skript.error("You must provide more than one object to spread");
			return false;
		}
		Expression<?> potentialSpreadTarget = expressions[1];
		if (!(potentialSpreadTarget instanceof ExpressionList<?>)) {
			Skript.error("The spread target must be a list of settable expressions");
			return false;
		}
		spreadTarget = (ExpressionList<?>) potentialSpreadTarget;
		if (!spreadTarget.getAnd()) {
			return false;
		}
		if (!listChildrenCanBeSet(spreadTarget)) {
			Skript.error("All expressions in the spread target list must be settable");
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object[] objectsToSpread = this.objectsToSpread.getArray(event);
		Expression<?>[] spreadTargets = spreadTarget.getExpressions();
		int finalObjectIndex = objectsToSpread.length - 1;
		int objectIndex = 0;
		for (int targetIndex = 0; targetIndex < spreadTargets.length; targetIndex++) {
			if (objectIndex > finalObjectIndex) {
				return;
			}
			Expression<?> spreadTarget = spreadTargets[targetIndex];
			if (spreadTarget.isSingle()) {
				spreadTarget.change(event, new Object[] { objectsToSpread[objectIndex] }, ChangeMode.SET);
				objectIndex++;
			} else {
				int remainingElements = computeRemainingElements(spreadTargets, targetIndex);
				// if there's fewer objects available than remaining, we don't want to be greedy
				if ((objectIndex + remainingElements) > finalObjectIndex) {
					spreadTarget.change(event, new Object[] { objectsToSpread[objectIndex] }, ChangeMode.SET);
					objectIndex++;
				} else {
					// otherwise, take as much as we can leaving only enough for the remaining spread targets
					Object[] objectsSlice = Arrays.copyOfRange(objectsToSpread, objectIndex, objectsToSpread.length - remainingElements);
					objectIndex += objectsSlice.length;
					spreadTarget.change(event, objectsSlice, ChangeMode.SET);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "spread " + objectsToSpread.toString(event, debug) + " across " + spreadTarget.toString(event, debug);
	}

	private boolean listChildrenCanBeSet(ExpressionList<?> list) {
		for (Expression<?> child : list.getExpressions()) {
			if (!ChangerUtils.acceptsChange(child, ChangeMode.SET, SINGLE_OBJECT_REFERENCE)) {
				return false;
			}
		}
		return true;
	}

	private int computeRemainingElements(Expression<?>[] spreadTargets, int currentTargetIndex) {
		int remainingElements = 0;
		for (currentTargetIndex += 1; currentTargetIndex < spreadTargets.length; currentTargetIndex++) {
			remainingElements += 1;
		}
		return remainingElements;
	}

}
