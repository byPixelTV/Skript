package org.skriptlang.skript.test.tests.syntaxes.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
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

public class CondIsPressingKeyTest extends SkriptJUnitTest {

	private static final boolean SUPPORTS_INPUT = Skript.classExists("org.bukkit.Input");

	static {
		setShutdownDelay(1);
	}

	private Player testPlayer;
	private InputKey[] testInputKeys;
	private Condition isPressingKeyCondition;

	@Before
	public void setup() {
		if (!SUPPORTS_INPUT)
			return;
		testPlayer = EasyMock.niceMock(Player.class);
		testInputKeys = new InputKey[]{InputKey.FORWARD, InputKey.JUMP};
		isPressingKeyCondition = Condition.parse("{_player} is pressing {_input-keys::*}", null);
	}

	@Test
	public void test() {
		if (!SUPPORTS_INPUT)
			return;
		if (isPressingKeyCondition == null)
			Assert.fail("Is pressing key condition is null");

		ContextlessEvent event = ContextlessEvent.get();
		Variables.setVariable("player", testPlayer, event, true);
		Variables.setVariable("input-keys::1", testInputKeys[0], event, true);

		EasyMock.expect(testPlayer.getCurrentInput()).andReturn(fromKeys(testInputKeys));
		EasyMock.replay(testPlayer);
		assert isPressingKeyCondition.check(event);
		EasyMock.verify(testPlayer);

		EasyMock.resetToNice(testPlayer);
		EasyMock.expect(testPlayer.getCurrentInput()).andReturn(fromKeys(testInputKeys[1]));
		EasyMock.replay(testPlayer);
		assert !isPressingKeyCondition.check(event);
		EasyMock.verify(testPlayer);

		EasyMock.resetToNice(testPlayer);
		Variables.setVariable("input-keys::2", testInputKeys[1], event, true);
		EasyMock.expect(testPlayer.getCurrentInput()).andReturn(fromKeys(testInputKeys));
		EasyMock.replay(testPlayer);
		assert isPressingKeyCondition.check(event);
		EasyMock.verify(testPlayer);

		EasyMock.resetToNice(testPlayer);
		Variables.setVariable("input-keys::3", InputKey.SNEAK, event, true);
		EasyMock.expect(testPlayer.getCurrentInput()).andReturn(fromKeys(testInputKeys));
		EasyMock.replay(testPlayer);
		assert !isPressingKeyCondition.check(event);
		EasyMock.verify(testPlayer);
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
