/*
 * TDXML -- Traffic Data XML Reader
 * Copyright (C) 2003-2008  Minnesota Department of Transportation
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

package gov.ca.dot.d10.tdxml;

import us.mn.state.dot.tdxml.Direction;
import us.mn.state.dot.tdxml.EventTime;
import us.mn.state.dot.tdxml.Incident;
import us.mn.state.dot.tdxml.IncidentDescription;
import us.mn.state.dot.tdxml.Location;

import java.util.Iterator;
import java.util.List;

/**
 * A CHP Incident
 *
 * @author Erik Engstrom
 * @author Michael Darter
 */
public class CHPIncident implements Incident
{
	private String m_area = "";      // <Area> e.g. "Bakersfield"
	private String m_areaTB = "";    // <ThomasBrothers> e.g. "22 2C"

	// attributes
	private String m_centerId = "";               // Center ID e.g. STCC
	private IncidentDescription m_desc = null;    // description
	private String m_dispatchId = "";             // Dispatch ID e.g. SKCC
	private CHPLocation m_endLoca = null;         // not used
	private String m_locationDesc =
		"";    // <Location> e.g. "NB SR99 JSO S UNION AV"
	private String m_messageId =
		"";    // CHP id for the incident. e.g. "0132D0130"
	private String m_type =
		"";    // <LogType> e.g. "1125A - Traffic Hazard - Animal"
	private CHPLocation m_startLoca =
		new CHPLocation();    // <TBXY> e.g. "6261858:2249615"
	private EventTime m_eventtime =
		new CHPEventTime();    // event time <LogTime>
	private List<CHPEvent> m_events;    // list of events

	/**
	 * Constructor for CHPIncident.
	 */
	public CHPIncident() {
		super();
	}

	/** Get the center ID */
	public String getCenterId() {
		return (m_centerId);
	}

	/** Set the center ID */
	public void setCenterId(String x) {
		m_centerId = x;
	}

	/** Get the dispatch ID */
	public String getDispatchId() {
		return (m_dispatchId);
	}

	/** Set the dispatch ID */
	public void setDispatchId(String x) {
		m_dispatchId = x;
	}

	/** Get the message ID */
	public String getMessageId() {
		return (m_messageId);
	}

	/** Set the message ID */
	public void setMessageId(String x) {
		m_messageId = x;
	}

	/** Get the LocationDesc */
	public String getLocationDesc() {
		return (m_locationDesc);
	}

	/** Set the LocationDesc */
	public void setLocationDesc(String x) {
		m_locationDesc = x;
	}

	/** Get the AreaTB */
	public String getAreaTB() {
		return (m_areaTB);
	}

	/** Set the AreaTB */
	public void setAreaTB(String x) {
		m_areaTB = x;
	}

	/** Get the Type */
	public String getType() {
		return (m_type);
	}

	/** Set the Type */
	public void setType(String x) {
		m_type = x;
	}

	/** Get area */
	public String getArea() {
		return (m_area);
	}

	/** Set the area */
	public void setArea(String x) {
		m_area = x;
	}

	/** Get the event time */
	public EventTime getTime() {
		return (this.m_eventtime);
	}

	/** Set the event time */
	public void setTime(EventTime t) {
		this.m_eventtime = t;
	}

	/** Check if an incident is valid */
	public boolean isValid() {
		boolean valid = true;

		// filter based on center and dispatch id
		// valid=valid && ( this.getCenterId().equals("STCC") && this.getDispatchId().equals("SKCC") );

		// location valid?
		valid = valid
			&& ((CHPLocation) this.getStartLocation()).isValid();

		// time valid?
		valid = valid && this.getTime().isValid();

		return (valid);
	}

	/** Get the start location */
	public Location getStartLocation() {
		return (this.m_startLoca);
	}

	/** Set the start location. Handles a null argument. */
	public void setStartLocation(CHPLocation x) {
		if(x == null)
			x = new CHPLocation();

		this.m_startLoca = x;
	}

	/** Get the end location */
	public Location getEndLocation() {
		return (this.getStartLocation());
	}

	/**
	 * @see us.mn.state.dot.tdxml.Incident#getDescription()
	 */
	public IncidentDescription getDescription() {
		Direction dir = m_startLoca.getDirection();
		String roadway = "";
		String message = this.getType();
		String location = this.getLocationDesc();
		String extent = "";
		String time = this.getTime().toString();
		String sign = "";
		IncidentDescription id = new IncidentDescription(dir, roadway,
						 message, location, extent,
						 time, sign);

		return (id);
	}

	/** Set the description */
	public void setDescription(IncidentDescription d) {
		this.m_desc = d;
	}

	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Incident o) {
		return getDescription().compareTo(o.getDescription());
	}

	/**
	 * @see java.lang.Object.toString()
	 */
	public String toString() {

		StringBuffer b = new StringBuffer();

		b.append(this.getType()).append(",\n");
		b.append("  ").append(this.getLocationDesc()).append(
		    ", ").append(this.getArea()).append(",\n");
		b.append("  ").append(this.getTime().toString()).append(",\n");
		b.append("  TB Area:").append(this.getAreaTB()).append(
		    ", ").append(this.getStartLocation().toString());

		if(this.getEvents().size() > 0) {
			for(CHPEvent ev : m_events) {
				b.append(",\n");
				b.append("  ").append(ev.toString());
			}
		}

		return (b.toString());
	}

	/** Set event container */
	public void setEvents(List<CHPEvent> list) {
		m_events = list;
	}

	/** Get event container */
	public List<CHPEvent> getEvents() {
		return (m_events);
	}

	/**
	 *  Get a string which represents the direction of the
	 *  incident. This is: Northbound, Eastbound, Westbound,
	 *  Southbound, or Unknown. This method in the future
	 *  should return an IRIS-wide Direction enum.
	 * 
	 *  @see us.mn.state.dot.trafmap.IncidentLayer.
	 */
	public String getDirection() {

		// contains direction info, must be uppercase EB, SW, WB, NB
		String loca = this.getLocationDesc();

		if(loca.indexOf("NB") >= 0)
			return ("Northbound");
		else if(loca.indexOf("SB") >= 0)
			return ("Southbound");
		else if(loca.indexOf("EB") >= 0)
			return ("Eastbound");
		else if(loca.indexOf("WB") >= 0)
			return ("Westbound");
		return ("Unknown");
	}
}
