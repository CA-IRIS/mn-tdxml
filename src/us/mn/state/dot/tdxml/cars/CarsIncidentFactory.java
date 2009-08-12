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
package us.mn.state.dot.tdxml.cars;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import us.mn.state.dot.tdxml.AbstractXmlIncidentFactory;
import us.mn.state.dot.tdxml.Direction;
import us.mn.state.dot.tdxml.ElementCallback;
import us.mn.state.dot.tdxml.Incident;
import us.mn.state.dot.tdxml.IncidentException;
import us.mn.state.dot.geokit.UTMPosition;

/**
 * An incident factory for interpreting CARS incidents.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 */
public class CarsIncidentFactory extends AbstractXmlIncidentFactory {

	static protected final String LOCATION_XML = "/location_lookup.xml";

	static private final String TABLE_XML_EVENT = "xml_event";

	static private final String TABLE_CARS = "cars";

	/**
	 * Get the Description element of the EventReportMessage.
	 * @param details The details element
	 * @return The Description element
	 */
	static protected Element getDescription(Element details) {
		Element desc = lookupChild(details,
			"event-element-description");
		return lookupChild(desc, "event-phrase");
	}

	/** Add all CarsEvents to an incident */
	static protected void addIncidentEvents(final CarsIncident incident,
		Element details)
	{
		Element description = getDescription(details);
		lookupChildren(description, "eventType", new ElementCallback() {
			public void processElement(Element e) {
				Element descPhrase = (Element)e.getFirstChild();
				incident.addEvent(new CarsEvent(descPhrase));
			}
		});
	}

	/** Lookup the bearing */
	static protected Direction lookupBearing(Element rec) {
		String bearing = rec.getAttribute("bearing");
		if(bearing != null)
			return Direction.fromString(bearing);
		else
			return Direction.UNKNOWN;
	}

	/** Get the event message ID */
	static protected String getMessageId(Element erm) {
		Element child = lookupChild(erm, "message-header");
		return lookupChildText(child, "event-message-number");
	}

	/** Get the event time from a details element */
	static protected CarsEventTime getEventTime(Element details)
		throws IncidentException
	{
		Element times = lookupChild(details, "event-element-times");
		try {
			return new CarsEventTime(times);
		}
		catch(ParseException pe) {
			throw new IncidentException("Error parsing date", pe);
		}
	}

	/** Get the key event */
	static protected CarsEvent getKeyEvent(Element erm) {
		Element child = lookupChild(erm, "key-phrase");
		Element keyPhrase = (Element)child.getFirstChild();
		return new CarsEvent(keyPhrase);
	}

	/**
	 * Get the Details element of the EventReportMessage.
	 * @param erm The EventReportMessage element.
	 * @return The Details element.
	 */
	static protected Element getDetails(Element erm) {
		Element c = lookupChild(erm, "details");
		return lookupChild(c, "eventElementDetails");
	}

	/**
	 * Get the Link element of the EventReportMessage.
	 * @param erm The EventReportMessage element.
	 * @return The Link element
	 */
	static protected Element getLink(Element erm) {
		Element c = lookupChild(getDetails(erm),
			"event-element-location");
		Element gc = lookupChild(c, "event-location-type");
		Element ggc = lookupChild(gc, "event-location-type-link");
		return ggc;
	}

	/** Read any additional text */
	static protected String readAdditionalText(Element elem) {
		if(elem != null) {
			Element c = lookupChild(elem, "eventAdditionalText");
			return lookupChildText(c, "event-description");
		}
		return null;
	}

	/** Get the additional text */
	static protected String getAdditionalText(Element details) {
		return readAdditionalText(lookupChild(details,
			"event-additional-text"));
	}

	/**
	 * Determine the direction of an incident based on the default
	 * direction for a link and the value from the link-direction tag in
	 * the XML.
	 */
	static protected Direction calculateDirection(String link_dir,
		Direction default_dir)
	{
		if(link_dir.equals("positive-direction-only"))
			return default_dir;
		else if(link_dir.equals("negative-direction-only"))
			return default_dir.opposite();
		else if(link_dir.equals("both-directions"))
			return default_dir.both();
		else
			return Direction.UNKNOWN;
	}

