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
package us.mn.state.dot.tdxml.cars;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
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
import us.mn.state.dot.tdxml.ElementCallback;
import us.mn.state.dot.tdxml.Incident;
import us.mn.state.dot.tdxml.IncidentException;

/**
 * @author Erik Engstrom
 * @author Douglas Lau
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
	static public Element getDescription(Element details) {
		Element desc = lookupChild(details,
			"event-element-description");
		return lookupChild(desc, "event-phrase");
	}

	/** Read the list of event-phrases into List of CarsEvents */
	static protected List<CarsEvent> readEvents(Element details) {
		final List<CarsEvent> result = new ArrayList<CarsEvent>();
		Element description = getDescription(details);
		lookupChildren(description, "eventType", new ElementCallback() {
			public void processElement(Element e) {
				Element descPhrase = (Element)e.getFirstChild();
				result.add(new CarsEvent(descPhrase));
			}
		});
		return result;
	}

	/** Lookup the bearing */
	static protected char lookupBearing(Element rec) {
		String bearing = rec.getAttribute("bearing");
		if(bearing != null)
			return bearing.charAt(0);
		else
			return '?';
	}

	/**
	 * Get the LinkDirection element of the EventReportMessage
	 * @param erm The EventReportMessage element.
	 */
	static public String getLinkDirection(Element erm, String s) {
		return lookupChildText(getLinkLocation(erm, s),
			"link-direction");
	}

	/** Get the event message ID */
	static public String getMessageId(Element erm) {
		Element child = lookupChild(erm, "message-header");
		return lookupChildText(child, "event-message-number");
	}

	/** Get an event key phrase */
	static public Element getKeyPhrase(Element erm) {
		Element child = lookupChild(erm, "key-phrase");
		return (Element)child.getFirstChild();
	}

	private final HashMap<String, Element> tables =
		new HashMap<String, Element>();

	private final HashMap<String, SortedSet<Element>> routeRecords =
		new HashMap<String, SortedSet<Element>>();

	/** Create a new CARS incident factory */
	public CarsIncidentFactory(Properties props) throws IOException,
		ParserConfigurationException, SAXException
	{
		this(props, createLogger());
	}

	/** Create a new CARS incident factory */
	public CarsIncidentFactory(Properties props, Logger logger)
		throws IOException, ParserConfigurationException, SAXException
	{
		super(logger);
		initFactory(props);
	}

	protected void initFactory(Properties props) throws IOException,
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

	protected String lookupSign(CarsEvent keyPhrase) {
		Element table = tables.get(TABLE_XML_EVENT);
		NodeList list = table.getElementsByTagName("record");
		String eventType = keyPhrase.getType();
		String subType = keyPhrase.getMessage();
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

	protected String lookupName(String roadway, double linear,
		boolean extent) throws IncidentException
	{
		Element below = null;
		Element above = null;
		SortedSet<Element> records = routeRecords.get(roadway);
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
		String brief_name = null;
		char dir = ' ';
		if(below == null) {
			// no record below linear
			if(above == null)
				return "MP " + linear;
			else
				dir = oppositeDirection(lookupBearing(above));
		} else {
			// found record below linear
			brief_name = below.getAttribute("brief_name");
			char d = lookupBearing(below);
			if(above != null) {
				if(extent) {
					brief_name = above.getAttribute(
						"brief_name");
					dir = oppositeDirection(d);
				} else
					dir = d;
			} else
				dir = d;
		}
		return "MP " + linear + " " + dir + " of " + brief_name;
	}

	protected boolean lookupMetro(String roadway, double linear)
		throws IncidentException
	{
		boolean metro = false;
		double closest = Double.MAX_VALUE;
		SortedSet<Element> records = routeRecords.get(roadway);
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

	protected char lookupDefaultDirection(String roadway, double linear,
		String linkDirection) throws IncidentException
	{
		char defaultDirection = '?';
		double closest = Double.MAX_VALUE;
		SortedSet<Element> records = routeRecords.get(roadway);
		for(Element rec: records) {
			double actual = Double.parseDouble(rec.getAttribute(
				"actual_miles"));
			double diff = Math.abs(linear - actual);
			if(diff < closest) {
				String b = rec.getAttribute("bearing");
				if(b != null)
					defaultDirection = b.charAt(0);
				closest = diff;
			} else
				break;
		}
		return defaultDirection;
	}

	/* (non-Javadoc)
	 * @see us.mn.state.dot.tdxml.XmlIncidentFactory#createIncident(org.jdom.Element)
	 */
	public Incident createIncident(Element erm) throws IncidentException {
		CarsIncident incident = new CarsIncident();
		incident.setMessageId(getMessageId(erm));
		Element keyPhrase = getKeyPhrase(erm);
		CarsEvent keyEvent = new CarsEvent(keyPhrase);
		incident.setKeyPhrase(keyEvent);
		Element details = getDetails(erm);
		incident.setEvents(readEvents(details));
		incident.setAdditionalText(readAdditionalText(
			lookupChild(details, "event-additional-text")));
		incident.setSign(lookupSign(keyEvent));

		Element link = getLink(erm);
		if(link != null)
			setIncidentLocation(incident, link);
		else {
			incident.setLocation_type(
				CarsIncident.LOCATION_TYPE_AREA);
		}
		Element times = lookupChild(details, "event-element-times");
		try {
			incident.setTime(new CarsEventTime(times));
		}
		catch(ParseException pe) {
			throw new IncidentException("Error parsing date", pe);
		}
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
}
