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
package ch.njol.skript.expressions;

import java.util.Arrays;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Passenger")
@Description({"The passenger of a vehicle, or the rider of a mob.",
		"For 1.11.2 and above, it returns a list of passengers and you can use all changers in it.",
		"See also: <a href='#ExprVehicle'>vehicle</a>"})
@Examples({"#for 1.11 and lower",
		"passenger of the minecart is a creeper or a cow",
		"the saddled pig's passenger is a player",
		"#for 1.11.2+",
		"passengers of the minecart contains a creeper or a cow",
		"the boat's passenger contains a pig",
		"add a cow and a zombie to passengers of last spawned boat",
		"set passengers of player's vehicle to a pig and a horse",
		"remove all pigs from player's vehicle",
		"clear passengers of boat"})
@Since("2.0, 2.2-dev26 (Multiple passengers for 1.11.2+)")
public class ExprPassengers extends PropertyExpression<Entity, Entity> {

	// In 1.20 Spigot moved this event from the package org.spigotmc.event to org.bukkit.event
	private static final boolean HAS_NEW_MOUNT_EVENTS = Skript.classExists("org.bukkit.event.entity.EntityMountEvent");

	static {
		registerDefault(ExprPassengers.class, Entity.class, "passenger[:s]", "entities");
	}

	private boolean plural;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Entity>) exprs[0]);
		plural = parseResult.hasTag("s");
		return true;
	}

	@Override
	protected Entity[] get(Event event, Entity[] source) {
		Converter<Entity, Entity[]> converter = entity -> {
			if (getTime() != EventValues.TIME_PAST && event instanceof VehicleEnterEvent vehicleEnterEvent && entity.equals(vehicleEnterEvent.getVehicle()))
				return new Entity[] {vehicleEnterEvent.getEntered()};
			if (getTime() != EventValues.TIME_FUTURE && event instanceof VehicleExitEvent vehicleExitEvent && entity.equals(vehicleExitEvent.getVehicle()))
				return new Entity[] {vehicleExitEvent.getExited()};
			if (HAS_NEW_MOUNT_EVENTS) {
				if (getTime() != EventValues.TIME_PAST && event instanceof org.bukkit.event.entity.EntityMountEvent entityMountEvent && entity.equals(entityMountEvent.getEntity()))
					return new Entity[] {entityMountEvent.getEntity()};
				if (getTime() != EventValues.TIME_FUTURE && event instanceof org.bukkit.event.entity.EntityDismountEvent entityDismountEvent && entity.equals(entityDismountEvent.getEntity()))
					return new Entity[] {entityDismountEvent.getEntity()};
			}
			return entity.getPassengers().toArray(new Entity[0]);
		};
		return Arrays.stream(source)
				.map(converter::convert)
				.flatMap(Arrays::stream)
				.toArray(Entity[]::new);
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case DELETE:
			case REMOVE:
			case REMOVE_ALL:
			case RESET:
			case SET:
				return CollectionUtils.array(Entity[].class, EntityData[].class);
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Entity[] vehicles = getExpr().getArray(event);
		switch (mode) {
			case SET:
				for (Entity vehicle : vehicles)
					vehicle.eject();
				//$FALL-THROUGH$
			case ADD:
				for (Object object : delta) {
					for (Entity vehicle : vehicles) {
						Entity passenger = object instanceof Entity ? (Entity) object : ((EntityData<?>) object).spawn(vehicle.getLocation());
						vehicle.addPassenger(passenger);
					}
				}
				break;
			case REMOVE_ALL:
			case REMOVE:
				for (Object object : delta) {
					for (Entity vehicle : vehicles) {
						if (object instanceof Entity) {
							vehicle.removePassenger((Entity) object);
						} else {
							for (Entity passenger : vehicle.getPassengers()) {
								if (passenger != null && ((EntityData<?>) object).isInstance((passenger)))
									vehicle.removePassenger(passenger);
							}
						}
					}
				}
				break;
			case DELETE:
			case RESET:
				for (Entity vehicle : vehicles)
					vehicle.eject();
				break;
			default:
				break;
		}
	}

	@Override
	public boolean isSingle() {
		return !plural && getExpr().isSingle();
	}

	@Override
	public boolean setTime(int time) {
		if (time == EventValues.TIME_PAST)
			super.setTime(time, getExpr(), EntityDismountEvent.class, VehicleExitEvent.class);
		if (time == EventValues.TIME_FUTURE)
			return super.setTime(time, getExpr(), EntityMountEvent.class, VehicleEnterEvent.class);
		return super.setTime(time, getExpr(), EntityDismountEvent.class, VehicleExitEvent.class, EntityMountEvent.class, VehicleEnterEvent.class);
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "passenger" + (plural ? "s " : " ") + "of " + getExpr().toString(event, debug);
	}

}