	/** Element name for linear reference */
	static protected final String LINEAR_REF =
		"link-location-linear-reference";

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


	private final HashMap<String, Element> tables =
		new HashMap<String, Element>();

	private final HashMap<String, SortedSet<Element>> routeRecords =
		new HashMap<String, SortedSet<Element>>();

	/** Create a new CARS incident factory */
	public CarsIncidentFactory() throws IOException,
		ParserConfigurationException, SAXException
	{
		this(createLogger());
	}

	/** Create a new CARS incident factory */
	public CarsIncidentFactory(Logger logger) throws IOException,
		ParserConfigurationException, SAXException
	{
		super(logger);
		initFactory();
	}

	/** Initialize the incident factory */
	protected void initFactory() throws IOException,
		ParserConfigurationException, SAXException
	{
		InputStream is = CarsIncidentFactory.class.getResourceAsStream(
			LOCATION_XML);
		DocumentBuilderFactory fact =
			DocumentBuilderFactory.newInstance();
		DocumentBuilder builder =
			fact.newDocumentBuilder();
		Document doc = builder.parse(is);
		NodeList list = doc.getElementsByTagName("table");
		for(int i = 0; i < list.getLength(); i++) {
			Element table = (Element)list.item(i);
			tables.put(table.getAttribute("name"), table);
		}
		initRouteRecords();
	}

	private void initRouteRecords() {
		Element table = tables.get(TABLE_CARS);
		NodeList list = table.getElementsByTagName("record");
		for(int i = 0; i < list.getLength(); i++) {
			Element rec = (Element)list.item(i);
			SortedSet<Element> recSet =
				routeRecords.get(rec.getAttribute("route"));
			if(recSet == null)
				recSet = new TreeSet<Element>(new ElementComparator());
			recSet.add(rec);
			routeRecords.put(rec.getAttribute("route"), recSet);
		}
	}

	static public class ElementComparator implements Comparator<Element> {
		public int compare(Element e1, Element e2) {
			Double mp1 = new Double(e1.getAttribute(
				"actual_miles"));
			Double mp2 = new Double(e2.getAttribute(
				"actual_miles"));
			return mp1.compareTo(mp2);
		}
	}

	/** Lookup the "sign" to display for an event */
	protected String lookupSign(CarsEvent event) {
		Element table = tables.get(TABLE_XML_EVENT);
		NodeList list = table.getElementsByTagName("record");
		String eventType = event.getType();
		String subType = event.getMessage();
		for(int i = 0; i < list.getLength(); i++) {
			Element rec = (Element)list.item(i);
			if(rec.getAttribute("event_type").equals(eventType) &&
				rec.getAttribute("sub_type").equals(subType))
			{
				return rec.getAttribute("sign");
			}
		}
		return "info";
	}

	/** Lookup a location name */
	protected String lookupLocation(String roadway, double linear,
		boolean extent) throws IncidentException
	{
		return "MP " + linear + " " + lookupBriefName(roadway, linear,
			extent);
	}

	/** Lookup the brief name of a location */
	protected String lookupBriefName(String roadway, double linear,
		boolean extent) throws IncidentException
	{
		Element below = null;
		Element above = null;
		SortedSet<Element> records = routeRecords.get(roadway);
		if(records == null)
			return "on " + roadway;
		for(Element rec: records) {
			double actual = Double.parseDouble(rec.getAttribute(
				"actual_miles"));
			if(actual < linear)
				below = rec;
			else if(actual == linear)
				return rec.getAttribute("brief_name");
			else {
				above = rec;
				break;
			}
		}
		return composeBriefName(below, above, extent);
	}

	/** Compose the brief name of a location */
	static protected String composeBriefName(Element below, Element above,
		boolean extent)
	{
		String brief_name = null;
		Direction dir = Direction.UNKNOWN;
		if(below == null) {
			// no record below linear
			if(above == null)
				return "";
			else {
				dir = lookupBearing(above).opposite();
				brief_name = above.getAttribute("brief_name");
			}
		} else {
			// found record below linear
			brief_name = below.getAttribute("brief_name");
			Direction d = lookupBearing(below);
			if(above != null) {
				if(extent) {
					brief_name = above.getAttribute(
						"brief_name");
					dir = d.opposite();
				} else
					dir = d;
			} else
				dir = d;
		}
		if(dir == Direction.UNKNOWN)
			return "near " + brief_name;
		else
			return dir.toChar() + " of " + brief_name;
	}

