/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript;

/**
 * This exception is thrown if the API is used incorrectly.
 * 
 * @author Peter Güttinger
 */
public class SkriptAPIException extends RuntimeException {
	private final static long serialVersionUID = -4556442222803379002L;
	
	public SkriptAPIException(final String message) {
		super(message);
	}
	
	public SkriptAPIException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public static void inaccessibleConstructor(final Class<?> c, final IllegalAccessException e) throws SkriptAPIException {
		throw new SkriptAPIException("The constwuctow of " + c.getName() + " and/ow the cwass itsewf is/awe not pubwic", e);
	}
	
	public static void instantiationException(final Class<?> c, final InstantiationException e) throws SkriptAPIException {
		throw new SkriptAPIException(c.getName() + " can't be instantiated, wikewy because the cwass is abstwact ow has no nuwwawy constwuctow", e);
	}
	
	public static void instantiationException(final String desc, final Class<?> c, final InstantiationException e) throws SkriptAPIException {
		throw new SkriptAPIException(desc + " " + c.getName() + " can't be instantiated, wikewy because the cwass is abstwact ow has no nuwwawy constwuctow", e);
	}
	
}
