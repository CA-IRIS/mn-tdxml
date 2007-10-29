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
 * The <code>IncidentDescription</code> class is used to produce human
 * readable descriptions of <code>Incident</code> objects.
 *
 * @author Erik Engstrom
 */
public class IncidentDescription implements Comparable {

	/** Direction of incident ( NB, SB, EB, WB ) */
	private final String direction;

	/** Roadway that incident is located on ( ie. 35W or 394 ) */
	private final String road;

	/** The message for this incident ( ie. "lane blocked" ) */
	private final String message;

	/** The nearest cross-street to the incident */
	private final String location;

	/** The nearest cross-street to the extent of the incident */
	private final String extent;

	/** The expected duration of the incident */
	private final String duration;

	/** The sign to use for the incident */
	private final String sign;

	/** Create a new IncidentDescription */
	public IncidentDescription(String direction, String roadWay,
		String message, String location, String extent, String duration,
		String sign)
	{
		this.direction = direction;
		this.road = roadWay;
		this.message = message;
		this.location = location;
		this.extent = extent;
		this.duration = duration;
		this.sign = sign;
	}

	public String getDirection() {
		return direction;
	}

	public String getRoad() {
		return road;
	}

	public String getMessage() {
		return message;
	}

	public String getLocation() {
		return location;
	}

	public String getExtent() {
		return extent;
	}

	public String getDuration() {
		return duration;
	}

	public String getSign() {
		return sign;
	}

	public String toString(){
		StringBuffer result = new StringBuffer( direction );
		result.append( " " );
		result.append( road );
		appendTail( result );
		return result.toString();
	}

	public String toHtml(){
		StringBuffer result = new StringBuffer( "<B>" );
		result.append( direction );
		result.append( " " );
		result.append( road );
		result.append( "</B>" );
		appendTail( result );
		return result.toString();
	}

	public void appendTail( StringBuffer result ) {
		result.append( ", " );
		result.append( message );
		if ( ( extent == null ) || extent.equals( "" ) ) {
			result.append( " at " );
			result.append( location );
		} else {
			result.append( " from " );
			result.append( location );
			result.append( " to " );
			result.append( extent );
		}
		result.append( " " );
		result.append( duration );
		result.append( "." );
	}

	protected String getComparator(){
		StringBuffer result = new StringBuffer();
		result.append( road );
		appendTail( result );
		return result.toString();
	}

	public int compareTo( final java.lang.Object p1 ) {
		IncidentDescription comp = ( IncidentDescription ) p1;
		return getComparator().compareTo( comp.getComparator() );
	}
}