	protected boolean lookupMetro(String roadway, double linear)
		throws IncidentException
	{
		boolean metro = false;
		double closest = Double.MAX_VALUE;
		SortedSet<Element> records = routeRecords.get(roadway);
		if(records == null)
			return true;
		for(Element rec: records) {
			double actual = Double.parseDouble(rec.getAttribute(
				"actual_miles"));
			double diff = Math.abs(linear - actual);
			if(diff < closest) {
				metro = rec.getAttribute("metro").equals("T");
				closest = diff;
			}
		}
		return metro;
	}

	/** Lookup the default direction for a roadway */
	protected Direction lookupDefaultDirection(String roadway,
		double linear) throws IncidentException
	{
		Direction dir = Direction.UNKNOWN;
		double closest = Double.MAX_VALUE;
		SortedSet<Element> records = routeRecords.get(roadway);
		if(records == null)
			return dir;
		for(Element rec: records) {
			double actual = Double.parseDouble(rec.getAttribute(
				"actual_miles"));
			double diff = Math.abs(linear - actual);
			if(diff < closest) {
				String b = rec.getAttribute("bearing");
				if(b != null)
					dir = Direction.fromString(b);
				closest = diff;
			} else
				break;
		}
		return dir;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tdxml.XmlIncidentFactory#createIncident(org.jdom.Element)
	 */
	public Incident createIncident(Element erm) throws IncidentException {
		String mess_id = getMessageId(erm);
		Element details = getDetails(erm);
		CarsEventTime time = getEventTime(details);
		CarsEvent keyEvent = getKeyEvent(erm);
		String add_text = getAdditionalText(details);
		String sign = lookupSign(keyEvent);

		CarsIncident incident = new CarsIncident(mess_id, time,
			keyEvent, add_text, sign);
		addIncidentEvents(incident, details);

		Element link = getLink(erm);
		if(link != null)
			setIncidentLocation(incident, link);
		return incident;
	}

	/** Set the location of an incident */
	protected void setIncidentLocation(CarsIncident incident, Element link)
		throws IncidentException
	{
		String roadway = lookupChildText(link, "link-road-designator");
		if(roadway == null) {
			throw new IncidentException(
				"Error processing incident " +
				incident.getMessageId() +
				". No link-road-designator specified.");
		}
		incident.setRoadway(roadway);
		Element pri_loc = lookupChild(link, "link-primary-location");
		if(pri_loc == null) {
			throw new IncidentException(
				"Error processing incident " +
				incident.getMessageId() +
				". No link-primary-location.");
		}
		String link_dir = lookupChildText(link, "link-direction");
		incident.setStartLocation(readLocation(roadway, pri_loc, false,
			link_dir));
		Element sec_loc = lookupChild(link, "link-secondary-location");
		if(sec_loc != null) {
			incident.setEndLocation(readLocation(roadway, sec_loc,
				true, link_dir));
		}
	}

	/** Convert an XML LinkLocation to a CarsLocation */
	protected CarsLocation readLocation(String roadway,
		Element element, boolean extent, String link_dir)
		throws IncidentException
	{
		double latitude = readDegrees(element,
			"event-location-coordinates-latitude");
		double longitude = readDegrees(element,
			"event-location-coordinates-longitude");
		UTMPosition utm = latLongToUtm(latitude, longitude);
		double linear = getLinearReference(element);
		String name = lookupLocation(roadway, linear, extent);
		boolean metro = lookupMetro(roadway, linear);
		Direction default_dir = lookupDefaultDirection(roadway, linear);
		Direction dir = calculateDirection(link_dir, default_dir);
		return new CarsLocation(utm.getEasting(), utm.getNorthing(),
			linear, name, dir, metro);
	}
}
