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
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import us.mn.state.dot.tdxml.ElementCallback;
import us.mn.state.dot.tdxml.Incident;
import us.mn.state.dot.tdxml.IncidentException;

/**
 * CHP specific incident factory.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @author Michael Darter
 */
public class CHPXmlIncidentFactory extends AbstractCHPXmlIncidentFactory
{
	// attributes
	static protected final String LOCATION_XML = "/location_lookup.xml";
	static private final String TABLE_CARS = "cars";
	static private final String TABLE_XML_EVENT = "xml_event";
	private final HashMap<String, Element> tables = new HashMap<String,
								Element>();
	private final HashMap<String, SortedSet<Element>> routeRecords =
		new HashMap<String, SortedSet<Element>>();

	/** Default constructor */
	public CHPXmlIncidentFactory() throws IOException,
		ParserConfigurationException, SAXException
	{
		this(createLogger());
	}

	/** Create a CHP XML incident factory */
	public CHPXmlIncidentFactory(Logger logger) throws IOException,
		ParserConfigurationException, SAXException
	{
		super(logger);
		initFactory();
	}

	protected void initFactory()
		throws IOException, ParserConfigurationException, SAXException {
		InputStream is =
			CHPXmlIncidentFactory.class.getResourceAsStream(
			    LOCATION_XML);
		DocumentBuilderFactory fact =
			DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = fact.newDocumentBuilder();
		Document doc = builder.parse(is);
		NodeList list = doc.getElementsByTagName("table");
		for(int i = 0; i < list.getLength(); i++) {
			Element table = (Element) list.item(i);
			tables.put(table.getAttribute("name"), table);
		}

		initRouteRecords();
	}

	/**
	 * Method description
	 *
	 */
	private void initRouteRecords() {
		Element table = tables.get(TABLE_CARS);
		NodeList list = table.getElementsByTagName("record");
		for(int i = 0; i < list.getLength(); i++) {
			Element rec = (Element) list.item(i);
			SortedSet<Element> recSet =
				routeRecords.get(rec.getAttribute("route"));
			if(recSet == null)
				recSet = new TreeSet<Element>(
				    new ElementComparator());
			recSet.add(rec);
			routeRecords.put(rec.getAttribute("route"), recSet);
		}
	}

	/**
	 * Method description
	 *
	 *
	 * @param keyPhrase
	 *
	 * @return
	 */
	protected String lookupSign(CHPEvent keyPhrase) {
		Element table = tables.get(TABLE_XML_EVENT);
		NodeList list = table.getElementsByTagName("record");
		String eventType = keyPhrase.getType();
		String subType = keyPhrase.getMessage();
		for(int i = 0; i < list.getLength(); i++) {
			Element rec = (Element) list.item(i);
			if(rec.getAttribute("event_type").equals(eventType)
				&& rec.getAttribute("sub_type").equals(
					subType)) {
				return rec.getAttribute("sign");
			}
		}

		return "info";
	}

