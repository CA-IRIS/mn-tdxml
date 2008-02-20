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
package us.mn.state.dot.tdxml;

import java.util.logging.Logger;
import org.w3c.dom.Element;
import us.mn.state.dot.tdxml.cars.CarsEvent;
import us.mn.state.dot.tdxml.cars.CarsIncident;
import us.mn.state.dot.tdxml.cars.CarsLocation;
import us.mn.state.dot.tdxml.geo.LatLongUTMConversion;
import us.mn.state.dot.tdxml.geo.UTM;
import us.mn.state.dot.log.TmsLogFactory;

/**
 * An abstract incident factory which provides some commonly needed stuff.
 *
 * @author Erik Engstrom
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @author Douglas Lau
 */
abstract public class AbstractXmlIncidentFactory extends AbstractXmlFactory
	implements XmlIncidentFactory
{
	/** Element name for linear reference */
	static protected final String LINEAR_REF =
		"link-location-linear-reference";

	static protected Logger createLogger() {
		return TmsLogFactory.createLogger("XmlIncidentClient", null,
			null);
	}

	/**
	 * Convert latitude, longitude to UTM coordinates.
	 * @param latitude the latitude
	 * @param longitude the longitude
	 * @return a UTM coordinate object.
	 */
	static protected UTM latLongToUtm(double latitude, double longitude) {
		return LatLongUTMConversion.LLtoUTM(23, latitude, longitude);
	}

	/** Convert a string to degrees */
	static protected double toDegrees(String d) {
		return Double.parseDouble(d) / 1000000;
	}

	/** Read degrees from a child element */
	static protected double readDegrees(Element elem, String name)
		throws IncidentException
	{
		String d = lookupChildText(elem, name);
		if(d != null) {
			try {
				return toDegrees(d);
			}
			catch(NumberFormatException e) {
				throw new IncidentException("Invalid '" +
					name + "' element.");
			}
		} else
			return 0;
	}

	/** Parse a linear reference */
	static protected double parseLinearReference(String l)
		throws IncidentException
	{
		try {
			return Double.parseDouble(l);
		}
		catch(NumberFormatException e) {
			throw new IncidentException("Invalid '" + LINEAR_REF +
				"' element.");
		}
	}

	/** Get the linear reference from an element */
	static protected double getLinearReference(Element elem)
		throws IncidentException
	{
		String l = lookupChildText(elem, LINEAR_REF);
		if(l != null)
			return parseLinearReference(l);
		else
			throw new IncidentException("No '" + LINEAR_REF +
				"' element.");
	}

	/** Read any additional text */
	static protected String readAdditionalText(Element elem) {
		if(elem != null) {
			Element c = lookupChild(elem, "eventAdditionalText");
			return lookupChildText(c, "event-description");
		}
		return null;
	}

	/** Return the opposite direction */
	static protected char oppositeDirection(char direction) {
		char result = '?';
		switch(direction) {
			case 'N' :
				result = 'S';
				break;
			case 'S' :
				result = 'N';
				break;
			case 'E' :
				result = 'W';
				break;
			case 'W' :
				result = 'E';
				break;
			default :
				result = '?';
				break;
		}
		return result;
	}

	/**
	 * Determine the direction of an incident based on the default
	 * direction for a link and the value from the link-direction tag in
	 * the XML.
	 */
	static protected char calculateDirection(String link_dir,
		char defaultDirection)
	{
		if(link_dir.equals("positive-direction-only"))
			return defaultDirection;
		else if(link_dir.equals("negative-direction-only"))
			return oppositeDirection(defaultDirection);
		else if(link_dir.equals("both-directions"))
			return 'X';
		else
			return '?';
	}

	/** Logger to use for reporting */
	protected final Logger logger;

	/** Default constructor */
	protected AbstractXmlIncidentFactory() {
		logger = createLogger();
	}

	protected AbstractXmlIncidentFactory(Logger l) {
		logger = l;
	}


	protected abstract String lookupSign(CarsEvent keyPhrase)
		throws IncidentException;
	protected abstract String lookupName(String roadway, double linear,
		boolean extent) throws IncidentException;
	protected abstract boolean lookupMetro(String roadway, double linear)
		throws IncidentException;
	protected abstract char lookupDefaultDirection(String roadway,
		double linear, String link_dir) throws IncidentException;

	/** Convert an XML LinkLocation to a CarsLocation */
	protected CarsLocation readLocation(String roadway,
		Element element, boolean extent, String link_dir)
		throws IncidentException
	{
		double latitude = readDegrees(element,
			"event-location-coordinates-latitude");
		double longitude = readDegrees(element,
			"event-location-coordinates-longitude");
		UTM utm = latLongToUtm(latitude, longitude);
		double linear = getLinearReference(element);
		String name = lookupName(roadway, linear, extent);
		boolean metro = lookupMetro(roadway, linear);
		char defaultDirection = lookupDefaultDirection(roadway, linear,
			link_dir);
		char direction = calculateDirection(link_dir, defaultDirection);
		return new CarsLocation(utm.getEasting(), utm.getNorthing(),
			linear, name, direction, defaultDirection, metro);
	}
}
