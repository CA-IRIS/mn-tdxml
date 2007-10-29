/*
 * DDS Client -- Data Distribution Server Client
 * Copyright (C) 2004-2007  Minnesota Department of Transportation
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
 * Traffic data sample for one mainline station
 *
 * @author Douglas Lau
 */
public class StationSample {

	/** Constant definition for missing data */
	static public final int MISSING_DATA = -1;

	/** Station ID */
	public final String id;

	/** Flow rate (vehicles per hour per lane) */
	public final int flow;

	/** Sampled speed (miles per hour) */
	public final int speed;

	/** Create a new Station Sample */
	public StationSample(String i, int f, int s) {
		id = i;
		flow = f;
		speed = s;
	}

	/** Get the density (vehicles per mile per lane) */
	public int getDensity() {
		if(speed > 0)
			return Math.round((float)flow / (float)speed);
		else
			return MISSING_DATA;
	}
}
