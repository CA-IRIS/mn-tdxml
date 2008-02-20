/*
 * TDXML -- Traffic Data XML Reader
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
 * The <code>EventTime</code> interface represents an event time.
 * Each agency should implement this interface, e.g. CarsEventTime,
 * CHPEventTime, etc.
 *
 * Also, perhaps a name change to IncidentTime is more appropriate--it is
 * specific to the Incident, not Events within incidents. Also, some agencies
 * may use a time stamp within each Event (e.g. CHP).
 *
 * @author Erik Engstrom
 * @author Michael Darter
 *
 * @see us.mn.state.dot.tdxml.CarsEventTime
 */
public interface EventTime {

	/** Get a string representation */
	public String toString();

	/** Check if the event time is valid */
	public boolean isValid();
}
