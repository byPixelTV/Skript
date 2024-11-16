package ch.njol.skript.config;

/**
 * A line of a config that could not be parsed.
 */
public class InvalidNode extends VoidNode {

	public InvalidNode(String value, String comment, SectionNode parent, int lineNum) {
		super(value, comment, parent, lineNum);
		config.errors++;
	}

}
