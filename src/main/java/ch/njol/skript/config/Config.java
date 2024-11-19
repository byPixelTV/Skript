package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.config.validate.SectionValidator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Represents a config file.
 */
public class Config implements Comparable<Config> {

	boolean simple;

	/**
	 * One level of the indentation, e.g. a tab or 4 spaces.
	 */
	private String indentation = "\t";
	/**
	 * The indentation's name, i.e. 'tab' or 'space'.
	 */
	private String indentationName = "tab";

	final String defaultSeparator;
	String separator;

	int level = 0;

	private final SectionNode main;

	int errors = 0;

	final boolean allowEmptySections;

	String fileName;
	@Nullable Path file = null;

	public Config(InputStream source, String fileName, @Nullable File file,
				  boolean simple, boolean allowEmptySections, String defaultSeparator) throws IOException {
		try (source) {
			this.fileName = fileName;
			if (file != null) // Must check for null before converting to path
				this.file = file.toPath();
			this.simple = simple;
			this.allowEmptySections = allowEmptySections;
			this.defaultSeparator = defaultSeparator;
			separator = defaultSeparator;

			if (source.available() == 0) {
				main = new SectionNode(this);
				Skript.warning("'" + getFileName() + "' is empty");
				return;
			}

			if (Skript.logVeryHigh())
				Skript.info("loading '" + fileName + "'");

			try (ConfigReader reader = new ConfigReader(source)) {
				main = SectionNode.load(this, reader);
			}
		}
	}

	public Config(InputStream source, String fileName, boolean simple,
				  boolean allowEmptySections, String defaultSeparator) throws IOException {
		this(source, fileName, null, simple, allowEmptySections, defaultSeparator);
	}

	public Config(File file, boolean simple,
				  boolean allowEmptySections, String defaultSeparator) throws IOException {
		this(Files.newInputStream(file.toPath()), file.getName(), simple, allowEmptySections, defaultSeparator);
		this.file = file.toPath();
	}

	public Config(@NotNull Path file, boolean simple,
				  boolean allowEmptySections, String defaultSeparator) throws IOException {
		this(Channels.newInputStream(FileChannel.open(file)), "" + file.getFileName(), simple, allowEmptySections, defaultSeparator);
		this.file = file;
	}

	/**
	 * Sets all {@link Option} fields of the given object to the values from this config
	 */
	public void load(Object object) {
		load(object.getClass(), object, "");
	}

	/**
	 * Sets all static {@link Option} fields of the given class to the values from this config
	 */
	public void load(Class<?> clazz) {
		load(clazz, null, "");
	}

	private void load(Class<?> clazz, @Nullable Object object, String path) {
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			if (object != null || Modifier.isStatic(field.getModifiers())) {
				try {
					if (OptionSection.class.isAssignableFrom(field.getType())) {
						OptionSection section = (OptionSection) field.get(object);
						@NotNull Class<?> pc = section.getClass();
						load(pc, section, path + section.key + ".");
					} else if (Option.class.isAssignableFrom(field.getType())) {
						((Option<?>) field.get(object)).set(this, path);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					assert false;
				}
			}
		}
	}

	void setIndentation(String indent) {
		assert indent != null && !indent.isEmpty() : indent;
		indentation = indent;
		indentationName = indent.charAt(0) == ' ' ? "space" : "tab";
	}

	/**
	 * Saves the config to a file.
	 *
	 * @param file The file to save to
	 * @throws IOException If the file could not be written to.
	 */
	public void save(File file) throws IOException {
		separator = defaultSeparator;
		PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
		try {
			main.save(writer);
		} finally {
			writer.flush();
			writer.close();
		}
	}

	/**
	 * @deprecated This copies all values from the other config and sets them in this config,
	 * which could be destructive for sensitive data if something goes wrong.
	 * Use {@link #updateKeys(Config)} instead.
	 */
	@Deprecated(forRemoval = true)
	public boolean setValues(final Config other) {
		return getMainNode().setValues(other.getMainNode());
	}

	/**
	 * @deprecated This copies all values from the other config and sets them in this config,
	 * which could be destructive for sensitive data if something goes wrong.
	 * Use {@link #updateKeys(Config)} instead.
	 */
	@Deprecated(forRemoval = true)
	public boolean setValues(final Config other, final String... excluded) {
		return getMainNode().setValues(other.getMainNode(), excluded);
	}

	/*
	PLAN:
	1) get nodes that have changed between configs
	2) get node differences
	3) add nodes:
		- if node at index is already occupied, replace it
		- if node at index is empty, add it
	 */

