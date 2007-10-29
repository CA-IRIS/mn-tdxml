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
 * Interface for receiving station data updates from DDS
 *
 * @author Douglas Lau
 */
public interface StationListener extends DdsListener {

	/** Update one station with new data */
	void update(StationSample s);
}
