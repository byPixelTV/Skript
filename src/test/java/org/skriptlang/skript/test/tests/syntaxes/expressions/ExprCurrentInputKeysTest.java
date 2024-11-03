package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.bukkit.InputKey;

public class ExprCurrentInputKeysTest extends SkriptJUnitTest {

	private static final boolean SUPPORTS_INPUT = Skript.classExists("org.bukkit.Input");

	static {
		setShutdownDelay(1);
	}

	private Player player;
	private Expression<? extends InputKey> inputKeyExpression;

	@Before
	public void setup() {
		if (!SUPPORTS_INPUT)
			return;
		player = EasyMock.niceMock(Player.class);
		//noinspection unchecked
		inputKeyExpression = new SkriptParser("input keys of {_player}").parseExpression(InputKey.class);
	}

	@Test
	public void test() {
		if (!SUPPORTS_INPUT)
			return;
		if (inputKeyExpression == null)
			Assert.fail("Input keys expression is null");

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("player", player, event, true);

		EasyMock.expect(player.getCurrentInput()).andReturn(fromKeys(InputKey.FORWARD, InputKey.JUMP));
		EasyMock.replay(player);
		InputKey[] keys = inputKeyExpression.getArray(event);
		Assert.assertArrayEquals(keys, new InputKey[]{InputKey.FORWARD, InputKey.JUMP});
		EasyMock.verify(player);
	}

	private Input fromKeys(InputKey... keys) {
		Input input = EasyMock.niceMock(Input.class);
		for (InputKey key : keys) {
			switch (key) {
				case FORWARD -> EasyMock.expect(input.isForward()).andReturn(true);
				case BACKWARD -> EasyMock.expect(input.isBackward()).andReturn(true);
				case RIGHT -> EasyMock.expect(input.isRight()).andReturn(true);
				case LEFT -> EasyMock.expect(input.isLeft()).andReturn(true);
				case JUMP -> EasyMock.expect(input.isJump()).andReturn(true);
				case SNEAK -> EasyMock.expect(input.isSneak()).andReturn(true);
				case SPRINT -> EasyMock.expect(input.isSprint()).andReturn(true);
			}
		}
		EasyMock.replay(input);
		return input;
	}

}
