package ch.njol.skript.config;

public class SimpleNode extends Node {

	public SimpleNode(String value, String comment, int lineNum, SectionNode parent) {
		super(value, comment, parent, lineNum);
	}

	public SimpleNode(Config c) {
		super(c);
	}

	@Override
	String save_i() {
		return key;
	}

	public void set(String s) {
		key = s;
	}

}
