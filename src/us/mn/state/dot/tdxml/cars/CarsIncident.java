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
package us.mn.state.dot.tdxml.cars;

import java.util.Iterator;
import java.util.LinkedList;

import us.mn.state.dot.tdxml.Direction;
import us.mn.state.dot.tdxml.EventTime;
import us.mn.state.dot.tdxml.Incident;
import us.mn.state.dot.tdxml.IncidentDescription;
import us.mn.state.dot.tdxml.Location;

/**
 * An incident retrieved from the CARS system.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class CarsIncident implements Incident {

	/** Message ID for the incident */
	protected final String messageId;

	/** Event time */
	protected final EventTime time;

	/** Key event */
	protected final CarsEvent key_event;

	/** Additional text */
	protected final String additionalText;

	/** Sign to display for the incident */
	protected final String sign;

	/** List of all events for the incident */
	protected final LinkedList<CarsEvent> events =
		new LinkedList<CarsEvent>();

	private String roadway;

	/** Starting location for incident */
	private CarsLocation startLocation;

	/** Ending location for incident */
	private CarsLocation endLocation;

	/** Create a new CARS incident */
	public CarsIncident(String mid, EventTime t, CarsEvent event,
		String a_text, String s)
	{
		messageId = mid;
		time = t;
		key_event = event;
		additionalText = a_text;
		sign = s;
	}

	/** Get the roadway name for an incident */
	protected String getRoadname() {
		if(isLocationValid()) {
			Direction dir = startLocation.getDirection();
			switch(dir) {
				case NORTH_SOUTH:
				case EAST_WEST:
					return roadway + " in both directions";
				case UNKNOWN:
					return roadway;
				default:
					return dir.toAbbrev() + " " + roadway;
			}
		} else
			return "";
	}

	public String toString() {
		String roadname = getRoadname();
		StringBuffer buffer = new StringBuffer(
			key_event.getType().substring(0,1).toUpperCase());
		buffer.append(key_event.getType().substring(1));
		buffer.append(" - ");
		buffer.append(roadname);
		buffer.append("\n\t");
		buffer.append(getPhrases());
		if(additionalText != null) {
			buffer.append(", ");
			buffer.append(additionalText);
		}
		buffer.append(" on ");
		buffer.append(roadname);
		if(endLocation != null) {
			buffer.append(" from ");
			buffer.append(startLocation);
			buffer.append(" to ");
			buffer.append(endLocation);
		} else
			buffer.append(" at ").append(startLocation);
		buffer.append(' ').append(time.toString()).append('.');
		return buffer.toString();
	}

	/** Get the phrases for each event */
	protected String getPhrases() {
		StringBuilder b = new StringBuilder();
		for(CarsEvent event: events) {
			String temp = event.getMessage();
			b.append(temp.replace('-', ' '));
			b.append(", ");
		}
		if(b.length() > 2) {
			b.delete(b.length() - 2, b.length());
			b.setCharAt(0, Character.toUpperCase(b.charAt(0)));
		}
		return b.toString();
	}

	/** Get the end location */
	public Location getEndLocation() {
		return endLocation;
	}

	/** Returns the freeway */
	public String getRoadway() {
		return roadway;
	}

	/** Get the startLocation */
	public Location getStartLocation() {
		return startLocation;
	}

	/**
	 * Sets the endLocation.
	 * @param endLocation The endLocation to set
	 */
	public void setEndLocation(CarsLocation endLocation) {
		this.endLocation = endLocation;
	}

	/**
	 * Sets the roadway.
	 * @param roadway The roadway to set
	 */
	public void setRoadway(String roadway) {
		this.roadway = roadway;
	}

	/**
	 * Sets the startLocation.
	 * @param startLocation The startLocation to set
	 */
	public void setStartLocation(CarsLocation startLocation) {
		this.startLocation = startLocation;
	}

	/**
	 * Returns the time.
	 * @return EventTime
	 */
	public EventTime getTime() {
		return time;
	}

	/**
	 * @see us.mn.state.dot.tdxml.client.Incident#getDescription()
	 */
	public IncidentDescription getDescription() {
		Direction direction = startLocation.getDirection();
		String endDescription = null;
		if(endLocation != null)
			endDescription = endLocation.toString();
		return new IncidentDescription(direction, roadway,
			getPhrases(), startLocation.toString(),
			endDescription, time.toString(), sign);
	}

	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Incident o) {
		return getDescription().compareTo(o.getDescription());
	}

	/** Get the message ID */
	public String getMessageId() {
		return messageId;
	}

	/** Add an event to the incident */
	public void addEvent(CarsEvent e) {
		events.add(e);
	}

	protected boolean isLocationValid() {
		return startLocation != null;
	}

	protected boolean isInMetro() {
		return startLocation.isInMetro() &&
			(endLocation == null || endLocation.isInMetro());
	}

	protected boolean isTimeValid() {
		return time.isValid();
	}

	protected boolean isEventValid() {
		Iterator<CarsEvent> it = events.iterator();
		CarsEvent event = it.next();
		String type = event.getType();
		if(event.getMessage().equals("flooding"))
			return true;
		if(type.equals("incident"))
			return true;
		if(type.equals("roadwork")) {
			while(it.hasNext()) {
				event = it.next();
				String name = event.getType();
				String message = event.getMessage();
				if(name.equals("closures") &&
					!(message.matches(".*(ramp).*$") ||
					message.matches(".*(shoulder).*$")))
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean isValid() {
		return isLocationValid() && isInMetro() && isTimeValid() &&
			isEventValid();
	}
}