	/**
	 * Method description
	 *
	 *
	 * @param rec
	 *
	 * @return
	 */
	static protected char lookupBearing(Element rec) {
		String bearing = rec.getAttribute("bearing");
		if(bearing != null)
			return bearing.charAt(0);
		else
			return '?';
	}

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
	protected String lookupName(String roadway, double linear,
				    boolean extent)
		throws IncidentException {
		Element below = null;
		Element above = null;
		SortedSet<Element> records = routeRecords.get(roadway);
		for(Element rec : records) {
			double actual = Double.parseDouble(
					    rec.getAttribute("actual_miles"));
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
	protected boolean lookupMetro(String roadway, double linear)
		throws IncidentException {

		boolean metro = false;
		double closest = Double.MAX_VALUE;
		SortedSet<Element> records = routeRecords.get(roadway);
		for(Element rec : records) {
			double actual = Double.parseDouble(
					    rec.getAttribute("actual_miles"));
			double diff = Math.abs(linear - actual);
			if(diff < closest) {
				metro = rec.getAttribute("metro").equals("T");
				closest = diff;
			}
		}

		return metro;
	}

	/**
	 * Method description
	 *
	 *
	 * @param roadway
	 * @param linear
	 * @param linkDirection
	 *
	 * @return
	 *
	 * @throws IncidentException
	 */
	protected char lookupDefaultDirection(String roadway, double linear,
		String linkDirection)
		throws IncidentException {

		char defaultDirection = '?';
		double closest = Double.MAX_VALUE;
		SortedSet<Element> records = routeRecords.get(roadway);
		for(Element rec : records) {
			double actual = Double.parseDouble(
					    rec.getAttribute("actual_miles"));
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

	/** Set the location of an incident. Should never be called */
	protected void setIncidentLocation(CHPIncident incident, Element link)
		throws IncidentException {
		Contract.fail(
		    "CHPXmlIncidentFactory.setIncidentLocation(): should never be called.");
	}

	/**
	 * Return the incident ID given a "Log" element.
	 */
	static public String getMessageId(Element e) {

		// preconds
		if(!Contract.verify("CHPXmlIncidentFactory.getMessageId.1",
				    e.getNodeName().equals("Log")))
			return (null);

		NamedNodeMap attrs = e.getAttributes();
		Attr a = (Attr) attrs.item(0);
		String id = a.getNodeValue();
		return (id);
	}

	/**
	 * Parse "Area" from the CHP XML file given a "Log" element.
	 */
	static public String parseArea(Element e) {

		// preconds
		if(!Contract.verify("CHPXmlIncidentFactory.parseArea.1",
				    e.getNodeName().equals("Log")))
			return (null);

		String x = lookupChildText(e, "Area");
		x = SString.removeEnclosingQuotes(x);
		return (x);
	}

	/**
	 * Parse "Location" from the CHP XML file given a "Log" element.
	 */
	static public String parseLocationDesc(Element e) {

		// preconds
		if(!Contract.verify(
			"CHPXmlIncidentFactory.parseLocationDesc.1",
			e.getNodeName().equals("Log")))
			return (null);

		String x = lookupChildText(e, "Location");
		x = SString.removeEnclosingQuotes(x);
		return (x);
	}

	/**
	 * Parse "LogType" from the CHP XML file given a "Log" element.
	 */
	static public String parseType(Element e) {

		// preconds
		if(!Contract.verify("CHPXmlIncidentFactory.parseType.1",
				    e.getNodeName().equals("Log")))
			return (null);

		String x = lookupChildText(e, "LogType");
		x = SString.removeEnclosingQuotes(x);
		return (x);
	}

	/**
	 * Parse "ThomasBrothers" from the CHP XML file given a "Log" element.
	 */
	static public String parseAreaTB(Element e) {

		// preconds
		if(!Contract.verify("CHPXmlIncidentFactory.parseAreaTB.1",
				    e.getNodeName().equals("Log")))
			return (null);

		String x = lookupChildText(e, "ThomasBrothers");
		x = SString.removeEnclosingQuotes(x);
		return (x);
	}

	/**
	 * Should not be called.
	 *
	 * @see us.mn.state.dot.tdxml.XmlIncidentFactory#createIncident(org.jdom.Element)
	 */
	public Incident createIncident(Element e) throws IncidentException {
		Contract.fail("CHPXmlIncidentFactory.createIncident");
		return (null);
	}

	/**
	 * Create an Incident from the xml Element passed as an argument.
	 * The Element must be a "Log". Null may be returned on error.
	 *
	 * @see us.mn.state.dot.tdxml.XmlIncidentFactory#createIncident(org.jdom.Element)
	 */
	public Incident createIncident(String cid, String did, Element e)
		throws IncidentException {

		// Log.info("CHPXmlIncidentFactory.createIncident("+e.getTagName()+") called.");

		// preconds
		if(!Contract.verify("CHPXmlIncidentFactory.createIncident.1",
				    e.getNodeName().equals("Log")))
			return (null);
		if(!Contract.verify("CHPXmlIncidentFactory.createIncident.2",
				    e.hasAttributes()))
			return (null);
		if(!Contract.verify("CHPXmlIncidentFactory.createIncident.3",
				    e.hasChildNodes()))
			return (null);
		if(!Contract.verify("CHPXmlIncidentFactory.createIncident.4",
				    (cid != null) && (did != null)))
			return (null);

		// create incident
		CHPIncident incident = (CHPIncident) new CHPIncident();
		incident.setCenterId(cid);
		incident.setDispatchId(did);
		incident.setMessageId(this.getMessageId(e));

		// area
		incident.setArea(this.parseArea(e));

		// location desc
		incident.setLocationDesc(this.parseLocationDesc(e));

		// type
		incident.setType(this.parseType(e));

		// Area TB
		incident.setAreaTB(this.parseAreaTB(e));

		// TBXY (location)
		incident.setStartLocation(new CHPLocation(e,incident.getCenterId()));

		// log time
		try {
			incident.setTime(new CHPEventTime(e));
		} catch (ParseException pe) {
			throw new IncidentException("Error parsing date", pe);
		}

		// read events
		incident.setEvents(CHPEvent.parseEvents(e));

		return incident;
	}

	/** Convert an XML LinkLocation to a CHPLocation */
	protected CHPLocation readLocation(String roadway, Element element,
					   boolean extent, String link_dir)
		throws IncidentException {
		Contract.fail("CHPXmlIncidentFactory.readLocation");
		return (new CHPLocation());
	}

	/**
	 * Class description
	 *
	 *
	 * @version    Enter version here..., 08/06/16
	 * @author     Enter your name here...    
	 */
	static public class ElementComparator implements Comparator<Element>
	{
		/**
		 * Method description
		 *
		 *
		 * @param e1
		 * @param e2
		 *
		 * @return
		 */
		public int compare(Element e1, Element e2) {
			Double mp1 =
				new Double(e1.getAttribute("actual_miles"));
			Double mp2 =
				new Double(e2.getAttribute("actual_miles"));
			return mp1.compareTo(mp2);
		}
	}
}