	/**
	 * Updates the keys of this config with the keys of another config.
	 * Used for updating a config file to a newer version.
	 * This method only sets keys that are missing in this config, thus preserving any existing values.
	 *
	 * @param newer The newer config to update from.
	 * @return True if any keys were added to this config, false otherwise.
	 */
	public boolean updateKeys(@NotNull Config newer) {
		Set<Node> newNodes = findNodes(newer.getMainNode());
		Set<Node> oldNodes = findNodes(getMainNode());

		System.out.println(newNodes);
		System.out.println(oldNodes);

		newNodes.removeAll(oldNodes);
		Set<Node> nodesToUpdate = new LinkedHashSet<>(newNodes);

		System.out.println(nodesToUpdate);

		if (nodesToUpdate.isEmpty())
			return false;

		for (Node node : nodesToUpdate) {
			int idx = node.getFullIndex();
			String path = node.getPath();
			SectionNode newParent = node.getParent();
			if (newParent == null)
				throw new IllegalStateException("newParent is null");

			String pathToParent = newParent.getPath();

			System.out.println(idx);
			System.out.println("path: " + path);
			System.out.println("pathToParent: " + pathToParent);

			SectionNode parent = getNode(pathToParent);
			System.out.println("parent: " + parent);
			if (parent == null)
				throw new IllegalStateException("parent is null");

			if (idx >= parent.size()) { // add
				System.out.println("adding due to oversize " + node);
				parent.add(node);
				continue;
			}

			Node existing = parent.getFull(idx);
			if (existing != null) { // replace existing
				System.out.println("replacing " + existing + " with " + node);
				parent.remove(existing);
				parent.add(idx, node);
			} else { // add
				System.out.println("adding " + node);
				parent.add(node);
			}
		}
		return true;
	}

	/**
	 * Recursively finds all nodes in a section node.
	 *
	 * @param node The parent node to search.
	 * @return A set of the discovered nodes, guaranteed to be in the order of discovery.
	 */
	@Contract(pure = true)
	private static @NotNull Set<Node> findNodes(@NotNull SectionNode node) {
		Set<Node> nodes = new LinkedHashSet<>();

		for (Iterator<Node> iterator = node.fullIterator(); iterator.hasNext(); ) {
			Node child = iterator.next();
			if (child instanceof SectionNode sectionNode) {
				nodes.add(child);
				nodes.addAll(findNodes(sectionNode));
			} else if (child instanceof EntryNode || child instanceof VoidNode) {
				nodes.add(child);
			}
		}
		return nodes;
	}

	private SectionNode getNode(String path) {
		return getNode(path.split("\\."));
	}

	private SectionNode getNode(String... path) {
		SectionNode node = getMainNode();
		for (String key : path) {
			Node child = node.get(key);

			if (child instanceof SectionNode sectionNode) {
				node = sectionNode;
			} else {
				return node;
			}
		}
		return node;
	}

	/**
	 * Compares the keys and values of this Config and another.
	 *
	 * @param other    The other Config.
	 * @param excluded Keys to exclude from this comparison.
	 * @return True if there are differences in the keys and their values
	 * of this Config and the other Config.
	 */
	public boolean compareValues(Config other, String... excluded) {
		return getMainNode().compareValues(other.getMainNode(), excluded);
	}

	/**
	 * Splits the given path at the dot character and passes the result to {@link #get(String...)}.
	 *
	 * @param path
	 * @return <tt>get(path.split("\\."))</tt>
	 */
	public @Nullable String getByPath(@NotNull String path) {
		return get(path.split("\\."));
	}

	/**
	 * Gets an entry node's value at the designated path
	 *
	 * @param path
	 * @return The entry node's value at the location defined by path or null if it either doesn't exist or is not an entry.
	 */
	public @Nullable String get(String... path) {
		SectionNode section = main;
		for (int i = 0; i < path.length; i++) {
			Node node = section.get(path[i]);
			if (node == null)
				return null;
			if (node instanceof SectionNode sectionNode) {
				if (i == path.length - 1)
					return null;
				section = sectionNode;
			} else {
				if (node instanceof EntryNode entryNode && i == path.length - 1)
					return entryNode.getValue();
				else
					return null;
			}
		}
		return null;
	}

	public HashMap<String, String> toMap(final String separator) {
		return main.toMap("", separator);
	}

	public boolean validate(final SectionValidator validator) {
		return validator.validate(getMainNode());
	}

	/**
	 * @return Whether the config is empty.
	 */
	public boolean isEmpty() {
		return main.isEmpty();
	}

	/**
	 * @return The file this config was loaded from, or null if it was loaded from an InputStream.
	 */
	public @Nullable File getFile() {
		if (file != null) {
			try {
				return file.toFile();
			} catch (Exception e) {
				return null; // ZipPath, for example, throws undocumented exception
			}
		}
		return null;
	}

	/**
	 * @return The path this config was loaded from, or null if it was loaded from an InputStream.
	 */
	public @Nullable Path getPath() {
		return file;
	}

	/**
	 * @return The most recent separator. Only useful while the file is loading.
	 */
	public String getSeparator() {
		return separator;
	}

	/**
	 * @return A separator string useful for saving, e.g. ": " or " = ".
	 */
	public String getSaveSeparator() {
		if (separator.equals(":"))
			return ": ";
		if (separator.equals("="))
			return " = ";
		return " " + separator + " ";
	}

	String getIndentation() {
		return indentation;
	}

	String getIndentationName() {
		return indentationName;
	}

	public SectionNode getMainNode() {
		return main;
	}

	public String getFileName() {
		return fileName;
	}

	@Override
	public int compareTo(@Nullable Config other) {
		if (other == null)
			return 0;
		return fileName.compareTo(other.fileName);
	}

}
