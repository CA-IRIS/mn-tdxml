/*
 * TDXML -- Traffic Data XML Reader
 * Copyright (C) 2008  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tdxml;

/**
 * Direction-of-travel enumeration.
 *
 * @author Douglas Lau
 */
public enum Direction {

	/** Enumerated direction values */
	UNKNOWN("Unknown", "?"),
	NORTH("Northbound", "NB"),
	SOUTH("Southbound", "SB"),
	EAST("Eastbound", "EB"),
	WEST("Westbound", "WB"),
	NORTH_SOUTH("NorthSouth", "N-S"),
	EAST_WEST("EastWest", "E-W");

	/** Full direction string */
	protected final String dir;

	/** Abbreviated direction string */
	protected final String abbrev;

	/** Create a new direction */
	private Direction(String d, String a) {
		dir = d;
		abbrev = a;
	}

	/** Get the string representation of a direction */
	public String toString() {
		return dir;
	}

	/** Get the abbreviated string */
	public String toAbbrev() {
		return abbrev;
	}

	/** Get a direction from a string */
	static public Direction fromString(String sd) {
		String ud = sd.toUpperCase();
		for(Direction d: Direction.values()) {
			if(d.dir.startsWith(ud))
				return d;
		}
		return UNKNOWN;
	}
}
