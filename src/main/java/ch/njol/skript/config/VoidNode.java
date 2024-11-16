package ch.njol.skript.config;

/**
 * An empty line or a comment.
 * <p>
 * The subclass {@link InvalidNode} is used for invalid non-empty nodes, i.e. where a parsing error occurred.
 * </p>
 */
public class VoidNode extends Node {

	VoidNode(String line, String comment, SectionNode parent, int lineNum) {
		super(line.trim(), comment, parent, lineNum);
	}

	@Override
	public String getKey() {
		return key;
	}

	public void set(String s) {
		key = s;
	}

	@Override
	String save_i() {
		return key;
	}

}
