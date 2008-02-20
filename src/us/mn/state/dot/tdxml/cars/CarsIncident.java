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
import java.util.List;

import us.mn.state.dot.tdxml.EventTime;
import us.mn.state.dot.tdxml.Incident;
import us.mn.state.dot.tdxml.IncidentDescription;
import us.mn.state.dot.tdxml.Location;

/**
 *
 *
 * @author Erik Engstrom
 */
public class CarsIncident implements Incident {

	public static final int LOCATION_TYPE_LINK = 0;

	public static final int LOCATION_TYPE_AREA = 1;

	private CarsEvent keyPhrase;

	private List<CarsEvent> events;

	private String additionalText;

	private String roadway;

	private CarsLocation startLocation;

	private CarsLocation endLocation;

	private EventTime time;

	private String direction;

	private String sign;

	private int location_type = LOCATION_TYPE_LINK;

	/** The CARS id for the incident. */
	private String messageId;

	/**
	 * Constructor for CarsIncident.
	 */
	public CarsIncident() {
		super();
	}

	public String toString() {
		String roadname = null;
		if (location_type == LOCATION_TYPE_AREA){
			roadname = "";
		} else {
			char dir = startLocation.getDirection();
			switch (dir){
				case 'X':
					roadname = roadway + " in both directions";
					break;
				case 'N':
				case 'S':
				case 'E':
				case 'W':
					roadname  = dir + "B " + roadway;
					break;
				case '?':
				default:
					roadname = roadway;
					break;
			}
		}
		StringBuffer buffer = new StringBuffer(
			keyPhrase.getType().substring(0,1).toUpperCase());
		buffer.append(keyPhrase.getType().substring(1));
		buffer.append(" - ").append(roadname);
		buffer.append("\n\t").append(getPhrases());
		if ( additionalText != null ){
			buffer.append(", ").append(additionalText);
		}
		buffer.append(" on ").append(roadname);
		if (endLocation != null) {
			buffer.append(" from ").append(startLocation).append(" to ");
			buffer.append(endLocation);
		} else {
			buffer.append(" at ").append(startLocation);
		}
		buffer.append(' ').append(time.toString()).append('.');
		return buffer.toString();
	}

	public StringBuffer getPhrases(){
		StringBuffer buffer = new StringBuffer();
		for(CarsEvent event: events) {
			String temp = event.getMessage();
			temp = temp.replace('-', ' ');
			buffer.append(temp).append(", ");
		}
		if(buffer.length() > 2) {
			buffer.delete(buffer.length() - 2, buffer.length());
			buffer.setCharAt(0, Character.toUpperCase(buffer.charAt(0)));
		}
		return buffer;
	}

	/** Get the end location */
	public Location getEndLocation() {
		return endLocation;
	}

	/**
	 * Returns the freeway.
	 * @return String
	 */
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
	 * Returns the keyPhrase.
	 * @return String
	 */
	public CarsEvent getKeyPhrase() {
		return keyPhrase;
	}

	/**
	 * Sets the keyPhrase.
	 */
	public void setKeyPhrase(CarsEvent event) {
		this.keyPhrase = event;
	}

	/**
	 * Returns the time.
	 * @return EventTime
	 */
	public EventTime getTime() {
		return time;
	}

	/**
	 * Sets the time.
	 * @param time The time to set
	 */
	public void setTime(EventTime time) {
		this.time = time;
	}

	/**
	 * @see us.mn.state.dot.tdxml.client.Incident#getDescription()
	 */
	public IncidentDescription getDescription() {
		char dir = startLocation.getDirection();
		switch(dir){ //TODO need a better solution for this.
			case 'E':
				direction="Eastbound";
				break;
			case 'W':
				direction="Westbound";
				break;
			case 'N':
				direction="Northbound";
			    break;
			case 'S':
				direction="Southbound";
				break;
			case 'X':
				char defaultDir = startLocation.getDefaultDirection();
				direction=calcBothDirections( defaultDir );
			default:
				break;
		}
		String endDescription = null;
		if ( endLocation != null ){
			endDescription = endLocation.toString(); //TODO this is pretty damn ugly fix me!
		}
		return new IncidentDescription(
			direction, roadway, getPhrases().toString(),
			startLocation.toString(), endDescription,
			time.toString(), sign );
	}

	private String calcBothDirections( char defaultDirection ){
		String result = "";
		switch( defaultDirection ){
			case 'E': case 'W':
				result = "EastWest";
				break;
			case 'N': case 'S':
				result = "NorthSouth";
				break;
			default:
				break;
		}
		return result;
	}

	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		CarsIncident comp = ( CarsIncident) o;
		return getDescription().compareTo( comp.getDescription() );
	}

	public void setSign(String string) {
		sign = string;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String string) {
		messageId = string;
	}

	public void setEvents(List<CarsEvent> list) {
		events = list;
	}

	public String getAdditionalText() {
		return additionalText;
	}

	public void setAdditionalText(String string) {
		additionalText = string;
	}

	public void setLocation_type(int i) {
		location_type = i;
	}

	protected boolean isLocationTypeValid() {
		return location_type != LOCATION_TYPE_AREA;
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
		return isLocationTypeValid() && isInMetro() && isTimeValid() &&
			isEventValid();
	}
}
