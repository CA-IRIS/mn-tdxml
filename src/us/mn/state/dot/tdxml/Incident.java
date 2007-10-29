/*
 * TDXML -- Traffic Data XML Reader
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
package us.mn.state.dot.tdxml;

/**
 * The <code>Incident</code> class represents a traffic incident.
 *
 * @author Douglas Lau
 * @author Erik Engstrom
 */
public interface Incident extends Comparable {

	/** Get the message ID */
	String getMessageId();

	/** Get the event time */
	EventTime getTime();

	/** Check if an incident is valid */
	boolean isValid();

	/** Get the start location */
	Location getStartLocation();

	/** Get the start location */
	Location getEndLocation();

	/** Get the description */
	IncidentDescription getDescription();
}
