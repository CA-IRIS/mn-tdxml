/*
 * TDXML -- Traffic Data XML Reader
 * Copyright (C) 2003-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tdxml.d10;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import us.mn.state.dot.geokit.GeodeticDatum;
import us.mn.state.dot.geokit.Position;
import us.mn.state.dot.geokit.UTMPosition;
import us.mn.state.dot.log.TmsLogFactory;
import us.mn.state.dot.tdxml.AbstractXmlFactory;
import us.mn.state.dot.tdxml.ElementCallback;
import us.mn.state.dot.tdxml.Incident;
import us.mn.state.dot.tdxml.IncidentException;
import us.mn.state.dot.tdxml.XmlIncidentFactory;

/**
 *  An abstract factory for parsing XML stuff. This class was copied from the existing
 *  AbstractXmlIncidentFactory class. The original class contained Cars (mn/dot) specific
 *  definitions, otherwise this would be a subclass of AbstractXmlIncidentFactory. It is
 *  hoped that sometime in the future, the AbstractXmlIncidentFactory class will have
 *  no Cars specific definitions, and this class will go away.
 *
 *  @author Erik Engstrom
 *  @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 *  @author Douglas Lau
 *  @author Michael Darter
 */
abstract public class AbstractCHPXmlIncidentFactory extends AbstractXmlFactory
	implements XmlIncidentFactory
{
	/** Element name for linear reference */
	static protected final String LINEAR_REF =
		"link-location-linear-reference";

	/** Logger to use for reporting */
	protected final Logger logger;

	/** Default constructor */
	protected AbstractCHPXmlIncidentFactory() {
		logger = createLogger();
	}

	/**
	 * Constructs ...
	 *
	 *
	 * @param l
	 */
	protected AbstractCHPXmlIncidentFactory(Logger l) {
		logger = l;
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	static protected Logger createLogger() {
		return TmsLogFactory.createLogger("XmlIncidentClient", null,
						  null);
	}

	/**
	 * Convert latitude, longitude to UTM coordinates.
	 * @param lat the latitude
	 * @param lon the longitude
	 * @return a UTM coordinate object.
	 */
	static protected UTMPosition latLongToUtm(double lat, double lon) {
		Position p = new Position(lat, lon);
		return UTMPosition.convert(GeodeticDatum.WGS_84, p);
	}

	/** Convert a string to degrees */
	static protected double toDegrees(String d) {
		return Double.parseDouble(d) / 1000000;
	}

	/** Read degrees from a child element */
	static protected double readDegrees(Element elem, String name)
		throws IncidentException {
		String d = lookupChildText(elem, name);
		if(d != null) {
			try {
				return toDegrees(d);
			} catch (NumberFormatException e) {
				throw new IncidentException("Invalid '" + name
							    + "' element.");
			}
		} else
			return 0;
	}

	/** Parse a linear reference */
	static protected double parseLinearReference(String l)
		throws IncidentException {
		try {
			return Double.parseDouble(l);
		} catch (NumberFormatException e) {
			throw new IncidentException("Invalid '" + LINEAR_REF
						    + "' element.");
		}
	}

	/** Get the linear reference from an element */
	static protected double getLinearReference(Element elem)
		throws IncidentException {
		String l = lookupChildText(elem, LINEAR_REF);
		if(l != null)
			return parseLinearReference(l);
		else
			throw new IncidentException("No '" + LINEAR_REF
						    + "' element.");
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
		switch (direction) {
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
		char defaultDirection) {
		if(link_dir.equals("positive-direction-only"))
			return defaultDirection;
		else if(link_dir.equals("negative-direction-only"))
			return oppositeDirection(defaultDirection);
		else if(link_dir.equals("both-directions"))
			return 'X';
		else
			return '?';
	}

	/** Set the location of an incident */
	abstract protected void setIncidentLocation(CHPIncident incident,
		Element link)
		throws IncidentException;

	/*
	 *  (non-Javadoc)
	 * @see us.mn.state.dot.tdxml.XmlIncidentFactory#createIncident(org.jdom.Element)
	 */

	/**
	 * Method description
	 *
	 *
	 * @param erm
	 *
	 * @return
	 *
	 * @throws IncidentException
	 */
	abstract public Incident createIncident(Element erm)
		throws IncidentException;

	/**
	 * Get the Description element of the EventReportMessage.
	 * @param details The details element
	 * @return The Description element
	 */
	static public Element getDescription(Element details) {
		Element desc = lookupChild(details,
					   "event-element-description");
		return lookupChild(desc, "event-phrase");
	}

	/**
	 * Read the list of event-phrases into List of CHPEvents.
	 */
	static protected List<CHPEvent> readEvents(Element details) {
		final List<CHPEvent> result = new ArrayList<CHPEvent>();
		Element description = getDescription(details);
		lookupChildren(description, "eventType", new ElementCallback()
		{
			public void processElement(Element e) {
				Element descPhrase =
					(Element) e.getFirstChild();
				result.add(new CHPEvent(descPhrase));
			}
		});
		return result;
	}

	/**
	 * Method description
	 *
	 *
	 * @param keyPhrase
	 *
	 * @return
	 *
	 * @throws IncidentException
	 */
	protected abstract String lookupSign(CHPEvent keyPhrase)
		throws IncidentException;

	/**
	 * Method description
	 *
	 *
	 * @param roadway
	 * @param linear
	 * @param extent
	 *
	 * @return
	 *
	 * @throws IncidentException
	 */
	protected abstract String lookupName(String roadway, double linear,
		boolean extent)
		throws IncidentException;

	/**
	 * Method description
	 *
	 *
	 * @param roadway
	 * @param linear
	 *
	 * @return
	 *
	 * @throws IncidentException
	 */
	protected abstract boolean lookupMetro(String roadway, double linear)
		throws IncidentException;

	/**
	 * Method description
	 *
	 *
	 * @param roadway
	 * @param linear
	 * @param link_dir
	 *
	 * @return
	 *
	 * @throws IncidentException
	 */
	protected abstract char lookupDefaultDirection(String roadway,
		double linear, String link_dir)
		throws IncidentException;

	/** Convert an XML LinkLocation to a CHPLocation */
	abstract protected CHPLocation readLocation(String roadway,
		Element element, boolean extent, String link_dir)
		throws IncidentException;

	/**
	 * Method description
	 *
	 *
	 * @param erm
	 *
	 * @return
	 */
	static public String getMessageId(Element erm) {
		Element child = lookupChild(erm, "message-header");
		return lookupChildText(child, "event-message-number");
	}

	/**
	 * Method description
	 *
	 *
	 * @param erm
	 *
	 * @return
	 */
	static public Element getKeyPhrase(Element erm) {
		Element child = lookupChild(erm, "key-phrase");
		return (Element) child.getFirstChild();
	}

	/**
	 * Get the Details element of the EventReportMessage.
	 * @param erm The EventReportMessage element.
	 * @return The Details element.
	 */
	static public Element getDetails(Element erm) {
		Element c = lookupChild(erm, "details");
		return lookupChild(c, "eventElementDetails");
	}

	/**
	 * Get the Link element of the EventReportMessage.
	 * @param erm The EventReportMessage element.
	 * @return The Link element
	 */
	static public Element getLink(Element erm) {
		Element c = lookupChild(getDetails(erm),
					"event-element-location");
		Element gc = lookupChild(c, "event-location-type");
		Element ggc = lookupChild(gc, "event-location-type-link");
		return ggc;
	}

	/**
	 * Get the LinkLocation element of the EventReportMessage
	 * @param erm The EventReportMessage element.
	 */
	static public Element getLinkLocation(Element erm, String s) {
		return lookupChild(getLink(erm), s);
	}

	/**
	 * Get the LinkDirection element of the EventReportMessage
	 * @param erm The EventReportMessage element.
	 */
	static public String getLinkDirection(Element erm, String s) {
		return lookupChildText(getLinkLocation(erm, s),
				       "link-direction");
	}

	/**
	 * Get the Time element of the EventReportMessage.
	 * @param erm The EventReportMessage element
	 */
	public Element getTimes(Element erm) {
		return lookupChild(getDetails(erm), "event-element-times");
	}

	/**
	 * Get the roadway for the EventReportMessage.
	 * @param erm The EventReportMessage element
	 */
	public String getRoadway(Element erm) {
		return lookupChildText(getLink(erm), "link-road-designator");
	}
}
