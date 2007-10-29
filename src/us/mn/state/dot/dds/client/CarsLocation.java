/*
 * DDS Client -- Data Distribution Server Client
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.dds.client;

/**
 * Encapsulates all the data for a cars incident location.
 *
 * @author Erik Engstrom
 */
public class CarsLocation implements Location {

	private final double easting;
	private final double northing;
	private final double linear;
	private final String name;
	private final char direction;
	private final char defaultDirection;
	private final boolean metro;

	/** Create a new CARS location */
	public CarsLocation(double easting, double northing, double linear,
		String name, char direction, char defaultDirection,
		boolean metro)
	{
		this.easting = easting;
		this.northing = northing;
		this.linear = linear;
		this.name = name;
		this.direction = direction;
		this.defaultDirection = defaultDirection;
		this.metro = metro;
	}

	public String toString() {
		return name; //"" + linear;
	}

	public double getEasting() {
		return easting;
	}

	public double getLinear() {
		return linear;
	}

	public double getNorthing() {
		return northing;
	}

	public char getDirection() {
		return direction;
	}

	public char getDefaultDirection() {
		return defaultDirection;
	}

	public boolean isInMetro(){
		return metro;
	}
}
