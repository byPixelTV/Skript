package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInputEvent;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.bukkit.input.InputKey;

public class EvtPlayerInputTest extends SkriptJUnitTest {

	private static final boolean SUPPORTS_INPUT = Skript.classExists("org.bukkit.Input");

	static {
		setShutdownDelay(1);
	}

	private Player player;

	@Before
	public void setup() {
		if (!SUPPORTS_INPUT)
			return;
		player = EasyMock.niceMock(Player.class);
	}

	@Test
	public void test() {
		if (!SUPPORTS_INPUT)
			return;
		Input pastInput = fromKeys(InputKey.FORWARD);
		Input futureInput = fromKeys(InputKey.FORWARD, InputKey.JUMP);
		EasyMock.expect(player.getCurrentInput()).andStubReturn(pastInput);
		EasyMock.replay(player);
		Bukkit.getPluginManager().callEvent(new PlayerInputEvent(player, futureInput));
		EasyMock.verify(player);
	}

	private Input fromKeys(InputKey... keys) {
		Input input = EasyMock.niceMock(Input.class);
		for (InputKey key : keys) {
			switch (key) {
				case FORWARD -> EasyMock.expect(input.isForward()).andStubReturn(true);
				case BACKWARD -> EasyMock.expect(input.isBackward()).andStubReturn(true);
				case RIGHT -> EasyMock.expect(input.isRight()).andStubReturn(true);
				case LEFT -> EasyMock.expect(input.isLeft()).andStubReturn(true);
				case JUMP -> EasyMock.expect(input.isJump()).andStubReturn(true);
				case SNEAK -> EasyMock.expect(input.isSneak()).andStubReturn(true);
				case SPRINT -> EasyMock.expect(input.isSprint()).andStubReturn(true);
			}
		}
		EasyMock.replay(input);
		return input;
	}

}
