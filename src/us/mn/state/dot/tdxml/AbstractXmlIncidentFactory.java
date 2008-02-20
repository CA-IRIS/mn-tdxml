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
	/** Create a messaged logger */
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
