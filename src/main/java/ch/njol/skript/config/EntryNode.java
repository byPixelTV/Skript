package ch.njol.skript.config;

import org.jetbrains.annotations.Nullable;

import java.util.Map.Entry;

public class EntryNode extends Node implements Entry<String, String> {

	private String value;

	public EntryNode(String key, String value, String comment, SectionNode parent, int lineNum) {
		super(key, comment, parent, lineNum);
		this.value = value;
	}

	public EntryNode(String key, String value, SectionNode parent) {
		super(key, parent);
		this.value = value;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String setValue(@Nullable String newValue) {
		if (newValue == null)
			return value;
		String copy = value;
		value = newValue;
		return copy;
	}

	@Override
	String save_i() {
		return key + config.getSaveSeparator() + value;
	}

}
