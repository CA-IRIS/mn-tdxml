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
import us.mn.state.dot.tdxml.geo.LatLongUTMConversion;
import us.mn.state.dot.tdxml.geo.UTM;
import us.mn.state.dot.log.TmsLogFactory;

/**
 * An abstract incident factory which provides some commonly needed stuff.
 *
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

	/** Create an XML incident factory */
	protected AbstractXmlIncidentFactory() {
		logger = createLogger();
	}

	/** Create an XML incident factory */
	protected AbstractXmlIncidentFactory(Logger l) {
		logger = l;
	}
}
