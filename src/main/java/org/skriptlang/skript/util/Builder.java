package org.skriptlang.skript.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

/**
 * An interface providing common methods to be implemented for any builder.
 *
 * @param <T> The type of object being built.
 */
@ApiStatus.Experimental
public interface Builder<B extends Builder<B, T>, T> {

	/**
	 * @return An object of <code>T</code> built from the values specified by this builder.
	 */
	@Contract("-> new")
	T build();

	/**
	 * Applies the values of this builder onto <code>builder</code>.
	 * @param builder The builder to apply values onto.
	 */
	void applyTo(B builder);

}
